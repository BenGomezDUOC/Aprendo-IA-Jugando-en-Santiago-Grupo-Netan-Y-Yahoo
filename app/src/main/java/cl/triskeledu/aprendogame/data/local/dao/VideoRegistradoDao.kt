
package cl.triskeledu.aprendogame.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.triskeledu.aprendogame.model.VideoRegistrado
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoRegistradoDao {

    @Query(
        "SELECT * FROM videos_registrados ORDER BY id DESC"
    )
    fun obtenerTodos(): Flow<List<VideoRegistrado>>

    @Query(
        """
        SELECT * FROM videos_registrados
        WHERE contenidoId = :contenidoId
        ORDER BY id DESC
        """
    )
    fun obtenerPorContenido(
        contenidoId: Int
    ): Flow<List<VideoRegistrado>>

    @Query(
        "SELECT * FROM videos_registrados WHERE id = :id LIMIT 1"
    )
    suspend fun obtenerPorId(id: Int): VideoRegistrado?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun registrar(video: VideoRegistrado): Long

    // Actualiza el video y sus datos informativos.
    @Query("""
    UPDATE videos_registrados
    SET titulo = :titulo,
        descripcion = :descripcion,
        urlVideo = :urlVideo,
        contenidoId = :contenidoId,
        autor = :autor,
        duracion = :duracion,
        urlMiniatura = :urlMiniatura
    WHERE id = :id
""")
    suspend fun actualizar(
        id: Int,
        titulo: String,
        descripcion: String,
        urlVideo: String,
        contenidoId: Int?,
        autor: String,
        duracion: Long,
        urlMiniatura: String
    )
}



