package com.tecsup.productos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class ColorEstado(
    val contenedor: Color,
    val contenido: Color,
    val acento: Color
)

@Immutable
data class ColoresStock(
    val correcto: ColorEstado,
    val cercaMinimo: ColorEstado,
    val critico: ColorEstado,
    val sobreMaximo: ColorEstado
)

val ColoresStockClaro = ColoresStock(
    correcto = ColorEstado(
        contenedor = Color(0xFFC8F2D0),
        contenido = Color(0xFF00391A),
        acento = Color(0xFF1B8A3E)
    ),
    cercaMinimo = ColorEstado(
        contenedor = Color(0xFFFFE6B0),
        contenido = Color(0xFF3D2A00),
        acento = Color(0xFFB57A00)
    ),
    critico = ColorEstado(
        contenedor = Color(0xFFFFDAD6),
        contenido = Color(0xFF410002),
        acento = Color(0xFFC62828)
    ),
    sobreMaximo = ColorEstado(
        contenedor = Color(0xFFE6DEFF),
        contenido = Color(0xFF22005D),
        acento = Color(0xFF6A4FD8)
    )
)

val ColoresStockOscuro = ColoresStock(
    correcto = ColorEstado(
        contenedor = Color(0xFF0F5229),
        contenido = Color(0xFFA6F0B8),
        acento = Color(0xFF6EDC8E)
    ),
    cercaMinimo = ColorEstado(
        contenedor = Color(0xFF5C4300),
        contenido = Color(0xFFFFE08F),
        acento = Color(0xFFF5C04A)
    ),
    critico = ColorEstado(
        contenedor = Color(0xFF93000A),
        contenido = Color(0xFFFFDAD6),
        acento = Color(0xFFFF8A80)
    ),
    sobreMaximo = ColorEstado(
        contenedor = Color(0xFF4A2FB0),
        contenido = Color(0xFFE6DEFF),
        acento = Color(0xFFB9A6FF)
    )
)

val LocalColoresStock = staticCompositionLocalOf { ColoresStockClaro }

val MaterialTheme.coloresStock: ColoresStock
    @Composable
    @ReadOnlyComposable
    get() = LocalColoresStock.current
