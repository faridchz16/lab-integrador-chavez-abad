package com.tecsup.productos.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tecsup.productos.R
import com.tecsup.productos.domain.model.Item
import com.tecsup.productos.domain.model.estadoStock
import com.tecsup.productos.domain.model.tieneDescuento
import com.tecsup.productos.ui.theme.ProductosTheme

@Composable
fun ProductoCard(
    item: Item,
    onClick: () -> Unit,
    onAlternarFavorito: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ImagenProducto(
                url = item.imagenUrl,
                contentDescription = stringResource(R.string.producto_imagen_descripcion, item.titulo),
                modifier = Modifier
                    .size(88.dp)
                    .clip(MaterialTheme.shapes.medium)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.marca,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                PrecioProducto(item = item)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
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
            BotonFavorito(esFavorito = item.esFavorito, onClick = onAlternarFavorito)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductoCardPreview() {
    ProductosTheme {
        ProductoCard(
            item = Item(
                id = 1,
                titulo = "iPhone 13 Pro",
                marca = "Apple",
                precio = 1099.99,
                imagenUrl = null,
                rating = 4.6,
                stock = 6,
                descuento = 9.5
            ),
            onClick = {},
            onAlternarFavorito = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
