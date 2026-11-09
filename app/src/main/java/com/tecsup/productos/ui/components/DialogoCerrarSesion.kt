package com.tecsup.productos.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tecsup.productos.R

@Composable
fun DialogoCerrarSesion(
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    DialogoConfirmacion(
        icono = R.drawable.ic_cerrar_sesion,
        titulo = stringResource(R.string.sesion_cerrar_pregunta),
        mensaje = stringResource(R.string.sesion_cerrar_mensaje),
        textoConfirmar = stringResource(R.string.sesion_cerrar),
        textoCancelar = stringResource(R.string.sesion_cancelar),
        onConfirmar = onConfirmar,
        onCancelar = onCancelar
    )
}
