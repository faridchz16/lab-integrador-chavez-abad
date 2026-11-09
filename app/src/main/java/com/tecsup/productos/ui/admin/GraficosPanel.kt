package com.tecsup.productos.ui.admin

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class SegmentoGrafico(
    val valor: Int,
    val color: Color,
    val etiqueta: String
)

@Composable
fun GraficoDona(
    segmentos: List<SegmentoGrafico>,
    textoCentro: String,
    subtextoCentro: String,
    descripcion: String,
    modifier: Modifier = Modifier
) {
    val progreso = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progreso.animateTo(1f, tween(DURACION_ANIMACION_MS, easing = FastOutSlowInEasing))
    }
    val colorPista = MaterialTheme.colorScheme.surfaceVariant
    val total = segmentos.sumOf { it.valor }
    Box(
        modifier = modifier
            .size(TAMANO_DONA.dp)
            .semantics { contentDescription = descripcion },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val grosor = GROSOR_DONA.dp.toPx()
            val lado = size.minDimension - grosor
            val tamanoArco = Size(lado, lado)
            val origen = Offset((size.width - lado) / 2f, (size.height - lado) / 2f)
            drawArc(
                color = colorPista,
                startAngle = 0f,
                sweepAngle = GRADOS_CIRCULO,
                useCenter = false,
                topLeft = origen,
                size = tamanoArco,
                style = Stroke(width = grosor)
            )
            if (total > 0) {
                val visibles = segmentos.filter { it.valor > 0 }
                val separacion = if (visibles.size > 1) SEPARACION_GRADOS else 0f
                var inicio = ANGULO_INICIAL
                visibles.forEach { segmento ->
                    val barrido = GRADOS_CIRCULO * segmento.valor / total * progreso.value
                    drawArc(
                        color = segmento.color,
                        startAngle = inicio + separacion / 2f,
                        sweepAngle = (barrido - separacion).coerceAtLeast(0f),
                        useCenter = false,
                        topLeft = origen,
                        size = tamanoArco,
                        style = Stroke(width = grosor)
                    )
                    inicio += barrido
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = textoCentro,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtextoCentro,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LeyendaGrafico(
    segmentos: List<SegmentoGrafico>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        segmentos.forEach { segmento ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(segmento.color, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = segmento.etiqueta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = segmento.valor.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun GraficoBarras(
    valores: List<Double>,
    etiquetas: List<String>,
    indiceResaltado: Int,
    descripcion: String,
    modifier: Modifier = Modifier
) {
    val progreso = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progreso.animateTo(1f, tween(DURACION_ANIMACION_MS, easing = FastOutSlowInEasing))
    }
    val colorResaltado = MaterialTheme.colorScheme.primary
    val colorBarra = MaterialTheme.colorScheme.primary.copy(alpha = ALFA_BARRA_SECUNDARIA)
    val colorGuia = MaterialTheme.colorScheme.outlineVariant
    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(ALTO_BARRAS.dp)
                .semantics { contentDescription = descripcion }
        ) {
            val maximo = valores.maxOrNull()?.takeIf { it > 0.0 } ?: 1.0
            val anchoColumna = size.width / valores.size.coerceAtLeast(1)
            val anchoBarra = anchoColumna * PROPORCION_BARRA
            val grosorGuia = 1.dp.toPx()
            listOf(0f, 0.5f, 1f).forEach { fraccion ->
                val y = size.height * fraccion
                drawLine(
                    color = colorGuia,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = grosorGuia
                )
            }
            val radio = CornerRadius(RADIO_BARRA.dp.toPx())
            valores.forEachIndexed { indice, valor ->
                val alto = (size.height * (valor / maximo).toFloat() * progreso.value)
                if (alto > 0f) {
                    val x = anchoColumna * indice + (anchoColumna - anchoBarra) / 2f
                    drawRoundRect(
                        color = if (indice == indiceResaltado) colorResaltado else colorBarra,
                        topLeft = Offset(x, size.height - alto),
                        size = Size(anchoBarra, alto),
                        cornerRadius = radio
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            etiquetas.forEachIndexed { indice, etiqueta ->
                val resaltado = indice == indiceResaltado
                Text(
                    text = etiqueta,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (resaltado) FontWeight.Bold else FontWeight.Normal,
                    color = if (resaltado) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private const val DURACION_ANIMACION_MS = 900
private const val TAMANO_DONA = 132
private const val GROSOR_DONA = 18
private const val GRADOS_CIRCULO = 360f
private const val ANGULO_INICIAL = -90f
private const val SEPARACION_GRADOS = 3f
private const val ALTO_BARRAS = 132
private const val PROPORCION_BARRA = 0.56f
private const val RADIO_BARRA = 6
private const val ALFA_BARRA_SECUNDARIA = 0.35f
