package cl.triskeledu.aprendogame.data.remote.api

import cl.triskeledu.aprendogame.data.remote.dto.OpenMeteoResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ClimaApiService {

    @GET("v1/forecast")
    suspend fun obtenerClimaActual(
        @Query("latitude") latitude: Double = -33.4489,
        @Query("longitude") longitude: Double = -70.6693,
        @Query("current") currentFields: String = "temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m",
        @Query("timezone") timezone: String = "America/Santiago"
    ): Response<OpenMeteoResponse>
}
