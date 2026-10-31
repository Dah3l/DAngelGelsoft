package com.dangel.gelsoft

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.dangel.gelsoft.data.AnnouncementPrefs
import com.dangel.gelsoft.data.AppConfig
import com.dangel.gelsoft.data.ConfigRepository
import com.dangel.gelsoft.notifications.AnnouncementNotifier
import com.dangel.gelsoft.ui.screens.AnnouncementsScreen
import com.dangel.gelsoft.ui.screens.ContactScreen
import com.dangel.gelsoft.ui.screens.GalleryScreen
import com.dangel.gelsoft.ui.screens.HomeScreen
import com.dangel.gelsoft.ui.screens.MyReservationsScreen
import com.dangel.gelsoft.ui.screens.PricesScreen
import com.dangel.gelsoft.ui.screens.ReservationScreen
import com.dangel.gelsoft.ui.screens.SplashScreen
import com.dangel.gelsoft.ui.theme.DangelTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * D'Angel Gelsoft: cover + prices + gallery + contact, with a floating
 * reservation button and a notification bell for announcements.
 *
 * Offline-first: the cached content shows instantly; the network refresh
 * happens in the background and swaps in silently. New announcements
 * trigger the bell badge and a system notification.
 */
class MainActivity : ComponentActivity() {

    private var openAnnouncementsOnStart by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openAnnouncementsOnStart =
            intent?.getBooleanExtra(AnnouncementNotifier.EXTRA_OPEN_ANNOUNCEMENTS, false) == true
        setContent {
            DangelTheme {
                val context = LocalContext.current
                var splashDone by remember { mutableStateOf(false) }
                var config by remember { mutableStateOf<AppConfig?>(null) }
                var offline by remember { mutableStateOf(false) }
                var tab by remember { mutableIntStateOf(0) }
                // Overlays (null = none).
                var overlay by remember { mutableStateOf<String?>(null) }
                var unread by remember { mutableIntStateOf(0) }

                // 1. Instant: cached content, never a spinner.
                LaunchedEffect(Unit) {
                    val cached = withContext(Dispatchers.IO) {
                        ConfigRepository.loadCached(context)
                    }
                    config = cached
                    val seen = AnnouncementPrefs.seenIds(context)
                    unread = cached.announcements.count { it.id !in seen && it.id != 0L }
                    if (openAnnouncementsOnStart) overlay = "avisos"

                    // 2. Background refresh.
                    val fresh = ConfigRepository.refreshFromNetwork(context)
                    if (fresh != null) {
                        val freshSeen = AnnouncementPrefs.seenIds(context)
                        val newOnes = fresh.announcements.filter {
                            it.id != 0L && it.id !in freshSeen
                        }
                        config = fresh
                        offline = false
                        unread = newOnes.size
                        if (newOnes.isNotEmpty()) {
                            AnnouncementNotifier.notifyNew(context, newOnes)
                        }
                    } else {
                        offline = true
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

                // System back closes overlays first.
                BackHandler(enabled = overlay != null) {
                    overlay = null
                }

                val goReserve = { overlay = "reservar" }
                val openAvisos = {
                    overlay = "avisos"
                    AnnouncementPrefs.markSeen(context, current.announcements.map { it.id })
                    unread = 0
                }

                @OptIn(ExperimentalMaterial3Api::class)
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
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            onClick = goReserve,
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.Black,
                        ) {
                            Text("Reservar", fontSize = 15.sp)
                        }
                    },
                    bottomBar = {
                        NavigationBar {
                            val items = listOf(
                                Triple("Inicio", Icons.Filled.Home, 0),
                                Triple("Precios", Icons.Filled.AttachMoney, 1),
                                Triple("Galería", Icons.Filled.PhotoLibrary, 2),
                                Triple("Contacto", Icons.Filled.Person, 3),
                            )
                            for ((label, icon, index) in items) {
                                NavigationBarItem(
                                    selected = tab == index && overlay == null,
                                    onClick = {
                                        tab = index
                                        overlay = null
                                    },
                                    icon = { Icon(icon, contentDescription = label) },
                                    label = { Text(label) },
                                )
                            }
                        }
                    },
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    ) {
                        if (offline) {
                            Text(
                                text = "Sin conexión: mostrando datos guardados",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(top = 8.dp),
                            )
                        }
                        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                            when (overlay) {
                                "reservar" -> ReservationScreen(
                                    config = current,
                                    onDone = { overlay = null },
                                    onMyReservations = { overlay = "mis_reservas" },
                                )
                                "avisos" -> AnnouncementsScreen(current)
                                "mis_reservas" -> MyReservationsScreen(current)
                                else -> when (tab) {
                                    0 -> HomeScreen(
                                        config = current,
                                        onReserve = goReserve,
                                        onSeePrices = { tab = 1 },
                                        onSeeAnnouncements = openAvisos,
                                        onSeeGallery = { tab = 2 },
                                    )
                                    1 -> PricesScreen(current)
                                    2 -> GalleryScreen(current)
                                    else -> ContactScreen(
                                        config = current,
                                        onMyReservations = { overlay = "mis_reservas" },
                                    )
                                }
                            }
                        }
                    }
                }

                // Back from overlays returns to the tabs.
            }
        }
    }

    override fun onBackPressed() {
        // Overlays are handled by the compose state; the system back
        // closes the activity only from the main tabs.
        super.onBackPressed()
    }
}
