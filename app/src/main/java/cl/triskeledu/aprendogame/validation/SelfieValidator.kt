package cl.triskeledu.aprendogame.validation

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.File
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

object SelfieValidator {

    suspend fun validar(
        context: Context,
        rutaFoto: String,
        codigoQrEsperado: String
    ): Boolean {

        val archivo = File(rutaFoto)

        if (!archivo.isFile || codigoQrEsperado.isBlank()) {
            return false
        }

        val imagen = InputImage.fromFilePath(
            context,
            Uri.fromFile(archivo)
        )

        val opcionesRostro = FaceDetectorOptions.Builder()
            .setPerformanceMode(
                FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE
            )
            .build()

        val detectorRostros =
            FaceDetection.getClient(opcionesRostro)

        val lectorQr = BarcodeScanning.getClient()

        return try {

            // Comprueba que exista al menos un rostro.
            val rostros = suspendCancellableCoroutine<Boolean> {
                    continuacion ->

                detectorRostros.process(imagen)
                    .addOnSuccessListener { resultado ->
                        if (continuacion.isActive) {
                            continuacion.resume(resultado.isNotEmpty())
                        }
                    }
                    .addOnFailureListener {
                        if (continuacion.isActive) {
                            continuacion.resume(false)
                        }
                    }
            }

            if (!rostros) {
                false
            } else {

                // Comprueba que aparezca el QR correcto.
                suspendCancellableCoroutine<Boolean> {
                        continuacion ->

                    lectorQr.process(imagen)
                        .addOnSuccessListener { codigos ->

                            val qrCorrecto = codigos.any { codigo ->
                                codigo.format == Barcode.FORMAT_QR_CODE &&
                                        codigo.rawValue == codigoQrEsperado
                            }

                            if (continuacion.isActive) {
                                continuacion.resume(qrCorrecto)
                            }
                        }
                        .addOnFailureListener {
                            if (continuacion.isActive) {
                                continuacion.resume(false)
                            }
                        }
                }
            }

        } finally {
            detectorRostros.close()
            lectorQr.close()
        }
    }
}