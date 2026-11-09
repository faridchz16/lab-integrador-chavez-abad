package com.tecsup.productos.ui.lista

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tecsup.productos.R
import com.tecsup.productos.domain.model.Item
import com.tecsup.productos.domain.model.precioFinal
import com.tecsup.productos.ui.components.EstadoVacio
import com.tecsup.productos.ui.components.ImagenProducto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusquedaCatalogo(
    consulta: String,
    resultados: List<Item>,
    historial: List<String>,
    onConsultaChange: (String) -> Unit,
    onBuscar: (String) -> Unit,
    onEliminarHistorial: (String) -> Unit,
    onLimpiarHistorial: () -> Unit,
    onProductoClick: (Item) -> Unit,
    onCerrar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val enfoque = remember { FocusRequester() }
    LaunchedEffect(Unit) { enfoque.requestFocus() }

    SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = consulta,
                onQueryChange = onConsultaChange,
                onSearch = onBuscar,
                expanded = true,
                onExpandedChange = { expandida -> if (!expandida) onCerrar() },
                placeholder = { Text(text = stringResource(R.string.busqueda_sugerencia)) },
                leadingIcon = {
                    IconButton(onClick = onCerrar) {
                        Icon(
                            painter = painterResource(R.drawable.ic_volver),
                            contentDescription = stringResource(R.string.busqueda_cerrar)
                        )
                    }
                },
                trailingIcon = {
                    if (consulta.isNotEmpty()) {
                        IconButton(onClick = { onConsultaChange("") }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_cerrar),
                                contentDescription = stringResource(R.string.busqueda_limpiar)
                            )
                        }
                    }
                },
                modifier = Modifier.focusRequester(enfoque)
            )
        },
        expanded = true,
        onExpandedChange = { expandida -> if (!expandida) onCerrar() },
        modifier = modifier.fillMaxWidth()
    ) {
        if (consulta.isBlank()) {
            HistorialBusqueda(
                historial = historial,
                onSeleccionar = onConsultaChange,
                onEliminar = onEliminarHistorial,
                onLimpiar = onLimpiarHistorial
            )
        } else if (resultados.isEmpty()) {
            EstadoVacio(
                titulo = stringResource(R.string.sin_resultados_titulo),
                mensaje = stringResource(R.string.sin_resultados_busqueda, consulta),
                icono = R.drawable.ic_buscar
            )
        } else {
            LazyColumn {
                items(items = resultados, key = { it.id }) { item ->
                    ListItem(
                        headlineContent = {
                            Text(text = item.titulo, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        },
                        supportingContent = {
                            Text(
                                text = stringResource(R.string.producto_precio, item.precioFinal()),
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        leadingContent = {
                            ImagenProducto(
                                url = item.imagenUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(MaterialTheme.shapes.small)
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .clickable { onProductoClick(item) }
                            .animateItem()
                    )
                }
            }
        }
    }
}

@Composable
private fun HistorialBusqueda(
    historial: List<String>,
    onSeleccionar: (String) -> Unit,
    onEliminar: (String) -> Unit,
    onLimpiar: () -> Unit
) {
    if (historial.isEmpty()) {
        Text(
            text = stringResource(R.string.busqueda_sin_historial),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(24.dp)
        )
        return
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.busqueda_recientes),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onLimpiar) {
            Text(text = stringResource(R.string.busqueda_borrar_historial))
        }
    }
    LazyColumn {
        items(items = historial, key = { it }) { texto ->
            ListItem(
                headlineContent = { Text(text = texto) },
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.ic_historial),
                        contentDescription = null
                    )
                },
                trailingContent = {
                    IconButton(onClick = { onEliminar(texto) }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_cerrar),
                            contentDescription = stringResource(R.string.busqueda_eliminar, texto)
                        )
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier
                    .clickable { onSeleccionar(texto) }
                    .animateItem()
            )
        }
    }
}
