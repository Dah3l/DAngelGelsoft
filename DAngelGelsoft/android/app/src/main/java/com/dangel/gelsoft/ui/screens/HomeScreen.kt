package com.dangel.gelsoft.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dangel.gelsoft.R
import com.dangel.gelsoft.data.AppConfig

/**
 * Home: professional cover. Hero with brand + reservation CTA, then
 * featured prices, schedules and latest announcements as preview
 * sections. Contact info lives in its own tab.
 */
@Composable
fun HomeScreen(
    config: AppConfig,
    onReserve: () -> Unit,
    onSeePrices: () -> Unit,
    onSeeAnnouncements: () -> Unit,
    onSeeGallery: () -> Unit,
) {
    val info = config.info

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // ---------- Hero ----------
        Image(
            painter = painterResource(id = R.drawable.logo_dangel),
            contentDescription = "Logo de D'Angel Gelsoft",
            modifier = Modifier
                .size(150.dp)
                .clip(RoundedCornerShape(20.dp)),
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = info.slogan,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = info.description,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "📍 ${info.location}",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(18.dp))
        Button(
            onClick = onReserve,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.Black,
            ),
        ) {
            Text("Reservar mi sesión", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        // ---------- Modalidades destacadas ----------
        if (config.prices.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader(title = "Modalidades", onSeeAll = onSeePrices)
            Spacer(modifier = Modifier.height(8.dp))
            for (price in config.prices.take(3)) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(price.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            if (price.description.isNotBlank()) {
                                Text(
                                    price.description,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Text(
                            "${price.priceCup} CUP",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }

        // ---------- Horarios ----------
        if (config.schedules.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(title = "Horarios", onSeeAll = null)
            Spacer(modifier = Modifier.height(8.dp))
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
        }

        // ---------- Próximos avisos ----------
        if (config.announcements.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(title = "Avisos", onSeeAll = onSeeAnnouncements)
            Spacer(modifier = Modifier.height(8.dp))
            for (announcement in config.announcements.take(2)) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(announcement.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            announcement.body,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                        )
                    }
                }
            }
        }

        // ---------- Galería (vista previa) ----------
        val previewPhotos = config.gallery.firstOrNull()?.photos?.take(3)
        if (!previewPhotos.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(title = "Galería", onSeeAll = onSeeGallery)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${config.gallery.size} secciones con fotos del campo, las armas y los eventos.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SectionHeader(title: String, onSeeAll: (() -> Unit)?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        if (onSeeAll != null) {
            TextButton(onClick = onSeeAll) {
                Text("Ver todos", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
