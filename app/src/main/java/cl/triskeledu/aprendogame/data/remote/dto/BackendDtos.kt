package cl.triskeledu.aprendogame.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RegistroRequestDto(
    @SerializedName("nombreCompleto") val nombreCompleto: String,
    @SerializedName("correoElectronico") val correoElectronico: String,
    @SerializedName("contrasena") val contrasena: String,
    @SerializedName("avatarSeleccionado") val avatarSeleccionado: String = "avatar_explorador_1",
    @SerializedName("institucion") val institucion: String = "Triskeledu",
    @SerializedName("rol") val rol: String = "ESTUDIANTE"
)

data class LoginRequestDto(
    @SerializedName("correoElectronico") val correoElectronico: String,
    @SerializedName("contrasena") val contrasena: String
)

data class UsuarioResponseDto(
    @SerializedName("id") val id: Long,
    @SerializedName("nombreCompleto") val nombreCompleto: String,
    @SerializedName("correoElectronico") val correoElectronico: String,
    @SerializedName("avatarSeleccionado") val avatarSeleccionado: String?,
    @SerializedName("institucion") val institucion: String?,
    @SerializedName("rol") val rol: String,
    @SerializedName("puntosAcumulados") val puntosAcumulados: Int,
    @SerializedName("nivelActual") val nivelActual: Int,
    @SerializedName("fechaRegistro") val fechaRegistro: String?
)

data class PuntoInteresDto(
    @SerializedName("id") val id: Long?,
    @SerializedName("codigo") val codigo: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("latitud") val latitud: Double,
    @SerializedName("longitud") val longitud: Double,
    @SerializedName("puntosRecompensa") val puntosRecompensa: Int,
    @SerializedName("qrEsperado") val qrEsperado: String
)

data class PremioDto(
    @SerializedName("id") val id: Long?,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("puntosRequeridos") val puntosRequeridos: Int,
    @SerializedName("stockDisponible") val stockDisponible: Int,
    @SerializedName("imagenUrl") val imagenUrl: String?
)
