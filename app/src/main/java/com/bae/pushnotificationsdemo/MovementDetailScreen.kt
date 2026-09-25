package com.bae.pushnotificationsdemo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MovementDetailScreen(
    movementId: String,
    onBack: () -> Unit
) {
    val movement = FakeMovementRepository.getMovement(movementId)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(12.dp))

        Text(
            text = "Detalle del movimiento",
            style = MaterialTheme.typography.headlineMedium
        )

        if (movement != null) {
            Text(
                text = "Movimiento",
                style = MaterialTheme.typography.labelLarge
            )

            Text(
                text = "#${movement.id}",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Tipo",
                style = MaterialTheme.typography.labelLarge
            )

            Text(
                text = movement.type
            )

            Text(
                text = "Monto",
                style = MaterialTheme.typography.labelLarge
            )

            Text(
                text = movement.amount
            )

            Text(
                text = "Estado",
                style = MaterialTheme.typography.labelLarge
            )

            Text(
                text = movement.status
            )
        } else {
            Text(
                text = "Movimiento",
                style = MaterialTheme.typography.labelLarge
            )

            Text(
                text = "#$movementId",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Movimiento no encontrado",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = onBack
        ) {
            Text(
                text = "Regresar"
            )
        }
    }
}