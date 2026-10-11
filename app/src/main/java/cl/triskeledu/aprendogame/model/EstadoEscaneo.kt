package cl.triskeledu.aprendogame.model

// Resultado de escanear un código QR. Al ser sealed, el 'when' obliga a cubrir todos los casos.
sealed class EstadoEscaneo {
    data object Esperando : EstadoEscaneo()
    data class Valido(val capsula: Capsula) : EstadoEscaneo()
    data class Invalido(val mensaje: String) : EstadoEscaneo()
}