
package cl.triskeledu.aprendogame.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Datos geográficos obtenidos automáticamente.
data class UbicacionGeografica(
    val direccion: String,
    val latitud: Double,
    val longitud: Double,
    val altitud: Double
)

class ServicioGeografico(
    private val context: Context
) {

    // Busca una dirección y obtiene sus coordenadas.
    suspend fun buscarDireccion(
        direccion: String
    ): List<Address> {

        require(direccion.isNotBlank()) {
            "Ingresa una dirección."
        }

        if (!Geocoder.isPresent()) {
            throw IllegalStateException(
                "El dispositivo no dispone de un servicio de búsqueda de direcciones."
            )
        }

        val geocoder = Geocoder(
            context.applicationContext,
            Locale("es", "CL")
        )

        val consulta = "${direccion.trim()}, Santiago, Chile"

        return withTimeout(20000L) {

            val resultados = if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            ) {

                suspendCancellableCoroutine<List<Address>> { continuation ->

                    geocoder.getFromLocationName(
                        consulta,
                        5,
                        object : Geocoder.GeocodeListener {

                            override fun onGeocode(
                                addresses: MutableList<Address>
                            ) {

                                if (continuation.isActive) {
                                    continuation.resume(addresses)
                                }
                            }

                            override fun onError(
                                errorMessage: String?
                            ) {

                                if (continuation.isActive) {
                                    continuation.resumeWithException(
                                        IllegalStateException(
                                            errorMessage
                                                ?: "No se pudo buscar la dirección."
                                        )
                                    )
                                }
                            }
                        }
                    )
                }

            } else {

                withContext(Dispatchers.IO) {

                    @Suppress("DEPRECATION")
                    geocoder.getFromLocationName(
                        consulta,
                        5
                    ).orEmpty()
                }
            }

            resultados.filter { punto ->

                punto.hasLatitude() &&
                        punto.hasLongitude() &&
                        punto.countryCode.equals(
                            "CL",
                            ignoreCase = true
                        )
            }
        }
    }

    // Consulta la altitud del terreno en Open-Meteo.
    suspend fun obtenerAltitud(
        latitud: Double,
        longitud: Double
    ): Double = withContext(Dispatchers.IO) {

        val url = URL(
            "https://api.open-meteo.com/v1/elevation" +
                    "?latitude=$latitud" +
                    "&longitude=$longitud"
        )

        val conexion =
            url.openConnection() as HttpURLConnection

        try {

            conexion.requestMethod = "GET"
            conexion.connectTimeout = 10000
            conexion.readTimeout = 10000

            if (conexion.responseCode != 200) {

                throw IllegalStateException(
                    "No se pudo consultar la altitud."
                )
            }

            val respuesta = conexion.inputStream
                .bufferedReader()
                .use { it.readText() }

            val json = JSONObject(respuesta)

            val altitud = json.getJSONArray("elevation")
                .getDouble(0)

            require(altitud.isFinite()) {
                "La altitud obtenida no es válida."
            }

            altitud

        } finally {

            conexion.disconnect()
        }
    }

    // Construye la ubicación completa a guardar.
    suspend fun obtenerUbicacion(
        direccion: Address
    ): UbicacionGeografica {

        val latitud = direccion.latitude
        val longitud = direccion.longitude

        val altitud = obtenerAltitud(
            latitud,
            longitud
        )

        return UbicacionGeografica(

            direccion = direccion.getAddressLine(0)
                ?: "",

            latitud = latitud,

            longitud = longitud,

            altitud = altitud
        )
    }
}

