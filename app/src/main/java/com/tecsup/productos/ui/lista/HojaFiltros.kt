package com.tecsup.productos.ui.lista

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.tecsup.productos.R
import com.tecsup.productos.domain.catalogo.FiltroCatalogo
import com.tecsup.productos.domain.catalogo.FiltroStock
import com.tecsup.productos.domain.model.TipoProducto
import com.tecsup.productos.ui.components.nombreTipoProducto
import com.tecsup.productos.domain.catalogo.OrdenCatalogo
import com.tecsup.productos.domain.model.Categoria
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HojaFiltros(
    filtroActual: FiltroCatalogo,
    categorias: List<Categoria>,
    rangoPrecios: ClosedFloatingPointRange<Double>?,
    onAplicar: (FiltroCatalogo) -> Unit,
    onLimpiar: () -> Unit,
    onCerrar: () -> Unit
) {
    val estadoHoja = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val alcance = rememberCoroutineScope()
    var categoriasElegidas by remember { mutableStateOf(filtroActual.categorias) }
    var tiposElegidos by remember { mutableStateOf(filtroActual.tipos) }
    var estadosElegidos by remember { mutableStateOf(filtroActual.estadosStock) }
    var orden by remember { mutableStateOf(filtroActual.orden) }
    val limites = rangoPrecios?.let { it.start.toFloat()..it.endInclusive.toFloat() }
    var rango by remember {
        mutableStateOf(
            limites?.let {
                (filtroActual.precioMinimo?.toFloat() ?: it.start)..
                    (filtroActual.precioMaximo?.toFloat() ?: it.endInclusive)
            }
        )
    }
    val visibilidad = remember { MutableTransitionState(false).apply { targetState = true } }

    fun cerrarDespuesDe(accion: () -> Unit) {
        alcance.launch { estadoHoja.hide() }.invokeOnCompletion {
            accion()
            onCerrar()
        }
    }

    ModalBottomSheet(onDismissRequest = onCerrar, sheetState = estadoHoja) {
        AnimatedVisibility(
            visibleState = visibilidad,
            enter = scaleIn(initialScale = ESCALA_INICIAL) + fadeIn()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = stringResource(R.string.filtros_titulo),
                    style = MaterialTheme.typography.titleLarge
                )

                TituloSeccion(texto = stringResource(R.string.filtros_tipo))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TipoProducto.entries.forEach { tipo ->
                        val elegido = tipo in tiposElegidos
                        FilterChip(
                            selected = elegido,
                            onClick = {
                                tiposElegidos = if (elegido) tiposElegidos - tipo else tiposElegidos + tipo
                            },
                            label = { Text(text = nombreTipoProducto(tipo)) }
                        )
                    }
                }

                TituloSeccion(texto = stringResource(R.string.filtros_categorias))
                if (categorias.isEmpty()) {
                    Text(
                        text = stringResource(R.string.filtros_sin_categorias),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categorias.forEach { categoria ->
                            val elegida = categoria.id in categoriasElegidas
                            FilterChip(
                                selected = elegida,
                                onClick = {
                                    categoriasElegidas = if (elegida) {
                                        categoriasElegidas - categoria.id
                                    } else {
                                        categoriasElegidas + categoria.id
                                    }
                                },
                                label = { Text(text = categoria.nombre) }
                            )
                        }
                    }
                }

                val limitesPrecio = limites
                val rangoActual = rango
                if (limitesPrecio != null && rangoActual != null &&
                    limitesPrecio.endInclusive - limitesPrecio.start >= 1f
                ) {
                    TituloSeccion(texto = stringResource(R.string.filtros_precio))
                    Text(
                        text = stringResource(
                            R.string.filtros_rango,
                            stringResource(R.string.producto_precio, rangoActual.start),
                            stringResource(R.string.producto_precio, rangoActual.endInclusive)
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    RangeSlider(
                        value = rangoActual,
                        onValueChange = { rango = it },
                        valueRange = limitesPrecio
                    )
                }

                TituloSeccion(texto = stringResource(R.string.filtros_estado_stock))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FiltroStock.entries.forEach { estado ->
                        val elegido = estado in estadosElegidos
                        FilterChip(
                            selected = elegido,
                            onClick = {
                                estadosElegidos = if (elegido) {
                                    estadosElegidos - estado
                                } else {
                                    estadosElegidos + estado
                                }
                            },
                            label = { Text(text = etiquetaFiltroStock(estado)) }
                        )
                    }
                }

                TituloSeccion(texto = stringResource(R.string.filtros_ordenar))
                OrdenCatalogo.entries.forEach { opcion ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = opcion == orden,
                                onClick = { orden = opcion },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(selected = opcion == orden, onClick = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = etiquetaOrden(opcion),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { cerrarDespuesDe(onLimpiar) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(R.string.filtros_limpiar))
                    }
                    Button(
                        onClick = {
                            cerrarDespuesDe {
                                onAplicar(
                                    FiltroCatalogo(
                                        categorias = categoriasElegidas,
                                        tipos = tiposElegidos,
                                        precioMinimo = precioElegido(
                                            rango?.start,
                                            limitesPrecio?.start,
                                            esMinimo = true
                                        ),
                                        precioMaximo = precioElegido(
                                            rango?.endInclusive,
                                            limitesPrecio?.endInclusive,
                                            esMinimo = false
                                        ),
                                        estadosStock = estadosElegidos,
                                        orden = orden
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(R.string.filtros_aplicar))
                    }
                }
            }
        }
    }
}

@Composable
private fun TituloSeccion(texto: String) {
    Spacer(modifier = Modifier.height(20.dp))
    Text(
        text = texto,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun etiquetaFiltroStock(estado: FiltroStock): String = stringResource(
    when (estado) {
        FiltroStock.DISPONIBLE -> R.string.filtros_stock_disponible
        FiltroStock.STOCK_BAJO -> R.string.filtros_stock_bajo
        FiltroStock.AGOTADO -> R.string.filtros_stock_agotado
    }
)

@Composable
private fun etiquetaOrden(orden: OrdenCatalogo): String = stringResource(
    when (orden) {
        OrdenCatalogo.NOMBRE_ASC -> R.string.orden_nombre_asc
        OrdenCatalogo.NOMBRE_DESC -> R.string.orden_nombre_desc
        OrdenCatalogo.PRECIO_ASC -> R.string.orden_precio_asc
        OrdenCatalogo.PRECIO_DESC -> R.string.orden_precio_desc
        OrdenCatalogo.STOCK_ASC -> R.string.orden_stock_asc
        OrdenCatalogo.STOCK_DESC -> R.string.orden_stock_desc
    }
)

private fun precioElegido(valor: Float?, limite: Float?, esMinimo: Boolean): Double? {
    if (valor == null || limite == null) return null
    val enLimite = if (esMinimo) valor <= limite + TOLERANCIA else valor >= limite - TOLERANCIA
    return if (enLimite) null else valor.toDouble()
}

private const val ESCALA_INICIAL = 0.94f
private const val TOLERANCIA = 0.5f
