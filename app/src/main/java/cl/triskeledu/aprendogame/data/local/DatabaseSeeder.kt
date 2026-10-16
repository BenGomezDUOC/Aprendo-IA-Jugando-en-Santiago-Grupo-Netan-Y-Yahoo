package cl.triskeledu.aprendogame.data.local

import cl.triskeledu.aprendogame.model.Capsula
import cl.triskeledu.aprendogame.model.PuntoInteres
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Proveedor de datos precargados (Data Seeding) para cumplir con el requerimiento
 * de operación autónoma y 100% offline desde el primer segundo.
 */
object DatabaseSeeder {

    fun seedDatabase(database: AppDatabase) {
        CoroutineScope(Dispatchers.IO).launch {
            // 1. Poblar Puntos Turísticos de Santiago
            val puntos = listOf(
                PuntoInteres(
                    id = 1,
                    nombre = "Plaza de Armas",
                    descripcion = "Corazón cívico e histórico de la capital.",
                    latitud = -33.4372,
                    longitud = -70.6506,
                    altitud = 520.0,
                    completado = false,
                    radioToleranciaMetros = 50.0
                ),
                PuntoInteres(
                    id = 2,
                    nombre = "Cerro Santa Lucía",
                    descripcion = "Hito fundacional con miradores y jardines históricos.",
                    latitud = -33.4403,
                    longitud = -70.6436,
                    altitud = 580.0,
                    completado = false,
                    radioToleranciaMetros = 60.0
                ),
                PuntoInteres(
                    id = 3,
                    nombre = "Cerro San Cristóbal",
                    descripcion = "Gran pulmón verde y mirador panorámico de Santiago.",
                    latitud = -33.4256,
                    longitud = -70.6347,
                    altitud = 800.0,
                    completado = false,
                    radioToleranciaMetros = 80.0
                ),
                PuntoInteres(
                    id = 4,
                    nombre = "Palacio de La Moneda",
                    descripcion = "Sede de gobierno y centro cultural cívico.",
                    latitud = -33.4429,
                    longitud = -70.6539,
                    altitud = 521.0,
                    completado = false,
                    radioToleranciaMetros = 50.0
                ),
                PuntoInteres(
                    id = 5,
                    nombre = "Parque Forestal",
                    descripcion = "Parque lineal tradicional junto al río Mapocho.",
                    latitud = -33.4361,
                    longitud = -70.6428,
                    altitud = 535.0,
                    completado = false,
                    radioToleranciaMetros = 70.0
                ),
                PuntoInteres(
                    id = 6,
                    nombre = "Barrio Bellavista",
                    descripcion = "Polo cultural, artístico y gastronómico de Santiago.",
                    latitud = -33.4335,
                    longitud = -70.6358,
                    altitud = 540.0,
                    completado = false,
                    radioToleranciaMetros = 60.0
                )
            )
            database.puntoInteresDao().insertarPuntos(puntos)

            // 2. Poblar Cápsulas Educativas sobre Inteligencia Artificial asociadas a cada QR
            val capsulas = listOf(
                Capsula(
                    id = 1,
                    puntoId = 1,
                    titulo = "IA en la Historia Urbana",
                    codigoQr = "QR_PLAZA_ARMAS",
                    urlVideo = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    puntos = 100,
                    descripcion = "Cómo los algoritmos de visión por computadora reconstruyen archivos históricos de la ciudad."
                ),
                Capsula(
                    id = 2,
                    puntoId = 2,
                    titulo = "Visión Artificial y Topografía",
                    codigoQr = "QR_SANTA_LUCIA",
                    urlVideo = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    puntos = 150,
                    descripcion = "Reconocimiento de patrones del terreno mediante modelos de satélite y redes convolucionales."
                ),
                Capsula(
                    id = 3,
                    puntoId = 3,
                    titulo = "Redes Neuronales y Panorámicas",
                    codigoQr = "QR_SAN_CRISTOBAL",
                    urlVideo = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    puntos = 200,
                    descripcion = "Monitoreo de calidad del aire y sensores urbanos inteligentes usando Deep Learning."
                ),
                Capsula(
                    id = 4,
                    puntoId = 4,
                    titulo = "Gobernanza y Ética en IA",
                    codigoQr = "QR_LA_MONEDA",
                    urlVideo = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    puntos = 100,
                    descripcion = "Políticas públicas, sesgo algorítmico y transparencia para una ciudadanía digital protegida."
                ),
                Capsula(
                    id = 5,
                    puntoId = 5,
                    titulo = "IA y Sustentabilidad Ambiental",
                    codigoQr = "QR_PARQUE_FORESTAL",
                    urlVideo = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                    puntos = 120,
                    descripcion = "Optimización del riego urbano y biodiversidad mediante gemelos digitales e IA."
                ),
                Capsula(
                    id = 6,
                    puntoId = 6,
                    titulo = "Creatividad e IA Generativa",
                    codigoQr = "QR_BELLAVISTA",
                    urlVideo = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoy.mp4",
                    puntos = 130,
                    descripcion = "Exploración del arte interactivo, música y síntesis multimedia con Transformers."
                )
            )
            database.capsulaDao().insertarCapsulas(capsulas)

        }
    }
}
