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
import cl.triskeledu.aprendogame.model.Capsula
import cl.triskeledu.aprendogame.model.PuntoInteres

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MapaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository =
        GameRepositoryImpl(AppDatabase.getInstance(application))

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
    // CÁPSULAS EDUCATIVAS (necesarias para el mapa y detalle)
    // --------------------------------------------------

    val capsulas: StateFlow<List<Capsula>> =
        repository.getTodasLasCapsulas().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
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
}
