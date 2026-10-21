package cl.triskeledu.aprendogame.viewmodel

import android.app.Application
import android.content.Context
import android.location.Location

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import cl.triskeledu.aprendogame.data.local.AppDatabase
import cl.triskeledu.aprendogame.data.repository.GameRepository
import cl.triskeledu.aprendogame.data.repository.GameRepositoryImpl
import cl.triskeledu.aprendogame.location.LocationService
import cl.triskeledu.aprendogame.model.PuntoInteres
import cl.triskeledu.aprendogame.model.Usuario
import cl.triskeledu.aprendogame.model.VisitaRealizada
import cl.triskeledu.aprendogame.model.Notificacion

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import cl.triskeledu.aprendogame.validation.SelfieValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import cl.triskeledu.aprendogame.model.Capsula
import cl.triskeledu.aprendogame.model.VideoVisto

import cl.triskeledu.aprendogame.security.PasswordUtils

import cl.triskeledu.aprendogame.model.VideoAdicionalVisto

import cl.triskeledu.aprendogame.model.DescubrimientoQr

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class GameViewModel(
    application: Application
) : AndroidViewModel(application) {

    // Repositorio principal conectado a Room.
    private val repository: GameRepository =
        GameRepositoryImpl(AppDatabase.getInstance(application))

    // DAO independiente para los dos videos adicionales.
    private val videoAdicionalVistoDao =
        AppDatabase.getInstance(application).videoAdicionalVistoDao()

    // --------------------------------------------------
    // JUGADOR ACTIVO
    // --------------------------------------------------

    private val _usuarioActivoId = MutableStateFlow<Int?>(null)

    val usuarioActivoId: StateFlow<Int?> =
        _usuarioActivoId.asStateFlow()

    fun establecerUsuarioActivo(id: Int) {
        _usuarioActivoId.value = id
    }

    fun cerrarSesion() {
        _usuarioActivoId.value = null
        _puntoSeleccionado.value = null
    }

    // --------------------------------------------------
    // PUNTOS TURÍSTICOS
    // --------------------------------------------------

    // Todos los lugares turísticos disponibles en Room.
    val puntos: StateFlow<List<PuntoInteres>> =
        repository.getPuntos().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --------------------------------------------------
    // USUARIO AUTENTICADO
    // --------------------------------------------------

    val usuario: StateFlow<Usuario?> =
        _usuarioActivoId.flatMapLatest { id ->

            if (id == null) {
                flowOf(null)
            } else {
                repository.getUsuario(id)
            }

        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // --------------------------------------------------
    // EDICIÓN DEL PERFIL
    // --------------------------------------------------

    /**
     * Edita el perfil del usuario que inició sesión.
     *
     * Si no escribe una contraseña nueva,
     * conserva la contraseña actual.
     *
     * No modifica el ID, rol, puntos ni progreso.
     */
    fun editarPerfil(
        nombre: String,
        alias: String,
        correo: String,
        telefono: String,
        direccion: String,
        avatar: Int,
        contrasenaActual: String,
        contrasenaNueva: String,
        onResultado: (Boolean, String) -> Unit
    ) {

        // Obtiene el ID del usuario que inició sesión.
        val idJugador = _usuarioActivoId.value

        if (idJugador == null) {
            onResultado(false, "Debes iniciar sesión")
            return
        }

        viewModelScope.launch {

            try {

                // 1. Validar el nombre.
                if (nombre.isBlank()) {
                    onResultado(false, "El nombre es obligatorio")
                    return@launch
                }

                // 2. Validar el correo.
                val correoLimpio = correo.trim().lowercase()

                if (!android.util.Patterns.EMAIL_ADDRESS
                        .matcher(correoLimpio).matches()
                ) {
                    onResultado(false, "Ingresa un correo válido")
                    return@launch
                }

                // 3. Comprobar que otro usuario no tenga el correo.
                val correoOcupado = withContext(Dispatchers.IO) {
                    repository.contarCorreoDeOtroUsuario(
                        correo = correoLimpio,
                        usuarioId = idJugador
                    )
                }

                if (correoOcupado > 0) {
                    onResultado(false, "Este correo ya está registrado")
                    return@launch
                }

                // 4. Obtener los datos actuales del usuario.
                val usuarioActual = withContext(Dispatchers.IO) {
                    repository.getUsuarioDirecto(idJugador)
                }

                if (usuarioActual == null) {
                    onResultado(false, "No se encontró el usuario")
                    return@launch
                }

                // 5. Conservar la contraseña existente por defecto.
                var hashParaGuardar = usuarioActual.contrasena

                // Solo cambia la contraseña si escribe una nueva.
                if (contrasenaNueva.isNotEmpty()) {

                    if (contrasenaActual.isEmpty()) {
                        onResultado(
                            false,
                            "Escribe tu contraseña actual"
                        )
                        return@launch
                    }

                    // Verifica que la contraseña actual sea correcta.
                    val contrasenaCorrecta = withContext(Dispatchers.Default) {
                        PasswordUtils.verificarContrasena(
                            contrasenaActual,
                            usuarioActual.contrasena
                        )
                    }

                    if (!contrasenaCorrecta) {
                        onResultado(
                            false,
                            "La contraseña actual es incorrecta"
                        )
                        return@launch
                    }

                    if (contrasenaNueva.length < 8) {
                        onResultado(
                            false,
                            "La contraseña nueva debe tener al menos 8 caracteres"
                        )
                        return@launch
                    }

                    // Protege la contraseña nueva mediante hash.
                    hashParaGuardar = withContext(Dispatchers.Default) {
                        PasswordUtils.crearHash(contrasenaNueva)
                    }
                }

                // 6. Guardar los cambios en Room.
                val filasActualizadas = withContext(Dispatchers.IO) {
                    repository.actualizarPerfil(
                        usuarioId = idJugador,
                        nombre = nombre.trim(),
                        alias = alias.trim(),
                        correo = correoLimpio,
                        telefono = telefono.trim(),
                        direccion = direccion.trim(),
                        avatar = avatar,
                        contrasenaHash = hashParaGuardar
                    )
                }

                // 7. Informar el resultado.
                if (filasActualizadas > 0) {
                    onResultado(
                        true,
                        "Perfil actualizado correctamente"
                    )
                } else {
                    onResultado(
                        false,
                        "No se pudieron guardar los cambios"
                    )
                }

            } catch (e: Exception) {
                onResultado(
                    false,
                    "Ocurrió un error al actualizar el perfil"
                )
            }
        }
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


    // --------------------------------------------------
    // HISTORIAL DE VISITAS
    // --------------------------------------------------

    val historialVisitas: StateFlow<List<VisitaRealizada>> =
        _usuarioActivoId.flatMapLatest { id ->

            if (id == null) {
                flowOf(emptyList())
            } else {
                repository.getHistorialVisitas(id)
            }

        }.stateIn(
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
        }
    }


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
    // UBICACIÓN GPS
    // --------------------------------------------------

    private val _ubicacionActual =
        MutableStateFlow<Location?>(null)

    val ubicacionActual: StateFlow<Location?> =
        _ubicacionActual.asStateFlow()

    private var locationService: LocationService? = null

    fun iniciarRastreoGPS(context: Context) {

        if (locationService == null) {
            locationService = LocationService(
                context.applicationContext
            )
        }

        viewModelScope.launch {
            locationService?.obtenerUbicacionFlow()
                ?.catch { error ->
                    error.printStackTrace()
                }
                ?.collect { location ->
                    _ubicacionActual.value = location
                }
        }
    }

    // --------------------------------------------------
    // PUNTO TURÍSTICO SELECCIONADO
    // --------------------------------------------------

    private val _puntoSeleccionado =
        MutableStateFlow<PuntoInteres?>(null)

    val puntoSeleccionado: StateFlow<PuntoInteres?> =
        _puntoSeleccionado.asStateFlow()

    fun seleccionarPunto(punto: PuntoInteres?) {
        _puntoSeleccionado.value = punto
    }

    // --------------------------------------------------
    // CÁLCULO DE DISTANCIA
    // --------------------------------------------------

    fun calcularDistanciaMetros(
        punto: PuntoInteres
    ): Float? {

        val ubicacion = _ubicacionActual.value ?: return null
        val resultados = FloatArray(1)

        Location.distanceBetween(
            ubicacion.latitude,
            ubicacion.longitude,
            punto.latitud,
            punto.longitud,
            resultados
        )

        return resultados[0]
    }

    // --------------------------------------------------
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




