package com.tecsup.productos.data.recursos

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

interface ProveedorTextos {
    fun texto(@StringRes id: Int, vararg argumentos: Any): String
    fun plural(@PluralsRes id: Int, cantidad: Int, vararg argumentos: Any): String
}
