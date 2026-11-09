package com.tecsup.productos.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.tecsup.productos.MainActivity
import com.tecsup.productos.R

class NotificadorAndroid(
    private val context: Context
) : Notificador {

    init {
        crearCanales()
    }

    private fun crearCanales() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val administrador = context.getSystemService(NotificationManager::class.java)
            administrador.createNotificationChannel(
                NotificationChannel(
                    CANAL_SINCRONIZACION,
                    context.getString(R.string.notificacion_canal_nombre),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = context.getString(R.string.notificacion_canal_descripcion)
                }
            )
            administrador.createNotificationChannel(
                NotificationChannel(
                    CANAL_ALERTAS,
                    context.getString(R.string.alertas_canal_nombre),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = context.getString(R.string.alertas_canal_descripcion)
                }
            )
        }
    }

    private fun tienePermiso(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

    override fun mostrar(titulo: String, texto: String) {
        publicar(CANAL_SINCRONIZACION, NOTIFICACION_SINCRONIZACION, titulo, texto)
    }

    override fun mostrarAlertaStock(titulo: String, texto: String) {
        publicar(CANAL_ALERTAS, NOTIFICACION_ALERTA_STOCK, titulo, texto)
    }

    @SuppressLint("MissingPermission")
    private fun publicar(canal: String, id: Int, titulo: String, texto: String) {
        if (!tienePermiso()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, id, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificacion = NotificationCompat.Builder(context, canal)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setPriority(
                if (canal == CANAL_ALERTAS) {
                    NotificationCompat.PRIORITY_HIGH
                } else {
                    NotificationCompat.PRIORITY_DEFAULT
                }
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(id, notificacion)
    }

    private companion object {
        const val CANAL_SINCRONIZACION = "sincronizacion"
        const val CANAL_ALERTAS = "alertas_stock"
        const val NOTIFICACION_SINCRONIZACION = 1
        const val NOTIFICACION_ALERTA_STOCK = 2
    }
}
