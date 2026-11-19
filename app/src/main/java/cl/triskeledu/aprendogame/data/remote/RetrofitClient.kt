package cl.triskeledu.aprendogame.data.remote

import cl.triskeledu.aprendogame.data.remote.api.ClientesApiService
import cl.triskeledu.aprendogame.data.remote.api.LocalizacionesApiService
import cl.triskeledu.aprendogame.data.remote.api.PremiosApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // En emulador Android estándar, 10.0.2.2 mapea a localhost de la máquina host.
    // Para dispositivo físico por Wi-Fi, reemplazar por la IP local de tu computador (ej. 192.168.1.X).
    private const val HOST = "10.0.2.2"

    private const val CLIENTES_URL = "http://$HOST:8081/"
    private const val PREMIOS_URL = "http://$HOST:8082/"
    private const val LOCALIZACIONES_URL = "http://$HOST:8083/"
    private const val OPEN_METEO_URL = "https://api.open-meteo.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val clientesApi: ClientesApiService by lazy {
        createRetrofit(CLIENTES_URL).create(ClientesApiService::class.java)
    }

    val premiosApi: PremiosApiService by lazy {
        createRetrofit(PREMIOS_URL).create(PremiosApiService::class.java)
    }

    val localizacionesApi: LocalizacionesApiService by lazy {
        createRetrofit(LOCALIZACIONES_URL).create(LocalizacionesApiService::class.java)
    }

    val climaApi: cl.triskeledu.aprendogame.data.remote.api.ClimaApiService by lazy {
        createRetrofit(OPEN_METEO_URL).create(cl.triskeledu.aprendogame.data.remote.api.ClimaApiService::class.java)
    }
}
