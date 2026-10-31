package com.dangel.gelsoft.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dangel.gelsoft.R
import com.dangel.gelsoft.data.AppConfig
import com.dangel.gelsoft.data.GalleryPhoto

/**
 * Home v3: Hero + gallery carousel (random photos, "Ver más") +
 * schedules + contact info. Prices live in their own tab; announcements
 * behind the bell; reservations behind the center button.
 */
@Composable
fun HomeScreen(
    config: AppConfig,
    onReserve: () -> Unit,
    onSeeGallery: () -> Unit,
) {
    val context = LocalContext.current
    val info = config.info

    // 4-5 random photos from the whole gallery, picked once per config.
    val carouselPhotos: List<GalleryPhoto> = remember(config) {
        config.gallery.flatMap { it.photos }.shuffled().take(5)
    }

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

        // ---------- Galería (carrusel) ----------
        if (carouselPhotos.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader(title = "Galería", onSeeAll = onSeeGallery, seeAllText = "Ver más")
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(carouselPhotos, key = { it.id }) { photo ->
                    AsyncImage(
                        model = photo.url,
                        contentDescription = "Foto de la galería",
                        modifier = Modifier
                            .size(width = 170.dp, height = 120.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSeeGallery() },
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }

        // ---------- Horarios ----------
        if (config.schedules.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
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

        // ---------- Información de contacto ----------
        Spacer(modifier = Modifier.height(24.dp))
        SectionHeader(title = "Contacto", onSeeAll = null)
        Spacer(modifier = Modifier.height(8.dp))

        HomeContactRow(
            icon = Icons.Filled.LocationOn,
            title = "Ubicación",
            value = info.location,
        )
        HomeContactRow(
            icon = Icons.Filled.Call,
            title = "Reservas",
            value = "${info.phone1}\n${info.phone2}",
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
            HomeContactRow(
                icon = Icons.Filled.Email,
                title = "Correo",
                value = info.email,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onSeeAll: (() -> Unit)?,
    seeAllText: String = "Ver todos",
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        if (onSeeAll != null) {
            TextButton(onClick = onSeeAll) {
                Text(seeAllText, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun HomeContactRow(icon: ImageVector, title: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
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
