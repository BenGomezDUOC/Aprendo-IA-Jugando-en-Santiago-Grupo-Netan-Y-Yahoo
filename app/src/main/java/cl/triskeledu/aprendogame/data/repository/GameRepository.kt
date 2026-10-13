package cl.triskeledu.aprendogame.data.repository

import cl.triskeledu.aprendogame.model.Capsula
import cl.triskeledu.aprendogame.model.PuntoInteres
import cl.triskeledu.aprendogame.model.Usuario
import cl.triskeledu.aprendogame.model.VisitaRealizada
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del Repositorio principal.
 * Abstrae la persistencia local en Room y provee los métodos que consumen los ViewModels
 * de Fernando (GPS), Mary (QR/Selfie) y Juan Pablo (Video/Gamificación).
 */
interface GameRepository {
    // Puntos turísticos de Santiago (Mapa y Exploración - Fernando)
    fun getPuntos(): Flow<List<PuntoInteres>>
    suspend fun getPuntoPorId(id: Int): PuntoInteres?
    suspend fun marcarPuntoCompletado(id: Int, puntosRecompensa: Int = 0)

    // Escáner QR y Validación (Cámara y Evidencias - Mary)
    suspend fun validarQr(codigoQr: String): Capsula?
    suspend fun registrarVisitaConEvidencia(
        puntoId: Int,
        capsulaId: Int?,
        rutaFotoSelfie: String?,
        puntosGanados: Int
    ): Long

    // Multimedia y Contenidos IA (ExoPlayer - Juan Pablo)
    suspend fun getCapsulaPorPunto(puntoId: Int): Capsula?
    fun getTodasLasCapsulas(): Flow<List<Capsula>>

    // Gamificación y Perfil (Ranking y Puntajes - Juan Pablo)
    fun getUsuario(id: Int = 1): Flow<Usuario?>
    suspend fun sumarPuntos(puntos: Int)
    fun getHistorialVisitas(): Flow<List<VisitaRealizada>>
}
