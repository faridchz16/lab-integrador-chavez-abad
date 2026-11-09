package com.tecsup.productos.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.tecsup.productos.R

@Composable
fun rememberBrilloCarga(): Brush {
    val colores = listOf(
        MaterialTheme.colorScheme.surfaceContainerHigh,
        MaterialTheme.colorScheme.surfaceContainerLowest,
        MaterialTheme.colorScheme.surfaceContainerHigh
    )
    val transicion = rememberInfiniteTransition(label = "brilloCarga")
    val desplazamiento by transicion.animateFloat(
        initialValue = 0f,
        targetValue = RECORRIDO_BRILLO,
        animationSpec = infiniteRepeatable(
            animation = tween(DURACION_BRILLO_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "desplazamientoBrillo"
    )
    return Brush.linearGradient(
        colors = colores,
        start = Offset(desplazamiento - ANCHO_BRILLO, desplazamiento - ANCHO_BRILLO),
        end = Offset(desplazamiento, desplazamiento)
    )
}

@Composable
fun ListaEsqueleto(
    modifier: Modifier = Modifier,
    cantidad: Int = CANTIDAD_ESQUELETOS
) {
    val brillo = rememberBrilloCarga()
    val descripcion = stringResource(R.string.carga_productos)
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = descripcion },
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = false
    ) {
        items(cantidad) {
            TarjetaEsqueleto(brillo = brillo)
        }
    }
}

@Composable
private fun TarjetaEsqueleto(brillo: Brush) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(brillo)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                BloqueEsqueleto(brillo = brillo, fraccion = 0.8f, alto = 18)
                Spacer(modifier = Modifier.height(8.dp))
                BloqueEsqueleto(brillo = brillo, fraccion = 0.4f, alto = 12)
                Spacer(modifier = Modifier.height(10.dp))
                BloqueEsqueleto(brillo = brillo, fraccion = 0.5f, alto = 20)
                Spacer(modifier = Modifier.height(10.dp))
                BloqueEsqueleto(brillo = brillo, fraccion = 0.65f, alto = 16)
            }
        }
    }
}

@Composable
private fun BloqueEsqueleto(brillo: Brush, fraccion: Float, alto: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth(fraccion)
            .height(alto.dp)
            .clip(MaterialTheme.shapes.small)
            .background(brillo)
    )
}

private const val CANTIDAD_ESQUELETOS = 6
private const val RECORRIDO_BRILLO = 1400f
private const val ANCHO_BRILLO = 400f
private const val DURACION_BRILLO_MS = 1100
