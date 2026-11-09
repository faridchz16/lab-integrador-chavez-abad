package com.tecsup.productos.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.tecsup.productos.R

@Composable
fun BotonFavorito(
    esFavorito: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaccion = remember { MutableInteractionSource() }
    val haptica = LocalHapticFeedback.current
    val escala = remember { Animatable(1f) }
    var inicializado by remember { mutableStateOf(false) }

    LaunchedEffect(esFavorito) {
        if (inicializado && esFavorito) {
            escala.snapTo(ESCALA_INICIO_POP)
            escala.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioHighBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
        inicializado = true
    }

    IconButton(
        onClick = {
            haptica.performHapticFeedback(
                if (esFavorito) HapticFeedbackType.ToggleOff else HapticFeedbackType.ToggleOn
            )
            onClick()
        },
        interactionSource = interaccion,
        modifier = modifier.rebote(interaccion)
    ) {
        Icon(
            painter = painterResource(
                if (esFavorito) R.drawable.ic_favorito else R.drawable.ic_favorito_borde
            ),
            contentDescription = stringResource(
                if (esFavorito) R.string.favorito_quitar else R.string.favorito_agregar
            ),
            tint = if (esFavorito) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.graphicsLayer {
                scaleX = escala.value
                scaleY = escala.value
            }
        )
    }
}

private const val ESCALA_INICIO_POP = 0.5f
