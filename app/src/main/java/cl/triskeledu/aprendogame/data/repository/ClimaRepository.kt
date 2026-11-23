
package cl.triskeledu.aprendogame.data.repository

import android.util.Log

import cl.triskeledu.aprendogame.data.remote.RetrofitClient
import cl.triskeledu.aprendogame.data.remote.dto.ClimaSantiagoInfo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ClimaRepository {

    private val TAG = "ClimaRepository"

    // --------------------------------------------
    // CONSULTAR CLIMA DE SANTIAGO
    // --------------------------------------------

    suspend fun obtenerClimaSantiago(): Result<ClimaSantiagoInfo> =
        withContext(Dispatchers.IO) {

            try {

                val response =
                    RetrofitClient.climaApi.obtenerClimaActual()

                val current = response.body()?.current

                if (response.isSuccessful && current != null) {

                    val (descripcion, icono, recomendacion) =
                        interpretarCodigoClima(current.weatherCode)

                    val info = ClimaSantiagoInfo(
                        temperatura = current.temperature,
                        humedad = current.humidity,
                        velocidadViento = current.windSpeed,
                        estadoDescripcion = descripcion,
                        icono = icono,
                        recomendacionExploracion = recomendacion
                    )

                    Result.success(info)

                } else {

                    Result.failure(
                        Exception(
                            "Error en respuesta de API Open-Meteo: ${response.code()}"
                        )
                    )
                }

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "Error consultando el clima de Santiago",
                    e
                )

                Result.failure(e)
            }
        }

    // --------------------------------------------
    // INTERPRETAR CÓDIGOS DEL CLIMA
    // --------------------------------------------

    private fun interpretarCodigoClima(
        code: Int
    ): Triple<String, String, String> {

        return when (code) {

            0 -> Triple(
                "Cielo Despejado",
                "☀️",
                "¡Excelente día soleado en Santiago para buscar cápsulas y escanear códigos QR!"
            )

            1, 2, 3 -> Triple(
                "Parcialmente Nublado",
                "⛅",
                "Buen clima para caminatas turísticas por los hitos históricos de la ciudad."
            )

            45, 48 -> Triple(
                "Neblina",
                "🌫️",
                "Visibilidad reducida en la capital, ten precaución en tus desplazamientos."
            )

            51, 53, 55, 61, 63, 65 -> Triple(
                "Lluvias / Chubascos",
                "🌧️",
                "Lleva paraguas o impermeable para proteger tu dispositivo al registrar evidencias."
            )

            71, 73, 75, 77 -> Triple(
                "Nieve",
                "❄️",
                "Frío intenso en la precordillera de Santiago, ¡abrígate bien!"
            )

            80, 81, 82 -> Triple(
                "Chubascos Aislados",
                "🌦️",
                "Lluvias intermitentes, planifica tus visitas a cápsulas bajo techo si es necesario."
            )

            95, 96, 99 -> Triple(
                "Tormenta Eléctrica",
                "⛈️",
                "Precaución por tormenta en la zona urbana, prioriza cápsulas seguras."
            )

            else -> Triple(
                "Clima Templado",
                "🌤️",
                "Condiciones favorables para completar los desafíos de Triskeledu."
            )
        }
    }
}
