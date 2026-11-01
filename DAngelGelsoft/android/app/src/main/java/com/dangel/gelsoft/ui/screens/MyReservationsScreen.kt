package com.dangel.gelsoft.ui.screens
import com.dangel.gelsoft.ui.theme.DangelType
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dangel.gelsoft.data.AppConfig
import com.dangel.gelsoft.data.ProfileStore
import com.dangel.gelsoft.data.ReservationStore
import com.dangel.gelsoft.data.SupabaseDirect
import kotlinx.coroutines.launch

/**
 * "Mis reservas" v2: the on-device list is the source of truth (no phone
 * lookup — the profile phone is used automatically). Supabase is only
 * queried to refresh each reservation's estado (pendiente / confirmada /
 * rechazada). Phone matching ignores any +53 prefix: last 8 digits rule.
 */
@Composable
fun MyReservationsScreen(config: AppConfig) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    data class Row(
        val fecha: String,
        val bloqueId: Long,
        val bloqueNombre: String,
        val nombre: String,
        val personas: Int,
        val total: Double,
        val estado: String,
    )

    var rows by remember { mutableStateOf<List<Row>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    val blockNames = config.blocks.associate { it.id to it.name }

    fun refresh() {
        loading = true
        scope.launch {
            val local = ReservationStore.all(context)
            val phone = ProfileStore.getTelefono(context)
            val db = if (phone.isNotBlank()) SupabaseDirect.myReservations(phone) else emptyList()

            val out = mutableListOf<Row>()
            val seen = mutableSetOf<Triple<String, Long, String>>()

            for (l in local) {
                val phone8 = ReservationStore.normPhone(l.telefono)
                seen.add(Triple(l.fecha, l.bloqueId, phone8))
                val remote = db.firstOrNull { d ->
                    d.fecha == l.fecha && d.bloqueId == l.bloqueId &&
                        ReservationStore.normPhone(d.telefono) == phone8
                }
                // Persist the fresh estado (offline-first); fall back to the
                // cached one when there's no connection.
                val estado = remote?.estado ?: l.estado
                if (remote != null && remote.estado != l.estado) {
                    ReservationStore.updateEstado(
                        context, l.fecha, l.bloqueId, phone8, remote.estado,
                    )
                }
                out.add(
                    Row(
                        fecha = l.fecha,
                        bloqueId = l.bloqueId,
                        bloqueNombre = l.bloqueNombre.ifBlank { blockNames[l.bloqueId] ?: "" },
                        nombre = l.nombre,
                        personas = l.personas,
                        total = l.total,
                        estado = estado,
                    )
                )
            }
            // DB rows not on this device (created before this feature).
            for (d in db) {
                val k = Triple(d.fecha, d.bloqueId, ReservationStore.normPhone(d.telefono))
                if (seen.add(k)) {
                    out.add(
                        Row(
                            fecha = d.fecha,
                            bloqueId = d.bloqueId,
                            bloqueNombre = blockNames[d.bloqueId] ?: "",
                            nombre = d.nombre,
                            personas = d.personas,
                            total = 0.0,
                            estado = d.estado,
                        )
                    )
                }
            }
            rows = out.sortedByDescending { it.fecha }
            loading = false
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Mis reservas", style = DangelType.Title)
            IconButton(onClick = { refresh() }) {
                Icon(Icons.Filled.Refresh, contentDescription = "Actualizar")
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Tus solicitudes y su estado. La base de datos solo confirma si fueron aceptadas o rechazadas.",
            style = DangelType.Body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (rows.isEmpty()) {
            Text(
                "Aún no tienes reservas. Usa el botón central para reservar tu sesión.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        for (r in rows) {
            val estadoColor = when (r.estado) {
                "confirmada" -> Color(0xFF4ADE80)
                "rechazada" -> Color(0xFFFF7B7B)
                else -> Color(0xFFFFD60A)
            }
            val estadoLabel = when (r.estado) {
                "confirmada" -> "Confirmada"
                "rechazada" -> "Rechazada"
                else -> "Pendiente"
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        estadoLabel,
                        style = DangelType.BodyBold,
                        color = estadoColor,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${r.fecha} · Bloque ${r.bloqueNombre}",
                        style = DangelType.Body,
                    )
                    val detail = buildString {
                        append("${r.personas} persona(s)")
                        if (r.total > 0) append(" · ${formatCupLocal(r.total)}")
                        append(" · ${r.nombre}")
                    }
                    Text(
                        detail,
                        style = DangelType.Body,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

private fun formatCupLocal(value: Double): String {
    val int = value.toInt()
    return if (value == int.toDouble()) "$int CUP" else "%.2f CUP".format(value)
}
