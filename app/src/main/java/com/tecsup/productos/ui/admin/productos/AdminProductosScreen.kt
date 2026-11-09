package com.tecsup.productos.ui.admin.productos

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tecsup.productos.R
import com.tecsup.productos.domain.model.Item
import com.tecsup.productos.domain.model.estadoStock
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.admin.AvisoInventario
import com.tecsup.productos.ui.components.Cargando
import com.tecsup.productos.ui.components.ErrorView
import com.tecsup.productos.ui.components.EstadoVacio
import com.tecsup.productos.ui.components.EtiquetaStock
import com.tecsup.productos.ui.components.ImagenProducto
import com.tecsup.productos.ui.components.PrecioProducto
import com.tecsup.productos.ui.components.rebote
import com.tecsup.productos.ui.theme.ProductosTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductosScreen(
    estado: UiState<List<Item>>,
    consulta: String,
    avisos: Flow<AvisoInventario>,
    onConsultaChange: (String) -> Unit,
    onProductoClick: (Item) -> Unit,
    onNuevoProducto: () -> Unit,
    onEliminar: (Item) -> Unit,
    onDeshacer: () -> Unit,
    onReintentar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val textoDeshacer = stringResource(R.string.inventario_deshacer)
    val deshacerActual by rememberUpdatedState(onDeshacer)
    val estadoLista = rememberLazyListState()
    val botonExpandido by remember {
        derivedStateOf { estadoLista.firstVisibleItemIndex == 0 }
    }

    LaunchedEffect(avisos) {
        avisos.collect { aviso ->
            launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                val resultado = snackbarHostState.showSnackbar(
                    message = aviso.texto,
                    actionLabel = if (aviso.conDeshacer) textoDeshacer else null,
                    duration = if (aviso.conDeshacer) SnackbarDuration.Long else SnackbarDuration.Short
                )
                if (resultado == SnackbarResult.ActionPerformed) deshacerActual()
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.inventario_productos_titulo)) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            painter = painterResource(R.drawable.ic_volver),
                            contentDescription = stringResource(R.string.detalle_volver)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(text = stringResource(R.string.inventario_nuevo_producto)) },
                icon = { Icon(painter = painterResource(R.drawable.ic_mas), contentDescription = null) },
                onClick = onNuevoProducto,
                expanded = botonExpandido
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            CampoBusqueda(consulta = consulta, onConsultaChange = onConsultaChange)
            Box(modifier = Modifier.weight(1f)) {
                when (estado) {
                    UiState.Cargando -> Cargando()
                    is UiState.Error -> ErrorView(mensaje = estado.mensaje, onReintentar = onReintentar)
                    is UiState.Exito -> when {
                        estado.datos.isEmpty() && consulta.isNotBlank() -> EstadoVacio(
                            titulo = stringResource(R.string.inventario_sin_resultados_titulo),
                            mensaje = stringResource(R.string.inventario_sin_resultados_mensaje, consulta),
                            icono = R.drawable.ic_buscar
                        )
                        estado.datos.isEmpty() -> EstadoVacio(
                            titulo = stringResource(R.string.inventario_vacio_titulo),
                            mensaje = stringResource(R.string.inventario_vacio_mensaje),
                            accion = {
                                Button(onClick = onNuevoProducto) {
                                    Text(text = stringResource(R.string.inventario_nuevo_producto))
                                }
                            }
                        )
                        else -> LazyColumn(
                            state = estadoLista,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 4.dp,
                                bottom = RELLENO_INFERIOR_LISTA.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item(key = CLAVE_CONTEO) {
                                Text(
                                    text = pluralStringResource(
                                        R.plurals.inventario_conteo,
                                        estado.datos.size,
                                        estado.datos.size
                                    ),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            items(items = estado.datos, key = { it.id }) { item ->
                                FilaProductoAdmin(
                                    item = item,
                                    onClick = { onProductoClick(item) },
                                    onEliminar = { onEliminar(item) },
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CampoBusqueda(
    consulta: String,
    onConsultaChange: (String) -> Unit
) {
    val foco = LocalFocusManager.current
    OutlinedTextField(
        value = consulta,
        onValueChange = onConsultaChange,
        placeholder = { Text(text = stringResource(R.string.inventario_buscar)) },
        leadingIcon = { Icon(painter = painterResource(R.drawable.ic_buscar), contentDescription = null) },
        trailingIcon = {
            if (consulta.isNotEmpty()) {
                IconButton(onClick = { onConsultaChange("") }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_cerrar),
                        contentDescription = stringResource(R.string.inventario_limpiar_busqueda)
                    )
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.extraLarge,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { foco.clearFocus() }),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun FilaProductoAdmin(
    item: Item,
    onClick: () -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
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
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(MaterialTheme.shapes.medium)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.titulo,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.categoria ?: stringResource(R.string.categoria_sin_nombre),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.editadoLocalmente) {
                    Text(
                        text = stringResource(
                            if (item.id < 0) R.string.inventario_creado_local else R.string.inventario_editado_local
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PrecioProducto(
                        item = item,
                        estilo = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    EtiquetaStock(estado = item.estadoStock(), stock = item.stock)
                }
            }
            IconButton(onClick = onEliminar) {
                Icon(
                    painter = painterResource(R.drawable.ic_borrar),
                    contentDescription = stringResource(R.string.inventario_eliminar_producto, item.titulo),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private const val CLAVE_CONTEO = "conteo"
private const val RELLENO_INFERIOR_LISTA = 96

@Preview(showBackground = true)
@Composable
private fun AdminProductosScreenPreview() {
    ProductosTheme {
        AdminProductosScreen(
            estado = UiState.Exito(
                listOf(
                    Item(id = 1, titulo = "iPhone 9", marca = "Apple", precio = 549.0, imagenUrl = null, stock = 4, categoria = "Apple"),
                    Item(id = -1, titulo = "Galaxy A55", marca = "Samsung", precio = 399.0, imagenUrl = null, stock = 30, editadoLocalmente = true)
                )
            ),
            consulta = "",
            avisos = emptyFlow(),
            onConsultaChange = {},
            onProductoClick = {},
            onNuevoProducto = {},
            onEliminar = {},
            onDeshacer = {},
            onReintentar = {},
            onVolver = {}
        )
    }
}
