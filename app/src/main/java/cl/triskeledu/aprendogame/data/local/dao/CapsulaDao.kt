package cl.triskeledu.aprendogame.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.triskeledu.aprendogame.model.Capsula
import kotlinx.coroutines.flow.Flow

@Dao
interface CapsulaDao {
    @Query("SELECT * FROM capsulas WHERE codigoQr = :codigoQr LIMIT 1")
    suspend fun getCapsulaPorQr(codigoQr: String): Capsula?

    @Query("SELECT * FROM capsulas WHERE puntoId = :puntoId LIMIT 1")
    suspend fun getCapsulaPorPuntoId(puntoId: Int): Capsula?

    @Query("SELECT * FROM capsulas ORDER BY id ASC")
    fun getTodasLasCapsulas(): Flow<List<Capsula>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCapsulas(capsulas: List<Capsula>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCapsula(capsula: Capsula): Long
}
