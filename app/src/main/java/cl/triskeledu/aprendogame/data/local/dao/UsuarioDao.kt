package cl.triskeledu.aprendogame.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.triskeledu.aprendogame.model.Usuario
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    fun getUsuario(id: Int = 1): Flow<Usuario?>

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun getUsuarioDirecto(id: Int = 1): Usuario?

    @Query("UPDATE usuarios SET puntosAcumulados = puntosAcumulados + :puntos WHERE id = :id")
    suspend fun sumarPuntos(id: Int = 1, puntos: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarOActualizar(usuario: Usuario)
}
