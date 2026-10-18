package cl.triskeledu.aprendogame.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.triskeledu.aprendogame.model.DescubrimientoQr
import kotlinx.coroutines.flow.Flow

@Dao
interface DescubrimientoQrDao {

    // Registra un QR descubierto.
    // Si el jugador ya descubrió esa cápsula,
    // no crea otro registro.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun registrarDescubrimiento(
        descubrimiento: DescubrimientoQr
    ): Long

    // Cuenta los QR diferentes descubiertos
    // por un jugador específico.
    @Query(
        """
        SELECT COUNT(*)
        FROM descubrimientos_qr
        WHERE usuarioId = :usuarioId
        """
    )
    fun contarQrDescubiertos(
        usuarioId: Int
    ): Flow<Int>

    // Recupera los descubrimientos del jugador,
    // comenzando por el más reciente.
    @Query(
        """
        SELECT * FROM descubrimientos_qr
        WHERE usuarioId = :usuarioId
        ORDER BY fechaTimestamp DESC
        """
    )
    fun getDescubrimientosPorUsuario(
        usuarioId: Int
    ): Flow<List<DescubrimientoQr>>
}