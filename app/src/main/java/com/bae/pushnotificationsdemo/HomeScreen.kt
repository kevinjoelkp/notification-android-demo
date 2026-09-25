package com.bae.pushnotificationsdemo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    permissionGranted: Boolean,
    notificationsEnabled: Boolean,
    feedback: String?,
    firebaseStatus: String,
    registrationToken: String?,
    onRequestPermission: () -> Unit,
    onSimulateTransfer: () -> Unit,
    onCopyToken: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Notificaciones en Android",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Demo técnica de notificaciones locales y Firebase Cloud Messaging."
        )
        Text(
            text = "Permiso de notificaciones: ${if (permissionGranted) "Concedido" else "No concedido"}",
            style = MaterialTheme.typography.titleMedium
        )
        if (!notificationsEnabled) Text("Las notificaciones de la app están desactivadas en Ajustes.")
        OutlinedButton(onClick = onRequestPermission) {
            Text(
                text = "Solicitar permiso"
            )
        }
        Button(
            onClick = onSimulateTransfer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Simular transferencia"
            )
        }
        feedback?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.primary
            )
        }
        HorizontalDivider()
        Text(
            text = "Firebase Cloud Messaging",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "Estado: $firebaseStatus"
        )
        Text(
            text = "Registration token:",
            style = MaterialTheme.typography.titleSmall
        )
        SelectionContainer {
            Text(
                text = registrationToken ?: "Aún no disponible"
            )
        }
        OutlinedButton(
            onClick = onCopyToken,
            enabled = registrationToken != null
        ) {
            Text(
                text = "Copiar token"
            )
        }
    }
}
