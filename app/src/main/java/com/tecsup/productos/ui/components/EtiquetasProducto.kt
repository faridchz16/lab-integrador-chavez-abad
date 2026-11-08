package com.tecsup.productos.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tecsup.productos.R
import com.tecsup.productos.domain.model.Item
import com.tecsup.productos.domain.model.estadoStock
import com.tecsup.productos.domain.model.tieneDescuento

@Composable
fun EtiquetasProducto(
    item: Item,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        item.rating?.let { rating ->
            Text(
                text = stringResource(R.string.producto_rating, rating),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        EtiquetaDescuento(item = item)
        if (item.tieneDescuento()) {
            Spacer(modifier = Modifier.width(6.dp))
        }
        EtiquetaStock(estado = item.estadoStock(), stock = item.stock)
    }
}
