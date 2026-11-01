package com.dangel.gelsoft.ui.screens
import com.dangel.gelsoft.ui.theme.DangelType
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 104.dp),
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
            style = DangelType.Section,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = info.description,
            style = DangelType.Body,
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
            Text("Reservar mi sesión", style = DangelType.CardTitle)
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
                        Text(schedule.title, style = DangelType.CardTitle)
                        Text(
                            "${schedule.days} · ${schedule.openTime} – ${schedule.closeTime}",
                            style = DangelType.Body,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        if (schedule.note.isNotBlank()) {
                            Text(
                                schedule.note,
                                style = DangelType.Caption,
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

        // Un tile por número: WhatsApp directo + llamada.
        val phones = listOf(info.phone1, info.phone2).filter { it.isNotBlank() }
        for (phone in phones) {
            PhoneTile(phone = phone)
        }

        // Instagram con icono estilizado.
        if (info.instagram.isNotBlank()) {
            InstagramTile(
                handle = info.instagram,
                onClick = { openUrl(context, "https://instagram.com/" + info.instagram) },
            )
        }

        if (info.email.isNotBlank()) {
            HomeContactRow(
                icon = Icons.Filled.Email,
                title = "Correo",
                value = info.email,
            )
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
        Text(title, style = DangelType.Section)
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
                Text(title, style = DangelType.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = DangelType.Body, fontWeight = FontWeight.Medium)
            }
        }
    }
}

/** One tile per phone number: direct WhatsApp + call. */
@Composable
private fun PhoneTile(phone: String) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.Phone,
                contentDescription = "Teléfono",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 12.dp),
            )
            Text(
                phone,
                style = DangelType.Lead,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            CircleIconButton(
                containerColor = Color(0xFF25D366),
                onClick = {
                    openWhatsApp(
                        context, phone,
                        "¡Hola D'Angel Gelsoft! Quiero información para reservar una sesión.",
                    )
                },
            ) {
                Icon(
                    Icons.Filled.Chat,
                    contentDescription = "WhatsApp",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            CircleIconButton(
                containerColor = MaterialTheme.colorScheme.primary,
                onClick = { dialPhone(context, phone) },
            ) {
                Icon(
                    Icons.Filled.Call,
                    contentDescription = "Llamar",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun CircleIconButton(
    containerColor: Color,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/** Instagram tile with a styled gradient icon. */
@Composable
private fun InstagramTile(handle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            InstagramIcon(modifier = Modifier.padding(end = 12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Instagram",
                    style = DangelType.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "@" + handle,
                    style = DangelType.CardTitle,
                )
            }
            Text(
                "›",
                style = DangelType.Title,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Instagram glyph drawn with the brand gradient. */
@Composable
private fun InstagramIcon(modifier: Modifier = Modifier, size: Dp = 30.dp) {
    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFF833AB4), Color(0xFFFD1D1D), Color(0xFFFCB045)),
    )
    Canvas(modifier = modifier.size(size)) {
        val px = size.toPx()
        val stroke = px * 0.10f
        drawRoundRect(
            brush = gradient,
            topLeft = Offset(stroke / 2f, stroke / 2f),
            size = Size(px - stroke, px - stroke),
            cornerRadius = CornerRadius(px * 0.30f, px * 0.30f),
            style = Stroke(width = stroke),
        )
        drawCircle(
            brush = gradient,
            radius = px * 0.20f,
            center = center,
            style = Stroke(width = stroke),
        )
        drawCircle(
            brush = gradient,
            radius = stroke * 0.65f,
            center = Offset(px * 0.71f, px * 0.29f),
        )
    }
}
