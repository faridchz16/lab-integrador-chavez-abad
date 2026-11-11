package com.tecsup.productos.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tecsup.productos.R
import com.tecsup.productos.domain.model.TipoProducto

@Composable
fun nombreTipoProducto(tipo: TipoProducto): String = stringResource(
    when (tipo) {
        TipoProducto.SMARTPHONE -> R.string.tipo_smartphone
        TipoProducto.TABLET -> R.string.tipo_tablet
        TipoProducto.RELOJ -> R.string.tipo_reloj
        TipoProducto.ACCESORIO -> R.string.tipo_accesorio
    }
)
