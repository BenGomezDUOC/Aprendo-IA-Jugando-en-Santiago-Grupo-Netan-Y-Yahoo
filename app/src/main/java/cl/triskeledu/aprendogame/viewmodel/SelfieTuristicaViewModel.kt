package cl.triskeledu.aprendogame.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import cl.triskeledu.aprendogame.data.local.AppDatabase
import cl.triskeledu.aprendogame.data.local.SessionManager
import cl.triskeledu.aprendogame.data.repository.RemoteBackendRepository
import cl.triskeledu.aprendogame.data.remote.RetrofitClient
import java.io.File
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

    private val database = AppDatabase.getInstance(application)
    private val repository: GameRepository = GameRepositoryImpl(database)
    private val remoto = RemoteBackendRepository()
    private val sesion = SessionManager(application)

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

                // PASO 3: La sesión de Spring Boot es la fuente de verdad.
                val token = sesion.obtenerTokenBackend()
                val usuarioBackendId = sesion.obtenerUsuarioBackendId()

                android.util.Log.d(
                    "SELFIE_BACKEND",
                    "Token disponible: ${token != null}, " +
                            "ID backend disponible: ${usuarioBackendId != null}"
                )

                if (token != null && usuarioBackendId != null) {
                    // La ruta local es un archivo privado creado por la cámara.
                    val foto = File(rutaFoto)
                    if (!foto.isFile) {
                        onResultado(false, "No se encontró el archivo de la selfie")
                        return@launch
                    }

                    // Obtener los IDs reales de MySQL usando el código QR.
                    val capsulaRemota = withContext(Dispatchers.IO) {
                        val respuesta = RetrofitClient.localizacionesApi
                            .validarCapsulaQr(
                                authorization = if (token.startsWith("Bearer ")) token else "Bearer $token",
                                codigo = codigoQrEsperado
                            )

                        if (!respuesta.isSuccessful) {
                            throw IllegalStateException("No se pudo validar el QR en el servidor (HTTP ${respuesta.code()})")
                        }
                        respuesta.body() ?: throw IllegalStateException("El servidor no devolvió la cápsula")
                    }
                    val respuesta = remoto.registrarSelfie(
                        token = token,
                        puntoIdBackend = capsulaRemota.puntoId,
                        capsulaIdBackend = capsulaRemota.id,
                        codigoQrEsperado = codigoQrEsperado,
                        foto = foto
                    ).getOrThrow()

                    // No duplicar los 100 puntos al repetir la misma selfie.
                    val totalRemoto = respuesta.totalPuntosUsuario
                    if (respuesta.nuevaVisita != true || (respuesta.puntosGanados ?: 0) == 0) {
                        totalRemoto?.let { total ->
                            withContext(Dispatchers.IO) {
                                database.usuarioDao().establecerPuntos(idJugador, total)
                            }
                        }
                        onResultado(false, respuesta.mensaje ?: "Esta selfie ya estaba registrada")
                        return@launch
                    }

                    // Mantener el historial local. No dejar que Room decida el saldo final.
                    withContext(Dispatchers.IO) {
                        try {
                            repository.registrarVisitaConEvidencia(
                                puntoId = puntoId,
                                capsulaId = capsulaId,
                                rutaFotoSelfie = rutaFoto,
                                puntosGanados = 100,
                                usuarioId = idJugador
                            )
                        } finally {
                            totalRemoto?.let { total ->
                                database.usuarioDao().establecerPuntos(idJugador, total)
                            }
                        }
                    }
                    onResultado(true, respuesta.mensaje ?: "¡Selfie validada! Ganaste 100 puntos.")
                } else {
                    // Cuentas predefinidas que funcionan solo con Room.
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
                        onResultado(false, "Ya recibiste 100 puntos por este QR")
                        return@launch
                    }
                    onResultado(true, "¡Selfie validada! Ganaste 100 puntos.")
                }

                registrarNotificacionInterna(
                    titulo = "¡Selfie validada! 📸",
                    mensaje = "Ganaste 100 puntos por tu visita en terreno.",
                    tipo = "EVIDENCIA",
                    rutaDestino = "historial_actividades"
                )


            } catch (e: Exception) {
                android.util.Log.e(
                    "SELFIE_BACKEND",
                    "Error al registrar selfie en el backend",
                    e
                )

                onResultado(
                    false,
                    "No se pudo registrar la selfie: ${e.message}"
                )
            }

        }
    }
}
