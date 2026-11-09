package com.tecsup.productos.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tecsup.productos.R

@Composable
fun SelectorCantidad(
    cantidad: Int,
    maximo: Int,
    onCambiar: (Int) -> Unit,
    modifier: Modifier = Modifier,
    minimo: Int = 1
) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { onCambiar(cantidad - 1) },
                enabled = cantidad > minimo
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_menos),
                    contentDescription = stringResource(R.string.carrito_disminuir)
                )
            }
            AnimatedContent(
                targetState = cantidad,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInVertically { it } + fadeIn()) togetherWith
                            (slideOutVertically { -it } + fadeOut())
                    } else {
                        (slideInVertically { -it } + fadeIn()) togetherWith
                            (slideOutVertically { it } + fadeOut())
                    }
                },
                label = "cantidad"
            ) { valor ->
                Text(
                    text = valor.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(min = 28.dp)
                )
            }
            IconButton(
                onClick = { onCambiar(cantidad + 1) },
                enabled = cantidad < maximo
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_mas),
                    contentDescription = stringResource(R.string.carrito_aumentar)
                )
            }
        }
    }
}
