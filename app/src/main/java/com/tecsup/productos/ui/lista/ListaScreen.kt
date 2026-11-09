package com.tecsup.productos.ui.lista

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tecsup.productos.R
import com.tecsup.productos.domain.model.Item
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.components.AvisoBanner
import com.tecsup.productos.ui.components.ErrorView
import com.tecsup.productos.ui.components.LogoStockify
import com.tecsup.productos.ui.components.EstadoVacio
import com.tecsup.productos.ui.components.ListaEsqueleto
import com.tecsup.productos.ui.components.ProductoCard
import com.tecsup.productos.ui.theme.ProductosTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaScreen(
    estado: UiState<List<Item>>,
    aviso: String?,
    onReintentar: () -> Unit,
    onProductoClick: (Item) -> Unit,
    onAlternarFavorito: (Int) -> Unit,
    modifier: Modifier = Modifier,
    acciones: @Composable RowScope.() -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LogoStockify(tamano = 36.dp, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = stringResource(R.string.lista_titulo),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = acciones
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (aviso != null) {
                AvisoBanner(mensaje = aviso)
            }
            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = estado,
                    contentKey = { it::class },
                    label = "estadoLista"
                ) { actual ->
                    when (actual) {
                        UiState.Cargando -> ListaEsqueleto()
                        is UiState.Error -> ErrorView(
                            mensaje = actual.mensaje,
                            onReintentar = onReintentar
                        )
                        is UiState.Exito -> if (actual.datos.isEmpty()) {
                            EstadoVacio()
                        } else {
                            ListaProductos(
                                productos = actual.datos,
                                onProductoClick = onProductoClick,
                                onAlternarFavorito = onAlternarFavorito
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ListaProductos(
    productos: List<Item>,
    onProductoClick: (Item) -> Unit,
    onAlternarFavorito: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items = productos, key = { it.id }) { item ->
            ProductoCard(
                item = item,
                onClick = { onProductoClick(item) },
                onAlternarFavorito = { onAlternarFavorito(item.id) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ListaScreenPreview() {
    ProductosTheme {
        ListaScreen(
            estado = UiState.Exito(ProductosDePrueba.lista),
            aviso = null,
            onReintentar = {},
            onProductoClick = {},
            onAlternarFavorito = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ListaScreenSinConexionPreview() {
    ProductosTheme {
        ListaScreen(
            estado = UiState.Exito(ProductosDePrueba.lista),
            aviso = stringResource(R.string.lista_aviso_sin_conexion),
            onReintentar = {},
            onProductoClick = {},
            onAlternarFavorito = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ListaScreenErrorPreview() {
    ProductosTheme {
        ListaScreen(
            estado = UiState.Error(stringResource(R.string.estado_error_ejemplo)),
            aviso = null,
            onReintentar = {},
            onProductoClick = {},
            onAlternarFavorito = {}
        )
    }
}
