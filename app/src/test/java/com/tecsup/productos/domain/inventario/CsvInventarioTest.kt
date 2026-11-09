package com.tecsup.productos.domain.inventario

import com.tecsup.productos.domain.model.Item
import org.junit.Assert.assertEquals
import org.junit.Test

class CsvInventarioTest {

    private val encabezados = listOf("ID", "Producto", "Marca", "Categoría", "Precio", "Descuento",
        "Precio final", "Stock", "Mínimo", "Máximo", "Unidades", "Estado")

    @Test
    fun generaEncabezadoYUnaFilaPorProducto() {
        val csv = generarCsvInventario(
            items = listOf(
                Item(id = 1, titulo = "iPhone 9", marca = "Apple", precio = 1899.0, imagenUrl = null, stock = 30)
            ),
            encabezados = encabezados,
            etiquetaEstado = { it.name }
        )
        val lineas = csv.trim().lines()
        assertEquals(2, lineas.size)
        assertEquals(encabezados.joinToString(","), lineas[0])
        assertEquals("1,iPhone 9,Apple,,1899.00,0.00,1899.00,30,10,80,1,DISPONIBLE", lineas[1])
    }

    @Test
    fun encierraEntreComillasLosTextosConComas() {
        val csv = generarCsvInventario(
            items = listOf(
                Item(
                    id = 2,
                    titulo = "Galaxy \"S24\", 256 GB",
                    marca = "Samsung",
                    precio = 2999.5,
                    imagenUrl = null,
                    stock = 5,
                    descuento = 10.0
                )
            ),
            encabezados = encabezados,
            etiquetaEstado = { it.name }
        )
        val fila = csv.trim().lines()[1]
        assertEquals(
            "2,\"Galaxy \"\"S24\"\", 256 GB\",Samsung,,2999.50,10.00,2699.55,5,10,80,1,BAJO_MINIMO",
            fila
        )
    }
}
