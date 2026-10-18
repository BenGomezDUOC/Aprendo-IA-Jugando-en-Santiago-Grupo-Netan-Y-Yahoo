package cl.triskeledu.aprendogame.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import cl.triskeledu.aprendogame.model.VideoVisto
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoVistoDao {

    // 1. REGISTRAR UN VIDEO VISTO

    // Guarda que un jugador visualizó un video.
    // Si ya existe ese registro, no lo duplica.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun registrarVideoVisto(
        videoVisto: VideoVisto
    ): Long


    // 2. CONTAR LOS VIDEOS VISTOS

    // Cuenta cuántos videos diferentes ha visto
    // un jugador específico.
    @Query(
        """
        SELECT COUNT(*)
        FROM videos_vistos
        WHERE usuarioId = :usuarioId
        """
    )
    fun contarVideosVistos(
        usuarioId: Int
    ): Flow<Int>


    // 3. OBTENER LOS VIDEOS VISTOS

    // Recupera los videos vistos por el jugador,
    // ordenados desde el más reciente.
    @Query(
        """
        SELECT * FROM videos_vistos
        WHERE usuarioId = :usuarioId
        ORDER BY fechaTimestamp DESC
        """
    )
    fun getVideosVistosPorUsuario(
        usuarioId: Int
    ): Flow<List<VideoVisto>>
}