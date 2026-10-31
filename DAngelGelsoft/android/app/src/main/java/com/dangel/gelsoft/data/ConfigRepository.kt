package com.dangel.gelsoft.data

import android.content.Context
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Loads the app content.
 *
 * Offline-first: [loadCached] returns instantly from the local cache (or
 * the bundled fallback) so the app NEVER shows an infinite spinner.
 * [refreshFromNetwork] then updates in the background when there is
 * connection; the UI swaps in the fresh data silently.
 *
 * Priority: 1. network (background refresh) 2. local cache 3. bundled.
 */
object ConfigRepository {

    // JSON generado por el workflow syncSupabase.yml (Supabase -> Pages).
    const val ENDPOINT_URL = "https://dah3l.github.io/DAngelGelsoft/app-config.json"

    private const val CACHE_FILE = "app_config.json"

    data class LoadResult(
        val config: AppConfig,
        /** True when the data did NOT come from the network. */
        val offline: Boolean,
        /** "red", "cache" or "incluido" — shown only as a small status line. */
        val source: String,
    )

    /** Instant: cache or bundled fallback. Never touches the network. */
    fun loadCached(context: Context): AppConfig {
        try {
            val cache = File(context.filesDir, CACHE_FILE)
            if (cache.exists()) {
                val text = cache.readText()
                if (text.isNotBlank()) return parseConfig(text)
            }
        } catch (_: Exception) {
            // Corrupt cache: fall through to the bundled fallback.
        }
        val fallback = context.assets.open("config.json").bufferedReader().readText()
        return parseConfig(fallback)
    }

    /**
     * Background refresh: tries the network, saves to cache and returns
     * the fresh config; returns null when offline or on any error.
     */
    suspend fun refreshFromNetwork(context: Context): AppConfig? = withContext(Dispatchers.IO) {
        if (ENDPOINT_URL.contains("TU-PROYECTO")) return@withContext null
        try {
            val connection = URL(ENDPOINT_URL).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val text = connection.inputStream.bufferedReader().readText()
                connection.disconnect()
                File(context.filesDir, CACHE_FILE).writeText(text)
                return@withContext parseConfig(text)
            }
            connection.disconnect()
        } catch (_: Exception) {
            // No connection, timeout, bad URL... the cached content stays.
        }
        null
    }

    /** Legacy full load (network → cache → bundled). Kept for compat. */
    suspend fun load(context: Context): LoadResult = withContext(Dispatchers.IO) {
        val fresh = refreshFromNetwork(context)
        if (fresh != null) return@withContext LoadResult(fresh, offline = false, source = "red")
        try {
            val cache = File(context.filesDir, CACHE_FILE)
            if (cache.exists()) {
                val text = cache.readText()
                if (text.isNotBlank()) {
                    return@withContext LoadResult(parseConfig(text), offline = true, source = "cache")
                }
            }
        } catch (_: Exception) {
            // Corrupt cache: fall through to the bundled fallback.
        }
        val fallback = context.assets.open("config.json").bufferedReader().readText()
        LoadResult(parseConfig(fallback), offline = true, source = "incluido")
    }
}
