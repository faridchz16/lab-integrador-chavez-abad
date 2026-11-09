package com.tecsup.productos.data.exportacion

interface ExportadorArchivos {
    suspend fun escribirTexto(destino: String, contenido: String)
}
