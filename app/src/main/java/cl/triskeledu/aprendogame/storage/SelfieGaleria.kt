
package cl.triskeledu.aprendogame.storage

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import java.io.File

object SelfieGaleria {

    // Guarda la fotografía capturada en la galería
    // del teléfono, antes de validar el rostro y el QR.
    //
    // La fotografía permanece en la galería.
    // Solo el usuario decide cuándo eliminarla.

    fun guardar(
        context: Context,
        rutaFoto: String
    ): Uri? {

        // Busca el archivo que capturó CameraX.
        val archivo = File(rutaFoto)

        if (!archivo.isFile) {
            return null
        }

        // Utiliza MediaStore en Android 10 o superior.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return null
        }

        val resolver = context.contentResolver

        var uriCreada: Uri? = null

        return try {

            // Prepara la información de la fotografía.
            val valores = ContentValues().apply {

                put(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    archivo.name
                )

                put(
                    MediaStore.Images.Media.MIME_TYPE,
                    "image/jpeg"
                )

                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "Pictures/Triskeledu"
                )

                // Mantiene la imagen oculta mientras
                // termina de copiarse.
                put(
                    MediaStore.Images.Media.IS_PENDING,
                    1
                )
            }

            // Crea la entrada de la foto en la galería.
            val uri = resolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                valores
            ) ?: return null

            uriCreada = uri

            // Copia la fotografía capturada a la galería.
            resolver.openOutputStream(uri)?.use { salida ->

                archivo.inputStream().use { entrada ->

                    entrada.copyTo(salida)
                }

            } ?: throw IllegalStateException(
                "No se pudo escribir la fotografía"
            )

            // Publica la fotografía para que aparezca
            // en la galería del teléfono.
            val valoresFinales = ContentValues().apply {

                put(
                    MediaStore.Images.Media.IS_PENDING,
                    0
                )
            }

            val filas = resolver.update(
                uri,
                valoresFinales,
                null,
                null
            )

            if (filas <= 0) {
                throw IllegalStateException(
                    "No se pudo publicar la fotografía"
                )
            }

            // Devuelve la ubicación de la foto guardada.
            uri

        } catch (e: Exception) {

            Log.e(
                "SelfieGaleria",
                "Error al guardar fotografía",
                e
            )

            // Limpia únicamente una copia incompleta
            // si ocurrió un error durante el guardado.
            uriCreada?.let { uri ->
                try {
                    resolver.delete(uri, null, null)
                } catch (error: Exception) {
                    Log.e(
                        "SelfieGaleria",
                        "No se pudo limpiar la copia incompleta",
                        error
                    )
                }
            }

            null
        }
    }
}



