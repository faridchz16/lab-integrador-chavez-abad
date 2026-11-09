package com.tecsup.productos.domain.inventario

import com.tecsup.productos.domain.catalogo.normalizada
import com.tecsup.productos.domain.model.Categoria

data class CategoriaEliminada(
    val id: Long,
    val nombre: String,
    val productos: List<Int>
)

enum class ErrorNombreCategoria { OBLIGATORIO, CORTO, LARGO, DUPLICADO }

object ValidadorCategoria {

    const val LARGO_MAXIMO = 30
    private const val LARGO_MINIMO = 2

    fun validar(
        nombre: String,
        existentes: List<Categoria>,
        idEditado: Long? = null
    ): ErrorNombreCategoria? {
        val limpio = nombre.trim()
        return when {
            limpio.isEmpty() -> ErrorNombreCategoria.OBLIGATORIO
            limpio.length < LARGO_MINIMO -> ErrorNombreCategoria.CORTO
            limpio.length > LARGO_MAXIMO -> ErrorNombreCategoria.LARGO
            existentes.any { it.id != idEditado && it.nombre.normalizada() == limpio.normalizada() } ->
                ErrorNombreCategoria.DUPLICADO
            else -> null
        }
    }
}
