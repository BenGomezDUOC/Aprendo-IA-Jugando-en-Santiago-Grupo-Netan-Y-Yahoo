package cl.triskeledu.aprendogame.data.remote.api

import cl.triskeledu.aprendogame.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ClientesApiService {
    @POST("api/clientes/usuarios/registro")
    suspend fun registrar(@Body request: RegistroBackendRequestDto): Response<UsuarioBackendDto>

    @POST("api/clientes/usuarios/login")
    suspend fun login(@Body request: LoginBackendRequestDto): Response<SesionBackendDto>

    @GET("api/clientes/usuarios/{id}")
    suspend fun obtenerPorId(
        @Header("Authorization") authorization: String,
        @Path("id") id: Long
    ): Response<UsuarioBackendDto>
}

interface LocalizacionesApiService {
    @GET("api/localizaciones/capsulas/validar-qr")
    suspend fun validarCapsulaQr(
        @Header("Authorization") authorization: String,
        @Query("codigo") codigo: String
    ): Response<CapsulaBackendDto>

    @GET("api/localizaciones/puntos-interes")
    suspend fun obtenerPuntos(
        @Header("Authorization") authorization: String
    ): Response<List<PuntoInteresBackendDto>>

    @GET("api/localizaciones/puntos-interes/{id}")
    suspend fun obtenerPuntoPorId(
        @Header("Authorization") authorization: String,
        @Path("id") id: Long
    ): Response<PuntoInteresBackendDto>


    @PUT("api/localizaciones/puntos-interes/{id}")
    suspend fun actualizarPunto(
        @Header("Authorization") authorization: String,
        @Path("id") id: Long,
        @Body punto: PuntoInteresBackendDto
    ): Response<PuntoInteresBackendDto>


    @GET("api/localizaciones/contenidos")
    suspend fun obtenerContenidos(
        @Header("Authorization") authorization: String
    ): Response<List<ContenidoEducativoBackendDto>>

    @POST("api/localizaciones/contenidos")
    suspend fun crearContenido(
        @Header("Authorization") authorization: String,
        @Body contenido: ContenidoEducativoBackendDto
    ): Response<ContenidoEducativoBackendDto>

    @PUT("api/localizaciones/contenidos/{id}")
    suspend fun actualizarContenido(
        @Header("Authorization") authorization: String,
        @Path("id") id: Long,
        @Body contenido: ContenidoEducativoBackendDto
    ): Response<ContenidoEducativoBackendDto>


    @GET("api/localizaciones/videos")
    suspend fun obtenerVideos(
        @Header("Authorization") authorization: String
    ): Response<List<VideoRegistradoBackendDto>>

    @POST("api/localizaciones/videos")
    suspend fun crearVideo(
        @Header("Authorization") authorization: String,
        @Body video: VideoRegistradoBackendDto
    ): Response<VideoRegistradoBackendDto>

    @PUT("api/localizaciones/videos/{id}")
    suspend fun actualizarVideo(
        @Header("Authorization") authorization: String,
        @Path("id") id: Long,
        @Body video: VideoRegistradoBackendDto
    ): Response<VideoRegistradoBackendDto>

    @Multipart
    @POST("api/localizaciones/multimedia/video")
    suspend fun subirVideo(
        @Header("Authorization") authorization: String,
        @Part archivo: MultipartBody.Part
    ): Response<Map<String, String>>

    @Multipart
    @POST("api/localizaciones/multimedia/imagen")
    suspend fun subirMiniatura(
        @Header("Authorization") authorization: String,
        @Part archivo: MultipartBody.Part
    ): Response<Map<String, String>>


    @GET("api/localizaciones/capsulas")
    suspend fun obtenerCapsulasBackend(
        @Header("Authorization") authorization: String
    ): Response<List<CapsulaGestionBackendDto>>

    @POST("api/localizaciones/capsulas")
    suspend fun crearCapsulaBackend(
        @Header("Authorization") authorization: String,
        @Body capsula: CapsulaGestionBackendDto
    ): Response<CapsulaGestionBackendDto>

    @PUT("api/localizaciones/capsulas/{id}/publicar")
    suspend fun publicarCapsulaBackend(
        @Header("Authorization") authorization: String,
        @Path("id") id: Long
    ): Response<CapsulaGestionBackendDto>

    @POST("api/localizaciones/puntos-interes")
    suspend fun crearPunto(
        @Header("Authorization") authorization: String,
        @Body punto: PuntoInteresBackendDto
    ): Response<PuntoInteresBackendDto>


    @DELETE("api/localizaciones/puntos-interes/{id}")
    suspend fun eliminarPunto(
        @Header("Authorization") authorization: String,
        @Path("id") id: Long
    ): Response<Void>


}

interface PremiosApiService {
    @POST("api/premios/gamificacion/escanear-qr")
    suspend fun escanearQr(
        @Header("Authorization") authorization: String,
        @Body request: EscanearQrBackendRequestDto
    ): Response<EscanearQrBackendResponseDto>

    @Multipart
    @POST("api/premios/gamificacion/visita-selfie")
    suspend fun registrarSelfie(
        @Header("Authorization") authorization: String,
        @Part("puntoId") puntoId: RequestBody,
        @Part("capsulaId") capsulaId: RequestBody,
        @Part("codigoQrEsperado") codigoQrEsperado: RequestBody,
        @Part archivo: MultipartBody.Part
    ): Response<VisitaSelfieBackendResponseDto>


    @POST("api/premios/gamificacion/video-visto")
    suspend fun registrarVideoVistoBackend(
        @Header("Authorization") authorization: String,
        @Query("capsulaId") capsulaId: Long
    ): Response<Void>

    @POST("api/premios/gamificacion/video-adicional-visto")
    suspend fun registrarVideoAdicionalVistoBackend(
        @Header("Authorization") authorization: String,
        @Query("videoAdicionalId") videoAdicionalId: Int
    ): Response<Void>

}
