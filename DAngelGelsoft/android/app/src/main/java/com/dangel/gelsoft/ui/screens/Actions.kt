package com.dangel.gelsoft.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URLEncoder

/** Keeps only digits: "+53 54753951" -> "5354753951" (what wa.me needs). */
fun digitsOnly(phone: String): String = phone.filter { it.isDigit() }

/** Opens WhatsApp with a prefilled message. The user only taps "send". */
fun openWhatsApp(context: Context, phone: String, message: String) {
    val encoded = URLEncoder.encode(message, "UTF-8")
    val url = "https://wa.me/${digitsOnly(phone)}?text=$encoded"
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

/** Opens any web URL (Instagram profile, etc.) in the browser. */
fun openUrl(context: Context, url: String) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

/** Opens the phone dialer with the number ready (does not call by itself). */
fun dialPhone(context: Context, phone: String) {
    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${digitsOnly(phone)}")))
}
