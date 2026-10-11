package cl.triskeledu.aprendogame.model

// Un punto turístico georreferenciado que se muestra en el mapa.
data class PuntoInteres(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val latitud: Double,
    val longitud: Double,
    val altitud: Double,
    val completado: Boolean = false,
)