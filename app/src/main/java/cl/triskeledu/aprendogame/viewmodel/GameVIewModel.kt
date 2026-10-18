
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



@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class GameViewModel(
    application: Application
) : AndroidViewModel(application) {

    // Repositorio principal conectado a Room.
    private val repository: GameRepository =
        GameRepositoryImpl(AppDatabase.getInstance(application))

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
    // GAMIFICACIÓN
    // --------------------------------------------------

    // Conservamos esta función para no afectar
    // las llamadas existentes de otras pantallas.
    fun marcarCompletado(id: Int) {

        val idJugador = _usuarioActivoId.value ?: return

        viewModelScope.launch {
            repository.marcarPuntoCompletado(
                id = id,
                puntosRecompensa = 10,
                usuarioId = idJugador
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



