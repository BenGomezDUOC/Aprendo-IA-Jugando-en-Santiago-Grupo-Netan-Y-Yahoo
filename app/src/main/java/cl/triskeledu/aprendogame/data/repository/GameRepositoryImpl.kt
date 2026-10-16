
package cl.triskeledu.aprendogame.data.repository

import cl.triskeledu.aprendogame.data.local.AppDatabase
import cl.triskeledu.aprendogame.model.Capsula
import cl.triskeledu.aprendogame.model.PuntoInteres
import cl.triskeledu.aprendogame.model.Usuario
import cl.triskeledu.aprendogame.model.VisitaRealizada
import kotlinx.coroutines.flow.Flow

/**
 * Implementación concreta del GameRepository
 * conectada a AppDatabase mediante Room.
 *
 * Los puntos, visitas y consultas de historial
 * se asocian al usuario que inició sesión.
 *
 * Se conservan las funciones existentes de
 * exploración, QR y gestión de contenidos.
 */
class GameRepositoryImpl(
    private val database: AppDatabase
) : GameRepository {

    // DAO para los puntos turísticos.
    private val puntoDao = database.puntoInteresDao()

    // DAO para las cápsulas educativas y códigos QR.
    private val capsulaDao = database.capsulaDao()

    // DAO para los usuarios y sus puntos.
    private val usuarioDao = database.usuarioDao()

    // DAO para registrar y consultar visitas.
    private val visitaDao = database.visitaDao()

    // --------------------------------------------------
    // PUNTOS TURÍSTICOS
    // --------------------------------------------------

    override fun getPuntos(): Flow<List<PuntoInteres>> {
        return puntoDao.getPuntos()
    }

    override suspend fun getPuntoPorId(id: Int): PuntoInteres? {
        return puntoDao.getPuntoPorId(id)
    }

    override suspend fun marcarPuntoCompletado(
        id: Int,
        puntosRecompensa: Int,
        usuarioId: Int
    ) {

        // Los puntos se suman solamente al usuario indicado.
        if (puntosRecompensa > 0) {
            usuarioDao.sumarPuntos(
                id = usuarioId,
                puntos = puntosRecompensa
            )
        }

        // No modificamos aquí el estado global del punto.
        // El progreso individual se consultará desde las visitas.
    }

    // --------------------------------------------------
    // ESCÁNER QR Y VALIDACIÓN
    // --------------------------------------------------

    override suspend fun validarQr(codigoQr: String): Capsula? {
        return capsulaDao.getCapsulaPorQr(codigoQr)
    }

    override suspend fun registrarVisitaConEvidencia(
        puntoId: Int,
        capsulaId: Int?,
        rutaFotoSelfie: String?,
        puntosGanados: Int,
        usuarioId: Int
    ): Long {

        // 1. Crea una visita para el usuario autenticado.
        val nuevaVisita = VisitaRealizada(
            puntoId = puntoId,
            usuarioId = usuarioId,
            capsulaId = capsulaId,
            rutaFotoSelfie = rutaFotoSelfie,
            fechaTimestamp = System.currentTimeMillis(),
            completada = true
        )

        // 2. Guarda la visita en Room.
        val visitaId = visitaDao.registrarVisita(nuevaVisita)

        // 3. Suma los puntos al usuario correcto.
        if (puntosGanados > 0) {
            usuarioDao.sumarPuntos(
                id = usuarioId,
                puntos = puntosGanados
            )
        }

        // 4. Devuelve el ID de la visita registrada.
        return visitaId
    }

    // --------------------------------------------------
    // CÁPSULAS EDUCATIVAS
    // --------------------------------------------------

    override suspend fun getCapsulaPorPunto(puntoId: Int): Capsula? {
        return capsulaDao.getCapsulaPorPuntoId(puntoId)
    }

    override fun getTodasLasCapsulas(): Flow<List<Capsula>> {
        return capsulaDao.getTodasLasCapsulas()
    }

    // --------------------------------------------------
    // USUARIO Y GAMIFICACIÓN
    // --------------------------------------------------

    override fun getUsuario(id: Int): Flow<Usuario?> {
        return usuarioDao.getUsuario(id)
    }

    override suspend fun sumarPuntos(
        puntos: Int,
        usuarioId: Int
    ) {

        // Actualiza únicamente los puntos del usuario indicado.
        usuarioDao.sumarPuntos(
            id = usuarioId,
            puntos = puntos
        )
    }

    override fun getHistorialVisitas(
        usuarioId: Int
    ): Flow<List<VisitaRealizada>> {

        // Devuelve solamente las visitas del usuario indicado.
        return visitaDao.getVisitasPorUsuario(usuarioId)
    }

    // --------------------------------------------------
    // ADMINISTRADOR Y GENERADOR DE CONTENIDO
    // --------------------------------------------------

    override suspend fun crearPunto(punto: PuntoInteres): Long {
        return puntoDao.insertarPunto(punto)
    }

    override suspend fun crearCapsula(capsula: Capsula): Long {
        return capsulaDao.insertarCapsula(capsula)
    }
}
