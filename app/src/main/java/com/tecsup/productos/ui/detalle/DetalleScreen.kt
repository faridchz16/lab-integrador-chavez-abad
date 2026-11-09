package com.tecsup.productos.ui.detalle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tecsup.productos.R
import com.tecsup.productos.domain.model.Item
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.components.BotonFavorito
import com.tecsup.productos.ui.components.Cargando
import com.tecsup.productos.ui.components.ErrorView
import com.tecsup.productos.ui.components.EtiquetasProducto
import com.tecsup.productos.ui.components.claveImagenProducto
import com.tecsup.productos.ui.components.elementoCompartido
import com.tecsup.productos.ui.components.ImagenProducto
import com.tecsup.productos.ui.components.PrecioProducto
import com.tecsup.productos.ui.lista.ProductosDePrueba
import com.tecsup.productos.ui.theme.ProductosTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleScreen(
    estado: UiState<Item>,
    imagenInicial: String?,
    idProducto: Int,
    onVolver: () -> Unit,
    onReintentar: () -> Unit,
    onAlternarFavorito: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.detalle_titulo)) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            painter = painterResource(R.drawable.ic_volver),
                            contentDescription = stringResource(R.string.detalle_volver)
                        )
                    }
                },
                actions = {
                    if (estado is UiState.Exito) {
                        BotonFavorito(
                            esFavorito = estado.datos.esFavorito,
                            onClick = onAlternarFavorito
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
            when (estado) {
                UiState.Cargando -> CargandoDetalle(
                    imagen = imagenInicial,
                    idProducto = idProducto
                )
                is UiState.Error -> ErrorView(
                    mensaje = estado.mensaje,
                    onReintentar = onReintentar
                )
                is UiState.Exito -> ContenidoDetalle(item = estado.datos)
            }
        }
    }
}

@Composable
private fun ContenidoDetalle(item: Item) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        GaleriaImagenes(item = item)
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = item.marca.ifBlank { stringResource(R.string.producto_sin_marca) },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.titulo,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(12.dp))
            PrecioProducto(
                item = item,
                estilo = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            EtiquetasProducto(item = item)
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.detalle_descripcion),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.descripcion?.takeIf { it.isNotBlank() }
                    ?: stringResource(R.string.detalle_sin_descripcion),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GaleriaImagenes(item: Item) {
    val imagenes = item.imagenes.ifEmpty { listOfNotNull(item.imagenUrl) }
    val modificador = Modifier
        .elementoCompartido(claveImagenProducto(item.id))
        .fillMaxWidth()
        .aspectRatio(1f)
    if (imagenes.isEmpty()) {
        ImagenProducto(
            url = null,
            contentDescription = stringResource(R.string.producto_imagen_descripcion, item.titulo),
            modifier = modificador,
            contentScale = ContentScale.Fit
        )
        return
    }
    val estadoPaginas = rememberPagerState(pageCount = { imagenes.size })
    Box(modifier = modificador) {
        HorizontalPager(
            state = estadoPaginas,
            modifier = Modifier.fillMaxSize()
        ) { pagina ->
            ImagenProducto(
                url = imagenes[pagina],
                contentDescription = stringResource(
                    R.string.detalle_imagen_descripcion,
                    pagina + 1,
                    imagenes.size,
                    item.titulo
                ),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
        if (imagenes.size > 1) {
            IndicadorPaginas(
                total = imagenes.size,
                actual = estadoPaginas.currentPage,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(12.dp)
            )
        }
    }
}

@Composable
private fun CargandoDetalle(imagen: String?, idProducto: Int) {
    Column(modifier = Modifier.fillMaxSize()) {
        ImagenProducto(
            url = imagen,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .elementoCompartido(claveImagenProducto(idProducto))
                .fillMaxWidth()
                .aspectRatio(1f)
        )
        Cargando(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun IndicadorPaginas(
    total: Int,
    actual: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(total) { indice ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (indice == actual) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        }
                    )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DetalleScreenPreview() {
    ProductosTheme {
        DetalleScreen(
            imagenInicial = null,
            idProducto = 1,
            estado = UiState.Exito(
                ProductosDePrueba.lista.first().copy(
                    descripcion = "Pantalla Super Retina XDR, chip A15 Bionic y triple cámara."
                )
            ),
            onVolver = {},
            onReintentar = {},
            onAlternarFavorito = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DetalleScreenErrorPreview() {
    ProductosTheme {
        DetalleScreen(
            imagenInicial = null,
            idProducto = 1,
            estado = UiState.Error(stringResource(R.string.estado_error_ejemplo)),
            onVolver = {},
            onReintentar = {},
            onAlternarFavorito = {}
        )
    }
}
