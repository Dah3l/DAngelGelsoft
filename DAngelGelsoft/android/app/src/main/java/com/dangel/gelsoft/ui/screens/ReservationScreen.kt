package com.dangel.gelsoft.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dangel.gelsoft.data.AppConfig
import com.dangel.gelsoft.data.BlockItem
import com.dangel.gelsoft.data.SupabaseDirect
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

/**
 * Reservation v2: pick a date → choose a free block → fill the form.
 * The reservation is saved as "pendiente"; the admin accepts it in the
 * web panel. The DB unique index is the final guard against double booking.
 */
@Composable
fun ReservationScreen(config: AppConfig, onDone: () -> Unit, onMyReservations: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var fecha by remember { mutableStateOf(LocalDate.now()) }
    var bloque by remember { mutableStateOf<BlockItem?>(null) }
    var taken by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var loadingSlots by remember { mutableStateOf(false) }

    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var personas by remember { mutableIntStateOf(1) }

    var sending by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<SupabaseDirect.InsertResult?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val fechaStr = fecha.format(DateTimeFormatter.ISO_LOCAL_DATE)
    val dayOfWeek = fecha.dayOfWeek.value // 1=Mon … 7=Sun
    val availableBlocks = config.blocks.filter { it.appliesTo(dayOfWeek) }

    fun reloadSlots() {
        loadingSlots = true
        bloque = null
        scope.launch {
            taken = SupabaseDirect.takenBlocks(fechaStr)
            loadingSlots = false
        }
    }

    LaunchedEffect(fechaStr) { reloadSlots() }

    // ---------- Success view ----------
    if (result is SupabaseDirect.InsertResult.Ok && bloque != null) {
        val b = bloque!!
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("✔", fontSize = 48.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "¡Solicitud registrada!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "$fechaStr · Bloque ${b.name} (${b.startTime} – ${b.endTime})",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "El administrador la revisará. Toca el botón para avisarle por WhatsApp y agilizar la confirmación.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    val msg = "¡Hola! Soy $nombre, acabo de solicitar una reserva en D'Angel Gelsoft: " +
                        "$fechaStr, bloque ${b.name} (${b.startTime}-${b.endTime}), $personas persona(s). " +
                        "Mi teléfono: $telefono. Revísala aquí: ${SupabaseDirect.PANEL_RESERVAS_URL}"
                    openWhatsApp(context, config.info.phone1, msg)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.Black,
                ),
            ) {
                Text("Avisar al administrador por WhatsApp", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text("Reservar", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Elige el día y el bloque. La reserva queda pendiente hasta que el administrador la confirme.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))

        // ---------- Fecha ----------
        Text("1. Elige el día", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                DatePickerDialog(
                    context,
                    { _, y, m, d -> fecha = LocalDate.of(y, m + 1, d) },
                    fecha.year, fecha.monthValue - 1, fecha.dayOfMonth,
                ).apply {
                    datePicker.minDate = System.currentTimeMillis()
                }.show()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(fecha.format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM yyyy")))
        }

        // ---------- Bloque ----------
        Spacer(modifier = Modifier.height(16.dp))
        Text("2. Elige el bloque", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        if (availableBlocks.isEmpty()) {
            Text(
                "No hay bloques este día. Prueba con otra fecha.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else if (loadingSlots) {
            CircularProgressIndicator()
        } else {
            for (b in availableBlocks) {
                val isTaken = taken.contains(b.id)
                val isSelected = bloque?.id == b.id
                FilterChip(
                    selected = isSelected,
                    onClick = { if (!isTaken) bloque = b },
                    enabled = !isTaken,
                    label = {
                        Text(
                            "${b.name}: ${b.startTime} – ${b.endTime}" +
                                if (isTaken) " (ocupado)" else ""
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.Black,
                    ),
                )
            }
        }

        // ---------- Datos ----------
        Spacer(modifier = Modifier.height(16.dp))
        Text("3. Tus datos", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
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
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Personas: ", fontSize = 15.sp)
            OutlinedButton(onClick = { if (personas > 1) personas-- }) { Text("−") }
            Text(" $personas ", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            OutlinedButton(onClick = { if (personas < 50) personas++ }) { Text("+") }
        }

        errorMsg?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                errorMsg = null
                if (bloque == null) {
                    errorMsg = "Elige un bloque primero."
                    return@Button
                }
                if (nombre.isBlank() || telefono.filter { it.isDigit() }.length < 8) {
                    errorMsg = "Escribe tu nombre y un teléfono válido."
                    return@Button
                }
                sending = true
                scope.launch {
                    result = SupabaseDirect.insertReservation(
                        bloque!!.id, fechaStr, nombre.trim(),
                        telefono.trim(), personas,
                    )
                    sending = false
                    if (result is SupabaseDirect.InsertResult.JustTaken) {
                        errorMsg = "Ese bloque se acaba de ocupar. Elige otro."
                        reloadSlots()
                    } else if (result is SupabaseDirect.InsertResult.Error) {
                        errorMsg = "No se pudo guardar: " +
                            (result as SupabaseDirect.InsertResult.Error).message
                    }
                }
            },
            enabled = !sending,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.Black,
            ),
        ) {
            if (sending) CircularProgressIndicator(color = Color.Black)
            else Text("Enviar solicitud", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
            Text("Cancelar")
        }
        Spacer(modifier = Modifier.height(4.dp))
        androidx.compose.material3.TextButton(
            onClick = onMyReservations,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Ver mis reservas", color = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
