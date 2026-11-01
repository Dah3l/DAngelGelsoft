package com.dangel.gelsoft.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * The user's own reservations, stored on-device.
 *
 * This is the source of truth for "Mis reservas": no phone lookup needed.
 * Supabase is only queried to refresh each reservation's estado
 * (pendiente / confirmada / rechazada).
 */
data class LocalReservation(
    val fecha: String,
    val bloqueId: Long,
    val bloqueNombre: String,
    val nombre: String,
    val telefono: String,
    val personas: Int,
    val total: Double,
    val creadoEn: Long,
    /** Last known estado (pendiente / confirmada / rechazada) — works offline. */
    val estado: String = "pendiente",
)

object ReservationStore {
    private const val PREFS = "dangel_prefs"
    private const val KEY = "mis_reservas"

    /** Cuban numbers: only the last 8 digits matter ("+53 5…" == "5…"). */
    fun normPhone(phone: String): String =
        phone.filter { it.isDigit() }.takeLast(8)

    fun all(context: Context): List<LocalReservation> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    add(
                        LocalReservation(
                            fecha = o.optString("fecha"),
                            bloqueId = o.optLong("bloqueId"),
                            bloqueNombre = o.optString("bloqueNombre"),
                            nombre = o.optString("nombre"),
                            telefono = o.optString("telefono"),
                            personas = o.optInt("personas", 1),
                            total = o.optDouble("total", 0.0),
                            creadoEn = o.optLong("creadoEn", 0),
                            estado = o.optString("estado", "pendiente"),
                        )
                    )
                }
            }.sortedByDescending { it.creadoEn }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun add(context: Context, r: LocalReservation) {
        val current = all(context).toMutableList()
        // Avoid duplicates: same date + block + normalized phone.
        val isDup = current.any {
            it.fecha == r.fecha && it.bloqueId == r.bloqueId &&
                normPhone(it.telefono) == normPhone(r.telefono)
        }
        if (!isDup) current.add(0, r)
        val arr = JSONArray()
        for (x in current.take(100)) {
            arr.put(
                JSONObject()
                    .put("fecha", x.fecha)
                    .put("bloqueId", x.bloqueId)
                    .put("bloqueNombre", x.bloqueNombre)
                    .put("nombre", x.nombre)
                    .put("telefono", x.telefono)
                    .put("personas", x.personas)
                    .put("total", x.total)
                    .put("creadoEn", x.creadoEn)
                    .put("estado", x.estado)
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, arr.toString()).apply()
    }

    /** Updates the cached estado of a reservation (from a DB refresh). */
    fun updateEstado(
        context: Context,
        fecha: String,
        bloqueId: Long,
        phone8: String,
        estado: String,
    ) {
        val updated = all(context).map { r ->
            if (r.fecha == fecha && r.bloqueId == bloqueId &&
                normPhone(r.telefono) == phone8
            ) {
                r.copy(estado = estado)
            } else r
        }
        val arr = JSONArray()
        for (x in updated.take(100)) {
            arr.put(
                JSONObject()
                    .put("fecha", x.fecha)
                    .put("bloqueId", x.bloqueId)
                    .put("bloqueNombre", x.bloqueNombre)
                    .put("nombre", x.nombre)
                    .put("telefono", x.telefono)
                    .put("personas", x.personas)
                    .put("total", x.total)
                    .put("creadoEn", x.creadoEn)
                    .put("estado", x.estado)
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, arr.toString()).apply()
    }
}
