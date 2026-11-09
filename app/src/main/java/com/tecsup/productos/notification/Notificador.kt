package com.tecsup.productos.notification

interface Notificador {
    fun mostrar(titulo: String, texto: String)
    fun mostrarAlertaStock(titulo: String, texto: String)
}