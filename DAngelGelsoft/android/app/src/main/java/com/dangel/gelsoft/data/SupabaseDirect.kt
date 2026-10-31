package com.dangel.gelsoft.data

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Direct Supabase REST access for the LIVE parts of the app:
 * reservation availability and reservation inserts.
 *
 * Everything else (content) comes from the GitHub Pages JSON. These calls
 * are tiny (a few KB) and only happen inside the reservation flow, so the
 * free-tier bandwidth is never at risk.
 *
 * The anon key is public by design (it also ships in the web panel).
 */
object SupabaseDirect {

    const val URL_BASE = "https://cxkacnymioovvhvwqlsj.supabase.co"
    private const val ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImN4a2FjbnltaW9vdnZodndxbHNqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTMzMjI2ODEsImV4cCI6MjEwODg5ODY4MX0.shNDQbIqO3qPU2bngrD6SyC49gCfv84jJYjz7uYBTyc"

    /** Link to the admin's pending-reservations page (sent via WhatsApp). */
    const val PANEL_RESERVAS_URL = "https://dah3l.github.io/DAngelGelsoft/panel.html"

    private fun headers(con: HttpURLConnection) {
        con.setRequestProperty("apikey", ANON_KEY)
        con.setRequestProperty("Authorization", "Bearer $ANON_KEY")
        con.setRequestProperty("Content-Type", "application/json")
    }

    private fun readText(con: HttpURLConnection): String {
        val stream = if (con.responseCode < 400) con.inputStream else con.errorStream
        return stream.bufferedReader().readText().also { con.disconnect() }
    }

    /**
     * Block IDs already taken for [fecha] (yyyy-MM-dd): reservations with
     * estado 'pendiente' or 'confirmada'. Read from the public view, which
     * exposes no personal data.
     */
    suspend fun takenBlocks(fecha: String): Set<Long> = withContext(Dispatchers.IO) {
        try {
            val q = "fecha=eq.$fecha&select=bloque_id"
            val con = URL("$URL_BASE/rest/v1/reservation_slots?$q").openConnection() as HttpURLConnection
            con.requestMethod = "GET"
            con.connectTimeout = 8000
            con.readTimeout = 8000
            headers(con)
            if (con.responseCode != HttpURLConnection.HTTP_OK) {
                con.disconnect()
                return@withContext emptySet()
            }
            val arr = JSONArray(readText(con))
            buildSet {
                for (i in 0 until arr.length()) {
                    add(arr.optJSONObject(i)?.optLong("bloque_id", -1) ?: -1)
                }
            }.filter { it >= 0 }.toSet()
        } catch (_: Exception) {
            emptySet()
        }
    }

    sealed interface InsertResult {
        data object Ok : InsertResult
        /** The unique (fecha, bloque) constraint rejected it: just taken. */
        data object JustTaken : InsertResult
        data class Error(val message: String) : InsertResult
    }

    /** Inserts a pending reservation. The DB unique index is the final guard. */
    suspend fun insertReservation(
        bloqueId: Long,
        fecha: String,
        nombre: String,
        telefono: String,
        personas: Int,
    ): InsertResult = withContext(Dispatchers.IO) {
        try {
            val con = URL("$URL_BASE/rest/v1/reservas").openConnection() as HttpURLConnection
            con.requestMethod = "POST"
            con.connectTimeout = 10000
            con.readTimeout = 10000
            con.doOutput = true
            headers(con)
            val body = JSONObject()
                .put("bloque_id", bloqueId)
                .put("fecha", fecha)
                .put("nombre", nombre)
                .put("telefono", telefono)
                .put("personas", personas)
                .put("estado", "pendiente")
                .toString()
            con.outputStream.bufferedWriter().use { it.write(body) }
            val code = con.responseCode
            val text = readText(con)
            when {
                code in 200..299 -> InsertResult.Ok
                code == 409 || text.contains("reservas_unicas") || text.contains("duplicate") ->
                    InsertResult.JustTaken
                else -> InsertResult.Error("Error $code")
            }
        } catch (e: Exception) {
            InsertResult.Error(e.message ?: "sin conexión")
        }
    }

    data class MyReservation(
        val id: Long,
        val fecha: String,
        val bloqueId: Long,
        val nombre: String,
        val personas: Int,
        val estado: String,
    )

    /** Reservations made with this phone number (digits only comparison). */
    suspend fun myReservations(telefono: String): List<MyReservation> = withContext(Dispatchers.IO) {
        try {
            val digits = telefono.filter { it.isDigit() }
            val q = "select=id,fecha,bloque_id,nombre,personas,estado&telefono=eq." +
                URLEncoder.encode(telefono, "UTF-8") + "&order=fecha.desc"
            val con = URL("$URL_BASE/rest/v1/reservas?$q").openConnection() as HttpURLConnection
            con.requestMethod = "GET"
            con.connectTimeout = 8000
            con.readTimeout = 8000
            headers(con)
            if (con.responseCode != HttpURLConnection.HTTP_OK) {
                con.disconnect()
                return@withContext emptyList()
            }
            val arr = JSONArray(readText(con))
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    // Match by digits so "+53 5..." equals "535...".
                    if (o.optString("telefono").filter { c -> c.isDigit() } != digits) continue
                    add(
                        MyReservation(
                            id = o.optLong("id"),
                            fecha = o.optString("fecha"),
                            bloqueId = o.optLong("bloque_id"),
                            nombre = o.optString("nombre"),
                            personas = o.optInt("personas", 1),
                            estado = o.optString("estado"),
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
