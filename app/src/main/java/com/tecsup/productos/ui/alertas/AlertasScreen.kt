package com.tecsup.productos.ui.alertas

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tecsup.productos.R
import com.tecsup.productos.domain.inventario.AlertaStock
import com.tecsup.productos.domain.inventario.NivelAlerta
import com.tecsup.productos.domain.inventario.ValidadorProducto
import com.tecsup.productos.domain.inventario.alertas
import com.tecsup.productos.domain.inventario.requiereReposicion
import com.tecsup.productos.domain.model.Item
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.admin.AvisoInventario
import com.tecsup.productos.ui.components.Cargando
import com.tecsup.productos.ui.components.ErrorView
import com.tecsup.productos.ui.components.EstadoVacio
import com.tecsup.productos.ui.components.ImagenProducto
import com.tecsup.productos.ui.components.SelectorCantidad
import com.tecsup.productos.ui.components.rebote
import com.tecsup.productos.ui.theme.ColorEstado
import com.tecsup.productos.ui.theme.ProductosTheme
import com.tecsup.productos.ui.theme.coloresStock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertasScreen(
    estado: UiState<EstadoAlertas>,
    filtro: NivelAlerta?,
    avisos: Flow<AvisoInventario>,
    onSeleccionarFiltro: (NivelAlerta?) -> Unit,
    onProductoClick: (Item) -> Unit,
    onReponer: (AlertaStock, Int) -> Unit,
    onDeshacer: () -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val textoDeshacer = stringResource(R.string.inventario_deshacer)
    val deshacerActual by rememberUpdatedState(onDeshacer)
    var idPorReponer by rememberSaveable { mutableStateOf<Int?>(null) }
    val datos = (estado as? UiState.Exito)?.datos
    val alertaPorReponer = datos?.alertas?.firstOrNull { it.producto.id == idPorReponer }

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
            TopAppBar(title = { Text(text = stringResource(R.string.alertas_titulo)) })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (estado) {
                UiState.Cargando -> Cargando()
                is UiState.Error -> ErrorView(mensaje = estado.mensaje, onReintentar = onReintentar)
                is UiState.Exito -> if (estado.datos.total == 0) {
                    EstadoVacio(
                        titulo = stringResource(R.string.alertas_vacio_titulo),
                        mensaje = stringResource(R.string.alertas_vacio_mensaje),
                        icono = R.drawable.ic_check_circulo
                    )
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        FiltrosAlertas(
                            datos = estado.datos,
                            filtro = filtro,
                            onSeleccionar = onSeleccionarFiltro
                        )
                        if (estado.datos.alertas.isEmpty()) {
                            EstadoVacio(
                                titulo = stringResource(R.string.alertas_filtro_vacio_titulo),
                                mensaje = stringResource(R.string.alertas_filtro_vacio_mensaje),
                                icono = R.drawable.ic_check_circulo
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(items = estado.datos.alertas, key = { it.producto.id }) { alerta ->
                                    TarjetaAlerta(
                                        alerta = alerta,
                                        onClick = { onProductoClick(alerta.producto) },
                                        onReponer = { idPorReponer = alerta.producto.id },
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

    if (alertaPorReponer != null) {
        DialogoReponer(
            alerta = alertaPorReponer,
            onConfirmar = { unidades ->
                idPorReponer = null
                onReponer(alertaPorReponer, unidades)
            },
            onCancelar = { idPorReponer = null }
        )
    }
}

@Composable
private fun FiltrosAlertas(
    datos: EstadoAlertas,
    filtro: NivelAlerta?,
    onSeleccionar: (NivelAlerta?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = CLAVE_TODAS) {
            FilterChip(
                selected = filtro == null,
                onClick = { onSeleccionar(null) },
                label = { Text(text = stringResource(R.string.alertas_filtro_todas, datos.total)) }
            )
        }
        items(items = NivelAlerta.entries, key = { it.name }) { nivel ->
            val cantidad = datos.conteos[nivel] ?: 0
            FilterChip(
                selected = filtro == nivel,
                onClick = { onSeleccionar(nivel) },
                enabled = cantidad > 0 || filtro == nivel,
                label = { Text(text = "${etiquetaNivel(nivel)} · $cantidad") },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(colorNivel(nivel).acento, CircleShape)
                    )
                }
            )
        }
    }
}

@Composable
private fun TarjetaAlerta(
    alerta: AlertaStock,
    onClick: () -> Unit,
    onReponer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val producto = alerta.producto
    val colores = colorNivel(alerta.nivel)
    val interaccion = remember { MutableInteractionSource() }
    val fraccion by animateFloatAsState(
        targetValue = (alerta.stock.toFloat() / maxOf(producto.stockMaximo, 1)).coerceIn(0f, 1f),
        label = "fraccionAlerta"
    )
    ElevatedCard(
        onClick = onClick,
        interactionSource = interaccion,
        modifier = modifier
            .fillMaxWidth()
            .rebote(interaccion)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    ImagenProducto(
                        url = producto.imagenUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(MaterialTheme.shapes.medium)
                    )
                    Surface(
                        shape = CircleShape,
                        color = colores.acento,
                        contentColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .size(22.dp)
                            .align(Alignment.BottomEnd)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(iconoNivel(alerta.nivel)),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = etiquetaNivel(alerta.nivel),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = colores.acento
                    )
                    Text(
                        text = producto.titulo,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = detalleAlerta(alerta),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { fraccion },
                color = colores.acento,
                trackColor = colores.contenedor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (alerta.nivel.requiereReposicion()) {
                        pluralStringResource(
                            R.plurals.alertas_sugerencia,
                            alerta.unidadesSugeridas,
                            alerta.unidadesSugeridas
                        )
                    } else {
                        pluralStringResource(
                            R.plurals.alertas_excedente,
                            alerta.excedente,
                            alerta.excedente
                        )
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                if (alerta.nivel.requiereReposicion()) {
                    FilledTonalButton(onClick = onReponer) {
                        Icon(
                            painter = painterResource(R.drawable.ic_mas),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = stringResource(R.string.alertas_reponer))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoReponer(
    alerta: AlertaStock,
    onConfirmar: (Int) -> Unit,
    onCancelar: () -> Unit
) {
    val maximo = (ValidadorProducto.CANTIDAD_MAXIMA - alerta.stock).coerceAtLeast(1)
    var unidades by rememberSaveable(alerta.producto.id) {
        mutableIntStateOf(alerta.unidadesSugeridas.coerceIn(1, maximo))
    }
    val visibilidad = remember { MutableTransitionState(false).apply { targetState = true } }
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
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.alertas_reponer_titulo),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = alerta.producto.titulo,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    SelectorCantidad(
                        cantidad = unidades,
                        maximo = maximo,
                        onCambiar = { unidades = it.coerceIn(1, maximo) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(INCREMENTO_CORTO, INCREMENTO_LARGO).forEach { incremento ->
                            AssistChip(
                                onClick = { unidades = (unidades + incremento).coerceIn(1, maximo) },
                                label = { Text(text = stringResource(R.string.alertas_sumar, incremento)) }
                            )
                        }
                        AssistChip(
                            onClick = { unidades = alerta.unidadesSugeridas.coerceIn(1, maximo) },
                            label = { Text(text = stringResource(R.string.alertas_hasta_maximo)) }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(
                            R.string.alertas_reponer_resumen,
                            alerta.stock,
                            alerta.stock + unidades
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.align(Alignment.End)) {
                        TextButton(onClick = onCancelar) {
                            Text(text = stringResource(R.string.admin_cancelar))
                        }
                        TextButton(onClick = { onConfirmar(unidades) }) {
                            Text(text = stringResource(R.string.alertas_reponer))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun detalleAlerta(alerta: AlertaStock): String = when (alerta.nivel) {
    NivelAlerta.AGOTADO -> stringResource(
        R.string.alertas_detalle_agotado,
        alerta.producto.stockMinimo
    )
    NivelAlerta.BAJO_MINIMO, NivelAlerta.CERCA_MINIMO -> stringResource(
        R.string.alertas_detalle_quedan,
        alerta.stock,
        alerta.producto.stockMinimo
    )
    NivelAlerta.SOBRE_MAXIMO -> stringResource(
        R.string.alertas_detalle_sobre,
        alerta.stock,
        alerta.producto.stockMaximo
    )
}

@Composable
private fun etiquetaNivel(nivel: NivelAlerta): String = stringResource(
    when (nivel) {
        NivelAlerta.AGOTADO -> R.string.alertas_nivel_agotado
        NivelAlerta.BAJO_MINIMO -> R.string.alertas_nivel_bajo
        NivelAlerta.CERCA_MINIMO -> R.string.alertas_nivel_cerca
        NivelAlerta.SOBRE_MAXIMO -> R.string.alertas_nivel_sobre
    }
)

@Composable
private fun colorNivel(nivel: NivelAlerta): ColorEstado {
    val colores = MaterialTheme.coloresStock
    return when (nivel) {
        NivelAlerta.AGOTADO, NivelAlerta.BAJO_MINIMO -> colores.critico
        NivelAlerta.CERCA_MINIMO -> colores.cercaMinimo
        NivelAlerta.SOBRE_MAXIMO -> colores.sobreMaximo
    }
}

@DrawableRes
private fun iconoNivel(nivel: NivelAlerta): Int = when (nivel) {
    NivelAlerta.AGOTADO -> R.drawable.ic_agotado
    NivelAlerta.BAJO_MINIMO, NivelAlerta.CERCA_MINIMO -> R.drawable.ic_alerta
    NivelAlerta.SOBRE_MAXIMO -> R.drawable.ic_inventario
}

private const val CLAVE_TODAS = "todas"
private const val INCREMENTO_CORTO = 10
private const val INCREMENTO_LARGO = 50
private const val ESCALA_INICIAL = 0.85f
private const val DURACION_APARICION_MS = 200

@Preview(showBackground = true)
@Composable
private fun AlertasScreenPreview() {
    val productos = listOf(
        Item(id = 1, titulo = "iPhone 9", marca = "Apple", precio = 1899.0, imagenUrl = null, stock = 0),
        Item(id = 2, titulo = "Galaxy S24", marca = "Samsung", precio = 2999.0, imagenUrl = null, stock = 6),
        Item(id = 3, titulo = "Realme XT", marca = "Realme", precio = 999.0, imagenUrl = null, stock = 95)
    )
    val alertas = productos.alertas()
    ProductosTheme {
        AlertasScreen(
            estado = UiState.Exito(
                EstadoAlertas(
                    alertas = alertas,
                    conteos = alertas.groupingBy { it.nivel }.eachCount(),
                    total = alertas.size
                )
            ),
            filtro = null,
            avisos = emptyFlow(),
            onSeleccionarFiltro = {},
            onProductoClick = {},
            onReponer = { _, _ -> },
            onDeshacer = {},
            onReintentar = {}
        )
    }
}
