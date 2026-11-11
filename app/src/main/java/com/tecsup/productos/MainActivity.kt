package com.tecsup.productos

import android.graphics.Color
import android.os.Bundle
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.productos.domain.model.ModoTema
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.ajustes.AjustesViewModel
import com.tecsup.productos.ui.navigation.AppNav
import com.tecsup.productos.ui.navigation.SesionViewModel
import com.tecsup.productos.ui.theme.ProductosTheme

class MainActivity : ComponentActivity() {

    private val contenedor by lazy { (application as ProductosApp).container }

    private val sesionViewModel: SesionViewModel by viewModels(
        factoryProducer = { SesionViewModel.factory(contenedor) }
    )

    private val ajustesViewModel: AjustesViewModel by viewModels(
        factoryProducer = { AjustesViewModel.factory(contenedor) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        splash.setKeepOnScreenCondition {
            sesionViewModel.estado.value is UiState.Cargando ||
                ajustesViewModel.modoTema.value == null
        }
        splash.setOnExitAnimationListener(::animarSalidaSplash)
        enableEdgeToEdge()
        setContent {
            val modoTema by ajustesViewModel.modoTema.collectAsStateWithLifecycle()
            val temaOscuro = modoTema == ModoTema.OSCURO
            DisposableEffect(temaOscuro) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) {
                        temaOscuro
                    },
                    navigationBarStyle = SystemBarStyle.auto(SCRIM_CLARO, SCRIM_OSCURO) {
                        temaOscuro
                    }
                )
                onDispose {}
            }
            ProductosTheme(darkTheme = temaOscuro) {
                AppNav(container = contenedor)
            }
        }
    }

    private fun animarSalidaSplash(proveedor: SplashScreenViewProvider) {
        proveedor.iconView.animate()
            .scaleX(ESCALA_SALIDA)
            .scaleY(ESCALA_SALIDA)
            .setInterpolator(AnticipateInterpolator())
            .setDuration(DURACION_SALIDA_MS)
            .start()
        proveedor.view.animate()
            .alpha(0f)
            .setDuration(DURACION_SALIDA_MS)
            .withEndAction { proveedor.remove() }
            .start()
    }

    private companion object {
        const val ESCALA_SALIDA = 1.6f
        const val DURACION_SALIDA_MS = 450L
        val SCRIM_CLARO = Color.argb(0xE6, 0xFF, 0xFF, 0xFF)
        val SCRIM_OSCURO = Color.argb(0x80, 0x1B, 0x1B, 0x1B)
    }
}
