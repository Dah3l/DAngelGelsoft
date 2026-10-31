package com.dangel.gelsoft.data

import android.content.Context

/**
 * User profile stored on-device (no account): name + phone auto-fill the
 * reservation form, and the notification toggle gates announcement alerts.
 */
object ProfileStore {
    private const val PREFS = "dangel_prefs"
    private const val KEY_NOMBRE = "perfil_nombre"
    private const val KEY_TELEFONO = "perfil_telefono"
    private const val KEY_NOTIF = "perfil_notif_avisos"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getNombre(context: Context): String =
        prefs(context).getString(KEY_NOMBRE, "") ?: ""

    fun getTelefono(context: Context): String =
        prefs(context).getString(KEY_TELEFONO, "") ?: ""

    fun save(context: Context, nombre: String, telefono: String) {
        prefs(context).edit()
            .putString(KEY_NOMBRE, nombre.trim())
            .putString(KEY_TELEFONO, telefono.trim())
            .apply()
    }

    fun initials(nombre: String): String {
        val parts = nombre.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
        return when {
            parts.isEmpty() -> "?"
            parts.size == 1 -> parts[0].take(1).uppercase()
            else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
        }
    }

    fun notifEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_NOTIF, true)

    fun setNotifEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_NOTIF, enabled).apply()
    }
}
