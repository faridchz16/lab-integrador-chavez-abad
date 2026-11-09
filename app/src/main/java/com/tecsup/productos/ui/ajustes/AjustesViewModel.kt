package com.tecsup.productos.ui.ajustes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tecsup.productos.AppContainer
import com.tecsup.productos.data.preferencias.PreferenciasRepository
import com.tecsup.productos.domain.model.ModoTema
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AjustesViewModel(
    private val preferencias: PreferenciasRepository
) : ViewModel() {

    val modoTema: StateFlow<ModoTema?> = preferencias.modoTema.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    fun cambiarModoTema(modo: ModoTema) {
        viewModelScope.launch { preferencias.guardarModoTema(modo) }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { AjustesViewModel(container.preferenciasRepository) }
        }
    }
}
