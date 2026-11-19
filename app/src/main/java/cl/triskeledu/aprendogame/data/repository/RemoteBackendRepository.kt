package cl.triskeledu.aprendogame.data.repository

import android.util.Log
import cl.triskeledu.aprendogame.data.remote.RetrofitClient
import cl.triskeledu.aprendogame.data.remote.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio que orquesta la sincronización con los microservicios en Spring Boot:
 * - ms-clientes (puerto 8081)
 * - ms-premios (puerto 8082)
 * - ms-localizaciones (puerto 8083)
 */
class RemoteBackendRepository {

    private val TAG = "RemoteBackendRepo"

    suspend fun sincronizarRegistro(
        nombreCompleto: String,
        correo: String,
        contrasena: String
    ): Result<UsuarioResponseDto> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.clientesApi.registrar(
                RegistroRequestDto(
                    nombreCompleto = nombreCompleto,
                    correoElectronico = correo,
                    contrasena = contrasena
                )
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al registrar en backend: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción en registro remoto", e)
            Result.failure(e)
        }
    }

    suspend fun sincronizarLogin(
        correo: String,
        contrasena: String
    ): Result<UsuarioResponseDto> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.clientesApi.login(
                LoginRequestDto(
                    correoElectronico = correo,
                    contrasena = contrasena
                )
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al autenticar en backend: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción en login remoto", e)
            Result.failure(e)
        }
    }

    suspend fun sumarPuntosRemoto(
        usuarioId: Long,
        puntos: Int
    ): Result<UsuarioResponseDto> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.clientesApi.sumarPuntos(usuarioId, puntos)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al actualizar puntos en backend: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al sumar puntos remoto", e)
            Result.failure(e)
        }
    }

    suspend fun obtenerPuntosInteresRemotos(): Result<List<PuntoInteresDto>> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.localizacionesApi.obtenerPuntos()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener localizaciones: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al obtener localizaciones", e)
            Result.failure(e)
        }
    }

    suspend fun obtenerPremiosRemotos(): Result<List<PremioDto>> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.premiosApi.obtenerPremios()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener premios: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al obtener premios", e)
            Result.failure(e)
        }
    }
}
