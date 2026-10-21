package cl.triskeledu.aprendogame.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cl.triskeledu.aprendogame.model.PuntoInteres
import kotlinx.coroutines.flow.Flow

@Dao
interface PuntoInteresDao {
    @Query("SELECT * FROM puntos_interes ORDER BY id ASC")
    fun getPuntos(): Flow<List<PuntoInteres>>

    @Query("SELECT * FROM puntos_interes WHERE id = :id LIMIT 1")
    suspend fun getPuntoPorId(id: Int): PuntoInteres?

    @Query("UPDATE puntos_interes SET completado = :completado WHERE id = :id")
    suspend fun actualizarEstado(id: Int, completado: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPuntos(puntos: List<PuntoInteres>)

    // Inserta lugares iniciales solo si todavía no existen.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarPuntosIniciales(puntos: List<PuntoInteres>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPunto(punto: PuntoInteres): Long

    @Update
    suspend fun actualizar(punto: PuntoInteres)

    // Registra un nuevo punto turístico sin reemplazar otro existente.
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun crearPunto(punto: PuntoInteres): Long
}
