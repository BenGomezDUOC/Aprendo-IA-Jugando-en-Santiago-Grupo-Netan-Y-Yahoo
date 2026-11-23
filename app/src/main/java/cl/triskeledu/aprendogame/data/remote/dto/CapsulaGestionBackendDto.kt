
package cl.triskeledu.aprendogame.data.remote.dto

data class CapsulaGestionBackendDto(
    val id: Long? = null,
    val puntoId: Long,
    val contenidoId: Long,
    val videoId: Long,
    val codigoQr: String? = null,
    val titulo: String,
    val descripcion: String = "",
    val urlVideo: String = "",
    val categoria: String = "General",
    val estado: String = "BORRADOR"
)
