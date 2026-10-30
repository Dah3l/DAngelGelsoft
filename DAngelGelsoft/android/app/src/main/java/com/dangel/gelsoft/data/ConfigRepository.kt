package com.dangel.gelsoft.data

import android.content.Context
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Loads the app content with this priority order:
 *
 *  1. Network: the single JSON from the Supabase Edge Function `app-config`.
 *     On success the JSON is saved to the local cache.
 *  2. Local cache: the last JSON downloaded. This is what makes the app work
 *     offline — important for clients without VPN or without connection.
 *  3. Bundled fallback: `assets/config.json`, shipped inside the APK, so the
 *     demo runs standalone before any Supabase project exists.
 *
 * To point the app at a real backend, replace ENDPOINT_URL with the deployed
 * JSON URL (GitHub Pages, modelo Aguacero).
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

    suspend fun load(context: Context): LoadResult = withContext(Dispatchers.IO) {
        // 1. Network (skipped while the endpoint URL is still a placeholder).
        if (!ENDPOINT_URL.contains("TU-PROYECTO")) {
            try {
                val connection = URL(ENDPOINT_URL).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 8000
                connection.readTimeout = 8000
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val text = connection.inputStream.bufferedReader().readText()
                    connection.disconnect()
                    // Save to cache before parsing, so a later offline open works.
                    File(context.filesDir, CACHE_FILE).writeText(text)
                    return@withContext LoadResult(parseConfig(text), offline = false, source = "red")
                }
                connection.disconnect()
            } catch (_: Exception) {
                // No connection, timeout, bad URL... fall through to cache.
            }
        }

        // 2. Local cache from a previous successful download.
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

        // 3. Bundled fallback shipped inside the APK. Always available.
        val fallback = context.assets.open("config.json").bufferedReader().readText()
        LoadResult(parseConfig(fallback), offline = true, source = "incluido")
    }
}
