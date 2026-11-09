package com.tecsup.productos.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tecsup.productos.R
import com.tecsup.productos.ui.theme.ProductosTheme

@Composable
fun EstadoVacio(
    modifier: Modifier = Modifier,
    titulo: String = stringResource(R.string.estado_vacio_titulo),
    mensaje: String = stringResource(R.string.estado_vacio_mensaje),
    @DrawableRes icono: Int = R.drawable.ic_inventario,
    accion: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IlustracionFlotante(icono = icono)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (accion != null) {
            Spacer(modifier = Modifier.height(20.dp))
            accion()
        }
    }
}

@Composable
private fun IlustracionFlotante(@DrawableRes icono: Int) {
    val transicion = rememberInfiniteTransition(label = "ilustracion")
    val desplazamiento by transicion.animateFloat(
        initialValue = -AMPLITUD_DP,
        targetValue = AMPLITUD_DP,
        animationSpec = infiniteRepeatable(
            animation = tween(DURACION_FLOTACION_MS, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flotacion"
    )
    val densidad = LocalDensity.current
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier
            .size(112.dp)
            .graphicsLayer { translationY = with(densidad) { desplazamiento.dp.toPx() } }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(icono),
                contentDescription = null,
                modifier = Modifier.size(56.dp)
            )
        }
    }
}

private const val AMPLITUD_DP = 6f
private const val DURACION_FLOTACION_MS = 1600

@Preview(showBackground = true)
@Composable
private fun EstadoVacioPreview() {
    ProductosTheme {
        EstadoVacio()
    }
}
