package com.tecsup.productos.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val EsquemaClaro = lightColorScheme(
    primary = PrimarioClaro,
    onPrimary = SobrePrimarioClaro,
    primaryContainer = ContenedorPrimarioClaro,
    onPrimaryContainer = SobreContenedorPrimarioClaro,
    inversePrimary = PrimarioInversoClaro,
    secondary = SecundarioClaro,
    onSecondary = SobreSecundarioClaro,
    secondaryContainer = ContenedorSecundarioClaro,
    onSecondaryContainer = SobreContenedorSecundarioClaro,
    tertiary = TerciarioClaro,
    onTertiary = SobreTerciarioClaro,
    tertiaryContainer = ContenedorTerciarioClaro,
    onTertiaryContainer = SobreContenedorTerciarioClaro,
    background = FondoClaro,
    onBackground = SobreFondoClaro,
    surface = FondoClaro,
    onSurface = SobreFondoClaro,
    surfaceVariant = VarianteSuperficieClaro,
    onSurfaceVariant = SobreVarianteSuperficieClaro,
    surfaceTint = PrimarioClaro,
    inverseSurface = SuperficieInversaClaro,
    inverseOnSurface = SobreSuperficieInversaClaro,
    error = ErrorClaro,
    onError = SobreErrorClaro,
    errorContainer = ContenedorErrorClaro,
    onErrorContainer = SobreContenedorErrorClaro,
    outline = ContornoClaro,
    outlineVariant = VarianteContornoClaro,
    surfaceBright = SuperficieBrillanteClaro,
    surfaceDim = SuperficieTenueClaro,
    surfaceContainerLowest = ContenedorMinimoClaro,
    surfaceContainerLow = ContenedorBajoClaro,
    surfaceContainer = ContenedorClaro,
    surfaceContainerHigh = ContenedorAltoClaro,
    surfaceContainerHighest = ContenedorMaximoClaro
)

private val EsquemaOscuro = darkColorScheme(
    primary = PrimarioOscuro,
    onPrimary = SobrePrimarioOscuro,
    primaryContainer = ContenedorPrimarioOscuro,
    onPrimaryContainer = SobreContenedorPrimarioOscuro,
    inversePrimary = PrimarioInversoOscuro,
    secondary = SecundarioOscuro,
    onSecondary = SobreSecundarioOscuro,
    secondaryContainer = ContenedorSecundarioOscuro,
    onSecondaryContainer = SobreContenedorSecundarioOscuro,
    tertiary = TerciarioOscuro,
    onTertiary = SobreTerciarioOscuro,
    tertiaryContainer = ContenedorTerciarioOscuro,
    onTertiaryContainer = SobreContenedorTerciarioOscuro,
    background = FondoOscuro,
    onBackground = SobreFondoOscuro,
    surface = FondoOscuro,
    onSurface = SobreFondoOscuro,
    surfaceVariant = VarianteSuperficieOscuro,
    onSurfaceVariant = SobreVarianteSuperficieOscuro,
    surfaceTint = PrimarioOscuro,
    inverseSurface = SuperficieInversaOscuro,
    inverseOnSurface = SobreSuperficieInversaOscuro,
    error = ErrorOscuro,
    onError = SobreErrorOscuro,
    errorContainer = ContenedorErrorOscuro,
    onErrorContainer = SobreContenedorErrorOscuro,
    outline = ContornoOscuro,
    outlineVariant = VarianteContornoOscuro,
    surfaceBright = SuperficieBrillanteOscuro,
    surfaceDim = SuperficieTenueOscuro,
    surfaceContainerLowest = ContenedorMinimoOscuro,
    surfaceContainerLow = ContenedorBajoOscuro,
    surfaceContainer = ContenedorOscuro,
    surfaceContainerHigh = ContenedorAltoOscuro,
    surfaceContainerHighest = ContenedorMaximoOscuro
)

@Composable
fun ProductosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalColoresStock provides if (darkTheme) ColoresStockOscuro else ColoresStockClaro
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
            typography = Typography,
            shapes = FormasStockify,
            content = content
        )
    }
}
