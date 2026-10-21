
package cl.triskeledu.aprendogame.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import cl.triskeledu.aprendogame.model.VideoAdicionalVisto

import kotlinx.coroutines.flow.Flow

@Dao
interface VideoAdicionalVistoDao {

    // Guarda un video adicional como visto.
    // Si ya existe para ese usuario, no lo duplica.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun registrarVideoVisto(
        video: VideoAdicionalVisto
    )

    // Consulta los videos adicionales vistos
    // por un usuario específico.
    @Query(
        """
        SELECT *
        FROM videos_adicionales_vistos
        WHERE usuarioId = :usuarioId
        ORDER BY fechaTimestamp DESC
        """
    )
    fun obtenerVideosVistos(
        usuarioId: Int
    ): Flow<List<VideoAdicionalVisto>>

    // Comprueba si el usuario ya vio un video.
    @Query(
        """
        SELECT COUNT(*)
        FROM videos_adicionales_vistos
        WHERE usuarioId = :usuarioId
          AND videoAdicionalId = :videoAdicionalId
        """
    )
    suspend fun contarVideoVisto(
        usuarioId: Int,
        videoAdicionalId: Int
    ): Int
}


