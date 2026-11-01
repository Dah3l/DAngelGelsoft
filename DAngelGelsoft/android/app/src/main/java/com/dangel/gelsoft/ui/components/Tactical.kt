package com.dangel.gelsoft.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dangel.gelsoft.ui.theme.DangelType

/** Monospaced family for tactical data: prices, times, codes, statuses. */
val DangelMono = FontFamily.Monospace

/**
 * Shape with 45-degree chamfered corners (cut corners), the tactical
 * geometry. Use for cards and buttons; the floating pill navbar stays round.
 */
class CutCornerShape(private val cut: Dp = 6.dp) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val c = with(density) { cut.toPx() }
        val path = Path().apply {
            moveTo(c, 0f)
            lineTo(size.width - c, 0f)
            lineTo(size.width, c)
            lineTo(size.width, size.height - c)
            lineTo(size.width - c, size.height)
            lineTo(c, size.height)
            lineTo(0f, size.height - c)
            lineTo(0f, c)
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * HUD corner brackets: thin L-shaped lines at the four corners of a
 * container, like a scope reticle. Overlay it on a Box with fillMaxSize().
 */
@Composable
fun HudCorners(
    modifier: Modifier = Modifier,
    color: Color = Color.White.copy(alpha = 0.35f),
    length: Dp = 16.dp,
    stroke: Dp = 2.dp,
    inset: Dp = 4.dp,
) {
    Canvas(modifier = modifier) {
        val l = length.toPx()
        val s = stroke.toPx()
        val ins = inset.toPx()
        val w = size.width
        val h = size.height
        // Top-left.
        drawLine(color, Offset(ins, ins + l), Offset(ins, ins), s)
        drawLine(color, Offset(ins, ins), Offset(ins + l, ins), s)
        // Top-right.
        drawLine(color, Offset(w - ins - l, ins), Offset(w - ins, ins), s)
        drawLine(color, Offset(w - ins, ins), Offset(w - ins, ins + l), s)
        // Bottom-left.
        drawLine(color, Offset(ins, h - ins - l), Offset(ins, h - ins), s)
        drawLine(color, Offset(ins, h - ins), Offset(ins + l, h - ins), s)
        // Bottom-right.
        drawLine(color, Offset(w - ins, h - ins - l), Offset(w - ins, h - ins), s)
        drawLine(color, Offset(w - ins, h - ins), Offset(w - ins - l, h - ins), s)
    }
}

/**
 * Diagonal hazard stripes (danger tape) for occupied/unavailable blocks.
 * Drawn behind the content.
 */
fun Modifier.hazardStripes(
    stripeColor: Color = Color(0xFF6B2D2D),
    bgColor: Color = Color(0xFF2A1C1C),
): Modifier = this.drawBehind {
    drawRect(bgColor)
    val stripeW = 10.dp.toPx()
    val step = stripeW * 2.2f
    var x = -size.height
    while (x < size.width + size.height) {
        drawLine(
            color = stripeColor,
            start = Offset(x, 0f),
            end = Offset(x + size.height, size.height),
            strokeWidth = stripeW,
        )
        x += step
    }
}

/**
 * Section header with a tactical system prefix: [SYS.01] TITLE.
 * Drop-in replacement for the plain SectionHeader.
 */
@Composable
fun TacticalSectionHeader(
    code: String,
    title: String,
    onSeeAll: (() -> Unit)?,
    seeAllText: String = "Ver todos",
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "[$code]",
                fontFamily = DangelMono,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "  " + title.uppercase(),
                style = DangelType.Section,
            )
        }
        if (onSeeAll != null) {
            TextButton(onClick = onSeeAll) {
                Text(seeAllText, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

/**
 * Military dog tag avatar: rounded plate with a hole at the top,
 * initials stamped in the middle.
 */
@Composable
fun DogTagAvatar(
    initials: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(96.dp, 110.dp)
            .drawBehind {
                val w = size.width
                val h = size.height
                // Plate.
                drawRoundRect(
                    color = Color(0xFF2E2E2E),
                    size = Size(w, h),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f),
                )
                // Hole at the top (punched out).
                drawCircle(
                    color = Color(0xFF121212),
                    radius = 11f,
                    center = Offset(w / 2f, 20f),
                )
                // Stitched border (velcro-patch feel).
                drawRoundRect(
                    color = Color(0xFF8A8A8A),
                    size = Size(w - 12f, h - 12f),
                    topLeft = Offset(6f, 6f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f, 14f),
                    style = Stroke(
                        width = 2.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f),
                    ),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            fontSize = 34.sp,
            fontWeight = FontWeight.Black,
            fontFamily = DangelMono,
            color = Color.White,
            modifier = Modifier.padding(top = 10.dp),
        )
    }
}

/**
 * Velcro-patch rank badge: dark patch, stitched dashed border,
 * rank name in mono caps.
 */
@Composable
fun VelcroRank(
    rank: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(CutCornerShape(4.dp))
            .background(Color(0xFF23261F))
            .drawBehind {
                val w = size.width
                val h = size.height
                drawRect(
                    color = Color(0xFFC9A227),
                    topLeft = Offset(5f, 5f),
                    size = Size(w - 10f, h - 10f),
                    style = Stroke(
                        width = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f, 5f), 0f),
                    ),
                )
            }
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "RANGO: ${rank.uppercase()}",
            fontFamily = DangelMono,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE8D98A),
        )
    }
}

/**
 * Step header for the reservation flow: [PASO 01] ELIGE EL DÍA.
 */
@Composable
fun TacticalStepHeader(
    code: String,
    title: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "[$code]",
            fontFamily = DangelMono,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "  " + title.uppercase(),
            style = DangelType.Section,
        )
    }
}

/**
 * Small tactical tag for gallery photos: CAM_01, RECON_02...
 * Place it over the top-start corner of a photo.
 */
@Composable
fun CamTag(
    label: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        fontFamily = DangelMono,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = modifier
            .background(Color.White.copy(alpha = 0.85f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}
