package com.tecsup.productos.ui.pedidos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tecsup.productos.R
import com.tecsup.productos.domain.carrito.LineaPedido
import com.tecsup.productos.domain.carrito.Pedido
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.components.Cargando
import com.tecsup.productos.ui.components.ErrorView
import com.tecsup.productos.ui.components.EstadoVacio
import com.tecsup.productos.ui.theme.ProductosTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidosScreen(
    estado: UiState<List<Pedido>>,
    onReintentar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.pedidos_titulo)) },
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
            when (estado) {
                UiState.Cargando -> Cargando()
                is UiState.Error -> ErrorView(mensaje = estado.mensaje, onReintentar = onReintentar)
                is UiState.Exito -> if (estado.datos.isEmpty()) {
                    EstadoVacio(
                        titulo = stringResource(R.string.pedidos_vacio_titulo),
                        mensaje = stringResource(R.string.pedidos_vacio_mensaje),
                        icono = R.drawable.ic_recibo
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items = estado.datos, key = { it.id }) { pedido ->
                            TarjetaPedido(pedido = pedido, modifier = Modifier.animateItem())
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaPedido(pedido: Pedido, modifier: Modifier = Modifier) {
    var expandido by rememberSaveable(pedido.id) { mutableStateOf(false) }
    val rotacion by animateFloatAsState(
        targetValue = if (expandido) GIRO_EXPANDIDO else 0f,
        label = "flechaPedido"
    )
    val patron = stringResource(R.string.formato_fecha_pedido)
    val fecha = remember(pedido.fecha, patron) {
        SimpleDateFormat(patron, Locale.forLanguageTag(ETIQUETA_IDIOMA)).format(Date(pedido.fecha))
    }
    ElevatedCard(
        onClick = { expandido = !expandido },
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.pedidos_numero, pedido.id),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = fecha,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.producto_precio, pedido.total),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = pluralStringResource(
                            R.plurals.pedidos_articulos,
                            pedido.cantidadArticulos,
                            pedido.cantidadArticulos
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(R.drawable.ic_expandir),
                    contentDescription = stringResource(
                        if (expandido) R.string.pedidos_ocultar else R.string.pedidos_ver_detalle
                    ),
                    modifier = Modifier.rotate(rotacion)
                )
            }
            AnimatedVisibility(
                visible = expandido,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    pedido.lineas.forEach { linea -> FilaLineaPedido(linea = linea) }
                }
            }
        }
    }
}

@Composable
private fun FilaLineaPedido(linea: LineaPedido) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = stringResource(R.string.pedidos_linea, linea.cantidad, linea.titulo),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.producto_precio, linea.subtotal),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private const val GIRO_EXPANDIDO = 180f
private const val ETIQUETA_IDIOMA = "es-PE"

@Preview(showBackground = true)
@Composable
private fun PedidosScreenPreview() {
    ProductosTheme {
        PedidosScreen(
            estado = UiState.Exito(
                listOf(
                    Pedido(
                        id = 1,
                        fecha = 0L,
                        total = 1690.98,
                        cantidadArticulos = 2,
                        lineas = listOf(LineaPedido(1, "iPhone 13 Pro", null, 995.49, 1))
                    )
                )
            ),
            onReintentar = {},
            onVolver = {}
        )
    }
}
