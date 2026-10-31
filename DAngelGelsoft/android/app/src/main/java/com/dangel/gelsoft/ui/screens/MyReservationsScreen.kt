package com.dangel.gelsoft.ui.screens

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dangel.gelsoft.data.AppConfig
import com.dangel.gelsoft.data.SupabaseDirect
import kotlinx.coroutines.launch

/**
 * "Mis reservas": the client enters their phone number and sees the
 * status of their requests (pendiente / confirmada / rechazada).
 */
@Composable
fun MyReservationsScreen(config: AppConfig) {
    val scope = rememberCoroutineScope()
    var telefono by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var searched by remember { mutableStateOf(false) }
    var reservas by remember { mutableStateOf<List<SupabaseDirect.MyReservation>>(emptyList()) }

    val blockNames = config.blocks.associate { it.id to it.name }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text("Mis reservas", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Escribe el teléfono con el que reservaste para ver el estado de tus solicitudes.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = { Text("Teléfono") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                loading = true
                searched = false
                scope.launch {
                    reservas = SupabaseDirect.myReservations(telefono)
                    loading = false
                    searched = true
                }
            },
            enabled = !loading && telefono.filter { it.isDigit() }.length >= 8,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.Black,
            ),
        ) {
            if (loading) CircularProgressIndicator(color = Color.Black)
            else Text("Buscar", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (searched && reservas.isEmpty()) {
            Text(
                "No encontramos reservas con ese teléfono.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        for (r in reservas) {
            val estadoColor = when (r.estado) {
                "confirmada" -> Color(0xFF4ADE80)
                "rechazada" -> Color(0xFFFF7B7B)
                else -> Color(0xFFFFD60A)
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
                        r.estado.replaceFirstChar { it.uppercase() },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = estadoColor,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${r.fecha} · Bloque ${blockNames[r.bloqueId] ?: r.bloqueId}",
                        fontSize = 15.sp,
                    )
                    Text(
                        "${r.personas} persona(s) · ${r.nombre}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
