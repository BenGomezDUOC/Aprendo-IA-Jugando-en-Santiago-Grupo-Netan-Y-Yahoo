
package cl.triskeledu.aprendogame.data.remote

import cl.triskeledu.aprendogame.data.remote.api.ClientesApiService
import cl.triskeledu.aprendogame.data.remote.api.LocalizacionesApiService
import cl.triskeledu.aprendogame.data.remote.api.PremiosApiService
import cl.triskeledu.aprendogame.data.remote.api.ClimaApiService

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Dirección del computador desde el emulador Android.
    private const val HOST = "10.0.2.2"

    // Microservicios Spring Boot.
    private const val CLIENTES_URL = "http://$HOST:8081/"
    private const val PREMIOS_URL = "http://$HOST:8082/"
    private const val LOCALIZACIONES_URL = "http://$HOST:8083/"

    // Servicio del clima.
    private const val OPEN_METEO_URL =
        "https://api.open-meteo.com/"

    // No registrar contraseñas ni tokens en Logcat.
    private val loggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE
        }

    // Cliente HTTP compartido.
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // Crear conexión Retrofit.
    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Usuarios, inicio de sesión y perfiles.
    val clientesApi: ClientesApiService by lazy {
        createRetrofit(CLIENTES_URL)
            .create(ClientesApiService::class.java)
    }

    // QR, selfies y gamificación.
    val premiosApi: PremiosApiService by lazy {
        createRetrofit(PREMIOS_URL)
            .create(PremiosApiService::class.java)
    }

    // Puntos turísticos, contenidos y videos.
    val localizacionesApi: LocalizacionesApiService by lazy {
        createRetrofit(LOCALIZACIONES_URL)
            .create(LocalizacionesApiService::class.java)
    }

    // Clima de Santiago.
    val climaApi: ClimaApiService by lazy {
        createRetrofit(OPEN_METEO_URL)
            .create(ClimaApiService::class.java)
    }
}
