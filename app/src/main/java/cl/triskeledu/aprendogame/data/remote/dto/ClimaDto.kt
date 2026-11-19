package cl.triskeledu.aprendogame.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OpenMeteoResponse(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("current") val current: CurrentWeatherDto?
)

data class CurrentWeatherDto(
    @SerializedName("time") val time: String,
    @SerializedName("temperature_2m") val temperature: Double,
    @SerializedName("relative_humidity_2m") val humidity: Int,
    @SerializedName("weather_code") val weatherCode: Int,
    @SerializedName("wind_speed_10m") val windSpeed: Double
)

data class ClimaSantiagoInfo(
    val temperatura: Double,
    val humedad: Int,
    val velocidadViento: Double,
    val estadoDescripcion: String,
    val icono: String,
    val recomendacionExploracion: String
)
