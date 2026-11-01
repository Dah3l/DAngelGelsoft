package com.dangel.gelsoft.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dangel.gelsoft.data.ProfileStore
import com.dangel.gelsoft.ui.theme.DangelType

/**
 * First-launch profile setup: shown once after the splash so the user's
 * name/phone are ready before the first reservation. Skippable.
 */
@Composable
fun ProfileSetupDialog(onDone: () -> Unit) {
    val context = LocalContext.current
    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    fun finish() {
        context.getSharedPreferences("dangel_prefs", Context.MODE_PRIVATE)
            .edit().putBoolean("perfil_configurado", true).apply()
        onDone()
    }

    AlertDialog(
        onDismissRequest = { /* use the buttons */ },
        title = { Text("Configura tu perfil", style = DangelType.Title) },
        text = {
            Column {
                Text(
                    "Guarda tu nombre y teléfono una sola vez para reservar más rápido.",
                    style = DangelType.Body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono / WhatsApp") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                )
                error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, style = DangelType.Caption)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (nombre.isBlank()) {
                        error = "Escribe tu nombre."
                        return@TextButton
                    }
                    if (telefono.filter { it.isDigit() }.length < 8) {
                        error = "Escribe un teléfono válido."
                        return@TextButton
                    }
                    ProfileStore.save(context, nombre, telefono)
                    finish()
                },
            ) { Text("Guardar", color = MaterialTheme.colorScheme.primary) }
        },
        dismissButton = {
            TextButton(onClick = { finish() }) { Text("Omitir por ahora") }
        },
    )
}
