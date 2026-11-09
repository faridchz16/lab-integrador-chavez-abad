package com.tecsup.productos.ui.lista

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tecsup.productos.R
import com.tecsup.productos.domain.catalogo.FiltroCatalogo
import com.tecsup.productos.domain.model.Categoria
import com.tecsup.productos.domain.model.Item
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.components.AvisoBanner
import com.tecsup.productos.ui.components.ErrorView
import com.tecsup.productos.ui.components.EstadoVacio
import com.tecsup.productos.ui.components.ListaEsqueleto
import com.tecsup.productos.ui.components.LogoStockify
import com.tecsup.productos.ui.components.ProductoCard
import com.tecsup.productos.ui.theme.ProductosTheme

data class AccionesCatalogo(
    val onReintentar: () -> Unit,
    val onProductoClick: (Item) -> Unit,
    val onAlternarFavorito: (Int) -> Unit,
    val onAgregarAlCarrito: (Item) -> Unit,
    val onConsultaChange: (String) -> Unit,
    val onBuscar: (String) -> Unit,
    val onEliminarHistorial: (String) -> Unit,
    val onLimpiarHistorial: () -> Unit,
    val onSeleccionarCategoria: (Long?) -> Unit,
    val onAplicarFiltros: (FiltroCatalogo) -> Unit,
    val onLimpiarFiltros: () -> Unit,
    val onLimpiarConsulta: () -> Unit,
    val onLimpiarTodo: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaScreen(
    estado: UiState<List<Item>>,
    aviso: String?,
    filtro: FiltroCatalogo,
    categorias: List<Categoria>,
    historial: List<String>,
    rangoPrecios: ClosedFloatingPointRange<Double>?,
    acciones: AccionesCatalogo,
    modifier: Modifier = Modifier
) {
    var busquedaAbierta by rememberSaveable { mutableStateOf(false) }
    var filtrosAbiertos by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = busquedaAbierta) { busquedaAbierta = false }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { TituloCatalogo() },
                    actions = {
                        IconButton(onClick = { busquedaAbierta = true }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_buscar),
                                contentDescription = stringResource(R.string.catalogo_buscar)
                            )
                        }
                        IconButton(onClick = { filtrosAbiertos = true }) {
                            BotonFiltros(cantidadActivos = filtro.cantidadActivos)
                        }
                    }
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
                if (categorias.isNotEmpty()) {
                    FilaCategorias(
                        categorias = categorias,
                        seleccionadas = filtro.categorias,
                        onSeleccionar = acciones.onSeleccionarCategoria,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                AnimatedVisibility(
                    visible = filtro.consulta.isNotBlank() && !busquedaAbierta,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    InputChip(
                        selected = true,
                        onClick = acciones.onLimpiarConsulta,
                        label = { Text(text = stringResource(R.string.busqueda_activa, filtro.consulta)) },
                        trailingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_cerrar),
                                contentDescription = stringResource(R.string.busqueda_quitar),
                                modifier = Modifier.size(InputChipDefaults.IconSize)
                            )
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
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
                                onReintentar = acciones.onReintentar
                            )
                            is UiState.Exito -> when {
                                actual.datos.isNotEmpty() -> ListaProductos(
                                    productos = actual.datos,
                                    onProductoClick = acciones.onProductoClick,
                                    onAlternarFavorito = acciones.onAlternarFavorito,
                                    onAgregarAlCarrito = acciones.onAgregarAlCarrito
                                )
                                filtro.tieneCriterios -> EstadoVacio(
                                    titulo = stringResource(R.string.sin_resultados_titulo),
                                    mensaje = stringResource(R.string.sin_resultados_mensaje),
                                    icono = R.drawable.ic_buscar,
                                    accion = {
                                        OutlinedButton(onClick = acciones.onLimpiarTodo) {
                                            Text(text = stringResource(R.string.sin_resultados_accion))
                                        }
                                    }
                                )
                                else -> EstadoVacio()
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = busquedaAbierta,
            enter = fadeIn() + expandIn(expandFrom = Alignment.TopEnd),
            exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.TopEnd)
        ) {
            BusquedaCatalogo(
                consulta = filtro.consulta,
                resultados = (estado as? UiState.Exito)?.datos.orEmpty(),
                historial = historial,
                onConsultaChange = acciones.onConsultaChange,
                onBuscar = { texto ->
                    acciones.onBuscar(texto)
                    busquedaAbierta = false
                },
                onEliminarHistorial = acciones.onEliminarHistorial,
                onLimpiarHistorial = acciones.onLimpiarHistorial,
                onProductoClick = { item ->
                    acciones.onBuscar(filtro.consulta)
                    busquedaAbierta = false
                    acciones.onProductoClick(item)
                },
                onCerrar = { busquedaAbierta = false }
            )
        }

        if (filtrosAbiertos) {
            HojaFiltros(
                filtroActual = filtro,
                categorias = categorias,
                rangoPrecios = rangoPrecios,
                onAplicar = acciones.onAplicarFiltros,
                onLimpiar = acciones.onLimpiarFiltros,
                onCerrar = { filtrosAbiertos = false }
            )
        }
    }
}

@Composable
private fun TituloCatalogo() {
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
}

@Composable
private fun BotonFiltros(cantidadActivos: Int) {
    val descripcion = if (cantidadActivos > 0) {
        pluralStringResource(R.plurals.catalogo_filtros_activos, cantidadActivos, cantidadActivos)
    } else {
        stringResource(R.string.catalogo_filtrar)
    }
    BadgedBox(
        badge = {
            if (cantidadActivos > 0) {
                Badge { Text(text = cantidadActivos.toString()) }
            }
        }
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_filtro),
            contentDescription = descripcion
        )
    }
}

@Composable
private fun ListaProductos(
    productos: List<Item>,
    onProductoClick: (Item) -> Unit,
    onAlternarFavorito: (Int) -> Unit,
    onAgregarAlCarrito: (Item) -> Unit
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
                onAgregarAlCarrito = { onAgregarAlCarrito(item) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

private val accionesDePrueba = AccionesCatalogo(
    onReintentar = {},
    onProductoClick = {},
    onAlternarFavorito = {},
    onAgregarAlCarrito = {},
    onConsultaChange = {},
    onBuscar = {},
    onEliminarHistorial = {},
    onLimpiarHistorial = {},
    onSeleccionarCategoria = {},
    onAplicarFiltros = {},
    onLimpiarFiltros = {},
    onLimpiarConsulta = {},
    onLimpiarTodo = {}
)

@Preview(showBackground = true)
@Composable
private fun ListaScreenPreview() {
    ProductosTheme {
        ListaScreen(
            estado = UiState.Exito(ProductosDePrueba.lista),
            aviso = null,
            filtro = FiltroCatalogo(),
            categorias = listOf(Categoria(1, "Apple", 2), Categoria(2, "Samsung", 1)),
            historial = emptyList(),
            rangoPrecios = 300.0..1100.0,
            acciones = accionesDePrueba
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ListaScreenSinResultadosPreview() {
    ProductosTheme {
        ListaScreen(
            estado = UiState.Exito(emptyList()),
            aviso = stringResource(R.string.lista_aviso_sin_conexion),
            filtro = FiltroCatalogo(consulta = "xyz"),
            categorias = emptyList(),
            historial = emptyList(),
            rangoPrecios = null,
            acciones = accionesDePrueba
        )
    }
}
