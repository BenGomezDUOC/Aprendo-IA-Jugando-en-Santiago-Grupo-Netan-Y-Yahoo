
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
import cl.triskeledu.aprendogame.data.local.SessionManager
import cl.triskeledu.aprendogame.data.remote.RetrofitClient
import cl.triskeledu.aprendogame.data.remote.dto.CapsulaGestionBackendDto

private class VideoRemotoFaltanteException(message: String) : IllegalStateException(message)

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
    // Fuente de verdad: QR publicados que devolvió MySQL, NO los QR de Room.
    val qrPublicadosMySql = remember { mutableStateMapOf<Int, String>() }
    var consultandoMySql by remember { mutableStateOf(true) }


    suspend fun sincronizarQrBackend(contenidoLocalId: Int): String =
        withContext(Dispatchers.IO) {

            val authorization =
                SessionManager(context.applicationContext)
                    .obtenerAuthorizationHeader()
                    ?: error("Inicia sesión como administrador del backend.")

            val contenido = contenidos.firstOrNull {
                it.id == contenidoLocalId
            } ?: error("No se encontró el contenido local.")

            val video = videos.firstOrNull {
                it.contenidoId == contenido.id
            } ?: error("El contenido no tiene video.")

            val puntoLocalId = contenido.puntoId
                ?: error("El contenido no tiene punto turístico.")

            // Buscar el contenido correspondiente en MySQL.
            val consultaContenidos =
                RetrofitClient.localizacionesApi
                    .obtenerContenidos(authorization)

            if (!consultaContenidos.isSuccessful) {
                error("Error al consultar contenidos: HTTP ${consultaContenidos.code()}")
            }

            val coincidencias = consultaContenidos.body().orEmpty().filter {
                it.titulo == contenido.titulo &&
                        it.descripcion == contenido.descripcion &&
                        it.categoria == contenido.categoria
            }

            if (coincidencias.size != 1) {
                error("No se identificó una única cápsula en MySQL.")
            }

            val contenidoRemoto = coincidencias.single()
            val contenidoBackendId = contenidoRemoto.id
                ?: error("El contenido remoto no tiene ID.")

            val puntoBackendId = contenidoRemoto.puntoId
                ?: error("El contenido remoto no tiene punto turístico.")

            // Buscar el video correspondiente en MySQL.
            val consultaVideos =
                RetrofitClient.localizacionesApi
                    .obtenerVideos(authorization)

            if (!consultaVideos.isSuccessful) {
                error("Error al consultar videos: HTTP ${consultaVideos.code()}")
            }

            val videosBackend = consultaVideos.body().orEmpty().filter {
                it.contenidoId == contenidoBackendId
            }

            if (videosBackend.isEmpty()) {
                throw VideoRemotoFaltanteException(
                    "El contenido '${contenido.titulo}' todavía no tiene video en MySQL."
                )
            }
            if (videosBackend.size > 1) {
                error("Hay varios videos en MySQL asociados a '${contenido.titulo}'.")
            }

            val videoBackend = videosBackend.single()
            val videoBackendId = videoBackend.id
                ?: error("El video remoto no tiene ID.")

            // Comprobar si ya existe un QR remoto.
            val consultaCapsulas =
                RetrofitClient.localizacionesApi
                    .obtenerCapsulasBackend(authorization)

            if (!consultaCapsulas.isSuccessful) {
                error("Error al consultar QR: HTTP ${consultaCapsulas.code()}")
            }

            val capsulasBackend = consultaCapsulas.body().orEmpty().filter {
                it.contenidoId == contenidoBackendId
            }

            if (capsulasBackend.size > 1) {
                error("Hay varios QR remotos para este contenido.")
            }

            val capsulaRemota = if (capsulasBackend.isNotEmpty()) {
                capsulasBackend.single()
            } else {
                val respuesta =
                    RetrofitClient.localizacionesApi.crearCapsulaBackend(
                        authorization,
                        CapsulaGestionBackendDto(
                            puntoId = puntoBackendId,
                            contenidoId = contenidoBackendId,
                            videoId = videoBackendId,
                            titulo = contenidoRemoto.titulo,
                            descripcion = contenidoRemoto.descripcion,
                            urlVideo = videoBackend.urlVideo,
                            categoria = contenidoRemoto.categoria
                        )
                    )

                if (!respuesta.isSuccessful) {
                    error("No se creó el QR: HTTP ${respuesta.code()}")
                }

                respuesta.body()
                    ?: error("El backend no devolvió la cápsula.")
            }

            // Publicar en MySQL.
            val publicada = if (capsulaRemota.estado == "PUBLICADA") {
                capsulaRemota
            } else {
                val id = capsulaRemota.id
                    ?: error("El QR remoto no tiene ID.")

                val respuesta =
                    RetrofitClient.localizacionesApi.publicarCapsulaBackend(
                        authorization,
                        id
                    )

                if (!respuesta.isSuccessful) {
                    error("No se publicó el QR: HTTP ${respuesta.code()}")
                }

                respuesta.body()
                    ?: error("El backend no devolvió el QR publicado.")
            }

            val codigoDefinitivo = publicada.codigoQr
                ?.takeIf { it.isNotBlank() }
                ?: error("El backend no devolvió un código QR válido.")

            // Sincronizar Room conservando el ID local.
            val prefijo = prefijoQrContenido(contenidoLocalId)

            val capsulasLocales = capsulaDao.getTodasLasCapsulas().first()
            val capsulasRelacionadas = capsulasLocales.filter { capsula ->
                capsula.codigoQr == codigoDefinitivo ||
                        capsula.codigoQr.startsWith(prefijo) ||
                        (capsula.titulo == contenido.titulo &&
                                capsula.descripcion == contenido.descripcion &&
                                capsula.categoria == contenido.categoria &&
                                capsula.puntoId == puntoLocalId &&
                                capsula.urlVideo == video.urlVideo)
            }
            if (capsulasRelacionadas.size > 1) {
                error("Se encontraron varias cápsulas locales para este contenido. No se modificó Room.")
            }
            val capsulaLocal = capsulasRelacionadas.singleOrNull()

            if (capsulaLocal != null) {
                val filas = capsulaDao.actualizarQrBackend(
                    id = capsulaLocal.id,
                    codigoQr = codigoDefinitivo,
                    estado = publicada.estado
                )

                if (filas != 1) {
                    error("El QR se publicó en MySQL, pero no se actualizó Room.")
                }
            } else {
                capsulaDao.registrarNuevaCapsula(
                    Capsula(
                        titulo = contenido.titulo,
                        codigoQr = codigoDefinitivo,
                        urlVideo = video.urlVideo,
                        puntos = 10,
                        puntoId = puntoLocalId,
                        descripcion = contenido.descripcion,
                        categoria = contenido.categoria,
                        estado = publicada.estado
                    )
                )
            }

            "QR sincronizado y publicado correctamente."
        }


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

    // Lee los QR desde MySQL para mostrarlos. Room solo aporta datos locales para
    // reconocer contenidos antiguos; nunca se usa como prueba de publicación.
    suspend fun cargarQrDesdeMySql() {
        val resultado = withContext(Dispatchers.IO) {
            val authorization = SessionManager(context.applicationContext)
                .obtenerAuthorizationHeader()
                ?: error("Inicia sesión como administrador del backend.")

            val respuestaContenidos = RetrofitClient.localizacionesApi
                .obtenerContenidos(authorization)
            if (!respuestaContenidos.isSuccessful) {
                error("Error consultando contenidos: HTTP ${respuestaContenidos.code()}")
            }
            val respuestaQr = RetrofitClient.localizacionesApi
                .obtenerCapsulasBackend(authorization)
            if (!respuestaQr.isSuccessful) {
                error("Error consultando QR: HTTP ${respuestaQr.code()}")
            }
            val remotos = respuestaContenidos.body().orEmpty()
            val capsulasRemotas = respuestaQr.body().orEmpty()
            val mapa = mutableMapOf<Int, String>()
            for (local in contenidosDisponibles) {
                val coincidencias = remotos.filter {
                    it.titulo == local.titulo &&
                            it.descripcion == local.descripcion &&
                            it.categoria == local.categoria
                }
                if (coincidencias.size != 1) continue
                val remotoId = coincidencias.single().id ?: continue
                val publicadas = capsulasRemotas.filter {
                    it.contenidoId == remotoId && it.estado == "PUBLICADA" &&
                            !it.codigoQr.isNullOrBlank()
                }
                if (publicadas.size == 1) {
                    mapa[local.id] = publicadas.single().codigoQr!!
                }
            }
            mapa
        }
        qrPublicadosMySql.clear()
        qrPublicadosMySql.putAll(resultado)
    }

    // Migración automática una vez por entrada a la pantalla: solo QR antiguos
    // de Room. Los QR nuevos se crean únicamente al tocar Generar y asignar QR.
    val intentosMigracion = remember { mutableStateListOf<Int>() }
    LaunchedEffect(contenidosDisponibles, capsulas) {
        if (contenidosDisponibles.isEmpty()) return@LaunchedEffect
        consultandoMySql = true
        try {
            cargarQrDesdeMySql()
            val errores = mutableListOf<String>()
            for (contenido in contenidosDisponibles) {
                if (qrPublicadosMySql.containsKey(contenido.id)) continue
                if (contenido.id in intentosMigracion) continue
                val prefijo = prefijoQrContenido(contenido.id)
                val videoLocal = videos.firstOrNull {
                    it.contenidoId == contenido.id && it.urlVideo.isNotBlank()
                }
                val qrLocalAntiguo = capsulas.any { capsula ->
                    capsula.codigoQr.startsWith(prefijo) ||
                            (capsula.titulo == contenido.titulo &&
                                    capsula.descripcion == contenido.descripcion &&
                                    capsula.categoria == contenido.categoria &&
                                    capsula.puntoId == contenido.puntoId &&
                                    capsula.urlVideo == videoLocal?.urlVideo)
                }
                if (!qrLocalAntiguo) continue
                intentosMigracion.add(contenido.id)
                procesandoId = contenido.id
                try {
                    sincronizarQrBackend(contenido.id)
                    // Volver a consultar MySQL antes de mostrar el QR.
                    cargarQrDesdeMySql()
                } catch (e: VideoRemotoFaltanteException) {
                    errores.add("${contenido.titulo}: video pendiente en MySQL")
                } catch (e: Exception) {
                    errores.add("${contenido.titulo}: ${e.message ?: "error"}")
                } finally {
                    procesandoId = null
                }
            }
            if (errores.isNotEmpty()) mensaje = errores.joinToString("; ")
        } catch (e: Exception) {
            mensaje = "No se pudieron consultar los QR en MySQL: ${e.message ?: "error"}"
        } finally {
            consultandoMySql = false
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
                // El QR visible viene exclusivamente de la respuesta de MySQL.
                val codigoQr = qrPublicadosMySql[contenido.id]

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
                            Text("QR publicado en MySQL", color = MaterialTheme.colorScheme.primary)
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
                                enabled = procesandoId == null && !consultandoMySql,
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
                                if (consultandoMySql) "Consultando QR en MySQL..." else "Video asignado · Falta QR publicado en MySQL",
                                color = MaterialTheme.colorScheme.primary
                            )

                            Button(
                                onClick = {
                                    scope.launch {
                                        procesandoId = contenido.id
                                        mensaje = ""

                                        try {
                                            mensaje = sincronizarQrBackend(contenido.id)
                                            cargarQrDesdeMySql()
                                        } catch (e: Exception) {
                                            mensaje = "Error: ${e.message ?: "Desconocido"}"
                                            // Si MySQL publicó pero falló Room, reflejar el estado remoto.
                                            try { cargarQrDesdeMySql() } catch (_: Exception) { }
                                        } finally {
                                            procesandoId = null
                                        }
                                    }
                                },
                                enabled = procesandoId == null && !consultandoMySql,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    if (procesandoId == contenido.id) {
                                        "Sincronizando QR..."
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
