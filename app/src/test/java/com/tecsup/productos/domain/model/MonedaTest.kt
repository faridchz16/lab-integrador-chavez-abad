package com.tecsup.productos.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class MonedaTest {

    @Test
    fun convierteDolaresASolesConDosDecimales() {
        assertEquals(346.0, 100.0.dolaresASoles(), 0.0001)
        assertEquals(34.57, 9.99.dolaresASoles(), 0.0001)
    }
}
