package com.tecsup.productos.ui.categorias

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tecsup.productos.R
import com.tecsup.productos.domain.model.Categoria
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.components.LogoMarca
import com.tecsup.productos.ui.components.Cargando
import com.tecsup.productos.ui.components.ErrorView
import com.tecsup.productos.ui.components.EstadoVacio
import com.tecsup.productos.ui.components.rebote
import com.tecsup.productos.ui.theme.ProductosTheme
import com.tecsup.productos.ui.theme.coloresStock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriasScreen(
    estado: UiState<List<Categoria>>,
    onReintentar: () -> Unit,
    onCategoriaClick: (Categoria) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text(text = stringResource(R.string.categorias_titulo)) })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (estado) {
                UiState.Cargando -> Cargando()
                is UiState.Error -> ErrorView(
                    mensaje = estado.mensaje,
                    onReintentar = onReintentar
                )
                is UiState.Exito -> if (estado.datos.isEmpty()) {
                    EstadoVacio(
                        titulo = stringResource(R.string.categorias_vacio_titulo),
                        mensaje = stringResource(R.string.categorias_vacio_mensaje),
                        icono = R.drawable.ic_categoria_borde
                    )
                } else {
                    GrillaCategorias(
                        categorias = estado.datos,
                        onCategoriaClick = onCategoriaClick
                    )
                }
            }
        }
    }
}

@Composable
private fun GrillaCategorias(
    categorias: List<Categoria>,
    onCategoriaClick: (Categoria) -> Unit
) {
    val paleta = paletaCategorias()
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = ANCHO_MINIMO_TARJETA.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(items = categorias, key = { _, categoria -> categoria.id }) { indice, categoria ->
            TarjetaCategoria(
                categoria = categoria,
                colores = paleta[indice % paleta.size],
                onClick = { onCategoriaClick(categoria) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

@Composable
private fun TarjetaCategoria(
    categoria: Categoria,
    colores: Pair<Color, Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaccion = remember { MutableInteractionSource() }
    val descripcion = stringResource(R.string.categorias_ver, categoria.nombre)
    ElevatedCard(
        onClick = onClick,
        interactionSource = interaccion,
        modifier = modifier
            .fillMaxWidth()
            .rebote(interaccion)
            .semantics { contentDescription = descripcion }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            LogoMarca(
                nombre = categoria.nombre,
                colorFondo = colores.first,
                colorLetra = colores.second
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = categoria.nombre,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = pluralStringResource(
                    R.plurals.categorias_productos,
                    categoria.cantidadProductos,
                    categoria.cantidadProductos
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun paletaCategorias(): List<Pair<Color, Color>> {
    val esquema = MaterialTheme.colorScheme
    val stock = MaterialTheme.coloresStock
    return listOf(
        esquema.primaryContainer to esquema.onPrimaryContainer,
        esquema.tertiaryContainer to esquema.onTertiaryContainer,
        esquema.secondaryContainer to esquema.onSecondaryContainer,
        stock.sobreMaximo.contenedor to stock.sobreMaximo.contenido,
        stock.correcto.contenedor to stock.correcto.contenido
    )
}

private const val ANCHO_MINIMO_TARJETA = 160

@Preview(showBackground = true)
@Composable
private fun CategoriasScreenPreview() {
    ProductosTheme {
        CategoriasScreen(
            estado = UiState.Exito(
                listOf(
                    Categoria(1, "Apple", 4),
                    Categoria(2, "Samsung", 3),
                    Categoria(3, "Oppo", 3)
                )
            ),
            onReintentar = {},
            onCategoriaClick = {}
        )
    }
}
