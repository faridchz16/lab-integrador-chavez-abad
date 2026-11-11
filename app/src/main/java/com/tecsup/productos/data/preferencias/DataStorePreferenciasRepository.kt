package com.tecsup.productos.data.preferencias

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tecsup.productos.domain.model.ModoTema
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.almacenPreferencias: DataStore<Preferences> by preferencesDataStore(
    name = "preferencias_stockify"
)

class DataStorePreferenciasRepository(
    private val context: Context
) : PreferenciasRepository {

    private val preferencias: Flow<Preferences> = context.almacenPreferencias.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }

    override val modoTema: Flow<ModoTema> = preferencias.map { valores ->
        val guardado = valores[CLAVE_MODO_TEMA]
        ModoTema.entries.firstOrNull { it.name == guardado } ?: ModoTema.CLARO
    }

    override val historialBusqueda: Flow<List<String>> = preferencias.map { valores ->
        valores[CLAVE_HISTORIAL].aLista()
    }

    override suspend fun guardarModoTema(modo: ModoTema) {
        context.almacenPreferencias.edit { valores ->
            valores[CLAVE_MODO_TEMA] = modo.name
        }
    }

    override suspend fun agregarBusqueda(consulta: String) {
        val limpia = consulta.trim()
        if (limpia.isEmpty()) return
        context.almacenPreferencias.edit { valores ->
            val actual = valores[CLAVE_HISTORIAL].aLista()
            val nuevo = (listOf(limpia) + actual.filterNot { it.equals(limpia, ignoreCase = true) })
                .take(MAXIMO_HISTORIAL)
            valores[CLAVE_HISTORIAL] = nuevo.joinToString(SEPARADOR)
        }
    }

    override suspend fun eliminarBusqueda(consulta: String) {
        context.almacenPreferencias.edit { valores ->
            val nuevo = valores[CLAVE_HISTORIAL].aLista().filterNot { it == consulta }
            valores[CLAVE_HISTORIAL] = nuevo.joinToString(SEPARADOR)
        }
    }

    override suspend fun limpiarHistorial() {
        context.almacenPreferencias.edit { valores -> valores.remove(CLAVE_HISTORIAL) }
    }

    private fun String?.aLista(): List<String> =
        this?.split(SEPARADOR)?.filter { it.isNotBlank() }.orEmpty()

    private companion object {
        val CLAVE_MODO_TEMA = stringPreferencesKey("modo_tema")
        val CLAVE_HISTORIAL = stringPreferencesKey("historial_busqueda")
        const val SEPARADOR = "\u001F"
        const val MAXIMO_HISTORIAL = 8
    }
}
