package com.tecsup.productos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tecsup.productos.ui.navigation.AppNav
import com.tecsup.productos.ui.theme.ProductosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as ProductosApp).container
        setContent {
            ProductosTheme {
                AppNav(container = container)
            }
        }
    }
}
