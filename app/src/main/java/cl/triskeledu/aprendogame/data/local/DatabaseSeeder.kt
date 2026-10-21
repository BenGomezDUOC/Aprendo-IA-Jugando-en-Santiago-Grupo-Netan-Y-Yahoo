
package cl.triskeledu.aprendogame.data.local

import cl.triskeledu.aprendogame.model.Capsula
import cl.triskeledu.aprendogame.model.PuntoInteres
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Carga los puntos turísticos y cápsulas iniciales.
 *
 * Los datos se insertan solamente si no existen.
 * No sobrescribe los cambios realizados por el Administrador.
 * Conserva los códigos QR y el progreso de los jugadores.
 */
object DatabaseSeeder {

    fun seedDatabase(database: AppDatabase) {

        CoroutineScope(Dispatchers.IO).launch {

            // 1. PUNTOS TURÍSTICOS INICIALES

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

            database.puntoInteresDao()
                .insertarPuntosIniciales(puntos)

            // 2. CÁPSULAS EDUCATIVAS INICIALES

            val capsulas = listOf(
                Capsula(
                    id = 1,
                    puntoId = 1,
                    titulo = "¿Qué es la Inteligencia Artificial?",
                    codigoQr = "QR_PLAZA_ARMAS",
                    urlVideo = "",
                    puntos = 10,
                    descripcion = "Autor: Magdiel Reynoso. Aprende qué es la inteligencia artificial, cómo reconoce imágenes, comprende preguntas y ayuda a estudiar la historia de Santiago."
                ),
                Capsula(
                    id = 2,
                    puntoId = 2,
                    titulo = "Reconocimiento de imágenes",
                    codigoQr = "QR_SANTA_LUCIA",
                    urlVideo = "",
                    puntos = 10,
                    descripcion = "Autor: Fernando Zapata. Descubre cómo la visión artificial reconoce objetos, plantas y monumentos mediante patrones aprendidos de imágenes."
                ),
                Capsula(
                    id = 3,
                    puntoId = 3,
                    titulo = "IA y medioambiente",
                    codigoQr = "QR_SAN_CRISTOBAL",
                    urlVideo = "",
                    puntos = 10,
                    descripcion = "Autor: Juan Pablo Taulis. Conoce cómo la inteligencia artificial analiza datos ambientales, estudia la calidad del aire y ayuda a proteger la naturaleza."
                ),
                Capsula(
                    id = 4,
                    puntoId = 4,
                    titulo = "Aprendizaje automático",
                    codigoQr = "QR_LA_MONEDA",
                    urlVideo = "",
                    puntos = 10,
                    descripcion = "Autor: Benjamín Gómez. Aprende qué es el machine learning y cómo los modelos utilizan datos para identificar patrones y realizar predicciones."
                ),
                Capsula(
                    id = 5,
                    puntoId = 5,
                    titulo = "Redes neuronales",
                    codigoQr = "QR_PARQUE_FORESTAL",
                    urlVideo = "",
                    puntos = 10,
                    descripcion = "Autor: Juan Pablo Taulis. Descubre cómo funcionan las redes neuronales artificiales y cómo pueden reconocer plantas, voces y otros patrones."
                ),
                Capsula(
                    id = 6,
                    puntoId = 6,
                    titulo = "Inteligencia Artificial generativa",
                    codigoQr = "QR_BELLAVISTA",
                    urlVideo = "",
                    puntos = 10,
                    descripcion = "Autor: Fernando Zapata. Aprende cómo la IA generativa puede crear textos, imágenes y sonidos, y por qué debemos utilizarla responsablemente."
                )
            )

            // IGNORE evita reemplazar las cápsulas existentes.
            database.capsulaDao()
                .insertarCapsulasIniciales(capsulas)

            // IMPORTANTE:
            // No actualizar aquí las cápsulas existentes.
            // Sus datos ahora pueden ser modificados por el Administrador.
        }
    }
}
