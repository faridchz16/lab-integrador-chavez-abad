package com.tecsup.productos.data.exportacion

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class ExportadorArchivosAndroid(
    private val context: Context
) : ExportadorArchivos {

    override suspend fun escribirTexto(destino: String, contenido: String) {
        withContext(Dispatchers.IO) {
            val salida = context.contentResolver.openOutputStream(Uri.parse(destino), MODO_ESCRITURA)
                ?: throw IOException()
            salida.use { flujo ->
                flujo.write(MARCA_UTF8)
                flujo.write(contenido.toByteArray(Charsets.UTF_8))
            }
        }
    }

    private companion object {
        const val MODO_ESCRITURA = "wt"
        val MARCA_UTF8 = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
    }
}
