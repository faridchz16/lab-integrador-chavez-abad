package com.tecsup.productos.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.tecsup.productos.R

@Composable
fun DialogoCerrarSesion(
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        icon = {
            Icon(
                painter = painterResource(R.drawable.ic_cerrar_sesion),
                contentDescription = null
            )
        },
        title = { Text(text = stringResource(R.string.sesion_cerrar_pregunta)) },
        text = { Text(text = stringResource(R.string.sesion_cerrar_mensaje)) },
        confirmButton = {
            TextButton(onClick = onConfirmar) {
                Text(text = stringResource(R.string.sesion_cerrar))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text(text = stringResource(R.string.sesion_cancelar))
            }
        }
    )
}
