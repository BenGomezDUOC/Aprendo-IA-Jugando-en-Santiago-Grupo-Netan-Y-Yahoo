package cl.triskeledu.aprendogame.model

// Una pregunta del juego: texto, opciones y posición (índice) de la correcta.
data class Pregunta(
    val texto: String,
    val opciones: List<String>,
    val correcta: Int,
)