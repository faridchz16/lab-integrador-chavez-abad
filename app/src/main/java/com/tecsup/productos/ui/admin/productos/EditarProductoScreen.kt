package com.tecsup.productos.ui.admin.productos

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tecsup.productos.R
import com.tecsup.productos.domain.inventario.CampoProducto
import com.tecsup.productos.domain.inventario.ErrorCampo
import com.tecsup.productos.domain.inventario.ValidadorProducto
import com.tecsup.productos.domain.model.Categoria
import com.tecsup.productos.domain.model.estadoStock
import com.tecsup.productos.ui.components.Cargando
import com.tecsup.productos.ui.components.DialogoConfirmacion
import com.tecsup.productos.ui.components.EstadoVacio
import com.tecsup.productos.ui.components.EtiquetaStock
import com.tecsup.productos.ui.components.ImagenProducto
import com.tecsup.productos.ui.components.PrecioProducto
import com.tecsup.productos.ui.components.rebote
import com.tecsup.productos.ui.theme.ProductosTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarProductoScreen(
    estado: EstadoEdicionProducto,
    categorias: List<Categoria>,
    avisos: Flow<String>,
    onCambiarCampo: (CampoProducto, String) -> Unit,
    onSeleccionarCategoria: (Long?) -> Unit,
    onGuardar: () -> Unit,
    onEliminar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var confirmarDescarte by rememberSaveable { mutableStateOf(false) }
    var confirmarEliminacion by rememberSaveable { mutableStateOf(false) }
    val salir: () -> Unit = {
        if (estado.hayCambios && !estado.guardando) {
            confirmarDescarte = true
        } else {
            onVolver()
        }
    }

    LaunchedEffect(avisos) {
        avisos.collect { texto ->
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(texto)
        }
    }

    BackHandler(enabled = estado.hayCambios && !estado.guardando) { confirmarDescarte = true }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            if (estado.esNuevo) R.string.producto_form_titulo_nuevo else R.string.producto_form_titulo_editar
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = salir) {
                        Icon(
                            painter = painterResource(R.drawable.ic_volver),
                            contentDescription = stringResource(R.string.detalle_volver)
                        )
                    }
                },
                actions = {
                    if (!estado.esNuevo && !estado.cargando && !estado.noEncontrado) {
                        IconButton(
                            onClick = { confirmarEliminacion = true },
                            enabled = !estado.guardando
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_borrar),
                                contentDescription = stringResource(R.string.producto_form_eliminar),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (!estado.cargando && !estado.noEncontrado) {
                BarraGuardar(estado = estado, onGuardar = onGuardar)
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                estado.cargando -> Cargando()
                estado.noEncontrado -> EstadoVacio(
                    titulo = stringResource(R.string.producto_form_no_encontrado_titulo),
                    mensaje = stringResource(R.string.producto_form_no_encontrado_mensaje),
                    icono = R.drawable.ic_inventario,
                    accion = {
                        Button(onClick = onVolver) {
                            Text(text = stringResource(R.string.detalle_volver))
                        }
                    }
                )
                else -> FormularioProductoContenido(
                    estado = estado,
                    categorias = categorias,
                    onCambiarCampo = onCambiarCampo,
                    onSeleccionarCategoria = onSeleccionarCategoria
                )
            }
        }
    }

    if (confirmarDescarte) {
        DialogoConfirmacion(
            icono = R.drawable.ic_alerta,
            titulo = stringResource(R.string.producto_form_descartar_pregunta),
            mensaje = stringResource(R.string.producto_form_descartar_mensaje),
            textoConfirmar = stringResource(R.string.producto_form_descartar),
            textoCancelar = stringResource(R.string.producto_form_seguir_editando),
            onConfirmar = {
                confirmarDescarte = false
                onVolver()
            },
            onCancelar = { confirmarDescarte = false },
            destructivo = true
        )
    }

    if (confirmarEliminacion) {
        DialogoConfirmacion(
            icono = R.drawable.ic_borrar,
            titulo = stringResource(R.string.producto_form_eliminar_pregunta),
            mensaje = stringResource(
                R.string.producto_form_eliminar_mensaje,
                estado.base?.titulo.orEmpty()
            ),
            textoConfirmar = stringResource(R.string.producto_form_eliminar),
            textoCancelar = stringResource(R.string.admin_cancelar),
            onConfirmar = {
                confirmarEliminacion = false
                onEliminar()
            },
            onCancelar = { confirmarEliminacion = false },
            destructivo = true
        )
    }
}

@Composable
private fun FormularioProductoContenido(
    estado: EstadoEdicionProducto,
    categorias: List<Categoria>,
    onCambiarCampo: (CampoProducto, String) -> Unit,
    onSeleccionarCategoria: (Long?) -> Unit
) {
    val formulario = estado.formulario
    val errores = estado.erroresVisibles
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        VistaPreviaProducto(estado = estado)

        SeccionFormulario(titulo = stringResource(R.string.producto_form_seccion_informacion)) {
            CampoFormulario(
                campo = CampoProducto.TITULO,
                valor = formulario.titulo,
                etiqueta = stringResource(R.string.producto_form_titulo),
                error = errores[CampoProducto.TITULO],
                onCambiarCampo = onCambiarCampo,
                capitalizacion = KeyboardCapitalization.Sentences
            )
            CampoFormulario(
                campo = CampoProducto.MARCA,
                valor = formulario.marca,
                etiqueta = stringResource(R.string.producto_form_marca),
                error = errores[CampoProducto.MARCA],
                onCambiarCampo = onCambiarCampo,
                capitalizacion = KeyboardCapitalization.Words
            )
            SelectorCategoria(
                categorias = categorias,
                seleccionada = formulario.categoriaId,
                onSeleccionar = onSeleccionarCategoria
            )
            CampoFormulario(
                campo = CampoProducto.DESCRIPCION,
                valor = formulario.descripcion,
                etiqueta = stringResource(R.string.producto_form_descripcion),
                error = errores[CampoProducto.DESCRIPCION],
                onCambiarCampo = onCambiarCampo,
                capitalizacion = KeyboardCapitalization.Sentences,
                lineas = LINEAS_DESCRIPCION,
                ayuda = stringResource(
                    R.string.producto_form_contador,
                    formulario.descripcion.trim().length,
                    ValidadorProducto.LARGO_MAXIMO_DESCRIPCION
                )
            )
            CampoFormulario(
                campo = CampoProducto.IMAGEN,
                valor = formulario.imagenUrl,
                etiqueta = stringResource(R.string.producto_form_imagen),
                error = errores[CampoProducto.IMAGEN],
                onCambiarCampo = onCambiarCampo,
                teclado = KeyboardType.Uri,
                ayuda = stringResource(R.string.producto_form_imagen_ayuda)
            )
        }

        SeccionFormulario(titulo = stringResource(R.string.producto_form_seccion_precio)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoFormulario(
                    campo = CampoProducto.PRECIO,
                    valor = formulario.precio,
                    etiqueta = stringResource(R.string.producto_form_precio),
                    error = errores[CampoProducto.PRECIO],
                    onCambiarCampo = onCambiarCampo,
                    teclado = KeyboardType.Decimal,
                    prefijo = stringResource(R.string.producto_form_prefijo_moneda),
                    modifier = Modifier.weight(1f)
                )
                CampoFormulario(
                    campo = CampoProducto.DESCUENTO,
                    valor = formulario.descuento,
                    etiqueta = stringResource(R.string.producto_form_descuento),
                    error = errores[CampoProducto.DESCUENTO],
                    onCambiarCampo = onCambiarCampo,
                    teclado = KeyboardType.Decimal,
                    sufijo = stringResource(R.string.producto_form_sufijo_porcentaje),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        SeccionFormulario(
            titulo = stringResource(R.string.producto_form_seccion_inventario),
            descripcion = stringResource(R.string.producto_form_inventario_ayuda)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoFormulario(
                    campo = CampoProducto.STOCK,
                    valor = formulario.stock,
                    etiqueta = stringResource(R.string.producto_form_stock),
                    error = errores[CampoProducto.STOCK],
                    onCambiarCampo = onCambiarCampo,
                    teclado = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
                CampoFormulario(
                    campo = CampoProducto.UNIDADES,
                    valor = formulario.unidadesPorPedido,
                    etiqueta = stringResource(R.string.producto_form_unidades),
                    error = errores[CampoProducto.UNIDADES],
                    onCambiarCampo = onCambiarCampo,
                    teclado = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoFormulario(
                    campo = CampoProducto.STOCK_MINIMO,
                    valor = formulario.stockMinimo,
                    etiqueta = stringResource(R.string.producto_form_stock_minimo),
                    error = errores[CampoProducto.STOCK_MINIMO],
                    onCambiarCampo = onCambiarCampo,
                    teclado = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
                CampoFormulario(
                    campo = CampoProducto.STOCK_MAXIMO,
                    valor = formulario.stockMaximo,
                    etiqueta = stringResource(R.string.producto_form_stock_maximo),
                    error = errores[CampoProducto.STOCK_MAXIMO],
                    onCambiarCampo = onCambiarCampo,
                    teclado = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun VistaPreviaProducto(estado: EstadoEdicionProducto) {
    val vista = estado.vistaPrevia
    val sinErroresDeInventario = listOf(
        CampoProducto.STOCK,
        CampoProducto.STOCK_MINIMO,
        CampoProducto.STOCK_MAXIMO
    ).none { it in estado.errores }
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ImagenProducto(
                url = vista.imagenUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .clip(MaterialTheme.shapes.medium)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.producto_form_vista_previa),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = vista.titulo.ifEmpty { stringResource(R.string.producto_form_sin_nombre) },
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (CampoProducto.PRECIO !in estado.errores && CampoProducto.DESCUENTO !in estado.errores) {
                    PrecioProducto(item = vista, estilo = MaterialTheme.typography.labelLarge)
                }
                if (sinErroresDeInventario) {
                    Spacer(modifier = Modifier.height(4.dp))
                    EtiquetaStock(estado = vista.estadoStock(), stock = vista.stock)
                }
            }
        }
    }
}

@Composable
private fun SeccionFormulario(
    titulo: String,
    descripcion: String? = null,
    contenido: @Composable ColumnScope.() -> Unit
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = titulo, style = MaterialTheme.typography.titleSmall)
            if (descripcion != null) {
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            contenido()
        }
    }
}

@Composable
private fun CampoFormulario(
    campo: CampoProducto,
    valor: String,
    etiqueta: String,
    error: ErrorCampo?,
    onCambiarCampo: (CampoProducto, String) -> Unit,
    modifier: Modifier = Modifier,
    teclado: KeyboardType = KeyboardType.Text,
    capitalizacion: KeyboardCapitalization = KeyboardCapitalization.None,
    imeAction: ImeAction = ImeAction.Next,
    lineas: Int = 1,
    prefijo: String? = null,
    sufijo: String? = null,
    ayuda: String? = null
) {
    val mensaje = error?.let { mensajeError(campo, it) } ?: ayuda
    OutlinedTextField(
        value = valor,
        onValueChange = { nuevo -> onCambiarCampo(campo, nuevo) },
        label = { Text(text = etiqueta, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        isError = error != null,
        supportingText = if (mensaje != null) {
            { Text(text = mensaje) }
        } else {
            null
        },
        prefix = if (prefijo != null) {
            { Text(text = prefijo) }
        } else {
            null
        },
        suffix = if (sufijo != null) {
            { Text(text = sufijo) }
        } else {
            null
        },
        singleLine = lineas == 1,
        minLines = lineas,
        keyboardOptions = KeyboardOptions(
            capitalization = capitalizacion,
            keyboardType = teclado,
            imeAction = if (lineas > 1) ImeAction.Default else imeAction
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun SelectorCategoria(
    categorias: List<Categoria>,
    seleccionada: Long?,
    onSeleccionar: (Long?) -> Unit
) {
    val existeSeleccion = seleccionada != null && categorias.any { it.id == seleccionada }
    Column {
        Text(
            text = stringResource(R.string.producto_form_categoria),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            item(key = CLAVE_SIN_CATEGORIA) {
                FilterChip(
                    selected = !existeSeleccion,
                    onClick = { onSeleccionar(null) },
                    label = { Text(text = stringResource(R.string.categoria_sin_nombre)) }
                )
            }
            items(items = categorias, key = { it.id }) { categoria ->
                FilterChip(
                    selected = categoria.id == seleccionada,
                    onClick = { onSeleccionar(categoria.id) },
                    label = { Text(text = categoria.nombre) }
                )
            }
        }
    }
}

@Composable
private fun BarraGuardar(
    estado: EstadoEdicionProducto,
    onGuardar: () -> Unit
) {
    val interaccion = remember { MutableInteractionSource() }
    Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
        Box(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                .padding(16.dp)
        ) {
            Button(
                onClick = onGuardar,
                enabled = estado.puedeGuardar,
                interactionSource = interaccion,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .rebote(interaccion)
            ) {
                if (estado.guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(painter = painterResource(R.drawable.ic_check), contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(
                            if (estado.esNuevo) R.string.producto_form_crear else R.string.producto_form_guardar
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun mensajeError(campo: CampoProducto, error: ErrorCampo): String = when (error) {
    ErrorCampo.OBLIGATORIO -> stringResource(R.string.producto_error_obligatorio)
    ErrorCampo.TITULO_CORTO -> stringResource(R.string.producto_error_titulo_corto)
    ErrorCampo.TEXTO_LARGO -> stringResource(
        R.string.producto_error_texto_largo,
        when (campo) {
            CampoProducto.TITULO -> ValidadorProducto.LARGO_MAXIMO_TITULO
            CampoProducto.MARCA -> ValidadorProducto.LARGO_MAXIMO_MARCA
            else -> ValidadorProducto.LARGO_MAXIMO_DESCRIPCION
        }
    )
    ErrorCampo.NUMERO_INVALIDO -> stringResource(R.string.producto_error_numero)
    ErrorCampo.ENTERO_INVALIDO -> stringResource(R.string.producto_error_entero)
    ErrorCampo.PRECIO_FUERA_RANGO -> stringResource(
        R.string.producto_error_precio,
        ValidadorProducto.PRECIO_MAXIMO.toInt()
    )
    ErrorCampo.DESCUENTO_FUERA_RANGO -> stringResource(
        R.string.producto_error_descuento,
        ValidadorProducto.DESCUENTO_MAXIMO.toInt()
    )
    ErrorCampo.NEGATIVO -> stringResource(R.string.producto_error_negativo)
    ErrorCampo.CANTIDAD_EXCESIVA -> stringResource(
        R.string.producto_error_cantidad,
        ValidadorProducto.CANTIDAD_MAXIMA
    )
    ErrorCampo.MAXIMO_MENOR_MINIMO -> stringResource(R.string.producto_error_maximo)
    ErrorCampo.UNIDADES_INVALIDAS -> stringResource(R.string.producto_error_unidades)
    ErrorCampo.URL_INVALIDA -> stringResource(R.string.producto_error_url)
}

private const val LINEAS_DESCRIPCION = 3
private const val CLAVE_SIN_CATEGORIA = "sin_categoria"

@Preview(showBackground = true)
@Composable
private fun EditarProductoScreenPreview() {
    ProductosTheme {
        EditarProductoScreen(
            estado = EstadoEdicionProducto(esNuevo = true),
            categorias = listOf(Categoria(id = 1, nombre = "Apple"), Categoria(id = 2, nombre = "Samsung")),
            avisos = emptyFlow(),
            onCambiarCampo = { _, _ -> },
            onSeleccionarCategoria = {},
            onGuardar = {},
            onEliminar = {},
            onVolver = {}
        )
    }
}
