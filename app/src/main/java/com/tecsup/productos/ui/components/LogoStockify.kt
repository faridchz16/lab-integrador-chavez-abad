package com.tecsup.productos.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tecsup.productos.R

@Composable
fun LogoStockify(
    modifier: Modifier = Modifier,
    tamano: Dp = 48.dp,
    contentDescription: String? = stringResource(R.string.logo_descripcion)
) {
    Image(
        painter = painterResource(R.drawable.logo_stockify),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(tamano)
            .clip(RoundedCornerShape(tamano * PROPORCION_ESQUINA))
    )
}

private const val PROPORCION_ESQUINA = 0.28f
