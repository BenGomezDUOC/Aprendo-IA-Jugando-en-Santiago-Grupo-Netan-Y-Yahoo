package cl.triskeledu.aprendogame.ui.screens

import android.graphics.Bitmap
import android.graphics.Color
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.triskeledu.aprendogame.data.local.AppDatabase
import cl.triskeledu.aprendogame.model.Capsula
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID

// Identificador estable por contenido: permite recuperar el QR al reabrir la pantalla.
private fun prefijoQrContenido(contenidoId: Int): String =
    "TRISKELEDU-CONTENIDO-$contenidoId-"

private fun generarImagenQr(codigo: String): Bitmap {
    val tamano = 512
    val matriz = MultiFormatWriter().encode(
        codigo, BarcodeFormat.QR_CODE, tamano, tamano
    )
    return Bitmap.createBitmap(tamano, tamano, Bitmap.Config.ARGB_8888).apply {
        for (x in 0 until tamano) {
            for (y in 0 until tamano) {
                setPixel(x, y, if (matriz[x, y]) Color.BLACK else Color.WHITE)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionQrScreen(onVolverClick: () -> Unit) {
    val context = LocalContext.current
    val database = remember(context) {
        AppDatabase.getInstance(context.applicationContext)
    }
    val contenidoDao = remember(database) { database.contenidoEducativoDao() }
    val videoDao = remember(database) { database.videoRegistradoDao() }
    val capsulaDao = remember(database) { database.capsulaDao() }

    val contenidos by remember(contenidoDao) {
        contenidoDao.obtenerTodos()
    }.collectAsState(initial = emptyList())
    val videos by remember(videoDao) {
        videoDao.obtenerTodos()
    }.collectAsState(initial = emptyList())
    val capsulas by remember(capsulaDao) {
        capsulaDao.getTodasLasCapsulas()
    }.collectAsState(initial = emptyList())

    val scope = rememberCoroutineScope()
    var procesandoId by remember { mutableStateOf<Int?>(null) }
    var mensaje by remember { mutableStateOf("") }
    var codigoQrParaGuardar by remember { mutableStateOf<String?>(null) }

    val guardarImagenQr = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/png")
    ) { uri ->
        val codigo = codigoQrParaGuardar
        if (uri != null && codigo != null) {
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        val imagen = generarImagenQr(codigo)
                        context.contentResolver.openOutputStream(uri)?.use { salida ->
                            if (!imagen.compress(Bitmap.CompressFormat.PNG, 100, salida)) {
                                throw IOException("No se pudo generar el PNG")
                            }
                        } ?: throw IOException("No se pudo abrir el archivo")
                    }
                    mensaje = "Imagen QR guardada correctamente."
                } catch (e: Exception) {
                    mensaje = "Error al guardar QR: ${e.message}"
                }
            }
        }
        codigoQrParaGuardar = null
    }

    // Solo se muestran contenidos que tienen un punto y un video asociado.
    val contenidosDisponibles = contenidos.filter { contenido ->
        contenido.puntoId != null && videos.any { video ->
            video.contenidoId == contenido.id && video.urlVideo.isNotBlank()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de códigos QR") },
                navigationIcon = {
                    IconButton(onClick = onVolverClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    "Cápsulas educativas y sus QR",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Genera un QR para cada contenido con video y punto turístico. " +
                            "Los QR generados quedan disponibles para el escáner del cliente.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (mensaje.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(mensaje, color = MaterialTheme.colorScheme.primary)
                }
            }

            if (contenidosDisponibles.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "No hay contenidos con punto turístico y video registrado.",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            items(contenidosDisponibles, key = { it.id }) { contenido ->
                // No se usa puntoId para identificar el QR: varios contenidos
                // pueden pertenecer al mismo lugar turístico.
                val prefijo = prefijoQrContenido(contenido.id)
                val capsulaExistente = capsulas.firstOrNull {
                    it.codigoQr.startsWith(prefijo)
                }
                val codigoQr = capsulaExistente?.codigoQr

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.QrCode, contentDescription = null)
                            Text(
                                contenido.titulo,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(contenido.descripcion)
                        Text(
                            "Categoría: ${contenido.categoria}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "Punto turístico ID: ${contenido.puntoId}",
                            style = MaterialTheme.typography.bodySmall
                        )

                        if (codigoQr != null) {
                            Text(
                                "QR asignado",
                                color = MaterialTheme.colorScheme.primary
                            )
                            val imagenQr = remember(codigoQr) {
                                generarImagenQr(codigoQr).asImageBitmap()
                            }
                            Image(
                                bitmap = imagenQr,
                                contentDescription = "QR de ${contenido.titulo}",
                                modifier = Modifier.size(220.dp)
                                    .align(Alignment.CenterHorizontally)
                            )
                            OutlinedButton(
                                onClick = {
                                    codigoQrParaGuardar = codigoQr
                                    guardarImagenQr.launch(
                                        "Triskeledu_QR_${contenido.id}.png"
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Guardar QR en PNG")
                            }
                            Text(
                                "Código: $codigoQr",
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Text(
                                "Video asignado · Falta QR",
                                color = MaterialTheme.colorScheme.primary
                            )
                            Button(
                                onClick = {
                                    scope.launch {
                                        procesandoId = contenido.id
                                        mensaje = ""
                                        try {
                                            val video = videos.firstOrNull {
                                                it.contenidoId == contenido.id &&
                                                        it.urlVideo.isNotBlank()
                                            } ?: error("No se encontró el video")
                                            val puntoId = contenido.puntoId
                                                ?: error("El contenido no tiene punto turístico")

                                            val nuevoCodigo = prefijoQrContenido(contenido.id) +
                                                    UUID.randomUUID().toString()
                                            val resultado = withContext(Dispatchers.IO) {
                                                // Evita duplicar el QR de este contenido.
                                                val existe = capsulaDao.getTodasLasCapsulas()
                                                    .first().any { it.codigoQr.startsWith(prefijo) }
                                                if (existe) error("Este contenido ya tiene un QR")
                                                capsulaDao.registrarNuevaCapsula(
                                                    Capsula(
                                                        titulo = contenido.titulo,
                                                        codigoQr = nuevoCodigo,
                                                        urlVideo = video.urlVideo,
                                                        puntos = 10,
                                                        puntoId = puntoId,
                                                        descripcion = contenido.descripcion,
                                                        categoria = contenido.categoria,
                                                        estado = "PUBLICADA"
                                                    )
                                                )
                                            }
                                            mensaje = if (resultado > 0L) {
                                                "QR creado para ${contenido.titulo}"
                                            } else {
                                                "No se pudo crear el QR"
                                            }
                                        } catch (e: Exception) {
                                            mensaje = "Error: ${e.message ?: "Desconocido"}"
                                        } finally {
                                            procesandoId = null
                                        }
                                    }
                                },
                                enabled = procesandoId == null,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    if (procesandoId == contenido.id) {
                                        "Generando QR..."
                                    } else {
                                        "Generar y asignar QR"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}



