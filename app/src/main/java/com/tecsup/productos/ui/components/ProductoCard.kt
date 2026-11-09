package com.tecsup.productos.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tecsup.productos.R
import com.tecsup.productos.domain.model.EstadoStock
import com.tecsup.productos.domain.model.Item
import com.tecsup.productos.domain.model.estadoStock
import com.tecsup.productos.ui.theme.ProductosTheme

@Composable
fun ProductoCard(
    item: Item,
    onClick: () -> Unit,
    onAlternarFavorito: () -> Unit,
    modifier: Modifier = Modifier,
    onAgregarAlCarrito: (() -> Unit)? = null
) {
    val interaccion = remember { MutableInteractionSource() }
    ElevatedCard(
        onClick = onClick,
        interactionSource = interaccion,
        modifier = modifier
            .fillMaxWidth()
            .rebote(interaccion)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ImagenProducto(
                url = item.imagenUrl,
                contentDescription = stringResource(R.string.producto_imagen_descripcion, item.titulo),
                modifier = Modifier
                    .elementoCompartido(claveImagenProducto(item.id))
                    .size(88.dp)
                    .clip(MaterialTheme.shapes.medium)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                item.categoria?.let { categoria ->
                    PildoraCategoria(texto = categoria)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = item.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.marca.isNotBlank() && !item.marca.equals(item.categoria, ignoreCase = true)) {
                    Text(
                        text = item.marca,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                item.descripcion?.takeIf { it.isNotBlank() }?.let { descripcion ->
                    Text(
                        text = descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                PrecioProducto(item = item)
                Spacer(modifier = Modifier.height(6.dp))
                EtiquetasProducto(item = item)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BotonFavorito(esFavorito = item.esFavorito, onClick = onAlternarFavorito)
                if (onAgregarAlCarrito != null) {
                    BotonAgregarCarrito(
                        onClick = onAgregarAlCarrito,
                        habilitado = item.estadoStock() != EstadoStock.AGOTADO
                    )
                }
            }
        }
    }
}

@Composable
fun PildoraCategoria(texto: String, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
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
                categoria = "Smartphones",
                descripcion = "Pantalla Super Retina XDR y chip A15 Bionic.",
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
