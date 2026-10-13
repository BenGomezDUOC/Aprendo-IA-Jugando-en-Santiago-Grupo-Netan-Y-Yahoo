package cl.triskeledu.aprendogame.data.repository

import cl.triskeledu.aprendogame.data.local.AppDatabase
import cl.triskeledu.aprendogame.model.Capsula
import cl.triskeledu.aprendogame.model.PuntoInteres
import cl.triskeledu.aprendogame.model.Usuario
import cl.triskeledu.aprendogame.model.VisitaRealizada
import kotlinx.coroutines.flow.Flow

/**
 * Implementación concreta del GameRepository conectada a AppDatabase (Room).
 */
class GameRepositoryImpl(
    private val database: AppDatabase
) : GameRepository {

    private val puntoDao = database.puntoInteresDao()
    private val capsulaDao = database.capsulaDao()
    private val usuarioDao = database.usuarioDao()
    private val visitaDao = database.visitaDao()

    override fun getPuntos(): Flow<List<PuntoInteres>> {
        return puntoDao.getPuntos()
    }

    override suspend fun getPuntoPorId(id: Int): PuntoInteres? {
        return puntoDao.getPuntoPorId(id)
    }

    override suspend fun marcarPuntoCompletado(id: Int, puntosRecompensa: Int) {
        puntoDao.actualizarEstado(id, true)
        if (puntosRecompensa > 0) {
            usuarioDao.sumarPuntos(1, puntosRecompensa)
        }
    }

    override suspend fun validarQr(codigoQr: String): Capsula? {
        return capsulaDao.getCapsulaPorQr(codigoQr)
    }

    override suspend fun registrarVisitaConEvidencia(
        puntoId: Int,
        capsulaId: Int?,
        rutaFotoSelfie: String?,
        puntosGanados: Int
    ): Long {
        // 1. Marcar el punto como completado
        puntoDao.actualizarEstado(puntoId, true)

        // 2. Registrar la evidencia de visita
        val nuevaVisita = VisitaRealizada(
            puntoId = puntoId,
            usuarioId = 1,
            capsulaId = capsulaId,
            rutaFotoSelfie = rutaFotoSelfie,
            fechaTimestamp = System.currentTimeMillis(),
            completada = true
        )
        val visitaId = visitaDao.registrarVisita(nuevaVisita)

        // 3. Sumar el puntaje al usuario
        if (puntosGanados > 0) {
            usuarioDao.sumarPuntos(1, puntosGanados)
        }

        return visitaId
    }

    override suspend fun getCapsulaPorPunto(puntoId: Int): Capsula? {
        return capsulaDao.getCapsulaPorPuntoId(puntoId)
    }

    override fun getTodasLasCapsulas(): Flow<List<Capsula>> {
        return capsulaDao.getTodasLasCapsulas()
    }

    override fun getUsuario(id: Int): Flow<Usuario?> {
        return usuarioDao.getUsuario(id)
    }

    override suspend fun sumarPuntos(puntos: Int) {
        usuarioDao.sumarPuntos(1, puntos)
    }

    override fun getHistorialVisitas(): Flow<List<VisitaRealizada>> {
        return visitaDao.getVisitas()
    }
}
