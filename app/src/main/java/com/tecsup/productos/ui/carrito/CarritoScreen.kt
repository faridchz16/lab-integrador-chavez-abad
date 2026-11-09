package com.tecsup.productos.ui.carrito

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tecsup.productos.R
import com.tecsup.productos.domain.carrito.Carrito
import com.tecsup.productos.domain.carrito.LineaCarrito
import com.tecsup.productos.domain.carrito.Pedido
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.components.Cargando
import com.tecsup.productos.ui.components.DialogoConfirmacion
import com.tecsup.productos.ui.components.ErrorView
import com.tecsup.productos.ui.components.EstadoVacio
import com.tecsup.productos.ui.components.ImagenProducto
import com.tecsup.productos.ui.components.SelectorCantidad
import com.tecsup.productos.ui.components.rebote
import com.tecsup.productos.ui.theme.ProductosTheme
import com.tecsup.productos.ui.theme.coloresStock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarritoScreen(
    estado: UiState<Carrito>,
    procesando: Boolean,
    pedidoConfirmado: Pedido?,
    onCambiarCantidad: (Int, Int) -> Unit,
    onQuitar: (Int) -> Unit,
    onVaciar: () -> Unit,
    onConfirmar: () -> Unit,
    onPedidoMostrado: () -> Unit,
    onVerPedidos: () -> Unit,
    onIrCatalogo: () -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var confirmarPedido by rememberSaveable { mutableStateOf(false) }
    var confirmarVaciado by rememberSaveable { mutableStateOf(false) }
    val carrito = (estado as? UiState.Exito)?.datos

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.carrito_titulo)) },
                actions = {
                    IconButton(onClick = onVerPedidos) {
                        Icon(
                            painter = painterResource(R.drawable.ic_recibo),
                            contentDescription = stringResource(R.string.pedidos_abrir)
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (carrito != null && !carrito.estaVacio) {
                ResumenCarrito(
                    carrito = carrito,
                    procesando = procesando,
                    onConfirmar = { confirmarPedido = true },
                    onVaciar = { confirmarVaciado = true }
                )
            }
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
                is UiState.Exito -> if (estado.datos.estaVacio) {
                    EstadoVacio(
                        titulo = stringResource(R.string.carrito_vacio_titulo),
                        mensaje = stringResource(R.string.carrito_vacio_mensaje),
                        icono = R.drawable.ic_carrito_borde,
                        accion = {
                            Button(onClick = onIrCatalogo) {
                                Text(text = stringResource(R.string.carrito_ir_catalogo))
                            }
                        }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items = estado.datos.lineas, key = { it.productoId }) { linea ->
                            TarjetaLineaCarrito(
                                linea = linea,
                                onCambiarCantidad = { cantidad ->
                                    onCambiarCantidad(linea.productoId, cantidad)
                                },
                                onQuitar = { onQuitar(linea.productoId) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }

    if (confirmarPedido && carrito != null) {
        DialogoConfirmacion(
            icono = R.drawable.ic_carrito,
            titulo = stringResource(R.string.carrito_confirmar_pregunta),
            mensaje = stringResource(
                R.string.carrito_confirmar_mensaje,
                stringResource(R.string.producto_precio, carrito.total)
            ),
            textoConfirmar = stringResource(R.string.carrito_confirmar),
            textoCancelar = stringResource(R.string.carrito_cancelar),
            onConfirmar = {
                confirmarPedido = false
                onConfirmar()
            },
            onCancelar = { confirmarPedido = false }
        )
    }

    if (confirmarVaciado) {
        DialogoConfirmacion(
            icono = R.drawable.ic_borrar,
            titulo = stringResource(R.string.carrito_vaciar_pregunta),
            mensaje = stringResource(R.string.carrito_vaciar_mensaje),
            textoConfirmar = stringResource(R.string.carrito_vaciar),
            textoCancelar = stringResource(R.string.carrito_cancelar),
            onConfirmar = {
                confirmarVaciado = false
                onVaciar()
            },
            onCancelar = { confirmarVaciado = false },
            destructivo = true
        )
    }

    if (pedidoConfirmado != null) {
        DialogoConfirmacion(
            icono = R.drawable.ic_check_circulo,
            titulo = stringResource(R.string.carrito_pedido_exito_titulo),
            mensaje = stringResource(
                R.string.carrito_pedido_exito_mensaje,
                pedidoConfirmado.id,
                stringResource(R.string.producto_precio, pedidoConfirmado.total)
            ),
            textoConfirmar = stringResource(R.string.carrito_ver_pedidos),
            textoCancelar = stringResource(R.string.carrito_seguir_comprando),
            onConfirmar = {
                onPedidoMostrado()
                onVerPedidos()
            },
            onCancelar = onPedidoMostrado,
            colorIcono = MaterialTheme.coloresStock.correcto.acento
        )
    }
}

@Composable
private fun TarjetaLineaCarrito(
    linea: LineaCarrito,
    onCambiarCantidad: (Int) -> Unit,
    onQuitar: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            ImagenProducto(
                url = linea.imagenUrl,
                contentDescription = stringResource(R.string.producto_imagen_descripcion, linea.titulo),
                modifier = Modifier
                    .size(72.dp)
                    .clip(MaterialTheme.shapes.medium)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = linea.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.producto_precio, linea.precioUnitario),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (linea.precioOriginal > linea.precioUnitario) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.producto_precio, linea.precioOriginal),
                            style = MaterialTheme.typography.bodySmall,
                            textDecoration = TextDecoration.LineThrough,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SelectorCantidad(
                        cantidad = linea.cantidad,
                        maximo = linea.maximoPermitido,
                        onCambiar = onCambiarCantidad
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = stringResource(R.string.producto_precio, linea.subtotal),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (linea.cantidad >= linea.maximoPermitido) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.carrito_maximo_disponible,
                            linea.maximoPermitido,
                            linea.maximoPermitido
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.coloresStock.cercaMinimo.acento,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            IconButton(onClick = onQuitar) {
                Icon(
                    painter = painterResource(R.drawable.ic_borrar),
                    contentDescription = stringResource(R.string.carrito_quitar, linea.titulo),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ResumenCarrito(
    carrito: Carrito,
    procesando: Boolean,
    onConfirmar: () -> Unit,
    onVaciar: () -> Unit
) {
    val interaccionConfirmar = remember { MutableInteractionSource() }
    Surface(
        tonalElevation = 3.dp,
        shadowElevation = 8.dp,
        shape = MaterialTheme.shapes.large.copy(
            bottomStart = MaterialTheme.shapes.extraSmall.bottomStart,
            bottomEnd = MaterialTheme.shapes.extraSmall.bottomEnd
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            FilaResumen(
                etiqueta = pluralStringResource(
                    R.plurals.carrito_subtotal,
                    carrito.cantidadArticulos,
                    carrito.cantidadArticulos
                ),
                valor = stringResource(R.string.producto_precio, carrito.subtotal)
            )
            if (carrito.descuentos > 0) {
                FilaResumen(
                    etiqueta = stringResource(R.string.carrito_descuentos),
                    valor = stringResource(
                        R.string.carrito_descuento_valor,
                        stringResource(R.string.producto_precio, carrito.descuentos)
                    ),
                    colorValor = MaterialTheme.coloresStock.correcto.acento
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.carrito_total),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                AnimatedContent(targetState = carrito.total, label = "totalCarrito") { total ->
                    Text(
                        text = stringResource(R.string.producto_precio, total),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onVaciar,
                    enabled = !procesando,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = stringResource(R.string.carrito_vaciar))
                }
                Button(
                    onClick = onConfirmar,
                    enabled = !procesando,
                    interactionSource = interaccionConfirmar,
                    modifier = Modifier
                        .weight(2f)
                        .rebote(interaccionConfirmar)
                ) {
                    if (procesando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(text = stringResource(R.string.carrito_confirmar_pedido))
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaResumen(
    etiqueta: String,
    valor: String,
    colorValor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(text = valor, style = MaterialTheme.typography.bodyMedium, color = colorValor)
    }
}

@Preview(showBackground = true)
@Composable
private fun CarritoScreenPreview() {
    ProductosTheme {
        CarritoScreen(
            estado = UiState.Exito(
                Carrito(
                    listOf(
                        LineaCarrito(1, "iPhone 13 Pro", null, 1099.99, 995.49, 2, 25, 1),
                        LineaCarrito(2, "Samsung Galaxy S10", null, 699.99, 699.99, 1, 1, 1)
                    )
                )
            ),
            procesando = false,
            pedidoConfirmado = null,
            onCambiarCantidad = { _, _ -> },
            onQuitar = {},
            onVaciar = {},
            onConfirmar = {},
            onPedidoMostrado = {},
            onVerPedidos = {},
            onIrCatalogo = {},
            onReintentar = {}
        )
    }
}
