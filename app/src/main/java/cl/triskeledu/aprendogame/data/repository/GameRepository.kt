
package cl.triskeledu.aprendogame.data.repository

import cl.triskeledu.aprendogame.model.Capsula
import cl.triskeledu.aprendogame.model.PuntoInteres
import cl.triskeledu.aprendogame.model.Usuario
import cl.triskeledu.aprendogame.model.VisitaRealizada
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del Repositorio principal.
 *
 * Abstrae la persistencia local en Room y provee los métodos
 * que consumen los ViewModels de:
 *
 * - Fernando: GPS y exploración.
 * - Mary: QR, cámara y evidencias.
 * - Juan Pablo: multimedia y gamificación.
 *
 * Las operaciones relacionadas con el progreso reciben
 * el ID del jugador para evitar mezclar sus datos.
 */
interface GameRepository {

    // --------------------------------------------------
    // PUNTOS TURÍSTICOS DE SANTIAGO
    // Mapa y exploración - Fernando
    // --------------------------------------------------

    fun getPuntos(): Flow<List<PuntoInteres>>

    suspend fun getPuntoPorId(id: Int): PuntoInteres?

    // Marca un punto como completado para un jugador.
    suspend fun marcarPuntoCompletado(
        id: Int,
        puntosRecompensa: Int = 0,
        usuarioId: Int
    )

    // --------------------------------------------------
    // ESCÁNER QR Y VALIDACIÓN
    // Cámara y evidencias - Mary
    // --------------------------------------------------

    suspend fun validarQr(codigoQr: String): Capsula?

    // Registra la visita asociándola al jugador autenticado.
    suspend fun registrarVisitaConEvidencia(
        puntoId: Int,
        capsulaId: Int?,
        rutaFotoSelfie: String?,
        puntosGanados: Int,
        usuarioId: Int
    ): Long

    // --------------------------------------------------
    // MULTIMEDIA Y CONTENIDOS IA
    // ExoPlayer - Juan Pablo
    // --------------------------------------------------

    suspend fun getCapsulaPorPunto(puntoId: Int): Capsula?

    fun getTodasLasCapsulas(): Flow<List<Capsula>>

    // --------------------------------------------------
    // GAMIFICACIÓN Y PERFIL
    // Ranking y puntajes - Juan Pablo
    // --------------------------------------------------

    // Obtiene los datos de un usuario específico.
    fun getUsuario(id: Int): Flow<Usuario?>

    // Suma puntos únicamente al jugador indicado.
    suspend fun sumarPuntos(
        puntos: Int,
        usuarioId: Int
    )

    // Obtiene las visitas de un jugador específico.
    fun getHistorialVisitas(
        usuarioId: Int
    ): Flow<List<VisitaRealizada>>

    // --------------------------------------------------
    // GESTIÓN DE CONTENIDOS
    // Administrador y generador de contenido
    // --------------------------------------------------

    suspend fun crearPunto(punto: PuntoInteres): Long

    suspend fun crearCapsula(capsula: Capsula): Long
}

