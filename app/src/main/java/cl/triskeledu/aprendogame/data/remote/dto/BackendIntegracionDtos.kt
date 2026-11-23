package cl.triskeledu.aprendogame.data.remote.dto

// Contratos verificados contra los controladores Java recibidos.
data class RegistroBackendRequestDto(
    val nombre: String,
    val alias: String,
    val correo: String,
    val contrasena: String,
    val telefono: String,
    val direccion: String = "",
    val avatar: Int = 1,
    val rol: String = "CLIENTE"
)

data class LoginBackendRequestDto(
    val correo: String,
    val contrasena: String
)

data class UsuarioBackendDto(
    val id: Long,
    val nombre: String,
    val alias: String,
    val correo: String,
    val telefono: String? = null,
    val direccion: String? = null,
    val avatar: Int? = null,
    val rol: String,
    val puntosAcumulados: Int = 0,
    val fechaRegistro: String? = null,
    val tipoCuentaSistema: String? = null,
    val nivelActual: Int? = null
)

data class SesionBackendDto(
    val token: String,
    val tipoToken: String = "Bearer",
    val expiraEn: Long? = null,
    val usuario: UsuarioBackendDto
)

data class PuntoInteresBackendDto(
    val id: Long,
    val codigo: String? = null,
    val nombre: String,
    val descripcion: String? = null,
    val direccion: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val altitud: Double? = null,
    val radioToleranciaMetros: Double? = null,
    val imagenUri: String? = null,
    val completado: Boolean? = null,
    val puntosRecompensa: Int? = null,
    val qrEsperado: String? = null
)

data class EscanearQrBackendRequestDto(
    val usuarioId: Long,
    val codigoQr: String,
    val puntoIdSeleccionado: Long? = null
)

// Respuesta que produce GamificacionService (JSON/mapa).
data class EscanearQrBackendResponseDto(
    val nuevoDescubrimiento: Boolean? = null,
    val puntosGanados: Int? = null,
    val totalPuntosUsuario: Int? = null,
    val mensaje: String? = null
)

data class VisitaSelfieBackendResponseDto(
    val nuevaVisita: Boolean? = null,
    val puntosGanados: Int? = null,
    val totalPuntosUsuario: Int? = null,
    val mensaje: String? = null
)

data class CapsulaBackendDto(
    val id: Long,
    val puntoId: Long,
    val codigoQr: String
)