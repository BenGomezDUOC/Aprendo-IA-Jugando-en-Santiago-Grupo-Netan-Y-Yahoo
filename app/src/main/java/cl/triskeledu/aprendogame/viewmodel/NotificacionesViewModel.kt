package cl.triskeledu.aprendogame.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import cl.triskeledu.aprendogame.data.local.AppDatabase
import cl.triskeledu.aprendogame.data.repository.GameRepository
import cl.triskeledu.aprendogame.data.repository.GameRepositoryImpl
import cl.triskeledu.aprendogame.model.Notificacion
import cl.triskeledu.aprendogame.notification.NotificationHelper

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class NotificacionesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository =
        GameRepositoryImpl(AppDatabase.getInstance(application))

    // --------------------------------------------------
    // USUARIO ACTIVO
    // --------------------------------------------------

    private val _usuarioActivoId = MutableStateFlow<Int?>(null)

    val usuarioActivoId: StateFlow<Int?> = _usuarioActivoId.asStateFlow()

    fun setUsuarioActivo(id: Int?) {
        _usuarioActivoId.value = id
    }

    // --------------------------------------------------
    // NOTIFICACIONES DEL USUARIO ACTIVO
    // --------------------------------------------------

    // Obtiene las notificaciones de la cuenta activa.
    // Cuando cambia el usuario, cambia la lista.
    val notificaciones: StateFlow<List<Notificacion>> =
        _usuarioActivoId.flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyList())
            } else {
                repository.getNotificacionesPorUsuario(id)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Cuenta las notificaciones que todavía no se han leído.
    val notificacionesNoLeidas: StateFlow<Int> =
        _usuarioActivoId.flatMapLatest { id ->
            if (id == null) {
                flowOf(0)
            } else {
                repository.contarNotificacionesNoLeidas(id)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // Guarda una notificación para el usuario activo.
    fun registrarNotificacion(
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

    // Envía una notificación de prueba local sin depender de Firebase.
    fun enviarNotificacionPruebaLocal() {
        registrarNotificacion(
            titulo = "Prueba Local de Notificación",
            mensaje = "¡Este es un aviso de prueba generado localmente para verificar el sistema!",
            tipo = "PRUEBA",
            rutaDestino = "mapa"
        )
    }

    // Marca una notificación como leída.
    fun marcarNotificacionLeida(notificacionId: Int) {
        val idJugador = _usuarioActivoId.value ?: return

        viewModelScope.launch {
            repository.marcarNotificacionLeida(
                notificacionId = notificacionId,
                usuarioId = idJugador
            )
        }
    }

    // Marca todas las notificaciones como leídas.
    fun marcarTodasNotificacionesLeidas() {
        val idJugador = _usuarioActivoId.value ?: return

        viewModelScope.launch {
            repository.marcarTodasComoLeidas(idJugador)
        }
    }
}
