package cl.triskeledu.aprendogame.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.triskeledu.aprendogame.model.EstadoJuego
import cl.triskeledu.aprendogame.model.Pregunta
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {

    // Backing property: privada editable, pública solo de lectura (igual que el ejemplo 103).
    private val _estado = MutableStateFlow<EstadoJuego>(EstadoJuego.Cargando)
    val estado: StateFlow<EstadoJuego> = _estado.asStateFlow()

    private var preguntas: List<Pregunta> = emptyList()
    private var indice = 0
    private var puntaje = 0

    init {
        iniciar()
    }

    // Simula una carga lenta con delay y luego arranca el juego.
    fun iniciar() {
        viewModelScope.launch {
            _estado.value = EstadoJuego.Cargando
            preguntas = cargarPreguntas()
            indice = 0
            puntaje = 0
            mostrarPregunta()
        }
    }

    // suspend fun: puede pausarse sin bloquear la pantalla.
    private suspend fun cargarPreguntas(): List<Pregunta> {
        delay(1_500)
        return listOf(
            Pregunta(
                "¿Qué es el aprendizaje automático?",
                listOf("Programar cada regla a mano", "Aprender patrones desde datos", "Un tipo de pantalla"),
                1,
            ),
            Pregunta(
                "¿Qué necesita una IA para aprender a reconocer gatos?",
                listOf("Muchos ejemplos de imágenes", "Solo electricidad", "Un teclado"),
                0,
            ),
            Pregunta(
                "¿Una IA puede equivocarse?",
                listOf("Nunca", "Sí, y conviene verificar", "Solo los lunes"),
                1,
            ),
        )
    }

    fun responder(opcion: Int) {
        val actual = _estado.value
        if (actual !is EstadoJuego.Jugando) return
        if (opcion == actual.pregunta.correcta) puntaje++
        indice++
        mostrarPregunta()
    }

    private fun mostrarPregunta() {
        _estado.value = if (indice < preguntas.size) {
            EstadoJuego.Jugando(preguntas[indice], indice + 1, preguntas.size, puntaje)
        } else {
            EstadoJuego.Terminado(puntaje, preguntas.size)
        }
    }
}