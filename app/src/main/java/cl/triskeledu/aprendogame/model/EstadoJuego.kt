package cl.triskeledu.aprendogame.model

// Estados posibles del juego. Al ser sealed, el 'when' obliga a cubrirlos todos.
sealed class EstadoJuego {
    data object Cargando : EstadoJuego()
    data class Jugando(
        val pregunta: Pregunta,
        val numero: Int,
        val total: Int,
        val puntaje: Int,
    ) : EstadoJuego()
    data class Terminado(val puntaje: Int, val total: Int) : EstadoJuego()
}