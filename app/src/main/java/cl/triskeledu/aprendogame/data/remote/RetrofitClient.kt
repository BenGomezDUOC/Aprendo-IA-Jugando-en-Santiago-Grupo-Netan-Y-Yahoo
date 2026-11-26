
package cl.triskeledu.aprendogame.data.remote

import android.os.Build
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

    private val isEmulator: Boolean
        get() = Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")

    // Dirección del computador: 10.0.2.2 para emulador, 127.0.0.1 para celular físico (con adb reverse).
    val HOST: String
        get() = if (isEmulator) "10.0.2.2" else "127.0.0.1"

    // API Gateway Centralizado (Puerto 8080)
    val GATEWAY_URL: String
        get() = "http://$HOST:8080/"

    // Microservicios Spring Boot a través del Gateway unificado
    private val CLIENTES_URL: String
        get() = GATEWAY_URL
    private val PREMIOS_URL: String
        get() = GATEWAY_URL
    private val LOCALIZACIONES_URL: String
        get() = GATEWAY_URL

    // Obtener URL completa para recursos multimedia servidos desde el backend
    fun resolverUrlMultimedia(rutaRelativa: String?): String? {
        if (rutaRelativa.isNullOrBlank()) return null
        return if (rutaRelativa.startsWith("http://") || rutaRelativa.startsWith("https://")) {
            rutaRelativa
        } else {
            val limpia = if (rutaRelativa.startsWith("/")) rutaRelativa.substring(1) else rutaRelativa
            "http://$HOST:8080/$limpia"
        }
    }

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
