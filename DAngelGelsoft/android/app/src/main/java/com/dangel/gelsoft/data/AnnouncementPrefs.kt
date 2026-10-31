package com.dangel.gelsoft.data

import android.content.Context

/**
 * Tracks which announcement IDs the user has already seen, so the bell
 * badge and the system notification only fire for NEW ones.
 */
object AnnouncementPrefs {

    private const val PREFS = "dangel_prefs"
    private const val KEY_SEEN = "seen_announcement_ids"

    fun seenIds(context: Context): Set<Long> =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getStringSet(KEY_SEEN, emptySet())
            ?.mapNotNull { it.toLongOrNull() }
            ?.toSet() ?: emptySet()

    fun markSeen(context: Context, ids: Collection<Long>) {
        val current = seenIds(context).toMutableSet()
        current.addAll(ids)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_SEEN, current.map { it.toString() }.toSet())
            .apply()
    }
}
