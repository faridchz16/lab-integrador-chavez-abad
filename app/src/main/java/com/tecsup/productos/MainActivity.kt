package com.tecsup.productos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.lista.ListaScreen
import com.tecsup.productos.ui.lista.ProductosDePrueba
import com.tecsup.productos.ui.theme.ProductosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProductosTheme {
                var productos by remember { mutableStateOf(ProductosDePrueba.lista) }
                ListaScreen(
                    estado = UiState.Exito(productos),
                    aviso = null,
                    onReintentar = {},
                    onProductoClick = {},
                    onAlternarFavorito = { id ->
                        productos = productos.map {
                            if (it.id == id) it.copy(esFavorito = !it.esFavorito) else it
                        }
                    }
                )
            }
        }
    }
}
