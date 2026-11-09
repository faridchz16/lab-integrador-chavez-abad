package com.tecsup.productos.domain.inventario

import com.tecsup.productos.domain.model.Item
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AlertasTest {

    private fun producto(id: Int, stock: Int, minimo: Int = 10, maximo: Int = 80) = Item(
        id = id,
        titulo = "Producto $id",
        marca = "Marca",
        precio = 100.0,
        imagenUrl = null,
        stock = stock,
        stockMinimo = minimo,
        stockMaximo = maximo
    )

    @Test
    fun clasificaCadaNivelDeStock() {
        assertEquals(NivelAlerta.AGOTADO, producto(1, stock = 0).nivelAlerta())
        assertEquals(NivelAlerta.BAJO_MINIMO, producto(2, stock = 5).nivelAlerta())
        assertEquals(NivelAlerta.CERCA_MINIMO, producto(3, stock = 12).nivelAlerta())
        assertNull(producto(4, stock = 50).nivelAlerta())
        assertEquals(NivelAlerta.SOBRE_MAXIMO, producto(5, stock = 90).nivelAlerta())
    }

    @Test
    fun ordenaPrimeroLosAgotados() {
        val alertas = listOf(producto(1, 12), producto(2, 0), producto(3, 5)).alertas()
        assertEquals(listOf(2, 3, 1), alertas.map { it.producto.id })
    }

    @Test
    fun sugiereReponerHastaElMaximo() {
        val alerta = listOf(producto(1, stock = 5, maximo = 80)).alertas().single()
        assertEquals(75, alerta.unidadesSugeridas)
    }

    @Test
    fun sobreMaximoNoSeCuentaParaReponer() {
        val items = listOf(producto(1, 0), producto(2, 90), producto(3, 50))
        assertEquals(1, items.cantidadPorReponer())
    }

    @Test
    fun avisaCuandoUnProductoBajaDelMinimo() {
        val nuevas = nuevasAlertasCriticas(emptyMap(), listOf(producto(1, 5), producto(2, 50)))
        assertEquals(listOf(1), nuevas.map { it.producto.id })
    }

    @Test
    fun avisaCuandoUnProductoSeAgota() {
        val anteriores = mapOf(1 to NivelAlerta.BAJO_MINIMO)
        val nuevas = nuevasAlertasCriticas(anteriores, listOf(producto(1, 0)))
        assertEquals(NivelAlerta.AGOTADO, nuevas.single().nivel)
    }

    @Test
    fun noRepiteAvisosDelMismoNivel() {
        val anteriores = mapOf(1 to NivelAlerta.BAJO_MINIMO)
        assertTrue(nuevasAlertasCriticas(anteriores, listOf(producto(1, 4))).isEmpty())
    }

    @Test
    fun noAvisaSiSeReponeParcialmenteUnAgotado() {
        val anteriores = mapOf(1 to NivelAlerta.AGOTADO)
        assertTrue(nuevasAlertasCriticas(anteriores, listOf(producto(1, 3))).isEmpty())
    }
}
