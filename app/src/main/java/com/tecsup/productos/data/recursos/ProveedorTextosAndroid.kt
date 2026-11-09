package com.tecsup.productos.data.recursos

import android.content.Context

class ProveedorTextosAndroid(
    private val context: Context
) : ProveedorTextos {

    override fun texto(id: Int, vararg argumentos: Any): String =
        context.getString(id, *argumentos)

    override fun plural(id: Int, cantidad: Int, vararg argumentos: Any): String =
        context.resources.getQuantityString(id, cantidad, *argumentos)
}
