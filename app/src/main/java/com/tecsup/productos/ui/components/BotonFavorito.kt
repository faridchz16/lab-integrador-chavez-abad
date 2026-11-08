package com.tecsup.productos.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.tecsup.productos.R

@Composable
fun BotonFavorito(
    esFavorito: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(
                if (esFavorito) R.drawable.ic_favorito else R.drawable.ic_favorito_borde
            ),
            contentDescription = stringResource(
                if (esFavorito) R.string.favorito_quitar else R.string.favorito_agregar
            ),
            tint = if (esFavorito) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}
