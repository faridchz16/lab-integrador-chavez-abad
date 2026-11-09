package com.tecsup.productos.domain.inventario

import com.tecsup.productos.domain.model.Categoria
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidadorCategoriaTest {

    private val existentes = listOf(
        Categoria(id = 1, nombre = "Electrónica"),
        Categoria(id = 2, nombre = "Samsung")
    )

    @Test
    fun nombreVacioEsObligatorio() {
        assertEquals(ErrorNombreCategoria.OBLIGATORIO, ValidadorCategoria.validar("  ", existentes))
    }

    @Test
    fun nombreDeUnaLetraEsCorto() {
        assertEquals(ErrorNombreCategoria.CORTO, ValidadorCategoria.validar("A", existentes))
    }

    @Test
    fun nombreRepetidoSinTildesNiMayusculasEsDuplicado() {
        assertEquals(ErrorNombreCategoria.DUPLICADO, ValidadorCategoria.validar("electronica", existentes))
    }

    @Test
    fun renombrarConSuMismoNombreEsValido() {
        assertNull(ValidadorCategoria.validar("Samsung", existentes, idEditado = 2))
    }

    @Test
    fun nombreNuevoEsValido() {
        assertNull(ValidadorCategoria.validar("Xiaomi", existentes))
    }
}
