package cl.triskeledu.aprendogame.model

// Cápsula de contenido sobre IA, asociada a un código QR y a un punto del mapa.
data class Capsula(
    val id: Int,
    val titulo: String,
    val codigoQr: String,
    val urlVideo: String,
    val puntos: Int,
    val puntoId: Int,
)