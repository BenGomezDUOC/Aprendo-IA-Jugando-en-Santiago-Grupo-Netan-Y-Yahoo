package cl.triskeledu.aprendogame.data.remote.api

import cl.triskeledu.aprendogame.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ClientesApiService {
    @POST("api/clientes/usuarios/registro")
    suspend fun registrar(@Body request: RegistroRequestDto): Response<UsuarioResponseDto>

    @POST("api/clientes/usuarios/login")
    suspend fun login(@Body request: LoginRequestDto): Response<UsuarioResponseDto>

    @GET("api/clientes/usuarios/{id}")
    suspend fun obtenerPorId(@Path("id") id: Long): Response<UsuarioResponseDto>

    @PUT("api/clientes/usuarios/{id}/sumar-puntos")
    suspend fun sumarPuntos(
        @Path("id") id: Long,
        @Query("puntos") puntos: Int
    ): Response<UsuarioResponseDto>
}

interface LocalizacionesApiService {
    @GET("api/localizaciones/puntos-interes")
    suspend fun obtenerPuntos(): Response<List<PuntoInteresDto>>

    @GET("api/localizaciones/puntos-interes/{id}")
    suspend fun obtenerPuntoPorId(@Path("id") id: Long): Response<PuntoInteresDto>
}

interface PremiosApiService {
    @GET("api/premios")
    suspend fun obtenerPremios(): Response<List<PremioDto>>
}
