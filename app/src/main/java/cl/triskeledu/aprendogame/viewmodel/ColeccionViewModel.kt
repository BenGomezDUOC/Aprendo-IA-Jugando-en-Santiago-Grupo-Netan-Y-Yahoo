package cl.triskeledu.aprendogame.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import cl.triskeledu.aprendogame.data.local.AppDatabase
import cl.triskeledu.aprendogame.data.repository.GameRepository
import cl.triskeledu.aprendogame.data.repository.GameRepositoryImpl
import cl.triskeledu.aprendogame.model.Capsula
import cl.triskeledu.aprendogame.model.DescubrimientoQr
import cl.triskeledu.aprendogame.model.Notificacion
import cl.triskeledu.aprendogame.model.VideoAdicionalVisto
import cl.triskeledu.aprendogame.model.VideoVisto
import cl.triskeledu.aprendogame.notification.NotificationHelper

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ColeccionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository =
        GameRepositoryImpl(AppDatabase.getInstance(application))

    // DAO independiente para los dos videos adicionales.
    private val videoAdicionalVistoDao =
        AppDatabase.getInstance(application).videoAdicionalVistoDao()

    // --------------------------------------------------
    // USUARIO ACTIVO
    // --------------------------------------------------

    private val _usuarioActivoId = MutableStateFlow<Int?>(null)

    val usuarioActivoId: StateFlow<Int?> = _usuarioActivoId.asStateFlow()

    fun setUsuarioActivo(id: Int?) {
        _usuarioActivoId.value = id
    }

    // --------------------------------------------------
    // CÁPSULAS EDUCATIVAS
    // --------------------------------------------------

    // Obtiene las cápsulas guardadas en Room.
    val capsulas: StateFlow<List<Capsula>> =
        repository.getTodasLasCapsulas().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --------------------------------------------------
    // QR DESCUBIERTOS
    // --------------------------------------------------

    val qrDescubiertos: StateFlow<Int> =
        _usuarioActivoId.flatMapLatest { id ->
            if (id == null) {
                flowOf(0)
            } else {
                repository.contarQrDescubiertos(id)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // --------------------------------------------------
    // HISTORIAL DE DESCUBRIMIENTOS QR
    // --------------------------------------------------

    // Recupera los QR descubiertos por el usuario activo.
    // Incluye la cápsula y la fecha del descubrimiento.
    val descubrimientosQr: StateFlow<List<DescubrimientoQr>> =
        _usuarioActivoId.flatMapLatest { idJugador ->
            if (idJugador == null) {
                flowOf(emptyList())
            } else {
                repository.getDescubrimientosPorUsuario(idJugador)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --------------------------------------------------
    // VIDEOS VISTOS
    // --------------------------------------------------

    // Recupera los videos vistos por el jugador activo.
    val videosVistos: StateFlow<List<VideoVisto>> =
        _usuarioActivoId.flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyList())
            } else {
                repository.getVideosVistosPorUsuario(id)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --------------------------------------------------
    // VIDEOS EDUCATIVOS ADICIONALES VISTOS
    // --------------------------------------------------

    // Recupera los dos videos adicionales vistos
    // por el usuario que inició sesión.
    val videosAdicionalesVistos: StateFlow<List<VideoAdicionalVisto>> =
        _usuarioActivoId.flatMapLatest { idJugador ->
            if (idJugador == null) {
                flowOf(emptyList())
            } else {
                videoAdicionalVistoDao.obtenerVideosVistos(idJugador)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --------------------------------------------------
    // VIDEOS DESCUBIERTOS
    // --------------------------------------------------

    // Cuenta las cápsulas encontradas por QR
    // que contienen una URL de video.
    val videosDescubiertos: StateFlow<Int> =
        _usuarioActivoId.flatMapLatest { id ->
            if (id == null) {
                flowOf(0)
            } else {
                combine(
                    repository.getDescubrimientosPorUsuario(id),
                    repository.getTodasLasCapsulas()
                ) { descubrimientos, capsulas ->
                    val idsDescubiertos = descubrimientos
                        .map { it.capsulaId }
                        .toSet()

                    capsulas.count { capsula ->
                        capsula.id in idsDescubiertos &&
                                capsula.urlVideo.isNotBlank()
                    }
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // --------------------------------------------------
    // REGISTRO DE NOTIFICACIONES (sin depender de NotificacionesViewModel)
    // --------------------------------------------------

    private fun registrarNotificacionInterna(
        titulo: String,
        mensaje: String,
        tipo: String,
        rutaDestino: String
    ) {
        val idJugador = _usuarioActivoId.value ?: return

        viewModelScope.launch {
            repository.registrarNotificacion(
                Notificacion(
                    usuarioId = idJugador,
                    titulo = titulo,
                    mensaje = mensaje,
                    tipo = tipo,
                    rutaDestino = rutaDestino
                )
            )

            // Disparar la notificación nativa en la barra de Android (Recurso Nativo IE 2.4.1)
            NotificationHelper.mostrarNotificacion(
                context = getApplication(),
                titulo = titulo,
                mensaje = mensaje,
                rutaDestino = rutaDestino
            )
        }
    }

    // --------------------------------------------------
    // REGISTRO DE VIDEOS VISTOS
    // --------------------------------------------------

    // Guarda un video como visto cuando termina su reproducción.
    fun registrarVideoVisto(capsulaId: Int) {
        val idJugador = _usuarioActivoId.value ?: return

        viewModelScope.launch {
            repository.registrarVideoVisto(
                VideoVisto(
                    usuarioId = idJugador,
                    capsulaId = capsulaId
                )
            )
            registrarNotificacionInterna(
                titulo = "¡Cápsula de IA completada! 🎬",
                mensaje = "Has completado la visualización de la cápsula educativa.",
                tipo = "VIDEO",
                rutaDestino = "coleccion"
            )
        }
    }

    // Registra el video adicional cuando termina.
    // No otorga puntos ni registra descubrimientos QR.
    fun registrarVideoAdicionalVisto(videoAdicionalId: Int) {

        // Solo existen dos videos adicionales.
        if (videoAdicionalId !in 1..2) return

        // Identifica al usuario que está viendo el video.
        val idJugador = _usuarioActivoId.value ?: return

        viewModelScope.launch {

            // Guarda el registro en Room.
            // El DAO evita registrar el mismo video dos veces.
            videoAdicionalVistoDao.registrarVideoVisto(
                VideoAdicionalVisto(
                    usuarioId = idJugador,
                    videoAdicionalId = videoAdicionalId
                )
            )
        }
    }

    // --------------------------------------------------
    // VIDEOS SIN QR CREADOS POR EL ADMINISTRADOR
    // --------------------------------------------------

    // Utiliza IDs negativos para diferenciarlos de los
    // dos videos adicionales originales (IDs 1 y 2).
    // No otorga puntos ni registra descubrimientos QR.
    fun registrarVideoAdministradorVisto(videoRegistradoId: Int) {
        if (videoRegistradoId <= 0) return

        val idJugador = _usuarioActivoId.value ?: return
        val identificadorVideo = -videoRegistradoId

        viewModelScope.launch {
            videoAdicionalVistoDao.registrarVideoVisto(
                VideoAdicionalVisto(
                    usuarioId = idJugador,
                    videoAdicionalId = identificadorVideo
                )
            )
        }
    }
}
