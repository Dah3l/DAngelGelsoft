package com.dangel.gelsoft.data

import org.json.JSONObject

/**
 * Data model of the D'Angel Gelsoft app.
 *
 * The shape mirrors exactly what the Supabase Edge Function `app-config`
 * returns, and what lives in `assets/config.json` as the bundled fallback.
 * Unknown fields (like `nota_demo`) are ignored on purpose.
 */
data class BusinessInfo(
    val name: String,
    val slogan: String,
    val description: String,
    val location: String,
    val instagram: String,
    val phone1: String,
    val phone2: String,
)

data class ScheduleItem(
    val title: String,
    val days: String,
    val openTime: String,
    val closeTime: String,
    val note: String,
)

data class PriceItem(
    val name: String,
    val description: String,
    val priceCup: Int,
    val unit: String,
)

data class AnnouncementItem(
    val title: String,
    val body: String,
    val dateLabel: String,
)

data class AppConfig(
    val info: BusinessInfo,
    val schedules: List<ScheduleItem>,
    val prices: List<PriceItem>,
    val announcements: List<AnnouncementItem>,
)

/** Parses the single JSON served by the Edge Function (or the fallback file). */
fun parseConfig(jsonText: String): AppConfig {
    val root = JSONObject(jsonText)
    val infoJson = root.optJSONObject("info") ?: JSONObject()

    val info = BusinessInfo(
        name = infoJson.optString("nombre", "D'Angel Gelsoft"),
        slogan = infoJson.optString("eslogan", "Más que un juego, una misión"),
        description = infoJson.optString("descripcion", ""),
        location = infoJson.optString("ubicacion", "Acuario Nacional, La Habana"),
        instagram = infoJson.optString("instagram", "d_angel_gelsoft"),
        phone1 = infoJson.optString("telefono_reserva_1", "+53 54753951"),
        phone2 = infoJson.optString("telefono_reserva_2", "+53 58878734"),
    )

    val schedules = mutableListOf<ScheduleItem>()
    val schedulesJson = root.optJSONArray("horarios")
    if (schedulesJson != null) {
        for (i in 0 until schedulesJson.length()) {
            val item = schedulesJson.optJSONObject(i) ?: continue
            schedules.add(
                ScheduleItem(
                    title = item.optString("titulo", ""),
                    days = item.optString("dias", ""),
                    openTime = item.optString("apertura", ""),
                    closeTime = item.optString("cierre", ""),
                    note = item.optString("nota", ""),
                )
            )
        }
    }

    val prices = mutableListOf<PriceItem>()
    val pricesJson = root.optJSONArray("precios")
    if (pricesJson != null) {
        for (i in 0 until pricesJson.length()) {
            val item = pricesJson.optJSONObject(i) ?: continue
            prices.add(
                PriceItem(
                    name = item.optString("nombre", ""),
                    description = item.optString("descripcion", ""),
                    priceCup = item.optInt("precio_cup", 0),
                    unit = item.optString("unidad", "por persona"),
                )
            )
        }
    }

    val announcements = mutableListOf<AnnouncementItem>()
    val announcementsJson = root.optJSONArray("anuncios")
    if (announcementsJson != null) {
        for (i in 0 until announcementsJson.length()) {
            val item = announcementsJson.optJSONObject(i) ?: continue
            announcements.add(
                AnnouncementItem(
                    title = item.optString("titulo", ""),
                    body = item.optString("texto", ""),
                    dateLabel = item.optString("fecha", ""),
                )
            )
        }
    }

    return AppConfig(info, schedules, prices, announcements)
}
