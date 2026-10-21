
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
import cl.triskeledu.aprendogame.model.Notificacion

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

    // DAO de notificaciones.
    private val notificacionDao = database.notificacionDao()


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
        require(!rutaFotoSelfie.isNullOrBlank()) { "La fotografía es obligatoria" }
        require(puntosGanados == 100) { "La bonificación debe ser de 100 puntos" }
        requireNotNull(capsulaId) { "La selfie debe corresponder a un QR" }

        return database.withTransaction {
            // La bonificación es por jugador y QR, NO por lugar turístico.
            val visitaExistente = visitaDao.getVisitaPorCapsulaYUsuario(
                capsulaId = capsulaId,
                usuarioId = usuarioId
            )
            if (visitaExistente?.completada == true) {
                -1L
            } else {
                val visitaId = visitaDao.registrarVisita(
                    VisitaRealizada(
                        puntoId = puntoId,
                        usuarioId = usuarioId,
                        capsulaId = capsulaId,
                        rutaFotoSelfie = rutaFotoSelfie,
                        fechaTimestamp = System.currentTimeMillis(),
                        completada = true
                    )
                )
                usuarioDao.sumarPuntos(id = usuarioId, puntos = 100)
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

    // Recupera el usuario directamente desde Room.
    override suspend fun getUsuarioDirecto(
        id: Int
    ): Usuario? {
        return usuarioDao.getUsuarioDirecto(id)
    }

    // Comprueba que el correo no pertenezca a otra cuenta.
    override suspend fun contarCorreoDeOtroUsuario(
        correo: String,
        usuarioId: Int
    ): Int {
        return usuarioDao.contarCorreoDeOtroUsuario(
            correo = correo,
            usuarioId = usuarioId
        )
    }

    // Edita los datos personales del usuario en Room.
    // Incluye la contraseña protegida mediante hash.
    // Conserva el ID, rol, puntos y progreso del usuario.
    override suspend fun actualizarPerfil(
        usuarioId: Int,
        nombre: String,
        alias: String,
        correo: String,
        telefono: String,
        direccion: String,
        avatar: Int,
        contrasenaHash: String
    ): Int {
        return usuarioDao.actualizarPerfil(
            usuarioId = usuarioId,
            nombre = nombre,
            alias = alias,
            correo = correo,
            telefono = telefono,
            direccion = direccion,
            avatar = avatar,
            contrasenaHash = contrasenaHash
        )
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



    // --------------------------------------------------
    // NOTIFICACIONES
    // Cliente, generador y administrador
    // --------------------------------------------------

    // Guarda una notificación en Room.
    override suspend fun registrarNotificacion(
        notificacion: Notificacion
    ): Long {
        return notificacionDao.registrarNotificacion(
            notificacion
        )
    }

    // Obtiene las notificaciones del usuario activo.
    override fun getNotificacionesPorUsuario(
        usuarioId: Int
    ): Flow<List<Notificacion>> {
        return notificacionDao.getNotificacionesPorUsuario(
            usuarioId
        )
    }

    // Cuenta las notificaciones pendientes de lectura.
    override fun contarNotificacionesNoLeidas(
        usuarioId: Int
    ): Flow<Int> {
        return notificacionDao.contarNotificacionesNoLeidas(
            usuarioId
        )
    }

    // Marca una notificación como leída.
    override suspend fun marcarNotificacionLeida(
        notificacionId: Int,
        usuarioId: Int
    ): Int {
        return notificacionDao.marcarNotificacionLeida(
            notificacionId = notificacionId,
            usuarioId = usuarioId
        )
    }

    // Marca todas las notificaciones del usuario como leídas.
    override suspend fun marcarTodasComoLeidas(
        usuarioId: Int
    ): Int {
        return notificacionDao.marcarTodasComoLeidas(
            usuarioId
        )
    }

}
