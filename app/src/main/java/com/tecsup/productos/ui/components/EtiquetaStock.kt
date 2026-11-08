package com.tecsup.productos.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tecsup.productos.R
import com.tecsup.productos.domain.model.EstadoStock

@Composable
fun EtiquetaStock(
    estado: EstadoStock,
    stock: Int?,
    modifier: Modifier = Modifier
) {
    val colores = MaterialTheme.colorScheme
    val estilo: Triple<String, Color, Color> = when (estado) {
        EstadoStock.DISPONIBLE -> Triple(
            stringResource(R.string.stock_disponible, stock ?: 0),
            colores.secondaryContainer,
            colores.onSecondaryContainer
        )
        EstadoStock.STOCK_BAJO -> Triple(
            stringResource(R.string.stock_bajo, stock ?: 0),
            colores.tertiaryContainer,
            colores.onTertiaryContainer
        )
        EstadoStock.AGOTADO -> Triple(
            stringResource(R.string.stock_agotado),
            colores.errorContainer,
            colores.onErrorContainer
        )
        EstadoStock.SIN_DATO -> return
    }
    Surface(
        color = estilo.second,
        contentColor = estilo.third,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        Text(
            text = estilo.first,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
