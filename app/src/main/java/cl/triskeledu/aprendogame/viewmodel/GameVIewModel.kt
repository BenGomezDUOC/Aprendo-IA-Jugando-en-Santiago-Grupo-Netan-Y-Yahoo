package cl.triskeledu.aprendogame.viewmodel

import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.triskeledu.aprendogame.location.LocationService
import cl.triskeledu.aprendogame.model.PuntoInteres
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {

    // Lista de puntos turísticos (en memoria por ahora, luego Room)
    private val _puntos = MutableStateFlow(puntosDePrueba())
    val puntos: StateFlow<List<PuntoInteres>> = _puntos.asStateFlow()

    // Ubicación GPS actual del usuario
    private val _ubicacionActual = MutableStateFlow<Location?>(null)
    val ubicacionActual: StateFlow<Location?> = _ubicacionActual.asStateFlow()

    // Punto turístico seleccionado actualmente para ver detalles en el mapa
    private val _puntoSeleccionado = MutableStateFlow<PuntoInteres?>(null)
    val puntoSeleccionado: StateFlow<PuntoInteres?> = _puntoSeleccionado.asStateFlow()

    private var locationService: LocationService? = null

    fun iniciarRastreoGPS(context: Context) {
        if (locationService == null) {
            locationService = LocationService(context.applicationContext)
        }
        viewModelScope.launch {
            locationService?.obtenerUbicacionFlow()
                ?.catch { e -> e.printStackTrace() }
                ?.collect { location ->
                    _ubicacionActual.value = location
                }
        }
    }

    fun seleccionarPunto(punto: PuntoInteres?) {
        _puntoSeleccionado.value = punto
    }

    // Calcula la distancia en metros entre el usuario y un punto de interés
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

    // Marca un punto como completado usando map para crear una lista nueva (inmutable).
    fun marcarCompletado(id: Int) = _puntos.update { lista ->
        lista.map { if (it.id == id) it.copy(completado = true) else it }
    }

    // Datos ficticios de prueba en Santiago.
    private fun puntosDePrueba() = listOf(
        PuntoInteres(1, "Plaza de Armas", "Centro histórico y cívico de Santiago", -33.4372, -70.6506, 520.0),
        PuntoInteres(2, "Cerro Santa Lucía", "Mirador histórico y parque urbano", -33.4403, -70.6436, 580.0),
        PuntoInteres(3, "Cerro San Cristóbal", "El mayor parque metropolitano de la capital", -33.4256, -70.6347, 800.0),
        PuntoInteres(4, "Palacio de La Moneda", "Sede de gobierno y patrimonio cultural", -33.4429, -70.6539, 521.0),
        PuntoInteres(5, "Parque Forestal", "Área verde emblemática junto al río Mapocho", -33.4361, -70.6428, 535.0),
    )
}