package com.dangel.gelsoft.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dangel.gelsoft.R
import com.dangel.gelsoft.data.AppConfig

/**
 * Cover screen: the featured presentation of the field.
 * Logo + slogan + a real field photo, then functional buttons
 * (WhatsApp, call, Instagram) and a photo gallery.
 */
@Composable
fun CoverScreen(config: AppConfig) {
    val context = LocalContext.current
    val info = config.info
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Brand logo (black background image, sits naturally on the black theme).
        Image(
            painter = painterResource(id = R.drawable.logo_dangel),
            contentDescription = "Logo de D'Angel Gelsoft",
            modifier = Modifier
                .size(260.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Fit,
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = info.slogan,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = info.description,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "📍 ${info.location}",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))
        // Hero field photo.
        Image(
            painter = painterResource(id = R.drawable.foto_campo_4),
            contentDescription = "Jugadores equipados en el campo D'Angel Gelsoft",
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop,
        )

        Spacer(modifier = Modifier.height(20.dp))
        // Main action: WhatsApp reservation (generic message; the full form
        // lives in the "Reservar" tab and builds a detailed message).
        Button(
            onClick = {
                openWhatsApp(
                    context,
                    info.phone1,
                    "¡Hola D'Angel Gelsoft! Quiero información para reservar una sesión.",
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.Black,
            ),
        ) {
            Text("Reservar por WhatsApp", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = { dialPhone(context, info.phone1) },
                modifier = Modifier.weight(1f),
            ) {
                Text("Llamar")
            }
            OutlinedButton(
                onClick = {
                    openWhatsApp(
                        context,
                        info.phone2,
                        "¡Hola D'Angel Gelsoft! Quiero información para reservar una sesión.",
                    )
                },
                modifier = Modifier.weight(1f),
            ) {
                Text("WhatsApp 2")
            }
            OutlinedButton(
                onClick = { openUrl(context, "https://instagram.com/${info.instagram}") },
                modifier = Modifier.weight(1f),
            ) {
                Text("Instagram")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "El campo",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Gallery: the real field photos sent by the business.
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val photos = listOf(
                R.drawable.foto_campo_1 to "Partidas para todas las edades",
                R.drawable.foto_campo_2 to "Jugadores en el campo",
                R.drawable.foto_campo_3 to "Equipo del campo",
                R.drawable.foto_campo_4 to "Equipo táctico completo",
            )
            for ((photo, caption) in photos) {
                Card(
                    modifier = Modifier.size(width = 220.dp, height = 190.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Column {
                        Image(
                            painter = painterResource(id = photo),
                            contentDescription = caption,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            contentScale = ContentScale.Crop,
                        )
                        Text(
                            text = caption,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(8.dp),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Reservas: ${info.phone1} · ${info.phone2}",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}
