
package cl.triskeledu.aprendogame.data.local.dao

// Anotaciones necesarias para trabajar con Room.
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// Modelo que representa una visita realizada.
import cl.triskeledu.aprendogame.model.VisitaRealizada

// Permite observar los cambios de la base de datos.
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitaDao {

    // --------------------------------------------------
    // 1. REGISTRAR UNA VISITA
    // --------------------------------------------------

    // Guarda una visita en la base de datos.
    // Devuelve el ID de la visita registrada.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun registrarVisita(
        visita: VisitaRealizada
    ): Long


    // --------------------------------------------------
    // 2. OBTENER TODAS LAS VISITAS
    // FUNCIÓN ORIGINAL DE BENJAMÍN
    // --------------------------------------------------

    // Recupera todas las visitas sin distinguir jugadores.
    // La conservamos para no romper el código existente.
    @Query(
        "SELECT * FROM visitas_realizadas ORDER BY fechaTimestamp DESC"
    )
    fun getVisitas(): Flow<List<VisitaRealizada>>


    // --------------------------------------------------
    // 3. BUSCAR UNA VISITA POR PUNTO
    // FUNCIÓN ORIGINAL DE BENJAMÍN
    // --------------------------------------------------

    // Busca una visita de un punto turístico.
    // No distingue a qué jugador pertenece.
    @Query(
        "SELECT * FROM visitas_realizadas WHERE puntoId = :puntoId LIMIT 1"
    )
    suspend fun getVisitaPorPunto(
        puntoId: Int
    ): VisitaRealizada?


    // --------------------------------------------------
    // 4. OBTENER VISITAS DE UN JUGADOR
    // NUEVA FUNCIÓN
    // --------------------------------------------------

    // Recupera solamente las visitas del jugador indicado.
    // Las ordena desde la más reciente hasta la más antigua.
    @Query(
        """
        SELECT * FROM visitas_realizadas
        WHERE usuarioId = :usuarioId
        ORDER BY fechaTimestamp DESC
        """
    )
    fun getVisitasPorUsuario(
        usuarioId: Int
    ): Flow<List<VisitaRealizada>>


    // --------------------------------------------------
    // 5. BUSCAR UN PUNTO VISITADO POR UN JUGADOR
    // NUEVA FUNCIÓN
    // --------------------------------------------------

    // Comprueba si un jugador específico
    // ya tiene registrada una visita a ese punto.
    @Query(
        """
        SELECT * FROM visitas_realizadas
        WHERE puntoId = :puntoId
        AND usuarioId = :usuarioId
        LIMIT 1
        """
    )
    suspend fun getVisitaPorPuntoYUsuario(
        puntoId: Int,
        usuarioId: Int
    ): VisitaRealizada?


    // --------------------------------------------------
    // 6. CONTAR LOS QR DESCUBIERTOS POR JUGADOR
    // NUEVA FUNCIÓN
    // --------------------------------------------------

    // Cuenta las cápsulas distintas asociadas
    // a las visitas completadas del jugador.
    // No cuenta dos veces el mismo QR.
    @Query(
        """
        SELECT COUNT(DISTINCT capsulaId)
        FROM visitas_realizadas
        WHERE usuarioId = :usuarioId
        AND completada = 1
        AND capsulaId IS NOT NULL
        """
    )
    fun contarQrDescubiertos(
        usuarioId: Int
    ): Flow<Int>
    // Opción B: una selfie bonificada por jugador y por código QR.
    @Query("""
        SELECT * FROM visitas_realizadas
        WHERE capsulaId = :capsulaId AND usuarioId = :usuarioId
        AND completada = 1
        LIMIT 1
    """)
    suspend fun getVisitaPorCapsulaYUsuario(
        capsulaId: Int,
        usuarioId: Int
    ): VisitaRealizada?
}