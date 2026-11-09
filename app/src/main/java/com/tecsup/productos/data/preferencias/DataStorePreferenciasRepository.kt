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

    override val modoTema: Flow<ModoTema> = context.almacenPreferencias.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferencias ->
            val guardado = preferencias[CLAVE_MODO_TEMA]
            ModoTema.entries.firstOrNull { it.name == guardado } ?: ModoTema.SISTEMA
        }

    override suspend fun guardarModoTema(modo: ModoTema) {
        context.almacenPreferencias.edit { preferencias ->
            preferencias[CLAVE_MODO_TEMA] = modo.name
        }
    }

    private companion object {
        val CLAVE_MODO_TEMA = stringPreferencesKey("modo_tema")
    }
}
