package com.tecsup.productos.domain.inventario

import com.tecsup.productos.domain.model.Item
import com.tecsup.productos.domain.model.STOCK_MAXIMO_PREDETERMINADO
import com.tecsup.productos.domain.model.STOCK_MINIMO_PREDETERMINADO
import com.tecsup.productos.domain.model.TipoProducto
import com.tecsup.productos.domain.model.UNIDADES_POR_PEDIDO_PREDETERMINADAS

data class FormularioProducto(
    val titulo: String = "",
    val marca: String = "",
    val categoriaId: Long? = null,
    val tipo: TipoProducto = TipoProducto.SMARTPHONE,
    val descripcion: String = "",
    val imagenUrl: String = "",
    val precio: String = "",
    val descuento: String = "0",
    val stock: String = "",
    val stockMinimo: String = STOCK_MINIMO_PREDETERMINADO.toString(),
    val stockMaximo: String = STOCK_MAXIMO_PREDETERMINADO.toString(),
    val unidadesPorPedido: String = UNIDADES_POR_PEDIDO_PREDETERMINADAS.toString()
)

enum class CampoProducto {
    TITULO,
    MARCA,
    DESCRIPCION,
    IMAGEN,
    PRECIO,
    DESCUENTO,
    STOCK,
    STOCK_MINIMO,
    STOCK_MAXIMO,
    UNIDADES
}

enum class ErrorCampo {
    OBLIGATORIO,
    TITULO_CORTO,
    TEXTO_LARGO,
    NUMERO_INVALIDO,
    ENTERO_INVALIDO,
    PRECIO_FUERA_RANGO,
    DESCUENTO_FUERA_RANGO,
    NEGATIVO,
    CANTIDAD_EXCESIVA,
    MAXIMO_MENOR_MINIMO,
    UNIDADES_INVALIDAS,
    URL_INVALIDA
}

object ValidadorProducto {

    const val LARGO_MAXIMO_TITULO = 60
    const val LARGO_MAXIMO_MARCA = 40
    const val LARGO_MAXIMO_DESCRIPCION = 500
    const val DESCUENTO_MAXIMO = 90.0
    const val PRECIO_MAXIMO = 100_000.0
    const val CANTIDAD_MAXIMA = 99_999
    private const val LARGO_MINIMO_TITULO = 3

    fun validar(formulario: FormularioProducto): Map<CampoProducto, ErrorCampo> = buildMap {
        validarTitulo(formulario.titulo)?.let { put(CampoProducto.TITULO, it) }
        if (formulario.marca.trim().length > LARGO_MAXIMO_MARCA) {
            put(CampoProducto.MARCA, ErrorCampo.TEXTO_LARGO)
        }
        if (formulario.descripcion.trim().length > LARGO_MAXIMO_DESCRIPCION) {
            put(CampoProducto.DESCRIPCION, ErrorCampo.TEXTO_LARGO)
        }
        if (!esUrlValida(formulario.imagenUrl)) put(CampoProducto.IMAGEN, ErrorCampo.URL_INVALIDA)
        validarPrecio(formulario.precio)?.let { put(CampoProducto.PRECIO, it) }
        validarDescuento(formulario.descuento)?.let { put(CampoProducto.DESCUENTO, it) }
        validarCantidad(formulario.stock)?.let { put(CampoProducto.STOCK, it) }
        validarCantidad(formulario.stockMinimo)?.let { put(CampoProducto.STOCK_MINIMO, it) }
        val errorMaximo = validarCantidad(formulario.stockMaximo) ?: run {
            val minimo = formulario.stockMinimo.aEntero()
            val maximo = formulario.stockMaximo.aEntero()
            if (minimo != null && maximo != null && maximo <= minimo) {
                ErrorCampo.MAXIMO_MENOR_MINIMO
            } else {
                null
            }
        }
        errorMaximo?.let { put(CampoProducto.STOCK_MAXIMO, it) }
        validarUnidades(formulario.unidadesPorPedido)?.let { put(CampoProducto.UNIDADES, it) }
    }

    private fun validarTitulo(valor: String): ErrorCampo? {
        val limpio = valor.trim()
        return when {
            limpio.isEmpty() -> ErrorCampo.OBLIGATORIO
            limpio.length < LARGO_MINIMO_TITULO -> ErrorCampo.TITULO_CORTO
            limpio.length > LARGO_MAXIMO_TITULO -> ErrorCampo.TEXTO_LARGO
            else -> null
        }
    }

    private fun validarPrecio(valor: String): ErrorCampo? {
        if (valor.isBlank()) return ErrorCampo.OBLIGATORIO
        val numero = valor.aDecimal() ?: return ErrorCampo.NUMERO_INVALIDO
        return if (numero <= 0.0 || numero > PRECIO_MAXIMO) ErrorCampo.PRECIO_FUERA_RANGO else null
    }

    private fun validarDescuento(valor: String): ErrorCampo? {
        if (valor.isBlank()) return null
        val numero = valor.aDecimal() ?: return ErrorCampo.NUMERO_INVALIDO
        return if (numero < 0.0 || numero > DESCUENTO_MAXIMO) {
            ErrorCampo.DESCUENTO_FUERA_RANGO
        } else {
            null
        }
    }

    private fun validarCantidad(valor: String): ErrorCampo? {
        if (valor.isBlank()) return ErrorCampo.OBLIGATORIO
        if (valor.trim().startsWith("-")) return ErrorCampo.NEGATIVO
        val numero = valor.aEntero() ?: return ErrorCampo.ENTERO_INVALIDO
        return if (numero > CANTIDAD_MAXIMA) ErrorCampo.CANTIDAD_EXCESIVA else null
    }

    private fun validarUnidades(valor: String): ErrorCampo? {
        if (valor.isBlank()) return ErrorCampo.OBLIGATORIO
        val numero = valor.aEntero() ?: return ErrorCampo.ENTERO_INVALIDO
        return when {
            numero < 1 -> ErrorCampo.UNIDADES_INVALIDAS
            numero > CANTIDAD_MAXIMA -> ErrorCampo.CANTIDAD_EXCESIVA
            else -> null
        }
    }

    private fun esUrlValida(valor: String): Boolean {
        val limpio = valor.trim()
        return limpio.isEmpty() || limpio.startsWith("https://") || limpio.startsWith("http://")
    }
}

fun String.aDecimal(): Double? = trim().replace(',', '.').toDoubleOrNull()

fun String.aEntero(): Int? = trim().toIntOrNull()

fun FormularioProducto.aProducto(base: Item?, id: Int): Item = Item(
    id = id,
    titulo = titulo.trim(),
    marca = marca.trim(),
    precio = precio.aDecimal() ?: 0.0,
    imagenUrl = imagenUrl.trim().ifEmpty { null },
    esFavorito = base?.esFavorito ?: false,
    descripcion = descripcion.trim(),
    rating = base?.rating ?: 0.0,
    stock = stock.aEntero() ?: 0,
    descuento = descuento.aDecimal() ?: 0.0,
    categoriaId = categoriaId,
    stockMinimo = stockMinimo.aEntero() ?: STOCK_MINIMO_PREDETERMINADO,
    stockMaximo = stockMaximo.aEntero() ?: STOCK_MAXIMO_PREDETERMINADO,
    unidadesPorPedido = unidadesPorPedido.aEntero() ?: UNIDADES_POR_PEDIDO_PREDETERMINADAS,
    editadoLocalmente = true,
    tipo = tipo
)

fun Item.aFormulario(): FormularioProducto = FormularioProducto(
    titulo = titulo,
    marca = marca,
    categoriaId = categoriaId,
    tipo = tipo,
    descripcion = descripcion.orEmpty(),
    imagenUrl = imagenUrl.orEmpty(),
    precio = precio.sinCerosFinales(),
    descuento = (descuento ?: 0.0).sinCerosFinales(),
    stock = (stock ?: 0).toString(),
    stockMinimo = stockMinimo.toString(),
    stockMaximo = stockMaximo.toString(),
    unidadesPorPedido = unidadesPorPedido.toString()
)

private fun Double.sinCerosFinales(): String {
    val redondeado = Math.round(this * CENTESIMOS) / CENTESIMOS
    return if (redondeado % 1.0 == 0.0) {
        redondeado.toLong().toString()
    } else {
        redondeado.toString()
    }
}

private const val CENTESIMOS = 100.0

fun FormularioProducto.con(campo: CampoProducto, valor: String): FormularioProducto = when (campo) {
    CampoProducto.TITULO -> copy(titulo = valor)
    CampoProducto.MARCA -> copy(marca = valor)
    CampoProducto.DESCRIPCION -> copy(descripcion = valor)
    CampoProducto.IMAGEN -> copy(imagenUrl = valor)
    CampoProducto.PRECIO -> copy(precio = valor)
    CampoProducto.DESCUENTO -> copy(descuento = valor)
    CampoProducto.STOCK -> copy(stock = valor)
    CampoProducto.STOCK_MINIMO -> copy(stockMinimo = valor)
    CampoProducto.STOCK_MAXIMO -> copy(stockMaximo = valor)
    CampoProducto.UNIDADES -> copy(unidadesPorPedido = valor)
}
