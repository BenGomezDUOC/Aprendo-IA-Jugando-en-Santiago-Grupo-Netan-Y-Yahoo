
package cl.triskeledu.aprendogame.ui.screens

import android.content.Context
import android.net.Uri
import android.util.Log

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

import cl.triskeledu.aprendogame.data.local.AppDatabase
import cl.triskeledu.aprendogame.data.repository.GameRepositoryImpl
import cl.triskeledu.aprendogame.model.Capsula
import cl.triskeledu.aprendogame.model.DescubrimientoQr
import cl.triskeledu.aprendogame.viewmodel.GameViewModel

import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.common.InputImage

import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun EscanerQRScreen(
    gameViewModel: GameViewModel,
    onResultadoQr: (Capsula, Int) -> Unit
) {
    val context = LocalContext.current
    val abrirEnlace = LocalUriHandler.current
    val scope = rememberCoroutineScope()

    // Reutilizamos Room y el repositorio del proyecto.
    val repository = remember(context) {
        GameRepositoryImpl(
            AppDatabase.getInstance(context.applicationContext)
        )
    }

    var mensaje by remember {
        mutableStateOf(
            "Se detectará automáticamente el código QR"
        )
    }

    var escaneando by remember {
        mutableStateOf(false)
    }

    var capsulaEncontrada by remember {
        mutableStateOf<Capsula?>(null)
    }

    var puntosGanados by remember {
        mutableIntStateOf(0)
    }

    // Procesa el contenido que entrega el escáner.
    val procesarQr: (String) -> Unit = { textoLeido ->

        val codigoQr = extraerCodigoQr(textoLeido)
        val usuarioId = gameViewModel.usuarioActivoId.value

        if (usuarioId == null) {
            mensaje = "Debes iniciar sesión para registrar QR."
            escaneando = false

        } else if (codigoQr.isBlank()) {
            mensaje = "No se pudo leer el contenido del QR."
            escaneando = false

        } else {
            scope.launch {
                try {
                    // 1. Buscar la cápsula en Room.
                    val capsula = repository.validarQr(codigoQr)

                    if (capsula == null) {
                        capsulaEncontrada = null
                        puntosGanados = 0
                        mensaje = "Este QR no pertenece a Triskeledu."

                    } else {
                        // 2. Registrar el descubrimiento.
                        val resultado =
                            repository.registrarDescubrimientoQr(
                                DescubrimientoQr(
                                    usuarioId = usuarioId,
                                    capsulaId = capsula.id,
                                    fechaTimestamp =
                                        System.currentTimeMillis()
                                )
                            )

                        // 3. Mostrar la cápsula.
                        capsulaEncontrada = capsula

                        if (resultado == -1L) {
                            puntosGanados = 0
                            mensaje =
                                "Ya habías descubierto este QR. No se suman puntos nuevamente."
                        } else {
                            puntosGanados = 10
                            mensaje =
                                "¡QR descubierto! Ganaste 10 puntos."
                        }

                        // Abre la pantalla 8 con la cápsula y los puntos reales.
                        onResultadoQr(capsula, puntosGanados)


                    }

                } catch (error: Exception) {
                    Log.e(
                        "TriskeleduQR",
                        "Error al registrar QR",
                        error
                    )

                    mensaje =
                        "No se pudo registrar el QR: ${error.message}"

                } finally {
                    escaneando = false
                }
            }
        }
    }

    // Google Code Scanner: el escáner que funcionaba.
    val iniciarEscaneo: () -> Unit = {
        if (!escaneando) {
            val usuarioId = gameViewModel.usuarioActivoId.value

            if (usuarioId == null) {
                mensaje = "Debes iniciar sesión para escanear."

            } else {
                escaneando = true
                capsulaEncontrada = null
                puntosGanados = 0
                mensaje = "Apunta la cámara al código QR."

                val opciones = GmsBarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .build()

                GmsBarcodeScanning
                    .getClient(context, opciones)
                    .startScan()
                    .addOnSuccessListener { codigo ->
                        procesarQr(codigo.rawValue.orEmpty())
                    }
                    .addOnCanceledListener {
                        mensaje = "Escaneo cancelado."
                        escaneando = false
                    }
                    .addOnFailureListener { error ->
                        mensaje =
                            "No se pudo escanear: ${error.message}"
                        escaneando = false
                    }
            }
        }
    }

    // Galería: permite analizar un QR desde una imagen.
    val seleccionarImagen =
        rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->

            if (uri == null) {
                mensaje = "Selección cancelada."
                escaneando = false

            } else {
                escaneando = true
                mensaje = "Analizando imagen..."

                val opciones = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .build()

                val lector = BarcodeScanning.getClient(opciones)

                try {
                    val imagen =
                        InputImage.fromFilePath(context, uri)

                    lector.process(imagen)
                        .addOnSuccessListener { codigos ->
                            val texto = codigos
                                .firstOrNull {
                                    !it.rawValue.isNullOrBlank()
                                }
                                ?.rawValue

                            if (texto.isNullOrBlank()) {
                                mensaje =
                                    "La imagen no contiene un QR reconocible."
                                escaneando = false
                            } else {
                                procesarQr(texto)
                            }
                        }
                        .addOnFailureListener { error ->
                            mensaje =
                                "Error al analizar imagen: ${error.message}"
                            escaneando = false
                        }
                        .addOnCompleteListener {
                            lector.close()
                        }

                } catch (error: Exception) {
                    lector.close()
                    mensaje =
                        "No se pudo abrir la imagen: ${error.message}"
                    escaneando = false
                }
            }
        }

    // Interfaz de la pantalla 7.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Escanea el código QR",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Apunta la cámara al código QR del punto",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Recuadro con el estilo de la referencia.
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .clickable(
                    enabled = !escaneando,
                    onClick = iniciarEscaneo
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF172D44)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(65.dp)
                    )

                    Text(
                        text = if (escaneando) {
                            "Procesando QR..."
                        } else {
                            "TOCA AQUÍ PARA ESCANEAR"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Se abrirá la cámara para leer el código",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }

                MarcoEscaneoQr(
                    modifier = Modifier.size(230.dp)
                )
            }
        }

        // Controles visuales y galería.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Google Code Scanner controla la linterna
            // desde su propia interfaz de cámara.
            OutlinedButton(
                onClick = iniciarEscaneo,
                enabled = !escaneando,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.FlashlightOn,
                    contentDescription = null
                )

                Spacer(Modifier.width(6.dp))
                Text("Cámara")
            }

            OutlinedButton(
                onClick = {
                    seleccionarImagen.launch("image/*")
                },
                enabled = !escaneando,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null
                )

                Spacer(Modifier.width(6.dp))
                Text("Galería")
            }
        }

        // Mensaje de estado.
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = mensaje,
                    fontWeight = FontWeight.Medium
                )

                if (escaneando) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Resultado del descubrimiento.
        capsulaEncontrada?.let { capsula ->

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    Text(
                        text = "¡Descubrimiento encontrado!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = if (puntosGanados > 0) {
                            "+10 puntos"
                        } else {
                            "QR descubierto anteriormente"
                        },
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = capsula.titulo,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(capsula.descripcion)

                    Text(
                        text = "Video educativo",
                        fontWeight = FontWeight.Bold
                    )

                    val url = capsula.urlVideo

                    if (
                        url.startsWith("https://") ||
                        url.startsWith("http://")
                    ) {
                        Text(
                            text = url,
                            color =
                                MaterialTheme.colorScheme.primary,
                            textDecoration =
                                TextDecoration.Underline,
                            modifier = Modifier.clickable {
                                abrirEnlace.openUri(url)
                            }
                        )
                    } else {
                        Text(
                            "Esta cápsula no tiene un enlace de video válido."
                        )
                    }
                }
            }
        }
    }
}

// Dibuja las cuatro esquinas blancas del escáner.
@Composable
private fun MarcoEscaneoQr(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {

        val largo = 38.dp.toPx()
        val grosor = 5.dp.toPx()

        val ancho = size.width
        val alto = size.height

        val color = Color.White

        // Esquina superior izquierda.
        drawLine(
            color,
            Offset(0f, largo),
            Offset(0f, 0f),
            grosor
        )

        drawLine(
            color,
            Offset(0f, 0f),
            Offset(largo, 0f),
            grosor
        )

        // Esquina superior derecha.
        drawLine(
            color,
            Offset(ancho - largo, 0f),
            Offset(ancho, 0f),
            grosor
        )

        drawLine(
            color,
            Offset(ancho, 0f),
            Offset(ancho, largo),
            grosor
        )

        // Esquina inferior izquierda.
        drawLine(
            color,
            Offset(0f, alto - largo),
            Offset(0f, alto),
            grosor
        )

        drawLine(
            color,
            Offset(0f, alto),
            Offset(largo, alto),
            grosor
        )

        // Esquina inferior derecha.
        drawLine(
            color,
            Offset(ancho - largo, alto),
            Offset(ancho, alto),
            grosor
        )

        drawLine(
            color,
            Offset(ancho, alto - largo),
            Offset(ancho, alto),
            grosor
        )
    }
}

// Acepta códigos QR simples o JSON.
private fun extraerCodigoQr(texto: String): String {
    return try {
        val json = JSONObject(texto)
        json.optString("codigoQr", texto).trim()
    } catch (error: Exception) {
        texto.trim()
    }
}


