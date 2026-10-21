
package cl.triskeledu.aprendogame.ui.screens

import android.util.Log
import android.widget.Toast

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Info

import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.core.content.FileProvider

import cl.triskeledu.aprendogame.storage.SelfieGaleria

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SelfieTuristicaScreen(
    onVolver: () -> Unit,
    onFotoGuardada: (String, (Boolean, String) -> Unit) -> Unit
) {

    val context = LocalContext.current

    // Permite guardar la foto sin bloquear la pantalla.
    val scope = rememberCoroutineScope()

    // Colores originales.
    val fondoOscuro = Color(0xFF101114)
    val fondoControles = Color(0xFF17181C)
    val textoSecundario = Color(0xFFBFC0C5)

    // --------------------------------------------------
    // ESTADOS DE LA FOTOGRAFÍA
    // --------------------------------------------------

    // Indica si se está capturando o guardando.
    var guardando by remember {
        mutableStateOf(false)
    }

    // Indica si ML Kit está validando.
    var validando by remember {
        mutableStateOf(false)
    }

    // Ruta privada utilizada para validar rostro y QR.
    var rutaFoto by remember {
        mutableStateOf<String?>(null)
    }

    // Archivo que utilizará la cámara externa.
    var archivoFotoPendiente by remember {
        mutableStateOf<File?>(null)
    }

    // --------------------------------------------------
    // ABRIR CÁMARA EXTERNA
    // --------------------------------------------------

    val tomarFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { fotoTomada ->

        val archivo = archivoFotoPendiente

        archivoFotoPendiente = null

        if (
            fotoTomada &&
            archivo != null &&
            archivo.isFile &&
            archivo.length() > 0L
        ) {

            // La cámara guardó una fotografía.
            // Ahora la copiamos a la galería.
            scope.launch {

                try {

                    val uriGaleria = withContext(Dispatchers.IO) {

                        SelfieGaleria.guardar(
                            context = context,
                            rutaFoto = archivo.absolutePath
                        )
                    }

                    if (uriGaleria != null) {

                        // Conservamos el archivo privado para ML Kit.
                        rutaFoto = archivo.absolutePath

                        Toast.makeText(
                            context,
                            "Fotografía guardada en la galería",
                            Toast.LENGTH_SHORT
                        ).show()

                    } else {

                        Toast.makeText(
                            context,
                            "No se pudo guardar la fotografía en la galería",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                } catch (e: Exception) {

                    Log.e(
                        "SelfieTuristica",
                        "Error al guardar la fotografía",
                        e
                    )

                    Toast.makeText(
                        context,
                        "Ocurrió un error al guardar la fotografía",
                        Toast.LENGTH_LONG
                    ).show()

                } finally {

                    guardando = false
                }
            }

        } else {

            // El usuario canceló la captura o no se generó
            // una fotografía válida.
            archivo?.delete()

            guardando = false

            if (fotoTomada) {

                Toast.makeText(
                    context,
                    "No se encontró la fotografía capturada",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // --------------------------------------------------
    // DISEÑO DE LA PANTALLA
    // --------------------------------------------------

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(fondoOscuro)
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // --------------------------------------------------
        // ENCABEZADO
        // --------------------------------------------------

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = 8.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onVolver,
                enabled = !guardando && !validando
            ) {

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }

            Text(
                text = "Toma una selfie",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            Spacer(
                modifier = Modifier.width(48.dp)
            )
        }

        Text(
            text = "Debe aparecer tu rostro y el código QR",
            color = textoSecundario,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                bottom = 14.dp
            )
        )

        // --------------------------------------------------
        // ÁREA PRINCIPAL
        // --------------------------------------------------

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Cámara",
                    tint = Color.White,
                    modifier = Modifier.size(70.dp)
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text = when {
                        guardando ->
                            "Capturando y guardando fotografía..."

                        rutaFoto != null ->
                            "Tu fotografía está guardada en la galería"

                        else ->
                            "Presiona el botón inferior para abrir la cámara"
                    },
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }

        // --------------------------------------------------
        // CONTROLES INFERIORES
        // --------------------------------------------------

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(fondoControles)
                .padding(
                    horizontal = 20.dp,
                    vertical = 18.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (rutaFoto == null) {

                // ------------------------------------------
                // ANTES DE TOMAR LA FOTOGRAFÍA
                // ------------------------------------------

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    // Espacio para mantener centrado
                    // el botón de captura.
                    Spacer(
                        modifier = Modifier.size(54.dp)
                    )

                    // ------------------------------------------
                    // BOTÓN CIRCULAR DE CAPTURA
                    // ------------------------------------------

                    Box(
                        modifier = Modifier
                            .size(78.dp)
                            .border(
                                width = 4.dp,
                                color = Color.White,
                                shape = CircleShape
                            )
                            .padding(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (!guardando && !validando) {
                                    Color.White
                                } else {
                                    Color.Gray
                                }
                            )
                            .clickable(
                                enabled = !guardando && !validando
                            ) {

                                guardando = true

                                try {

                                    // PASO 1:
                                    // Prepara la carpeta privada.
                                    val carpeta = File(
                                        context.filesDir,
                                        "selfies"
                                    )

                                    if (
                                        !carpeta.exists() &&
                                        !carpeta.mkdirs()
                                    ) {

                                        throw IllegalStateException(
                                            "No se pudo crear la carpeta de selfies"
                                        )
                                    }

                                    // PASO 2:
                                    // Genera un nombre único.
                                    val fecha = SimpleDateFormat(
                                        "yyyyMMdd_HHmmss_SSS",
                                        Locale.US
                                    ).format(Date())

                                    val archivo = File(
                                        carpeta,
                                        "selfie_$fecha.jpg"
                                    )

                                    // PASO 3:
                                    // Guarda el archivo pendiente.
                                    archivoFotoPendiente = archivo

                                    // PASO 4:
                                    // Comparte el archivo con la cámara.
                                    val uriFoto = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        archivo
                                    )

                                    // PASO 5:
                                    // Abre la cámara externa.
                                    tomarFoto.launch(uriFoto)

                                } catch (e: Exception) {

                                    archivoFotoPendiente?.delete()
                                    archivoFotoPendiente = null

                                    guardando = false

                                    Log.e(
                                        "SelfieTuristica",
                                        "Error al abrir la cámara",
                                        e
                                    )

                                    Toast.makeText(
                                        context,
                                        "No se pudo abrir la cámara",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {

                        if (guardando) {

                            CircularProgressIndicator(
                                modifier = Modifier.size(30.dp),
                                color = Color.DarkGray,
                                strokeWidth = 3.dp
                            )
                        }
                    }

                    // Información sobre la captura.
                    IconButton(
                        onClick = {

                            Toast.makeText(
                                context,
                                "Toma una fotografía donde aparezcan tu rostro y el QR del lugar",
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        modifier = Modifier.size(54.dp)
                    ) {

                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Información de captura",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Text(
                    text = if (guardando) {
                        "Capturando y guardando fotografía..."
                    } else {
                        "Coloca tu rostro y el QR dentro de la cámara"
                    },
                    color = textoSecundario,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

            } else {

                // ------------------------------------------
                // DESPUÉS DE GUARDAR LA FOTOGRAFÍA
                // ------------------------------------------

                Text(
                    text = "Fotografía guardada en la galería",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                // ------------------------------------------
                // VALIDAR SELFIE Y REGISTRAR LOS 100 PUNTOS
                // ------------------------------------------

                Button(
                    onClick = {

                        val ruta = rutaFoto

                        if (ruta != null && !validando) {

                            validando = true

                            // GameViewModel valida el rostro,
                            // el QR y registra los puntos.
                            onFotoGuardada(ruta) { exito, mensaje ->

                                validando = false

                                Toast.makeText(
                                    context,
                                    mensaje,
                                    Toast.LENGTH_LONG
                                ).show()

                                if (exito) {

                                    // Regresa a Resultado QR.
                                    onVolver()
                                }
                            }
                        }
                    },
                    enabled = !guardando && !validando,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {

                    if (validando) {

                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )

                        Spacer(
                            modifier = Modifier.width(10.dp)
                        )
                    }

                    Text(
                        if (validando) {
                            "Validando fotografía..."
                        } else {
                            "Continuar con la validación"
                        }
                    )
                }

                // ------------------------------------------
                // TOMAR OTRA FOTOGRAFÍA
                // ------------------------------------------

                OutlinedButton(
                    onClick = {

                        // Elimina solamente el archivo privado.
                        // La foto de la galería permanece guardada.
                        rutaFoto?.let { ruta ->
                            File(ruta).delete()
                        }

                        rutaFoto = null
                    },
                    enabled = !guardando && !validando,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {

                    Text(
                        text = "Tomar otra fotografía",
                        color = Color.White
                    )
                }
            }
        }
    }
}


