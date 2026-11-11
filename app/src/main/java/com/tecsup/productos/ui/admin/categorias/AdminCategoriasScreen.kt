package com.tecsup.productos.ui.admin.categorias

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tecsup.productos.R
import com.tecsup.productos.domain.inventario.ErrorNombreCategoria
import com.tecsup.productos.domain.inventario.ValidadorCategoria
import com.tecsup.productos.domain.model.Categoria
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.admin.AvisoInventario
import com.tecsup.productos.ui.components.Cargando
import com.tecsup.productos.ui.components.DialogoConfirmacion
import com.tecsup.productos.ui.components.ErrorView
import com.tecsup.productos.ui.components.EstadoVacio
import com.tecsup.productos.ui.components.LogoMarca
import com.tecsup.productos.ui.theme.ProductosTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCategoriasScreen(
    estado: UiState<List<Categoria>>,
    edicion: EdicionCategoria?,
    avisos: Flow<AvisoInventario>,
    onNueva: () -> Unit,
    onEditar: (Categoria) -> Unit,
    onCambiarNombre: (String) -> Unit,
    onGuardar: () -> Unit,
    onCerrarEdicion: () -> Unit,
    onEliminar: (Categoria) -> Unit,
    onDeshacer: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val textoDeshacer = stringResource(R.string.inventario_deshacer)
    val deshacerActual by rememberUpdatedState(onDeshacer)
    var porEliminar by remember { mutableStateOf<Categoria?>(null) }
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
                title = { Text(text = stringResource(R.string.categorias_admin_titulo)) },
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
                text = { Text(text = stringResource(R.string.categorias_admin_nueva)) },
                icon = { Icon(painter = painterResource(R.drawable.ic_mas), contentDescription = null) },
                onClick = onNueva,
                expanded = botonExpandido
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (estado) {
                UiState.Cargando -> Cargando()
                is UiState.Error -> ErrorView(mensaje = estado.mensaje, onReintentar = onVolver)
                is UiState.Exito -> if (estado.datos.isEmpty()) {
                    EstadoVacio(
                        titulo = stringResource(R.string.categorias_admin_vacio_titulo),
                        mensaje = stringResource(R.string.categorias_admin_vacio_mensaje),
                        icono = R.drawable.ic_marca_borde,
                        accion = {
                            Button(onClick = onNueva) {
                                Text(text = stringResource(R.string.categorias_admin_nueva))
                            }
                        }
                    )
                } else {
                    LazyColumn(
                        state = estadoLista,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = RELLENO_INFERIOR_LISTA.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items = estado.datos, key = { it.id }) { categoria ->
                            FilaCategoriaAdmin(
                                categoria = categoria,
                                onEditar = { onEditar(categoria) },
                                onEliminar = {
                                    if (categoria.cantidadProductos > 0) {
                                        porEliminar = categoria
                                    } else {
                                        onEliminar(categoria)
                                    }
                                },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }

    porEliminar?.let { categoria ->
        DialogoConfirmacion(
            icono = R.drawable.ic_borrar,
            titulo = stringResource(R.string.categorias_admin_eliminar_pregunta, categoria.nombre),
            mensaje = pluralStringResource(
                R.plurals.categorias_admin_eliminar_mensaje,
                categoria.cantidadProductos,
                categoria.cantidadProductos
            ),
            textoConfirmar = stringResource(R.string.categorias_admin_eliminar),
            textoCancelar = stringResource(R.string.admin_cancelar),
            onConfirmar = {
                porEliminar = null
                onEliminar(categoria)
            },
            onCancelar = { porEliminar = null },
            destructivo = true
        )
    }

    if (edicion != null) {
        DialogoNombreCategoria(
            edicion = edicion,
            onCambiarNombre = onCambiarNombre,
            onGuardar = onGuardar,
            onCancelar = onCerrarEdicion
        )
    }
}

@Composable
private fun FilaCategoriaAdmin(
    categoria: Categoria,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LogoMarca(
                nombre = categoria.nombre,
                colorFondo = MaterialTheme.colorScheme.secondaryContainer,
                colorLetra = MaterialTheme.colorScheme.onSecondaryContainer,
                tamano = 44.dp
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = categoria.nombre,
                    style = MaterialTheme.typography.titleSmall,
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
            IconButton(onClick = onEditar) {
                Icon(
                    painter = painterResource(R.drawable.ic_editar),
                    contentDescription = stringResource(R.string.categorias_admin_renombrar, categoria.nombre)
                )
            }
            IconButton(onClick = onEliminar) {
                Icon(
                    painter = painterResource(R.drawable.ic_borrar),
                    contentDescription = stringResource(R.string.categorias_admin_eliminar_de, categoria.nombre),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoNombreCategoria(
    edicion: EdicionCategoria,
    onCambiarNombre: (String) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit
) {
    val visibilidad = remember { MutableTransitionState(false).apply { targetState = true } }
    val enfoque = remember { FocusRequester() }
    val error = edicion.errorVisible
    BasicAlertDialog(onDismissRequest = onCancelar) {
        AnimatedVisibility(
            visibleState = visibilidad,
            enter = scaleIn(
                initialScale = ESCALA_INICIAL,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(animationSpec = tween(DURACION_APARICION_MS))
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = stringResource(
                            if (edicion.esNueva) R.string.categorias_admin_nueva else R.string.categorias_admin_editar
                        ),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = edicion.nombre,
                        onValueChange = onCambiarNombre,
                        label = { Text(text = stringResource(R.string.categorias_admin_nombre)) },
                        isError = error != null,
                        supportingText = {
                            Text(
                                text = if (error != null) {
                                    mensajeErrorCategoria(error)
                                } else {
                                    stringResource(
                                        R.string.producto_form_contador,
                                        edicion.nombre.trim().length,
                                        ValidadorCategoria.LARGO_MAXIMO
                                    )
                                }
                            )
                        },
                        singleLine = true,
                        enabled = !edicion.guardando,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { onGuardar() }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(enfoque)
                    )
                    LaunchedEffect(enfoque) {
                        runCatching { enfoque.requestFocus() }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onCancelar, enabled = !edicion.guardando) {
                            Text(text = stringResource(R.string.admin_cancelar))
                        }
                        TextButton(onClick = onGuardar, enabled = !edicion.guardando) {
                            if (edicion.guardando) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(text = stringResource(R.string.categorias_admin_guardar))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun mensajeErrorCategoria(error: ErrorNombreCategoria): String = when (error) {
    ErrorNombreCategoria.OBLIGATORIO -> stringResource(R.string.producto_error_obligatorio)
    ErrorNombreCategoria.CORTO -> stringResource(R.string.categorias_admin_error_corto)
    ErrorNombreCategoria.LARGO -> stringResource(
        R.string.producto_error_texto_largo,
        ValidadorCategoria.LARGO_MAXIMO
    )
    ErrorNombreCategoria.DUPLICADO -> stringResource(R.string.categorias_admin_error_duplicado)
}

private const val RELLENO_INFERIOR_LISTA = 96
private const val ESCALA_INICIAL = 0.85f
private const val DURACION_APARICION_MS = 200

@Preview(showBackground = true)
@Composable
private fun AdminCategoriasScreenPreview() {
    ProductosTheme {
        AdminCategoriasScreen(
            estado = UiState.Exito(
                listOf(
                    Categoria(id = 1, nombre = "Apple", cantidadProductos = 3),
                    Categoria(id = 2, nombre = "Samsung", cantidadProductos = 4)
                )
            ),
            edicion = null,
            avisos = emptyFlow(),
            onNueva = {},
            onEditar = {},
            onCambiarNombre = {},
            onGuardar = {},
            onCerrarEdicion = {},
            onEliminar = {},
            onDeshacer = {},
            onVolver = {}
        )
    }
}
