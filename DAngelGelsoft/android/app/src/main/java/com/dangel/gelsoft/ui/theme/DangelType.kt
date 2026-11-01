package com.dangel.gelsoft.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Uniform text hierarchy for the whole app. Use these instead of
 * hardcoded font sizes so every screen reads the same.
 *
 * - Title: screen titles ("Reservar", "Mis reservas", "Galería")
 * - Section: section headers inside a screen ("Horarios", "Contacto")
 * - CardTitle: titles inside cards
 * - Lead: highlighted one-liners (slogans, prices)
 * - Body / BodyBold: paragraphs and form labels
 * - Caption: muted descriptions
 * - Tiny: micro labels (bottom bar, counters)
 */
object DangelType {
    val Title = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold)
    val Section = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
    val CardTitle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
    val Lead = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal)
    val Body = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal)
    val BodyBold = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)
    val Caption = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal)
    val Tiny = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal)
}
