package com.tecsup.productos.ui.admin

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tecsup.productos.R
import com.tecsup.productos.domain.model.EstadoStock
import com.tecsup.productos.domain.model.Item
import com.tecsup.productos.domain.model.estadoStock
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.components.Cargando
import com.tecsup.productos.ui.components.DialogoConfirmacion
import com.tecsup.productos.ui.components.ErrorView
import com.tecsup.productos.ui.components.rebote
import com.tecsup.productos.ui.theme.ColorEstado
import com.tecsup.productos.ui.theme.ProductosTheme
import com.tecsup.productos.ui.theme.coloresStock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    correo: String,
    estado: UiState<PanelAdmin>,
    trabajando: Boolean,
    resultado: ResultadoAdmin?,
    onSincronizar: () -> Unit,
    onLimpiarCache: () -> Unit,
    onResultadoMostrado: () -> Unit,
    onGestionarProductos: () -> Unit,
    onGestionarCategorias: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val recursos = LocalContext.current.resources
    var confirmarLimpieza by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(resultado) {
        val actual = resultado ?: return@LaunchedEffect
        val mensaje = when (actual) {
            is ResultadoAdmin.Sincronizado -> recursos.getQuantityString(
                R.plurals.admin_sincronizado,
                actual.cantidad,
                actual.cantidad
            )
            ResultadoAdmin.CacheLimpiada -> recursos.getString(R.string.admin_cache_limpiada)
            is ResultadoAdmin.Error -> actual.mensaje
        }
        snackbarHostState.showSnackbar(mensaje)
        onResultadoMostrado()
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.admin_titulo)) }
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
                label = "estadoAdmin"
            ) { actual ->
                when (actual) {
                    UiState.Cargando -> Cargando()
                    is UiState.Error -> ErrorView(
                        mensaje = actual.mensaje,
                        onReintentar = onSincronizar
                    )
                    is UiState.Exito -> ContenidoAdmin(
                        correo = correo,
                        panel = actual.datos,
                        trabajando = trabajando,
                        onSincronizar = onSincronizar,
                        onLimpiarCache = { confirmarLimpieza = true },
                        onGestionarProductos = onGestionarProductos,
                        onGestionarCategorias = onGestionarCategorias
                    )
                }
            }
        }
    }

    if (confirmarLimpieza) {
        DialogoConfirmacion(
            icono = R.drawable.ic_borrar,
            titulo = stringResource(R.string.admin_limpiar_pregunta),
            mensaje = stringResource(R.string.admin_limpiar_mensaje),
            textoConfirmar = stringResource(R.string.admin_limpiar_confirmar),
            textoCancelar = stringResource(R.string.admin_cancelar),
            onConfirmar = {
                confirmarLimpieza = false
                onLimpiarCache()
            },
            onCancelar = { confirmarLimpieza = false },
            destructivo = true
        )
    }
}

@Composable
private fun ContenidoAdmin(
    correo: String,
    panel: PanelAdmin,
    trabajando: Boolean,
    onSincronizar: () -> Unit,
    onLimpiarCache: () -> Unit,
    onGestionarProductos: () -> Unit,
    onGestionarCategorias: () -> Unit
) {
    val resumen = panel.resumen
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        EncabezadoSesion(correo = correo)

        TituloSeccion(texto = stringResource(R.string.admin_hoy))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TarjetaEstadistica(
                icono = R.drawable.ic_recibo,
                valor = panel.pedidosHoy.toString(),
                etiqueta = stringResource(R.string.admin_pedidos_hoy),
                acento = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            TarjetaEstadistica(
                icono = R.drawable.ic_dinero,
                valor = stringResource(R.string.producto_precio, panel.ventasHoy),
                etiqueta = stringResource(R.string.admin_ventas_hoy),
                acento = MaterialTheme.coloresStock.correcto.acento,
                modifier = Modifier.weight(1f)
            )
        }

        TituloSeccion(texto = stringResource(R.string.admin_inventario))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TarjetaEstadistica(
                icono = R.drawable.ic_inventario,
                valor = resumen.totalGuardado.toString(),
                etiqueta = stringResource(R.string.admin_total_guardado),
                acento = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            TarjetaEstadistica(
                icono = R.drawable.ic_grafico,
                valor = stringResource(R.string.producto_precio, resumen.valorInventario),
                etiqueta = stringResource(R.string.admin_valor_inventario),
                acento = MaterialTheme.coloresStock.sobreMaximo.acento,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TarjetaEstadistica(
                icono = R.drawable.ic_alerta,
                valor = resumen.stockBajo.toString(),
                etiqueta = stringResource(R.string.admin_stock_bajo),
                acento = MaterialTheme.coloresStock.cercaMinimo.acento,
                modifier = Modifier.weight(1f)
            )
            TarjetaEstadistica(
                icono = R.drawable.ic_agotado,
                valor = resumen.agotados.toString(),
                etiqueta = stringResource(R.string.admin_agotados),
                acento = MaterialTheme.coloresStock.critico.acento,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        TarjetaEstadoInventario(distribucion = panel.distribucion)
        Spacer(modifier = Modifier.height(12.dp))
        TarjetaVentasSemana(panel = panel)
        if (panel.menorStock.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            TarjetaMenorStock(productos = panel.menorStock)
        }

        TituloSeccion(texto = stringResource(R.string.admin_gestion))
        TarjetaGestion(
            icono = R.drawable.ic_inventario,
            titulo = stringResource(R.string.admin_gestion_productos),
            detalle = pluralStringResource(
                R.plurals.admin_gestion_productos_detalle,
                resumen.totalGuardado,
                resumen.totalGuardado
            ),
            onClick = onGestionarProductos
        )
        Spacer(modifier = Modifier.height(12.dp))
        TarjetaGestion(
            icono = R.drawable.ic_categoria,
            titulo = stringResource(R.string.admin_gestion_categorias),
            detalle = pluralStringResource(
                R.plurals.admin_gestion_categorias_detalle,
                panel.totalCategorias,
                panel.totalCategorias
            ),
            onClick = onGestionarCategorias
        )

        TituloSeccion(texto = stringResource(R.string.admin_acciones))
        AccionesAdmin(
            trabajando = trabajando,
            onSincronizar = onSincronizar,
            onLimpiarCache = onLimpiarCache
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun EncabezadoSesion(correo: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_panel_admin),
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.admin_sesion),
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = correo,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun TituloSeccion(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
    )
}

@Composable
private fun TarjetaEstadoInventario(distribucion: DistribucionStock) {
    val colores = MaterialTheme.coloresStock
    val segmentos = listOf(
        SegmentoGrafico(
            valor = distribucion.correcto,
            color = colores.correcto.acento,
            etiqueta = stringResource(R.string.admin_leyenda_correcto)
        ),
        SegmentoGrafico(
            valor = distribucion.cercaMinimo,
            color = colores.cercaMinimo.acento,
            etiqueta = stringResource(R.string.admin_leyenda_cerca)
        ),
        SegmentoGrafico(
            valor = distribucion.critico,
            color = colores.critico.acento,
            etiqueta = stringResource(R.string.admin_leyenda_critico)
        ),
        SegmentoGrafico(
            valor = distribucion.sobreMaximo,
            color = colores.sobreMaximo.acento,
            etiqueta = stringResource(R.string.admin_leyenda_sobre)
        )
    )
    TarjetaGrafico(
        icono = R.drawable.ic_grafico,
        titulo = stringResource(R.string.admin_grafico_estado)
    ) {
        if (distribucion.total == 0) {
            TextoSinDatos(texto = stringResource(R.string.admin_sin_productos_grafico))
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GraficoDona(
                    segmentos = segmentos,
                    textoCentro = distribucion.total.toString(),
                    subtextoCentro = stringResource(R.string.admin_grafico_estado_centro),
                    descripcion = segmentos.joinToString { "${it.etiqueta}: ${it.valor}" }
                )
                Spacer(modifier = Modifier.width(20.dp))
                LeyendaGrafico(segmentos = segmentos, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TarjetaVentasSemana(panel: PanelAdmin) {
    val formatoDia = remember {
        SimpleDateFormat(PATRON_DIA, Locale.forLanguageTag(ETIQUETA_IDIOMA))
    }
    val etiquetas = remember(panel.ventasSemana) {
        panel.ventasSemana.map { dia ->
            formatoDia.format(Date(dia.inicioDia))
                .replace(".", "")
                .replaceFirstChar { it.titlecase(Locale.forLanguageTag(ETIQUETA_IDIOMA)) }
        }
    }
    val totalSemana = stringResource(R.string.producto_precio, panel.totalSemana)
    TarjetaGrafico(
        icono = R.drawable.ic_dinero,
        titulo = stringResource(R.string.admin_grafico_ventas)
    ) {
        Text(
            text = stringResource(R.string.admin_total_semana, totalSemana),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        GraficoBarras(
            valores = panel.ventasSemana.map { it.total },
            etiquetas = etiquetas,
            indiceResaltado = panel.ventasSemana.indexOfFirst { it.esHoy },
            descripcion = stringResource(R.string.admin_total_semana, totalSemana)
        )
        if (panel.totalSemana == 0.0) {
            Spacer(modifier = Modifier.height(12.dp))
            TextoSinDatos(texto = stringResource(R.string.admin_sin_ventas))
        }
    }
}

@Composable
private fun TarjetaMenorStock(productos: List<Item>) {
    TarjetaGrafico(
        icono = R.drawable.ic_alerta,
        titulo = stringResource(R.string.admin_menor_stock)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            productos.forEach { producto -> FilaMenorStock(producto = producto) }
        }
    }
}

@Composable
private fun FilaMenorStock(producto: Item) {
    val stock = producto.stock ?: 0
    val colores = colorEstado(producto.estadoStock())
    val fraccion by animateFloatAsState(
        targetValue = (stock.toFloat() / maxOf(producto.stockMaximo, 1)).coerceIn(0f, 1f),
        label = "fraccionStock"
    )
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = producto.titulo,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(
                    R.string.admin_menor_stock_detalle,
                    stock,
                    producto.stockMinimo
                ),
                style = MaterialTheme.typography.labelMedium,
                color = colores.acento,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { fraccion },
            color = colores.acento,
            trackColor = colores.contenedor,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
    }
}

@Composable
private fun colorEstado(estado: EstadoStock): ColorEstado {
    val colores = MaterialTheme.coloresStock
    return when (estado) {
        EstadoStock.CERCA_MINIMO -> colores.cercaMinimo
        EstadoStock.BAJO_MINIMO, EstadoStock.AGOTADO -> colores.critico
        EstadoStock.SOBRE_MAXIMO -> colores.sobreMaximo
        EstadoStock.DISPONIBLE, EstadoStock.SIN_DATO -> colores.correcto
    }
}

@Composable
private fun TarjetaGrafico(
    @DrawableRes icono: Int,
    titulo: String,
    contenido: @Composable () -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(icono),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = titulo, style = MaterialTheme.typography.titleSmall)
            }
            Spacer(modifier = Modifier.height(12.dp))
            contenido()
        }
    }
}

@Composable
private fun TextoSinDatos(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun TarjetaGestion(
    @DrawableRes icono: Int,
    titulo: String,
    detalle: String,
    onClick: () -> Unit
) {
    val interaccion = remember { MutableInteractionSource() }
    OutlinedCard(
        onClick = onClick,
        interactionSource = interaccion,
        modifier = Modifier
            .fillMaxWidth()
            .rebote(interaccion)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(painter = painterResource(icono), contentDescription = null)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = titulo, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = detalle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_siguiente),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AccionesAdmin(
    trabajando: Boolean,
    onSincronizar: () -> Unit,
    onLimpiarCache: () -> Unit
) {
    val interaccionSincronizar = remember { MutableInteractionSource() }
    val interaccionLimpiar = remember { MutableInteractionSource() }
    Button(
        onClick = onSincronizar,
        enabled = !trabajando,
        interactionSource = interaccionSincronizar,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .rebote(interaccionSincronizar)
    ) {
        if (trabajando) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_sincronizar),
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.admin_sincronizar))
        }
    }
    Text(
        text = stringResource(R.string.admin_sincronizar_ayuda),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
    )
    OutlinedButton(
        onClick = onLimpiarCache,
        enabled = !trabajando,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        ),
        interactionSource = interaccionLimpiar,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .rebote(interaccionLimpiar)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_borrar),
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = stringResource(R.string.admin_limpiar))
    }
    Text(
        text = stringResource(R.string.admin_limpiar_ayuda),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 6.dp)
    )
}

@Composable
private fun TarjetaEstadistica(
    @DrawableRes icono: Int,
    valor: String,
    etiqueta: String,
    acento: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                painter = painterResource(icono),
                contentDescription = null,
                tint = acento
            )
            Spacer(modifier = Modifier.height(12.dp))
            AnimatedContent(
                targetState = valor,
                transitionSpec = {
                    (slideInVertically { it } + fadeIn()) togetherWith
                        (slideOutVertically { -it } + fadeOut())
                },
                label = "valorEstadistica"
            ) { texto ->
                Text(
                    text = texto,
                    style = if (texto.length > LARGO_VALOR_COMPACTO) {
                        MaterialTheme.typography.titleLarge
                    } else {
                        MaterialTheme.typography.headlineSmall
                    },
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private const val LARGO_VALOR_COMPACTO = 9
private const val PATRON_DIA = "EEE"
private const val ETIQUETA_IDIOMA = "es-PE"

@Preview(showBackground = true)
@Composable
private fun AdminScreenPreview() {
    ProductosTheme {
        AdminScreen(
            correo = "admin@productos.com",
            estado = UiState.Exito(
                construirPanel(
                    items = emptyList(),
                    pedidos = emptyList(),
                    totalCategorias = 5,
                    ahora = 0L
                )
            ),
            trabajando = false,
            resultado = null,
            onSincronizar = {},
            onLimpiarCache = {},
            onResultadoMostrado = {},
            onGestionarProductos = {},
            onGestionarCategorias = {}
        )
    }
}
