package com.dangel.gelsoft.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dangel.gelsoft.data.AppConfig

/**
 * Contact: every way to reach the business (location, phones for WhatsApp
 * and direct call, Instagram, website, email) plus the schedules section.
 */
@Composable
fun ContactScreen(config: AppConfig, onMyReservations: () -> Unit) {
    val context = LocalContext.current
    val info = config.info

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text("Contacto", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        ContactRow(
            icon = Icons.Filled.LocationOn,
            title = "Ubicación",
            value = info.location,
            onClick = null,
        )
        ContactRow(
            icon = Icons.Filled.Call,
            title = "Reservas",
            value = "${info.phone1}\n${info.phone2}",
            onClick = null,
        )

        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                openWhatsApp(
                    context, info.phone1,
                    "¡Hola D'Angel Gelsoft! Quiero información para reservar una sesión.",
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.Black,
            ),
        ) {
            Text("WhatsApp ${info.phone1}", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { dialPhone(context, info.phone1) },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp),
            ) {
                Text("Llamar")
            }
            OutlinedButton(
                onClick = { openUrl(context, "https://instagram.com/${info.instagram}") },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
            ) {
                Text("Instagram")
            }
        }
        if (info.website.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { openUrl(context, info.website) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Sitio web")
            }
        }
        if (info.email.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            ContactRow(
                icon = Icons.Filled.Email,
                title = "Correo",
                value = info.email,
                onClick = {
                    openUrl(context, "mailto:${info.email}")
                },
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onMyReservations,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Mis reservas")
        }

        // ---------- Horarios ----------
        Spacer(modifier = Modifier.height(20.dp))
        Text("Horarios", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        if (config.schedules.isEmpty()) {
            Text(
                "Pronto publicaremos los horarios.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        for (schedule in config.schedules) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(schedule.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${schedule.days} · ${schedule.openTime} – ${schedule.closeTime}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    if (schedule.note.isNotBlank()) {
                        Text(
                            schedule.note,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ContactRow(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: (() -> Unit)?,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        onClick = onClick ?: {},
        enabled = onClick != null,
    ) {
        Row(modifier = Modifier.padding(14.dp)) {
            Icon(
                icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 12.dp),
            )
            Column {
                Text(title, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
