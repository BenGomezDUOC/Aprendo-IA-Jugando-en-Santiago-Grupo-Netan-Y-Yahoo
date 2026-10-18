package cl.triskeledu.aprendogame.data.repository

import cl.triskeledu.aprendogame.model.Capsula
import cl.triskeledu.aprendogame.model.DescubrimientoQr
import cl.triskeledu.aprendogame.model.PuntoInteres
import cl.triskeledu.aprendogame.model.Usuario
import cl.triskeledu.aprendogame.model.VideoVisto
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

    suspend fun registrarVisitaConEvidencia(
        puntoId: Int,
        capsulaId: Int?,
        rutaFotoSelfie: String?,
        puntosGanados: Int,
        usuarioId: Int
    ): Long

    // --------------------------------------------------
    // DESCUBRIMIENTOS QR
    // Registro individual por jugador
    // --------------------------------------------------

    // Registra el descubrimiento de una cápsula.
    // Devuelve el ID creado o -1 si ya existía.
    suspend fun registrarDescubrimientoQr(
        descubrimiento: DescubrimientoQr
    ): Long

    // Cuenta los QR descubiertos por un jugador.
    fun contarQrDescubiertos(
        usuarioId: Int
    ): Flow<Int>

    // Obtiene los QR descubiertos por un jugador.
    fun getDescubrimientosPorUsuario(
        usuarioId: Int
    ): Flow<List<DescubrimientoQr>>

    // --------------------------------------------------
    // MULTIMEDIA Y CONTENIDOS IA
    // ExoPlayer - Juan Pablo
    // --------------------------------------------------

    suspend fun getCapsulaPorPunto(puntoId: Int): Capsula?

    fun getTodasLasCapsulas(): Flow<List<Capsula>>

    // --------------------------------------------------
    // VIDEOS VISTOS
    // Registro individual por jugador
    // --------------------------------------------------

    // Registra un video visto, sin duplicarlo.
    suspend fun registrarVideoVisto(
        videoVisto: VideoVisto
    ): Long

    // Cuenta los videos vistos por un jugador.
    fun contarVideosVistos(
        usuarioId: Int
    ): Flow<Int>

    // Obtiene los videos vistos por un jugador.
    fun getVideosVistosPorUsuario(
        usuarioId: Int
    ): Flow<List<VideoVisto>>

    // --------------------------------------------------
    // GAMIFICACIÓN Y PERFIL
    // Ranking y puntajes - Juan Pablo
    // --------------------------------------------------

    fun getUsuario(id: Int): Flow<Usuario?>

    suspend fun sumarPuntos(
        puntos: Int,
        usuarioId: Int
    )

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

