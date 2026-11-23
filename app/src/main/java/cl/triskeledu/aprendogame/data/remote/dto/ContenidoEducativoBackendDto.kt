
package cl.triskeledu.aprendogame.data.remote.dto

data class ContenidoEducativoBackendDto(
    val id: Long? = null,
    val titulo: String,
    val descripcion: String = "",
    val categoria: String = "General",
    val estado: String = "BORRADOR",
    val puntoId: Long? = null,
    val codigoQr: String? = null,
    val generadorId: Long? = null
)
