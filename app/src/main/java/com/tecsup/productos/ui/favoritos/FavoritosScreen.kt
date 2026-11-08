package com.tecsup.productos.ui.favoritos

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tecsup.productos.R
import com.tecsup.productos.domain.model.Item
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.components.Cargando
import com.tecsup.productos.ui.components.ErrorView
import com.tecsup.productos.ui.components.EstadoVacio
import com.tecsup.productos.ui.components.ProductoCard
import com.tecsup.productos.ui.lista.ProductosDePrueba
import com.tecsup.productos.ui.theme.ProductosTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritosScreen(
    estado: UiState<List<Item>>,
    onVolver: () -> Unit,
    onReintentar: () -> Unit,
    onProductoClick: (Int) -> Unit,
    onAlternarFavorito: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val alcance = rememberCoroutineScope()
    val textoQuitado = stringResource(R.string.favoritos_quitado)
    val textoDeshacer = stringResource(R.string.favoritos_deshacer)

    val quitarConDeshacer: (Int) -> Unit = { id ->
        onAlternarFavorito(id)
        alcance.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val resultado = snackbarHostState.showSnackbar(
                message = textoQuitado,
                actionLabel = textoDeshacer,
                duration = SnackbarDuration.Short
            )
            if (resultado == SnackbarResult.ActionPerformed) {
                onAlternarFavorito(id)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.favoritos_titulo)) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            painter = painterResource(R.drawable.ic_volver),
                            contentDescription = stringResource(R.string.detalle_volver)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedContent(
                targetState = estado,
                contentKey = { it::class },
                label = "estadoFavoritos"
            ) { actual ->
                when (actual) {
                    UiState.Cargando -> Cargando()
                    is UiState.Error -> ErrorView(
                        mensaje = actual.mensaje,
                        onReintentar = onReintentar
                    )
                    is UiState.Exito -> if (actual.datos.isEmpty()) {
                        EstadoVacio(
                            titulo = stringResource(R.string.favoritos_vacio_titulo),
                            mensaje = stringResource(R.string.favoritos_vacio_mensaje)
                        )
                    } else {
                        ListaFavoritos(
                            productos = actual.datos,
                            onProductoClick = onProductoClick,
                            onQuitar = quitarConDeshacer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ListaFavoritos(
    productos: List<Item>,
    onProductoClick: (Int) -> Unit,
    onQuitar: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items = productos, key = { it.id }) { item ->
            ProductoCard(
                item = item,
                onClick = { onProductoClick(item.id) },
                onAlternarFavorito = { onQuitar(item.id) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoritosScreenPreview() {
    ProductosTheme {
        FavoritosScreen(
            estado = UiState.Exito(
                ProductosDePrueba.lista.map { it.copy(esFavorito = true) }.take(2)
            ),
            onVolver = {},
            onReintentar = {},
            onProductoClick = {},
            onAlternarFavorito = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoritosScreenVacioPreview() {
    ProductosTheme {
        FavoritosScreen(
            estado = UiState.Exito(emptyList()),
            onVolver = {},
            onReintentar = {},
            onProductoClick = {},
            onAlternarFavorito = {}
        )
    }
}
