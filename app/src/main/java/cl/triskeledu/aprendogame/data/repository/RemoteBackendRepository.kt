package cl.triskeledu.aprendogame.data.repository

import cl.triskeledu.aprendogame.data.remote.RetrofitClient
import cl.triskeledu.aprendogame.data.remote.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

/** Integración remota: IDs de MySQL, no los IDs de Room. */
class RemoteBackendRepository {

    private fun bearer(token: String) = "Bearer $token"

    private fun <T : Any> validar(response: Response<T>): T {
        return response.body()?.takeIf { response.isSuccessful }
            ?: throw IllegalStateException("Backend HTTP ${response.code()}: ${response.message()}")
    }

    suspend fun registrar(request: RegistroBackendRequestDto): Result<UsuarioBackendDto> =
        withContext(Dispatchers.IO) {
            runCatching { validar(RetrofitClient.clientesApi.registrar(request)) }
        }

    suspend fun login(correo: String, contrasena: String): Result<SesionBackendDto> =
        withContext(Dispatchers.IO) {
            runCatching {
                validar(RetrofitClient.clientesApi.login(LoginBackendRequestDto(correo, contrasena)))
            }
        }

    suspend fun obtenerPuntos(token: String): Result<List<PuntoInteresBackendDto>> =
        withContext(Dispatchers.IO) {
            runCatching { validar(RetrofitClient.localizacionesApi.obtenerPuntos(bearer(token))) }
        }

    suspend fun escanearQr(
        token: String,
        usuarioIdBackend: Long,
        codigoQr: String,
        puntoIdBackend: Long? = null
    ): Result<EscanearQrBackendResponseDto> = withContext(Dispatchers.IO) {
        runCatching {
            validar(RetrofitClient.premiosApi.escanearQr(
                bearer(token), EscanearQrBackendRequestDto(usuarioIdBackend, codigoQr, puntoIdBackend)
            ))
        }
    }

    suspend fun registrarSelfie(
        token: String,
        puntoIdBackend: Long,
        capsulaIdBackend: Long,
        codigoQrEsperado: String,
        foto: File
    ): Result<VisitaSelfieBackendResponseDto> = withContext(Dispatchers.IO) {
        runCatching {
            require(foto.isFile) { "No existe el archivo de la selfie" }
            val textoPlano = "text/plain".toMediaType()
            val archivo = MultipartBody.Part.createFormData(
                "archivo", foto.name, foto.asRequestBody("image/jpeg".toMediaType())
            )
            validar(RetrofitClient.premiosApi.registrarSelfie(
                bearer(token),
                puntoIdBackend.toString().toRequestBody(textoPlano),
                capsulaIdBackend.toString().toRequestBody(textoPlano),
                codigoQrEsperado.toRequestBody(textoPlano),
                archivo
            ))
        }
    }
}
