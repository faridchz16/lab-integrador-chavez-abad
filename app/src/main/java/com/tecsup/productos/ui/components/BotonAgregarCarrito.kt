package com.tecsup.productos.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.tecsup.productos.R

@Composable
fun BotonAgregarCarrito(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true
) {
    val interaccion = remember { MutableInteractionSource() }
    val haptica = LocalHapticFeedback.current
    FilledTonalIconButton(
        onClick = {
            haptica.performHapticFeedback(HapticFeedbackType.Confirm)
            onClick()
        },
        enabled = habilitado,
        interactionSource = interaccion,
        modifier = modifier.rebote(interaccion)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_carrito_agregar),
            contentDescription = stringResource(R.string.carrito_agregar)
        )
    }
}
