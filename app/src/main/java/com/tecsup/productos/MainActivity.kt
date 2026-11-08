package com.tecsup.productos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tecsup.productos.ui.lista.ListaScreen
import com.tecsup.productos.ui.lista.ListaViewModel
import com.tecsup.productos.ui.theme.ProductosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as ProductosApp
        setContent {
            ProductosTheme {
                val listaViewModel: ListaViewModel = viewModel(
                    factory = ListaViewModel.factory(app.container)
                )
                val estado by listaViewModel.estado.collectAsStateWithLifecycle()
                val aviso by listaViewModel.aviso.collectAsStateWithLifecycle()

                ListaScreen(
                    estado = estado,
                    aviso = aviso,
                    onReintentar = listaViewModel::refrescar,
                    onProductoClick = { /* el detalle lo conecta Abad en H7/H14 */ },
                    onAlternarFavorito = listaViewModel::alternarFavorito
                )
            }
        }
    }
}