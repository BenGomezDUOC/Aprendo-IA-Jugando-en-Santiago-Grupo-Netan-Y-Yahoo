package cl.triskeledu.aprendogame.data.repository

import androidx.room.withTransaction
import cl.triskeledu.aprendogame.data.local.AppDatabase
import cl.triskeledu.aprendogame.model.Capsula
import cl.triskeledu.aprendogame.model.DescubrimientoQr
import cl.triskeledu.aprendogame.model.PuntoInteres
import cl.triskeledu.aprendogame.model.Usuario
import cl.triskeledu.aprendogame.model.VideoVisto
import cl.triskeledu.aprendogame.model.VisitaRealizada
import kotlinx.coroutines.flow.Flow

/**
 * Implementación del repositorio principal.
 *
 * Conecta los ViewModels con Room y mantiene
 * los registros separados por jugador.
 */
class GameRepositoryImpl(
    private val database: AppDatabase
) : GameRepository {

    // DAO de puntos turísticos.
    private val puntoDao = database.puntoInteresDao()

    // DAO de cápsulas educativas.
    private val capsulaDao = database.capsulaDao()

    // DAO de usuarios y puntajes.
    private val usuarioDao = database.usuarioDao()

    // DAO de visitas realizadas.
    private val visitaDao = database.visitaDao()

    // DAO de descubrimientos QR.
    private val descubrimientoQrDao = database.descubrimientoQrDao()

    // DAO de videos vistos.
    private val videoVistoDao = database.videoVistoDao()


    // --------------------------------------------------
    // PUNTOS TURÍSTICOS
    // --------------------------------------------------

    override fun getPuntos(): Flow<List<PuntoInteres>> {
        return puntoDao.getPuntos()
    }

    override suspend fun getPuntoPorId(
        id: Int
    ): PuntoInteres? {
        return puntoDao.getPuntoPorId(id)
    }

    override suspend fun marcarPuntoCompletado(
        id: Int,
        puntosRecompensa: Int,
        usuarioId: Int
    ) {
        // Se conserva el comportamiento existente.
        if (puntosRecompensa > 0) {
            usuarioDao.sumarPuntos(
                id = usuarioId,
                puntos = puntosRecompensa
            )
        }
    }


    // --------------------------------------------------
    // ESCÁNER QR Y VALIDACIÓN
    // --------------------------------------------------

    override suspend fun validarQr(
        codigoQr: String
    ): Capsula? {
        return capsulaDao.getCapsulaPorQr(codigoQr)
    }

    override suspend fun registrarVisitaConEvidencia(
        puntoId: Int,
        capsulaId: Int?,
        rutaFotoSelfie: String?,
        puntosGanados: Int,
        usuarioId: Int
    ): Long {

        // La selfie debe tener una ruta de archivo válida.
        require(!rutaFotoSelfie.isNullOrBlank()) {
            "La fotografía es obligatoria"
        }

        require(puntosGanados == 100) {
            "La bonificación de selfie debe ser de 100 puntos"
        }

        // Ejecuta la consulta, el registro y los puntos
        // dentro de una única transacción de Room.
        return database.withTransaction {

            // Comprueba si este jugador ya completó
            // una visita al mismo punto turístico.
            val visitaExistente =
                visitaDao.getVisitaPorPuntoYUsuario(
                    puntoId = puntoId,
                    usuarioId = usuarioId
                )

            if (visitaExistente?.completada == true) {

                // Ya recibió la bonificación.
                // No se registra otra visita ni se suman puntos.
                -1L

            } else {

                // Crea una visita con su fotografía.
                val nuevaVisita = VisitaRealizada(
                    id = visitaExistente?.id ?: 0,
                    puntoId = puntoId,
                    usuarioId = usuarioId,
                    capsulaId = capsulaId,
                    rutaFotoSelfie = rutaFotoSelfie,
                    fechaTimestamp = System.currentTimeMillis(),
                    completada = true
                )

                // Guarda la evidencia en Room.
                val visitaId =
                    visitaDao.registrarVisita(nuevaVisita)

                // Suma 100 puntos únicamente
                // cuando se registra la visita.
                usuarioDao.sumarPuntos(
                    id = usuarioId,
                    puntos = 100
                )

                visitaId
            }
        }
    }


    // --------------------------------------------------
    // DESCUBRIMIENTOS QR
    // --------------------------------------------------

    override suspend fun registrarDescubrimientoQr(
        descubrimiento: DescubrimientoQr
    ): Long {

        // Ambas operaciones se ejecutan juntas.
        return database.withTransaction {

            val resultado =
                descubrimientoQrDao.registrarDescubrimiento(
                    descubrimiento
                )

            // Solo se entregan puntos cuando el QR
            // se registra por primera vez.
            if (resultado != -1L) {

                usuarioDao.sumarPuntos(
                    id = descubrimiento.usuarioId,
                    puntos = 10
                )
            }

            // -1 indica que ya estaba descubierto.
            resultado
        }
    }

    override fun contarQrDescubiertos(
        usuarioId: Int
    ): Flow<Int> {
        return descubrimientoQrDao.contarQrDescubiertos(
            usuarioId
        )
    }

    override fun getDescubrimientosPorUsuario(
        usuarioId: Int
    ): Flow<List<DescubrimientoQr>> {
        return descubrimientoQrDao.getDescubrimientosPorUsuario(
            usuarioId
        )
    }


    // --------------------------------------------------
    // CÁPSULAS EDUCATIVAS
    // --------------------------------------------------

    override suspend fun getCapsulaPorPunto(
        puntoId: Int
    ): Capsula? {
        return capsulaDao.getCapsulaPorPuntoId(puntoId)
    }

    override fun getTodasLasCapsulas(): Flow<List<Capsula>> {
        return capsulaDao.getTodasLasCapsulas()
    }


    // --------------------------------------------------
    // VIDEOS VISTOS
    // --------------------------------------------------

    override suspend fun registrarVideoVisto(
        videoVisto: VideoVisto
    ): Long {
        return videoVistoDao.registrarVideoVisto(
            videoVisto
        )
    }

    override fun contarVideosVistos(
        usuarioId: Int
    ): Flow<Int> {
        return videoVistoDao.contarVideosVistos(
            usuarioId
        )
    }

    override fun getVideosVistosPorUsuario(
        usuarioId: Int
    ): Flow<List<VideoVisto>> {
        return videoVistoDao.getVideosVistosPorUsuario(
            usuarioId
        )
    }


    // --------------------------------------------------
    // USUARIO Y GAMIFICACIÓN
    // --------------------------------------------------

    override fun getUsuario(
        id: Int
    ): Flow<Usuario?> {
        return usuarioDao.getUsuario(id)
    }

    override suspend fun sumarPuntos(
        puntos: Int,
        usuarioId: Int
    ) {
        usuarioDao.sumarPuntos(
            id = usuarioId,
            puntos = puntos
        )
    }

    override fun getHistorialVisitas(
        usuarioId: Int
    ): Flow<List<VisitaRealizada>> {
        return visitaDao.getVisitasPorUsuario(
            usuarioId
        )
    }


    // --------------------------------------------------
    // ADMINISTRADOR Y GENERADOR DE CONTENIDO
    // --------------------------------------------------

    override suspend fun crearPunto(
        punto: PuntoInteres
    ): Long {
        return puntoDao.insertarPunto(punto)
    }

    override suspend fun crearCapsula(
        capsula: Capsula
    ): Long {
        return capsulaDao.insertarCapsula(capsula)
    }
}
