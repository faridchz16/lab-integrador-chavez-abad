package com.tecsup.productos.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun Modifier.rebote(fuenteInteraccion: InteractionSource): Modifier {
    val presionado by fuenteInteraccion.collectIsPressedAsState()
    val escala by animateFloatAsState(
        targetValue = if (presionado) ESCALA_PRESIONADO else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "rebote"
    )
    return graphicsLayer {
        scaleX = escala
        scaleY = escala
    }
}

private const val ESCALA_PRESIONADO = 0.94f
