package com.dangel.gelsoft.ui.screens
import com.dangel.gelsoft.ui.theme.DangelType
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.dangel.gelsoft.data.LocalReservation
import com.dangel.gelsoft.data.ProfileStore
import com.dangel.gelsoft.data.ReservationStore
import com.dangel.gelsoft.data.SupabaseDirect
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

/** Formats a CUP amount without decimals when it's a whole number. */
private fun formatCup(value: Double): String {
    val int = value.toInt()
    return if (value == int.toDouble()) "$int CUP" else "%.2f CUP".format(value)
}

/**
 * Reservation v3: pick a date → choose a free block → fill the form
 * (name/phone prefilled from the profile) → see the price breakdown
 * (total + advance %) → send. On success the admin's WhatsApp chat opens
 * automatically with the prefilled message. The DB unique index is the
 * final guard against double booking.
 */
@Composable
fun ReservationScreen(config: AppConfig, onDone: () -> Unit, onMyReservations: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var fecha by remember { mutableStateOf(LocalDate.now()) }
    var bloque by remember { mutableStateOf<BlockItem?>(null) }
    var taken by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var loadingSlots by remember { mutableStateOf(false) }

    var nombre by remember { mutableStateOf(ProfileStore.getNombre(context)) }
    var telefono by remember { mutableStateOf(ProfileStore.getTelefono(context)) }
    var personas by remember { mutableIntStateOf(1) }

    var sending by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<SupabaseDirect.InsertResult?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var waFailed by remember { mutableStateOf(false) }

    val fechaStr = fecha.format(DateTimeFormatter.ISO_LOCAL_DATE)
    val dayOfWeek = fecha.dayOfWeek.value // 1=Mon … 7=Sun
    val availableBlocks = config.blocks.filter { it.appliesTo(dayOfWeek) }
    val adelantoPct = config.info.adelantoPct

    fun reloadSlots() {
        loadingSlots = true
        bloque = null
        scope.launch {
            taken = SupabaseDirect.takenBlocks(fechaStr)
            loadingSlots = false
        }
    }

    LaunchedEffect(fechaStr) { reloadSlots() }

    // ---------- Success view (WhatsApp opens automatically) ----------
    if (result is SupabaseDirect.InsertResult.Ok && bloque != null) {
        val b = bloque!!
        val total = b.price * personas
        val adelanto = total * adelantoPct / 100.0
        val waMsg = "¡Hola! Soy $nombre, acabo de solicitar una reserva en D'Angel Gelsoft:\n" +
            "$fechaStr · Bloque ${b.name} (${b.startTime} – ${b.endTime})\n" +
            "$personas persona(s) × ${formatCup(b.price)} = ${formatCup(total)}\n" +
            "Adelanto ($adelantoPct%): ${formatCup(adelanto)}\n" +
            "Mi teléfono: $telefono\n" +
            "Revísala aquí: ${SupabaseDirect.PANEL_RESERVAS_URL}"
        LaunchedEffect(result) {
            waFailed = false
            // Save on-device: "Mis reservas" reads local first.
            ReservationStore.add(
                context,
                LocalReservation(
                    fecha = fechaStr,
                    bloqueId = b.id,
                    bloqueNombre = b.name,
                    nombre = nombre.trim(),
                    telefono = telefono.trim(),
                    personas = personas,
                    total = total,
                    creadoEn = System.currentTimeMillis(),
                )
            )
            try {
                openWhatsApp(context, config.info.phone1, waMsg)
            } catch (e: Exception) {
                waFailed = true
            }
        }
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
                style = DangelType.Title,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "$fechaStr · Bloque ${b.name} (${b.startTime} – ${b.endTime})",
                style = DangelType.Body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Total: ${formatCup(total)} · Adelanto ($adelantoPct%): ${formatCup(adelanto)}",
                style = DangelType.BodyBold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Se abrió WhatsApp con el mensaje listo: solo toca enviar para avisar al administrador.",
                style = DangelType.Body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (waFailed) {
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = {
                    try {
                        openWhatsApp(context, config.info.phone1, waMsg)
                        waFailed = false
                    } catch (e: Exception) { /* keep showing */ }
                }) {
                    Text("Abrir WhatsApp de nuevo", color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
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
        Text("Reservar", style = DangelType.Title)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Elige el día y el bloque. La reserva queda pendiente hasta que el administrador la confirme.",
            style = DangelType.Body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))

        // ---------- Fecha ----------
        Text("1. Elige el día", style = DangelType.CardTitle)
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
        Text("2. Elige el bloque", style = DangelType.CardTitle)
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
                            "${b.name}: ${b.startTime} – ${b.endTime} · ${formatCup(b.price)}/persona" +
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
        Text("3. Tus datos", style = DangelType.CardTitle)
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
            Text("Personas: ", style = DangelType.Body)
            OutlinedButton(onClick = { if (personas > 1) personas-- }) { Text("−") }
            Text(" $personas ", style = DangelType.CardTitle)
            OutlinedButton(onClick = { if (personas < 50) personas++ }) { Text("+") }
        }

        // ---------- Precio ----------
        if (bloque != null) {
            val b = bloque!!
            val total = b.price * personas
            val adelanto = total * adelantoPct / 100.0
            Spacer(modifier = Modifier.height(16.dp))
            Text("4. Precio", style = DangelType.CardTitle)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    PriceRow("Bloque", "${b.name} (${b.startTime} – ${b.endTime})")
                    PriceRow("Personas", "$personas × ${formatCup(b.price)}")
                    Spacer(modifier = Modifier.height(6.dp))
                    PriceRow("Total", formatCup(total), bold = true)
                    PriceRow(
                        "Adelanto ($adelantoPct%)",
                        formatCup(adelanto),
                        bold = true,
                        highlight = true,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "El adelanto se paga por adelantado para confirmar la reserva.",
                        style = DangelType.Tiny,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        errorMsg?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = DangelType.Body)
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
        TextButton(
            onClick = onMyReservations,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Ver mis reservas", color = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PriceRow(
    label: String,
    value: String,
    bold: Boolean = false,
    highlight: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            label,
            style = DangelType.Body,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            value,
            style = DangelType.Body,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = if (highlight) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface,
        )
    }
}
