
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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel principal de Triskeledu.
 *
 * Integra:
 * 1. Los datos de Room mediante el repositorio de Benjamín.
 * 2. La ubicación GPS del jugador.
 * 3. El punto turístico seleccionado.
 * 4. El usuario que inició sesión.
 * 5. El progreso individual del jugador.
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class GameViewModel(
    application: Application
) : AndroidViewModel(application) {

    // Conecta el ViewModel con Room.
    private val repository: GameRepository =
        GameRepositoryImpl(AppDatabase.getInstance(application))

    // --------------------------------------------------
    // JUGADOR ACTIVO
    // --------------------------------------------------

    // null significa que todavía no hay una sesión activa.
    private val _usuarioActivoId = MutableStateFlow<Int?>(null)

    val usuarioActivoId: StateFlow<Int?> =
        _usuarioActivoId.asStateFlow()

    // Se ejecuta después de verificar la contraseña.
    fun establecerUsuarioActivo(id: Int) {
        _usuarioActivoId.value = id
    }

    // Permite limpiar el usuario cuando cierre sesión.
    fun cerrarSesion() {
        _usuarioActivoId.value = null
        _puntoSeleccionado.value = null
    }

    // --------------------------------------------------
    // PUNTOS TURÍSTICOS
    // --------------------------------------------------

    // Observa los puntos turísticos registrados en Room.
    val puntos: StateFlow<List<PuntoInteres>> =
        repository.getPuntos().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --------------------------------------------------
    // DATOS DEL USUARIO AUTENTICADO
    // --------------------------------------------------

    // Cambia automáticamente el usuario observado
    // cuando cambia el ID del jugador activo.
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
    // HISTORIAL INDIVIDUAL
    // --------------------------------------------------

    // Obtiene únicamente las visitas del usuario activo.
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
    // UBICACIÓN GPS
    // --------------------------------------------------

    // Guarda temporalmente la ubicación actual.
    private val _ubicacionActual = MutableStateFlow<Location?>(null)

    val ubicacionActual: StateFlow<Location?> =
        _ubicacionActual.asStateFlow()

    private var locationService: LocationService? = null

    // Inicia la recepción de actualizaciones GPS.
    fun iniciarRastreoGPS(context: Context) {

        if (locationService == null) {
            locationService = LocationService(context.applicationContext)
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

    // Guarda el punto seleccionado para mostrar sus detalles.
    fun seleccionarPunto(punto: PuntoInteres?) {
        _puntoSeleccionado.value = punto
    }

    // --------------------------------------------------
    // CÁLCULO DE DISTANCIA
    // --------------------------------------------------

    // Calcula la distancia entre el jugador y un punto turístico.
    fun calcularDistanciaMetros(punto: PuntoInteres): Float? {

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

    // Suma la recompensa al jugador autenticado.
    fun marcarCompletado(id: Int) {

        // Si nadie inició sesión, no se suman puntos.
        val idJugador = _usuarioActivoId.value ?: return

        viewModelScope.launch {

            repository.marcarPuntoCompletado(
                id = id,
                puntosRecompensa = 10,
                usuarioId = idJugador
            )
        }
    }
}
