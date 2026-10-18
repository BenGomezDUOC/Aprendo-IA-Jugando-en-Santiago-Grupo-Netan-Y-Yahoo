
package cl.triskeledu.aprendogame.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Refresh

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
import androidx.compose.ui.viewinterop.AndroidView

import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner

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
    val lifecycleOwner = LocalLifecycleOwner.current

    // Permite guardar la foto sin bloquear la pantalla.
    val scope = rememberCoroutineScope()

    // Colores del diseño.
    val fondoOscuro = Color(0xFF101114)
    val fondoControles = Color(0xFF17181C)
    val textoSecundario = Color(0xFFBFC0C5)

    // Comprueba si tenemos permiso para usar la cámara.
    var permisoCamara by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Solicita el permiso si todavía no fue concedido.
    val solicitarPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        permisoCamara = concedido
    }

    // Indica si estamos capturando o guardando la foto.
    var guardando by remember {
        mutableStateOf(false)
    }

    // Indica si ML Kit está validando la fotografía.
    var validando by remember {
        mutableStateOf(false)
    }

    // Ruta del archivo privado utilizado para validar.
    var rutaFoto by remember {
        mutableStateOf<String?>(null)
    }

    // Permite alternar entre cámara frontal y trasera.
    var camaraFrontal by remember {
        mutableStateOf(true)
    }

    // Prepara la captura de fotografías.
    val imageCapture = remember {
        ImageCapture.Builder().build()
    }

    // Vista donde se muestra la imagen de la cámara.
    val previewView = remember(context) {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    // --------------------------------------------------
    // INICIAR LA CÁMARA
    // --------------------------------------------------

    DisposableEffect(
        permisoCamara,
        camaraFrontal,
        lifecycleOwner,
        previewView
    ) {

        if (!permisoCamara) {

            onDispose { }

        } else {

            val cameraProviderFuture =
                ProcessCameraProvider.getInstance(context)

            var cameraProvider: ProcessCameraProvider? = null
            var cancelado = false

            val listener = Runnable {

                if (cancelado) return@Runnable

                try {

                    val provider = cameraProviderFuture.get()

                    cameraProvider = provider

                    // Prepara la vista previa.
                    val preview = Preview.Builder().build()

                    preview.surfaceProvider =
                        previewView.surfaceProvider

                    // Elige cámara frontal o trasera.
                    val selector = if (camaraFrontal) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    }

                    // Desconecta la cámara anterior.
                    provider.unbindAll()

                    // Conecta la cámara a esta pantalla.
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        selector,
                        preview,
                        imageCapture
                    )

                } catch (e: Exception) {

                    Log.e(
                        "SelfieTuristica",
                        "No se pudo iniciar la cámara",
                        e
                    )

                    Toast.makeText(
                        context,
                        "No se pudo abrir la cámara",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            cameraProviderFuture.addListener(
                listener,
                ContextCompat.getMainExecutor(context)
            )

            onDispose {

                cancelado = true

                cameraProvider?.unbindAll()
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
                    imageVector =
                        Icons.AutoMirrored.Filled.ArrowBack,
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
        // VISTA DE LA CÁMARA
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

            if (permisoCamara) {

                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )

            } else {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement =
                        Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        text = "Necesitamos permiso para utilizar la cámara.",
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Button(
                        onClick = {
                            solicitarPermiso.launch(
                                Manifest.permission.CAMERA
                            )
                        }
                    ) {
                        Text("Permitir cámara")
                    }
                }
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
                    horizontalArrangement =
                        Arrangement.SpaceEvenly
                ) {

                    // Cambia entre cámara frontal y trasera.
                    IconButton(
                        onClick = {
                            camaraFrontal = !camaraFrontal
                        },
                        enabled = permisoCamara &&
                                !guardando &&
                                !validando,
                        modifier = Modifier.size(54.dp)
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Cameraswitch,
                            contentDescription = "Cambiar cámara",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // ------------------------------------------
                    // BOTÓN CIRCULAR PARA TOMAR SELFIE
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
                                if (
                                    permisoCamara &&
                                    !guardando &&
                                    !validando
                                ) {
                                    Color.White
                                } else {
                                    Color.Gray
                                }
                            )
                            .clickable(
                                enabled = permisoCamara &&
                                        !guardando &&
                                        !validando
                            ) {

                                guardando = true

                                try {

                                    // PASO 1:
                                    // Crea una carpeta privada para
                                    // conservar la imagen que validará ML Kit.

                                    val carpeta = File(
                                        context.filesDir,
                                        "selfies"
                                    )

                                    if (
                                        !carpeta.exists() &&
                                        !carpeta.mkdirs()
                                    ) {
                                        throw IllegalStateException(
                                            "No se pudo crear la carpeta"
                                        )
                                    }

                                    // PASO 2:
                                    // Genera un nombre único para la foto.

                                    val fecha = SimpleDateFormat(
                                        "yyyyMMdd_HHmmss_SSS",
                                        Locale.US
                                    ).format(Date())

                                    val archivo = File(
                                        carpeta,
                                        "selfie_$fecha.jpg"
                                    )

                                    val opciones =
                                        ImageCapture.OutputFileOptions
                                            .Builder(archivo)
                                            .build()

                                    // PASO 3:
                                    // Captura la fotografía con CameraX.

                                    imageCapture.takePicture(
                                        opciones,
                                        ContextCompat.getMainExecutor(
                                            context
                                        ),

                                        object :
                                            ImageCapture.OnImageSavedCallback {

                                            override fun onImageSaved(
                                                outputFileResults:
                                                ImageCapture.OutputFileResults
                                            ) {

                                                // PASO 4:
                                                // La fotografía ya fue capturada.
                                                // Ahora se guarda en la galería.

                                                scope.launch {

                                                    val uriGaleria =
                                                        withContext(
                                                            Dispatchers.IO
                                                        ) {

                                                            SelfieGaleria.guardar(
                                                                context = context,
                                                                rutaFoto =
                                                                    archivo.absolutePath
                                                            )
                                                        }

                                                    guardando = false

                                                    if (uriGaleria != null) {

                                                        // PASO 5:
                                                        // Conserva la ruta privada
                                                        // para validar rostro y QR.

                                                        rutaFoto =
                                                            archivo.absolutePath

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
                                                }
                                            }

                                            override fun onError(
                                                exception:
                                                ImageCaptureException
                                            ) {

                                                guardando = false

                                                Log.e(
                                                    "SelfieTuristica",
                                                    "Error al capturar",
                                                    exception
                                                )

                                                Toast.makeText(
                                                    context,
                                                    "No se pudo tomar la fotografía",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    )

                                } catch (e: Exception) {

                                    guardando = false

                                    Log.e(
                                        "SelfieTuristica",
                                        "Error al preparar la captura",
                                        e
                                    )

                                    Toast.makeText(
                                        context,
                                        "No se pudo preparar la fotografía",
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

                    // Botón de información.
                    IconButton(
                        onClick = {
                            Toast.makeText(
                                context,
                                "Utiliza el botón central para tomar la fotografía",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.size(54.dp)
                    ) {

                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription =
                                "Información de captura",
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

                            // Llama a GameViewModel para validar
                            // el rostro y el QR correspondiente.

                            onFotoGuardada(ruta) {
                                    exito,
                                    mensaje ->

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
                        // NO elimina la foto de la galería.

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
