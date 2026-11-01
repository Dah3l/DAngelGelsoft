package com.dangel.gelsoft.ui.screens
import com.dangel.gelsoft.ui.theme.DangelType
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dangel.gelsoft.data.AppConfig

/** Schedules tab: regular hours, night experience, location and contact. */
@Composable
fun SchedulesScreen(config: AppConfig) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 104.dp),
    ) {
        Text("Horarios", style = DangelType.Title)
        Spacer(modifier = Modifier.height(12.dp))
        if (config.schedules.isEmpty()) {
            Text(
                "Pronto publicaremos los horarios. Escríbenos por WhatsApp para reservar.",
                style = DangelType.Body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        for (schedule in config.schedules) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(schedule.title, style = DangelType.CardTitle)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(schedule.days, style = DangelType.Body, color = MaterialTheme.colorScheme.primary)
                    Text(
                        "${schedule.openTime} – ${schedule.closeTime}",
                        style = DangelType.Body,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (schedule.note.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            schedule.note,
                            style = DangelType.Body,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Ubicación", style = DangelType.CardTitle)
                Spacer(modifier = Modifier.height(4.dp))
                Text(config.info.location, style = DangelType.Body)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Reservas", style = DangelType.CardTitle)
                Spacer(modifier = Modifier.height(4.dp))
                Text(config.info.phone1, style = DangelType.Body)
                Text(config.info.phone2, style = DangelType.Body)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Instagram: @${config.info.instagram}",
                    style = DangelType.Body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

/** Prices tab: modalities published by the business, read from the JSON. */
@Composable
fun PricesScreen(config: AppConfig) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 104.dp),
    ) {
        Text("Precios", style = DangelType.Title)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Modalidades y precios publicados por el negocio.",
            style = DangelType.Body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (config.prices.isEmpty()) {
            Text(
                "Pronto publicaremos las modalidades y precios. Escríbenos por WhatsApp para más información.",
                style = DangelType.Body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        for (price in config.prices) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(price.name, style = DangelType.CardTitle)
                    if (price.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            price.description,
                            style = DangelType.Body,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "${price.priceCup} CUP ${price.unit}",
                        style = DangelType.Section,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

/** Announcements tab: special nights and notices, read from the JSON. */
@Composable
fun AnnouncementsScreen(config: AppConfig) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 104.dp),
    ) {
        Text("Avisos y noches especiales", style = DangelType.Title)
        Spacer(modifier = Modifier.height(12.dp))
        if (config.announcements.isEmpty()) {
            Text(
                "Pronto publicaremos nuevas actividades. Síguenos en Instagram para enterarte primero.",
                style = DangelType.Body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        for (announcement in config.announcements) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (announcement.dateLabel.isNotBlank()) {
                        AssistChip(
                            onClick = {},
                            label = { Text(announcement.dateLabel) },
                            colors = AssistChipDefaults.assistChipColors(
                                labelColor = Color.Black,
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Text(announcement.title, style = DangelType.CardTitle)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(announcement.body, style = DangelType.Body)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
