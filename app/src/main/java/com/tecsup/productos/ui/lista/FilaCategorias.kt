package com.tecsup.productos.ui.lista

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tecsup.productos.R
import com.tecsup.productos.domain.model.Categoria

@Composable
fun FilaCategorias(
    categorias: List<Categoria>,
    seleccionadas: Set<Long>,
    onSeleccionar: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = CLAVE_TODAS) {
            ChipCategoria(
                texto = stringResource(R.string.catalogo_todas),
                seleccionado = seleccionadas.isEmpty(),
                onClick = { onSeleccionar(null) },
                modifier = Modifier.animateItem()
            )
        }
        items(items = categorias, key = { it.id }) { categoria ->
            ChipCategoria(
                texto = categoria.nombre,
                seleccionado = categoria.id in seleccionadas,
                onClick = { onSeleccionar(categoria.id) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

@Composable
private fun ChipCategoria(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = seleccionado,
        onClick = onClick,
        label = { Text(text = texto) },
        leadingIcon = if (seleccionado) {
            {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else {
            null
        },
        modifier = modifier.animateContentSize()
    )
}

private const val CLAVE_TODAS = "todas"
