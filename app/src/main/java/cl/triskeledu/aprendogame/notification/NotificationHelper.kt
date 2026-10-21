package cl.triskeledu.aprendogame.notification

import android.Manifest
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
import cl.triskeledu.aprendogame.MainActivity
import cl.triskeledu.aprendogame.R

/**
 * Gestor de Notificaciones Nativas del sistema Android.
 * Permite cumplir con el acceso seguro a recursos nativos del dispositivo (IE 2.4.1),
 * creando canales de notificación para Android 8+ y verificando permisos en Android 13+.
 */
object NotificationHelper {

    const val CANAL_ID = "triskeledu_avisos_canal"
    private const val CANAL_NOMBRE = "Avisos de Aprendo IA"
    private const val CANAL_DESCRIPCION = "Notificaciones sobre logros, puntos y actividades de Triskeledu"

    /**
     * Crea el canal de notificación requerido a partir de Android 8.0 (API 26).
     */
    fun crearCanalNotificaciones(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL_ID,
                CANAL_NOMBRE,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CANAL_DESCRIPCION
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(canal)
        }
    }

    /**
     * Muestra una notificación emergente nativa en la barra de estado del sistema.
     */
    fun mostrarNotificacion(
        context: Context,
        id: Int = System.currentTimeMillis().toInt(),
        titulo: String,
        mensaje: String,
        rutaDestino: String? = null
    ) {
        // Asegurar que el canal exista
        crearCanalNotificaciones(context)

        // En Android 13+ (API 33), verificar permiso POST_NOTIFICATIONS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val tienePermiso = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!tienePermiso) {
                // Si el permiso no está otorgado en el sistema, no intentar publicar para evitar SecurityException
                return
            }
        }

        // Intent para abrir la aplicación al presionar la notificación
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            rutaDestino?.let { putExtra("ruta_destino", it) }
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(context, id, intent, flags)

        val builder = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(id, builder.build())
        } catch (_: SecurityException) {
            // Manejo de seguridad en caso de revocación repentina de permisos
        }
    }
}
