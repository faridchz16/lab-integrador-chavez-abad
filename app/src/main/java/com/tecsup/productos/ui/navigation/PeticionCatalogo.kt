package com.tecsup.productos.ui.navigation

import androidx.lifecycle.ViewModel
import com.tecsup.productos.domain.model.TipoProducto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface PeticionCatalogo {
    data class Categoria(val id: Long) : PeticionCatalogo
    data class Tipo(val tipo: TipoProducto) : PeticionCatalogo
    data object Todo : PeticionCatalogo
}

class PeticionCatalogoViewModel : ViewModel() {

    private val _peticion = MutableStateFlow<PeticionCatalogo?>(null)
    val peticion: StateFlow<PeticionCatalogo?> = _peticion.asStateFlow()

    fun pedir(peticion: PeticionCatalogo) {
        _peticion.value = peticion
    }

    fun atendida() {
        _peticion.value = null
    }
}
