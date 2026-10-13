package cl.triskeledu.aprendogame.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.triskeledu.aprendogame.model.VisitaRealizada
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun registrarVisita(visita: VisitaRealizada): Long

    @Query("SELECT * FROM visitas_realizadas ORDER BY fechaTimestamp DESC")
    fun getVisitas(): Flow<List<VisitaRealizada>>

    @Query("SELECT * FROM visitas_realizadas WHERE puntoId = :puntoId LIMIT 1")
    suspend fun getVisitaPorPunto(puntoId: Int): VisitaRealizada?
}
