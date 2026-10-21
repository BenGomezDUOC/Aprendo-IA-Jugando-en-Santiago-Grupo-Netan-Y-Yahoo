
package cl.triskeledu.aprendogame.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cl.triskeledu.aprendogame.model.Notificacion
import kotlinx.coroutines.flow.Flow

/**
 * Gestiona las notificaciones almacenadas en Room.
 *
 * Cada usuario puede consultar únicamente
 * las notificaciones asociadas a su ID.
 */
@Dao
interface NotificacionDao {

    // Guarda una nueva notificación.
    @Insert
    suspend fun registrarNotificacion(
        notificacion: Notificacion
    ): Long

    // Obtiene las notificaciones de un usuario.
    // Las más recientes aparecen primero.
    @Query("""
        SELECT * FROM notificaciones
        WHERE usuarioId = :usuarioId
        ORDER BY fechaTimestamp DESC, id DESC
    """)
    fun getNotificacionesPorUsuario(
        usuarioId: Int
    ): Flow<List<Notificacion>>

    // Cuenta las notificaciones pendientes de lectura.
    @Query("""
        SELECT COUNT(*) FROM notificaciones
        WHERE usuarioId = :usuarioId
        AND leida = 0
    """)
    fun contarNotificacionesNoLeidas(
        usuarioId: Int
    ): Flow<Int>

    // Marca una notificación como leída.
    // Comprueba que pertenezca al usuario.
    @Query("""
        UPDATE notificaciones
        SET leida = 1
        WHERE id = :notificacionId
        AND usuarioId = :usuarioId
    """)
    suspend fun marcarNotificacionLeida(
        notificacionId: Int,
        usuarioId: Int
    ): Int

    // Marca todas las notificaciones del usuario como leídas.
    @Query("""
        UPDATE notificaciones
        SET leida = 1
        WHERE usuarioId = :usuarioId
        AND leida = 0
    """)
    suspend fun marcarTodasComoLeidas(
        usuarioId: Int
    ): Int
}


