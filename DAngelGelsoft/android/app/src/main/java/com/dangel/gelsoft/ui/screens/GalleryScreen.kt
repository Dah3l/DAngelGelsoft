package com.dangel.gelsoft.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dangel.gelsoft.data.AppConfig

/**
 * Gallery: sections (Armas, Eventos, Clientes...) as filter chips, photos
 * in a grid loaded from the Supabase bucket with Coil.
 */
@Composable
fun GalleryScreen(config: AppConfig) {
    val sections = config.gallery
    var selected by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Galería",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
        )
        if (sections.isEmpty()) {
            Text(
                text = "Pronto publicaremos fotos del campo.",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp),
            )
            return
        }
        // Section chips.
        androidx.compose.foundation.lazy.LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(sections.size) { index ->
                FilterChip(
                    selected = selected == index,
                    onClick = { selected = index },
                    label = { Text(sections[index].name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = androidx.compose.ui.graphics.Color.Black,
                    ),
                )
            }
        }
        val photos = sections.getOrNull(selected)?.photos.orEmpty()
        if (photos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Sin fotos en esta sección todavía.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(photos, key = { it.id }) { photo ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Column {
                        AsyncImage(
                            model = photo.url,
                            contentDescription = photo.title.ifBlank { "Foto de la galería" },
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop,
                        )
                        if (photo.title.isNotBlank()) {
                            Text(
                                text = photo.title,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(8.dp),
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}
