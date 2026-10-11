package cl.triskeledu.aprendogame.viewmodel

import androidx.lifecycle.ViewModel
import cl.triskeledu.aprendogame.model.PuntoInteres
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel : ViewModel() {

    // Backing property: editable solo aquí, de solo lectura para la interfaz.
    private val _puntos = MutableStateFlow(puntosDePrueba())
    val puntos: StateFlow<List<PuntoInteres>> = _puntos.asStateFlow()

    // Marca un punto como completado usando map para crear una lista nueva (inmutable).
    fun marcarCompletado(id: Int) = _puntos.update { lista ->
        lista.map { if (it.id == id) it.copy(completado = true) else it }
    }

    // Datos ficticios de prueba. Luego se reemplazarán por Room.
    private fun puntosDePrueba() = listOf(
        PuntoInteres(1, "Plaza de Armas", "Centro histórico de Santiago", -33.4372, -70.6506, 520.0),
        PuntoInteres(2, "Cerro Santa Lucía", "Mirador y parque en el centro", -33.4403, -70.6436, 580.0),
        PuntoInteres(3, "Cerro San Cristóbal", "El mayor parque urbano de la ciudad", -33.4256, -70.6347, 800.0),
        PuntoInteres(4, "Palacio de La Moneda", "Sede de gobierno", -33.4429, -70.6539, 521.0),
        PuntoInteres(5, "Parque Forestal", "Parque junto al río Mapocho", -33.4361, -70.6428, 535.0),
    )
}