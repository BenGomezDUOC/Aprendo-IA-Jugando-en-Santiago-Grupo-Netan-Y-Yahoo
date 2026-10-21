
package cl.triskeledu.aprendogame.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.triskeledu.aprendogame.model.ContenidoEducativo
import kotlinx.coroutines.flow.Flow

@Dao
interface ContenidoEducativoDao {

    // Obtener todos los contenidos del Generador.
    @Query(
        "SELECT * FROM contenidos_educativos ORDER BY id DESC"
    )
    fun obtenerTodos(): Flow<List<ContenidoEducativo>>

    // Obtener únicamente los contenidos publicados.
    @Query(
        """
        SELECT * FROM contenidos_educativos
        WHERE estado = 'PUBLICADA'
        ORDER BY id DESC
        """
    )
    fun obtenerPublicados(): Flow<List<ContenidoEducativo>>

    // Buscar un contenido por su identificador.
    @Query(
        "SELECT * FROM contenidos_educativos WHERE id = :id LIMIT 1"
    )
    suspend fun obtenerPorId(id: Int): ContenidoEducativo?

    // Crear un contenido educativo nuevo.
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun crear(contenido: ContenidoEducativo): Long

    // Modificar título, descripción, categoría y punto.
    @Query(
        """
        UPDATE contenidos_educativos
        SET titulo = :titulo,
            descripcion = :descripcion,
            categoria = :categoria,
            puntoId = :puntoId
        WHERE id = :id
        """
    )
    suspend fun actualizar(
        id: Int,
        titulo: String,
        descripcion: String,
        categoria: String,
        puntoId: Int?
    )

    // Publicar o devolver a borrador.
    @Query(
        """
        UPDATE contenidos_educativos
        SET estado = :estado
        WHERE id = :id
        """
    )
    suspend fun cambiarEstado(
        id: Int,
        estado: String
    )

    // Asignar un QR a una cápsula existente del Generador.
    // No crea otra cápsula ni modifica los QR originales.
    @Query(
        """
        UPDATE contenidos_educativos
        SET codigoQr = :codigoQr
        WHERE id = :contenidoId
          AND puntoId IS NOT NULL
          AND codigoQr IS NULL
        """
    )
    suspend fun asignarQr(
        contenidoId: Int,
        codigoQr: String
    ): Int
}



