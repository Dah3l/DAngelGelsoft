package com.dangel.gelsoft.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Gelsoft tactical iconography. Crosshair = the Reservar action
 * (locking onto the mission). Helmet = the Soldado (profile) tab.
 *
 * The helmet here is a simplified vector placeholder — it gets swapped
 * for the client's real helmet PNG (res/drawable/ic_helmet.png) once
 * uploaded via the GitHub web UI (binaries can't be pushed via API).
 */
@Composable
fun CrosshairIcon(
    modifier: Modifier = Modifier,
    color: Color,
    strokeWidth: Dp = 2.dp,
) {
    Canvas(modifier = modifier) {
        val r = size.minDimension / 2f
        val c = center
        val sw = strokeWidth.toPx()
        drawCircle(
            color = color, radius = r - sw / 2f, center = c,
            style = Stroke(width = sw),
        )
        drawLine(
            color = color,
            start = Offset(c.x - r, c.y), end = Offset(c.x + r, c.y),
            strokeWidth = sw, cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(c.x, c.y - r), end = Offset(c.x, c.y + r),
            strokeWidth = sw, cap = StrokeCap.Round,
        )
        drawCircle(color = color, radius = sw * 1.4f, center = c)
    }
}

@Composable
fun HelmetIcon(
    modifier: Modifier = Modifier,
    color: Color,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            // Dome.
            moveTo(w * 0.06f, h * 0.66f)
            cubicTo(
                w * 0.06f, h * 0.22f, w * 0.30f, h * 0.06f,
                w * 0.52f, h * 0.06f,
            )
            cubicTo(
                w * 0.76f, h * 0.06f, w * 0.93f, h * 0.28f,
                w * 0.95f, h * 0.60f,
            )
            // Brim flick.
            lineTo(w * 1.00f, h * 0.64f)
            lineTo(w * 0.02f, h * 0.72f)
            close()
        }
        drawPath(path = path, color = color)
        // Chin strap hint.
        val strap = Path().apply {
            moveTo(w * 0.30f, h * 0.70f)
            cubicTo(w * 0.38f, h * 0.86f, w * 0.46f, h * 0.94f, w * 0.52f, h * 0.96f)
            cubicTo(w * 0.58f, h * 0.94f, w * 0.64f, h * 0.86f, w * 0.70f, h * 0.70f)
        }
        drawPath(
            path = strap, color = color,
            style = Stroke(width = w * 0.07f, cap = StrokeCap.Round),
        )
    }
}
