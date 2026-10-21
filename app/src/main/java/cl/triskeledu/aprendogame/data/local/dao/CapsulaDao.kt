
package cl.triskeledu.aprendogame.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.triskeledu.aprendogame.model.Capsula
import kotlinx.coroutines.flow.Flow


@Dao
interface CapsulaDao {

    // Busca una cápsula cuando el Cliente escanea su QR.
    @Query("SELECT * FROM capsulas WHERE codigoQr = :codigoQr LIMIT 1")
    suspend fun getCapsulaPorQr(codigoQr: String): Capsula?

    // Busca una cápsula asociada a un punto turístico.
    @Query("SELECT * FROM capsulas WHERE puntoId = :puntoId LIMIT 1")
    suspend fun getCapsulaPorPuntoId(puntoId: Int): Capsula?

    // Obtiene todas las cápsulas.
    @Query("SELECT * FROM capsulas ORDER BY id ASC")
    fun getTodasLasCapsulas(): Flow<List<Capsula>>

    // Conservamos las funciones existentes.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCapsulas(capsulas: List<Capsula>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarCapsulasIniciales(capsulas: List<Capsula>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCapsula(capsula: Capsula): Long

    // NUEVO: registra una cápsula sin reemplazar otra.
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun registrarNuevaCapsula(capsula: Capsula): Long

    // NUEVO: comprueba si un QR ya está asignado.
    @Query("""
        SELECT COUNT(*)
        FROM capsulas
        WHERE codigoQr = :codigoQr
    """)
    suspend fun contarCodigoQr(codigoQr: String): Int

    // NUEVO: permite actualizar la clasificación.
    @Query("""
        UPDATE capsulas
        SET categoria = :categoria,
            estado = :estado
        WHERE id = :id
    """)
    suspend fun actualizarClasificacion(
        id: Int,
        categoria: String,
        estado: String
    )

    // Conserva el ID y los registros asociados.
    @Query("""
        UPDATE capsulas
        SET titulo = :titulo,
            descripcion = :descripcion,
            urlVideo = :urlVideo,
            puntos = :puntos
        WHERE id = :id
    """)
    suspend fun actualizarContenidoCapsula(
        id: Int,
        titulo: String,
        descripcion: String,
        urlVideo: String,
        puntos: Int
    )
}

