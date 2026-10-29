package cl.triskeledu.aprendogame.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import cl.triskeledu.aprendogame.data.local.AppDatabase
import cl.triskeledu.aprendogame.data.repository.GameRepository
import cl.triskeledu.aprendogame.data.repository.GameRepositoryImpl
import cl.triskeledu.aprendogame.model.Notificacion
import cl.triskeledu.aprendogame.notification.NotificationHelper
import cl.triskeledu.aprendogame.validation.SelfieValidator

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SelfieTuristicaViewModel(application: Application) : AndroidViewModel(application) {

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
    // VALIDACIÓN DE SELFIE TURÍSTICA
    // --------------------------------------------------

    fun validarYRegistrarSelfie(
        rutaFoto: String,
        codigoQrEsperado: String,
        puntoId: Int,
        capsulaId: Int,
        onResultado: (Boolean, String) -> Unit
    ) {
        val idJugador = _usuarioActivoId.value

        if (idJugador == null) {
            onResultado(false, "Debes iniciar sesión")
            return
        }

        viewModelScope.launch {
            try {

                // PASO 1: Validar el rostro y el QR de la selfie.
                // La foto ya está guardada en la galería.
                val selfieValida = withContext(Dispatchers.IO) {
                    SelfieValidator.validar(
                        context = getApplication(),
                        rutaFoto = rutaFoto,
                        codigoQrEsperado = codigoQrEsperado
                    )
                }

                if (!selfieValida) {
                    onResultado(
                        false,
                        "La fotografía debe mostrar tu rostro y el QR correcto. La foto permanece en tu galería."
                    )
                    return@launch
                }

                // PASO 2: Verificar que el QR pertenezca
                // a la cápsula y al punto turístico.
                val capsulaVerificada = withContext(Dispatchers.IO) {
                    repository.validarQr(codigoQrEsperado)
                }

                if (
                    capsulaVerificada == null ||
                    capsulaVerificada.id != capsulaId ||
                    capsulaVerificada.puntoId != puntoId
                ) {
                    onResultado(
                        false,
                        "El QR no corresponde a este lugar. La foto permanece en tu galería."
                    )
                    return@launch
                }

                // PASO 3: Registrar la visita y otorgar 100 puntos.
                // El repositorio evita duplicar la recompensa.
                val resultado = withContext(Dispatchers.IO) {
                    repository.registrarVisitaConEvidencia(
                        puntoId = puntoId,
                        capsulaId = capsulaId,
                        rutaFotoSelfie = rutaFoto,
                        puntosGanados = 100,
                        usuarioId = idJugador
                    )
                }

                if (resultado == -1L) {
                    onResultado(
                        false,
                        "Ya recibiste los 100 puntos por este lugar. La foto permanece en tu galería."
                    )
                } else {
                    onResultado(
                        true,
                        "¡Selfie validada! Ganaste 100 puntos."
                    )
                    registrarNotificacionInterna(
                        titulo = "¡Selfie validada! 📸",
                        mensaje = "Ganaste 100 puntos por tu visita en terreno.",
                        tipo = "EVIDENCIA",
                        rutaDestino = "historial_actividades"
                    )
                }

            } catch (e: Exception) {
                onResultado(
                    false,
                    "No se pudo validar o registrar la fotografía. La foto permanece en tu galería."
                )
            }
        }
    }
}
