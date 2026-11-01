package com.dangel.gelsoft.ui.screens
import com.dangel.gelsoft.ui.components.shimmer
import com.dangel.gelsoft.ui.theme.DangelType
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.dangel.gelsoft.data.AppConfig
import com.dangel.gelsoft.data.GalleryPhoto

/**
 * Gallery v3: sections as filter chips, photos in a grid WITHOUT titles
 * (image only). Tapping a photo opens it fullscreen, swipeable like a
 * normal photo gallery.
 */
@Composable
fun GalleryScreen(config: AppConfig) {
    val sections = config.gallery
    var selected by remember { mutableIntStateOf(0) }
    var viewer by remember { mutableStateOf<ViewerState?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Galería",
            style = DangelType.Title,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
        )
        if (sections.isEmpty()) {
            Text(
                text = "Pronto publicaremos fotos del campo.",
                style = DangelType.Body,
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
                        selectedLabelColor = Color.Black,
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
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 104.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(photos, key = { it.id }) { photo ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .shimmer(),
                ) {
                    AsyncImage(
                        model = photo.url,
                        contentDescription = "Foto de la galería",
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                            viewer = ViewerState(
                                photos = photos,
                                startIndex = photos.indexOf(photo).coerceAtLeast(0),
                            )
                        },
                    contentScale = ContentScale.Crop,
                    )
                }
            }
        }
    }

    viewer?.let { state ->
        FullscreenPhotoViewer(
            state = state,
            onClose = { viewer = null },
        )
    }
}

private data class ViewerState(val photos: List<GalleryPhoto>, val startIndex: Int)

/** Fullscreen photo viewer: swipe between photos, tap X or back to close. */
@Composable
private fun FullscreenPhotoViewer(state: ViewerState, onClose: () -> Unit) {
    val pagerState = rememberPagerState(
        initialPage = state.startIndex,
        pageCount = { state.photos.size },
    )
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                AsyncImage(
                    model = state.photos[page].url,
                    contentDescription = "Foto ampliada",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = Color.White)
            }
            Text(
                text = "${pagerState.currentPage + 1} / ${state.photos.size}",
                color = Color.White,
                style = DangelType.Body,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
            )
        }
    }
}
