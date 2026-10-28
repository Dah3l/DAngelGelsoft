package com.dangel.gelsoft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dangel.gelsoft.data.AppConfig
import com.dangel.gelsoft.data.ConfigRepository
import com.dangel.gelsoft.ui.screens.AnnouncementsScreen
import com.dangel.gelsoft.ui.screens.CoverScreen
import com.dangel.gelsoft.ui.screens.PricesScreen
import com.dangel.gelsoft.ui.screens.ReservationScreen
import com.dangel.gelsoft.ui.screens.SchedulesScreen
import com.dangel.gelsoft.ui.theme.DangelTheme

/**
 * Single activity for the D'Angel Gelsoft demo app.
 *
 * The app is a landing page in APK form: cover, schedules, prices,
 * announcements and a reservation form (WhatsApp checkout). All content
 * comes from one JSON (see ConfigRepository): network first, local cache
 * second, bundled fallback third — so it works offline.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DangelTheme {
                val context = LocalContext.current
                var config by remember { mutableStateOf<AppConfig?>(null) }
                var loading by remember { mutableStateOf(true) }
                var offline by remember { mutableStateOf(false) }
                var tab by remember { mutableIntStateOf(0) }

                LaunchedEffect(Unit) {
                    val result = ConfigRepository.load(context)
                    config = result.config
                    offline = result.offline
                    loading = false
                }

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            val items = listOf(
                                "Inicio" to Icons.Filled.Home,
                                "Horarios" to Icons.Filled.Schedule,
                                "Precios" to Icons.Filled.AttachMoney,
                                "Avisos" to Icons.Filled.Notifications,
                                "Reservar" to Icons.Filled.DateRange,
                            )
                            items.forEachIndexed { index, (label, icon) ->
                                NavigationBarItem(
                                    selected = tab == index,
                                    onClick = { tab = index },
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
                        // Aviso discreto cuando no hay red: el contenido que se
                        // muestra es el último JSON descargado o el incluido.
                        if (offline && config != null) {
                            Text(
                                text = "Sin conexión: mostrando datos guardados",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(top = 8.dp),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            when {
                                loading -> CircularProgressIndicator()
                                config != null -> {
                                    val current = config!!
                                    when (tab) {
                                        0 -> CoverScreen(current)
                                        1 -> SchedulesScreen(current)
                                        2 -> PricesScreen(current)
                                        3 -> AnnouncementsScreen(current)
                                        else -> ReservationScreen(current)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
