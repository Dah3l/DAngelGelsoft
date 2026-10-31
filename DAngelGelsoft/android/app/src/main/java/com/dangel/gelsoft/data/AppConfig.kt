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
    val website: String,
    val email: String,
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
    val id: Long,
    val title: String,
    val body: String,
    val dateLabel: String,
)

data class GalleryPhoto(
    val id: Long,
    val url: String,
    val title: String,
)

data class GallerySection(
    val id: Long,
    val name: String,
    val photos: List<GalleryPhoto>,
)

data class BlockItem(
    val id: Long,
    val name: String,
    val startTime: String,
    val endTime: String,
    /** Comma-separated day numbers: 1=Mon … 7=Sun (java.time DayOfWeek). */
    val days: String,
) {
    fun appliesTo(dayOfWeek: Int): Boolean =
        days.split(",").mapNotNull { it.trim().toIntOrNull() }.contains(dayOfWeek)
}

data class AppConfig(
    val info: BusinessInfo,
    val schedules: List<ScheduleItem>,
    val prices: List<PriceItem>,
    val announcements: List<AnnouncementItem>,
    val gallery: List<GallerySection>,
    val blocks: List<BlockItem>,
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
        website = infoJson.optString("sitio_web", ""),
        email = infoJson.optString("correo", ""),
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
                    id = item.optLong("id", 0),
                    title = item.optString("titulo", ""),
                    body = item.optString("texto", ""),
                    dateLabel = item.optString("fecha", ""),
                )
            )
        }
    }

    val gallery = mutableListOf<GallerySection>()
    val galleryJson = root.optJSONArray("galeria")
    if (galleryJson != null) {
        for (i in 0 until galleryJson.length()) {
            val sec = galleryJson.optJSONObject(i) ?: continue
            val photosJson = sec.optJSONArray("fotos")
            val photos = mutableListOf<GalleryPhoto>()
            if (photosJson != null) {
                for (j in 0 until photosJson.length()) {
                    val ph = photosJson.optJSONObject(j) ?: continue
                    photos.add(
                        GalleryPhoto(
                            id = ph.optLong("id", 0),
                            url = ph.optString("url", ""),
                            title = ph.optString("titulo", ""),
                        )
                    )
                }
            }
            gallery.add(
                GallerySection(
                    id = sec.optLong("id", 0),
                    name = sec.optString("nombre", ""),
                    photos = photos,
                )
            )
        }
    }

    val blocks = mutableListOf<BlockItem>()
    val blocksJson = root.optJSONArray("bloques")
    if (blocksJson != null) {
        for (i in 0 until blocksJson.length()) {
            val item = blocksJson.optJSONObject(i) ?: continue
            blocks.add(
                BlockItem(
                    id = item.optLong("id", 0),
                    name = item.optString("nombre", ""),
                    startTime = item.optString("hora_inicio", ""),
                    endTime = item.optString("hora_fin", ""),
                    days = item.optString("dias", "1,2,3,4,5,6"),
                )
            )
        }
    }

    return AppConfig(info, schedules, prices, announcements, gallery, blocks)
}
