package com.dangel.gelsoft

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.dangel.gelsoft.data.AnnouncementPrefs
import com.dangel.gelsoft.data.AppConfig
import com.dangel.gelsoft.data.ConfigRepository
import com.dangel.gelsoft.data.ProfileStore
import com.dangel.gelsoft.data.ReservationStore
import com.dangel.gelsoft.data.SupabaseDirect
import com.dangel.gelsoft.notifications.AnnouncementNotifier
import com.dangel.gelsoft.notifications.ReservationNotifier
import com.dangel.gelsoft.ui.DangelBottomBar
import com.dangel.gelsoft.ui.screens.AnnouncementsScreen
import com.dangel.gelsoft.ui.screens.GalleryScreen
import com.dangel.gelsoft.ui.screens.HomeScreen
import com.dangel.gelsoft.ui.screens.MyReservationsScreen
import com.dangel.gelsoft.ui.screens.PricesScreen
import com.dangel.gelsoft.ui.screens.ProfileScreen
import com.dangel.gelsoft.ui.screens.ProfileSetupDialog
import com.dangel.gelsoft.ui.screens.ReservationScreen
import com.dangel.gelsoft.ui.screens.SplashScreen
import com.dangel.gelsoft.ui.theme.DangelTheme
import com.dangel.gelsoft.ui.theme.DangelType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * D'Angel Gelsoft v3: tabs Inicio · Precios · Galería · Perfil with a
 * raised center reservation button, bell for announcements in the top bar.
 *
 * Offline-first: the cached content shows instantly; the network refresh
 * happens in the background and swaps in silently. New announcements
 * trigger the bell badge and a system notification (unless the user
 * disabled them in their profile).
 */
class MainActivity : ComponentActivity() {

    private var openAnnouncementsOnStart by mutableStateOf(false)
    private var openReservasOnStart by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val activity = this
        openAnnouncementsOnStart =
            intent?.getBooleanExtra(AnnouncementNotifier.EXTRA_OPEN_ANNOUNCEMENTS, false) == true
        openReservasOnStart =
            intent?.getBooleanExtra(ReservationNotifier.EXTRA_OPEN_RESERVAS, false) == true
        setContent {
            DangelTheme {
                val context = LocalContext.current
                var splashDone by remember { mutableStateOf(false) }
                var config by remember { mutableStateOf<AppConfig?>(null) }
                var offline by remember { mutableStateOf(false) }
                val tab = remember { mutableIntStateOf(0) }
                val overlay: MutableState<String?> = remember { mutableStateOf<String?>(null) }
                var unread by remember { mutableIntStateOf(0) }
                var showProfileSetup by remember {
                    mutableStateOf(
                        !context.getSharedPreferences("dangel_prefs", Context.MODE_PRIVATE)
                            .getBoolean("perfil_configurado", false)
                    )
                }

                // Back: overlay -> previous screen · any tab -> Home · Home: double-press to exit.
                var lastBackPress by remember { mutableLongStateOf(0L) }
                BackHandler(enabled = true) {
                    when {
                        overlay.value != null -> overlay.value = null
                        tab.value != 0 -> tab.value = 0
                        else -> {
                            val now = System.currentTimeMillis()
                            if (now - lastBackPress < 2000) {
                                activity.finish()
                            } else {
                                lastBackPress = now
                                Toast.makeText(
                                    context,
                                    "Pulsa de nuevo para salir",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        }
                    }
                }

                // 1. Instant: cached content, never a spinner.
                LaunchedEffect(Unit) {
                    val prefs = context.getSharedPreferences("dangel_prefs", Context.MODE_PRIVATE)
                    val initialized = prefs.getBoolean("avisos_init", false)
                    val cached = withContext(Dispatchers.IO) {
                        ConfigRepository.loadCached(context)
                    }
                    config = cached
                    unread = if (initialized) {
                        val seen = AnnouncementPrefs.seenIds(context)
                        cached.announcements.count { it.id !in seen && it.id != 0L }
                    } else 0
                    if (openAnnouncementsOnStart) overlay.value = "avisos"
                    if (openReservasOnStart) overlay.value = "mis_reservas"

                    // 2. Background refresh.
                    val fresh = ConfigRepository.refreshFromNetwork(context)
                    if (fresh != null) {
                        if (!initialized) {
                            // First run: mark everything as seen silently so
                            // old announcements don't notify as "new".
                            AnnouncementPrefs.markSeen(context, fresh.announcements.map { it.id })
                            prefs.edit().putBoolean("avisos_init", true).apply()
                            unread = 0
                        } else {
                            val freshSeen = AnnouncementPrefs.seenIds(context)
                            val newOnes = fresh.announcements.filter {
                                it.id != 0L && it.id !in freshSeen
                            }
                            unread = newOnes.size
                            if (newOnes.isNotEmpty() && ProfileStore.notifEnabled(context)) {
                                AnnouncementNotifier.notifyNew(context, newOnes)
                            }
                        }
                        config = fresh
                        offline = false
                    } else {
                        offline = true
                    }

                    // 3. Reservation status changes -> system notification.
                    withContext(Dispatchers.IO) {
                        checkReservationUpdates(context)
                    }

                    // Notification permission (Android 13+).
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(
                            context, Manifest.permission.POST_NOTIFICATIONS,
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
                    }
                }

                if (!splashDone) {
                    SplashScreen(
                        slogan = config?.info?.slogan ?: "Más que un juego, una misión",
                        onDone = { splashDone = true },
                    )
                    return@DangelTheme
                }

                val current = config
                if (current == null) {
                    // Practically unreachable: the bundled fallback always parses.
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) { Text("Cargando…") }
                    return@DangelTheme
                }

                DangelScaffold(
                    current = current,
                    tab = tab,
                    overlay = overlay,
                    offline = offline,
                    unread = unread,
                    onAvisosSeen = { unread = 0 },
                    onRequestNotifPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            requestPermissions(
                                arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1
                            )
                        }
                    },
                )

                if (showProfileSetup) {
                    ProfileSetupDialog(onDone = { showProfileSetup = false })
                }
            }
        }
    }
}

/**
 * Compares the locally cached reservation estados with the DB. Persists any
 * change (offline-first) and fires a system notification when a reservation
 * moved to a final state (confirmada / rechazada).
 */
private suspend fun checkReservationUpdates(context: Context) {
    val phone = ProfileStore.getTelefono(context)
    if (phone.isBlank()) return
    val local = ReservationStore.all(context)
    if (local.isEmpty()) return
    val db = SupabaseDirect.myReservations(phone)
    for (l in local) {
        val remote = db.firstOrNull { d ->
            d.fecha == l.fecha && d.bloqueId == l.bloqueId &&
                ReservationStore.normPhone(d.telefono) == ReservationStore.normPhone(l.telefono)
        } ?: continue
        if (remote.estado == l.estado) continue
        ReservationStore.updateEstado(
            context, l.fecha, l.bloqueId,
            ReservationStore.normPhone(l.telefono), remote.estado,
        )
        if ((remote.estado == "confirmada" || remote.estado == "rechazada") &&
            ProfileStore.notifEnabled(context)
        ) {
            ReservationNotifier.notifyStatus(context, l.fecha, l.bloqueNombre, remote.estado)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DangelScaffold(
    current: AppConfig,
    tab: MutableState<Int>,
    overlay: MutableState<String?>,
    offline: Boolean,
    unread: Int,
    onAvisosSeen: () -> Unit,
    onRequestNotifPermission: () -> Unit,
) {
    val context = LocalContext.current

    val goReserve = { overlay.value = "reservar" }
    val openAvisos = {
        overlay.value = "avisos"
        AnnouncementPrefs.markSeen(context, current.announcements.map { it.id })
        onAvisosSeen()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("D'Angel Gelsoft") },
                actions = {
                    IconButton(onClick = openAvisos) {
                        BadgedBox(
                            badge = {
                                if (unread > 0) Badge { Text("$unread") }
                            },
                        ) {
                            Icon(
                                Icons.Filled.Notifications,
                                contentDescription = "Avisos",
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        // Floating nav: content fills the screen, the bar overlays it.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                if (offline) {
                    Text(
                        text = "Sin conexión: mostrando datos guardados",
                        color = MaterialTheme.colorScheme.primary,
                        style = DangelType.Tiny,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 8.dp),
                    )
                }
                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    when (overlay.value) {
                        "reservar" -> ReservationScreen(
                            config = current,
                            onDone = { overlay.value = null },
                            onMyReservations = { overlay.value = "mis_reservas" },
                        )
                        "avisos" -> AnnouncementsScreen(current)
                        "mis_reservas" -> MyReservationsScreen(current)
                        else -> when (tab.value) {
                            0 -> HomeScreen(
                                config = current,
                                onReserve = goReserve,
                                onSeeGallery = { tab.value = 2 },
                            )
                            1 -> PricesScreen(current)
                            2 -> GalleryScreen(current)
                            else -> ProfileScreen(
                                onMyReservations = { overlay.value = "mis_reservas" },
                                onRequestNotifPermission = onRequestNotifPermission,
                            )
                        }
                    }
                    }
            DangelBottomBar(
                selectedTab = tab.value,
                onTabSelected = {
                    tab.value = it
                    overlay.value = null
                },
                onReserve = goReserve,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
}
