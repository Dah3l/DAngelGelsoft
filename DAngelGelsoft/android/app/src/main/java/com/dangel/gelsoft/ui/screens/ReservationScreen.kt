package com.dangel.gelsoft.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dangel.gelsoft.data.AppConfig
import java.util.Calendar

/**
 * Reservation form with WhatsApp checkout.
 *
 * The reservation is NOT written to any database. The form builds a summary
 * message and opens WhatsApp (wa.me) to the business's reservation number
 * with the message already written — the user only taps "send".
 * This matches how the business already takes reservations today (by chat),
 * but with the data organized instead of a free-form message.
 */
@Composable
fun ReservationScreen(config: AppConfig) {
    val context = LocalContext.current
    val info = config.info

    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var sesion by remember { mutableStateOf("Sesión de día") }
    var hora by remember { mutableStateOf("10:00 AM") }
    var jugadores by remember { mutableStateOf("4") }
    var error by remember { mutableStateOf("") }

    // Time options depend on the session: the night experience starts at night.
    val horasDia = listOf(
        "10:00 AM", "11:00 AM", "12:00 PM", "1:00 PM", "2:00 PM", "3:00 PM",
        "4:00 PM", "5:00 PM", "6:00 PM", "7:00 PM", "8:00 PM", "9:00 PM",
    )
    val horasNoche = listOf("8:00 PM", "9:00 PM")
    val horasDisponibles = if (sesion.contains("nocturna", ignoreCase = true)) horasNoche else horasDia

    // Estimated total from the published prices (for the message summary only).
    val precioSesion = config.prices.firstOrNull {
        if (sesion.contains("nocturna", ignoreCase = true)) {
            it.name.contains("nocturna", ignoreCase = true)
        } else {
            it.name.contains("día", ignoreCase = true)
        }
    }
    val cantidadJugadores = jugadores.toIntOrNull() ?: 0
    val totalEstimado = if (precioSesion != null && cantidadJugadores > 0) {
        precioSesion.priceCup * cantidadJugadores
    } else {
        null
    }

    val calendar = Calendar.getInstance()
    val datePicker = DatePickerDialog(
        context,
        { _, year, month, day ->
            fecha = "%02d/%02d/%d".format(day, month + 1, year)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text("Reservar", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Llena los datos y se abrirá WhatsApp con tu reserva ya escrita. Solo toca enviar.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = { Text("Tu teléfono") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = { datePicker.show() },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (fecha.isBlank()) "Elegir día" else "Día: $fecha")
        }
        Spacer(modifier = Modifier.height(12.dp))
        DropdownField(
            label = "Sesión",
            options = listOf("Sesión de día", "Experiencia nocturna"),
            selected = sesion,
            onSelected = {
                sesion = it
                // Al cambiar de sesión, la hora vuelve a una válida para ella.
                hora = if (it.contains("nocturna", ignoreCase = true)) horasNoche.first() else horasDia.first()
            },
        )
        Spacer(modifier = Modifier.height(12.dp))
        DropdownField(
            label = "Hora",
            options = horasDisponibles,
            selected = hora,
            onSelected = { hora = it },
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = jugadores,
            onValueChange = { jugadores = it.filter { char -> char.isDigit() } },
            label = { Text("Cantidad de jugadores") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )

        if (totalEstimado != null && precioSesion != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Total estimado: $totalEstimado CUP " +
                    "(${precioSesion.priceCup} CUP ${precioSesion.unit} × $cantidadJugadores)",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        if (error.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(error, fontSize = 14.sp, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                when {
                    nombre.isBlank() -> error = "Escribe tu nombre."
                    telefono.isBlank() -> error = "Escribe tu teléfono."
                    fecha.isBlank() -> error = "Elige el día de la reserva."
                    cantidadJugadores <= 0 -> error = "Indica cuántos jugadores son."
                    else -> {
                        error = ""
                        val mensaje = buildString {
                            append("¡Hola D'Angel Gelsoft! Quiero hacer una reserva:\n")
                            append("• Nombre: $nombre\n")
                            append("• Teléfono: $telefono\n")
                            append("• Día: $fecha\n")
                            append("• Hora: $hora\n")
                            append("• Sesión: $sesion\n")
                            append("• Jugadores: $cantidadJugadores\n")
                            if (totalEstimado != null) {
                                append("• Total estimado: $totalEstimado CUP (según precios publicados en la app)")
                            }
                        }
                        openWhatsApp(context, info.phone1, mensaje)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.Black,
            ),
        ) {
            Text("Enviar reserva por WhatsApp", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "La reserva llega como mensaje de WhatsApp al número ${info.phone1}. " +
                "También puedes escribir directo al ${info.phone2}.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            for (option in options) {
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}
