
package cl.triskeledu.aprendogame.data.remote.dto

data class VideoRegistradoBackendDto(
    val id: Long? = null,
    val titulo: String,
    val descripcion: String = "",
    val urlVideo: String,
    val contenidoId: Long,
    val autor: String = "",
    val duracion: Long = 0L,
    val urlMiniatura: String = ""
)
