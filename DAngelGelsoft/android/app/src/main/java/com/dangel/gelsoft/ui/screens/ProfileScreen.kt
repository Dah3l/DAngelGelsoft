package com.dangel.gelsoft.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.dangel.gelsoft.data.ProfileStore

/**
 * Profile tab: avatar with initials, name as title, phone as subtitle,
 * "Editar perfil" opens a modal to edit the data. Below: "Mis reservas"
 * and settings (announcement notifications toggle; room for more).
 * The saved name/phone auto-fill the reservation form.
 */
@Composable
fun ProfileScreen(
    onMyReservations: () -> Unit,
    onRequestNotifPermission: () -> Unit,
) {
    val context = LocalContext.current

    var nombre by remember { mutableStateOf(ProfileStore.getNombre(context)) }
    var telefono by remember { mutableStateOf(ProfileStore.getTelefono(context)) }
    var notifOn by remember { mutableStateOf(ProfileStore.notifEnabled(context)) }
    var showEdit by remember { mutableStateOf(false) }

    fun hasSystemPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // ---------- Avatar ----------
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                ProfileStore.initials(nombre),
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        // ---------- Name as title, phone as subtitle ----------
        Text(
            text = nombre.ifBlank { "Tu perfil" },
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = telefono.ifBlank { "Agrega tus datos para reservar más rápido" },
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(onClick = { showEdit = true }) {
            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.size(6.dp))
            Text("Editar perfil", color = MaterialTheme.colorScheme.primary)
        }

        // ---------- Mis reservas ----------
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            shape = RoundedCornerShape(16.dp),
            onClick = onMyReservations,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text("Mis reservas", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Mira el estado de tus solicitudes",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text("›", fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // ---------- Ajustes ----------
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Ajustes",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            shape = RoundedCornerShape(16.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    Icons.Filled.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text("Notificaciones", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(
                        if (notifOn && hasSystemPermission()) "Activadas"
                        else if (notifOn) "Actívalas en el sistema"
                        else "Desactivadas",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = notifOn,
                    onCheckedChange = { enabled ->
                        notifOn = enabled
                        ProfileStore.setNotifEnabled(context, enabled)
                        if (enabled && !hasSystemPermission()) {
                            onRequestNotifPermission()
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // ---------- Edit modal ----------
    if (showEdit) {
        var editNombre by remember { mutableStateOf(nombre) }
        var editTelefono by remember { mutableStateOf(telefono) }
        var editError by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showEdit = false },
            title = { Text("Editar perfil") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editNombre,
                        onValueChange = { editNombre = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editTelefono,
                        onValueChange = { editTelefono = it },
                        label = { Text("Teléfono / WhatsApp") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    )
                    editError?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editNombre.isBlank()) {
                            editError = "Escribe tu nombre."
                            return@TextButton
                        }
                        if (editTelefono.filter { it.isDigit() }.length < 8) {
                            editError = "Escribe un teléfono válido."
                            return@TextButton
                        }
                        ProfileStore.save(context, editNombre, editTelefono)
                        nombre = editNombre.trim()
                        telefono = editTelefono.trim()
                        showEdit = false
                    },
                ) { Text("Guardar", color = MaterialTheme.colorScheme.primary) }
            },
            dismissButton = {
                TextButton(onClick = { showEdit = false }) { Text("Cancelar") }
            },
        )
    }
}
