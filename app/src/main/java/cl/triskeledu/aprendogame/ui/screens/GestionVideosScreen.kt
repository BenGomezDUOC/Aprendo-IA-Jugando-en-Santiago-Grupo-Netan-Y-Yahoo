
package cl.triskeledu.aprendogame.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.triskeledu.aprendogame.data.local.AppDatabase
import cl.triskeledu.aprendogame.model.ContenidoEducativo
import cl.triskeledu.aprendogame.model.VideoRegistrado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import android.graphics.BitmapFactory

import androidx.compose.material.icons.filled.Image

private val azulGestion = Color(0xFF243F78)
private val fondoGestion = Color(0xFFF5F7FC)
private val grisGestion = Color(0xFF64748B)

// Convierte minutos:segundos a milisegundos.
// Ejemplo: 2:35 -> 155000.
private fun convertirDuracion(texto: String): Long? {
    val partes = texto.trim().split(":")

    if (partes.size != 2) return null

    val minutos = partes[0].toLongOrNull() ?: return null
    val segundos = partes[1].toLongOrNull() ?: return null

    if (minutos < 0 || segundos !in 0..59) return null

    val totalSegundos = minutos * 60 + segundos

    if (totalSegundos <= 0) return null

    return totalSegundos * 1000
}

// Convierte milisegundos a minutos:segundos.
// Ejemplo: 155000 -> 2:35.
private fun formatearDuracion(milisegundos: Long): String {
    if (milisegundos <= 0L) return ""

    val totalSegundos = milisegundos / 1000
    val minutos = totalSegundos / 60
    val segundos = totalSegundos % 60

    return "%d:%02d".format(minutos, segundos)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionVideosScreen(
    onVolverClick: () -> Unit
) {
    val context = LocalContext.current

    val database = remember(context) {
        AppDatabase.getInstance(context.applicationContext)
    }

    val contenidoDao = remember(database) {
        database.contenidoEducativoDao()
    }

    val videoDao = remember(database) {
        database.videoRegistradoDao()
    }

    // Cápsulas creadas por el Generador.
    val contenidos by remember(contenidoDao) {
        contenidoDao.obtenerTodos()
    }.collectAsState(initial = emptyList())

    // Videos registrados por el Administrador.
    val videos by remember(videoDao) {
        videoDao.obtenerTodos()
    }.collectAsState(initial = emptyList())

    val scope = rememberCoroutineScope()

    var contenidoSeleccionado by remember {
        mutableStateOf<ContenidoEducativo?>(null)
    }

    var videoEditar by remember {
        mutableStateOf<VideoRegistrado?>(null)
    }

    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    // Nuevos datos del video.
    var autor by remember { mutableStateOf("") }
    var duracionTexto by remember { mutableStateOf("") }

    var archivoSeleccionado by remember {
        mutableStateOf<Uri?>(null)
    }

    var nombreArchivo by remember {
        mutableStateOf("")
    }

    var miniaturaSeleccionada by remember {
        mutableStateOf<Uri?>(null)
    }

    var nombreMiniatura by remember {
        mutableStateOf("")
    }

    var guardando by remember {
        mutableStateOf(false)
    }

    var mensaje by remember {
        mutableStateOf("")
    }

    // Selecciona un archivo MP4 del dispositivo.
    val selectorVideo = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->

        if (uri != null) {
            archivoSeleccionado = uri

            nombreArchivo = try {
                context.contentResolver.query(
                    uri,
                    arrayOf(OpenableColumns.DISPLAY_NAME),
                    null,
                    null,
                    null
                )?.use { cursor ->

                    if (cursor.moveToFirst()) {
                        cursor.getString(0) ?: "Video seleccionado"
                    } else {
                        "Video seleccionado"
                    }

                } ?: "Video seleccionado"

            } catch (e: Exception) {
                "Video seleccionado"
            }
        }
    }


    val selectorMiniatura = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->

        if (uri != null) {
            miniaturaSeleccionada = uri

            nombreMiniatura = try {
                context.contentResolver.query(
                    uri,
                    arrayOf(OpenableColumns.DISPLAY_NAME),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        cursor.getString(0) ?: "Miniatura seleccionada"
                    } else {
                        "Miniatura seleccionada"
                    }
                } ?: "Miniatura seleccionada"

            } catch (e: Exception) {
                "Miniatura seleccionada"
            }
        }
    }


    // Guarda una copia privada del MP4.
    suspend fun copiarVideo(uri: Uri): String =
        withContext(Dispatchers.IO) {

            val carpeta = File(context.filesDir, "videos")

            if (!carpeta.exists() && !carpeta.mkdirs()) {
                error("No se pudo crear la carpeta de videos.")
            }

            val archivo = File(
                carpeta,
                "video_${UUID.randomUUID()}.mp4"
            )

            try {
                val entrada =
                    context.contentResolver.openInputStream(uri)
                        ?: error("No se pudo abrir el MP4.")

                entrada.use { origen ->
                    archivo.outputStream().use { destino ->
                        origen.copyTo(destino)
                    }
                }

                if (archivo.length() == 0L) {
                    error("El archivo seleccionado está vacío.")
                }

                Uri.fromFile(archivo).toString()

            } catch (e: Exception) {
                archivo.delete()
                throw e
            }
        }


    suspend fun copiarMiniatura(uri: Uri): String =
        withContext(Dispatchers.IO) {

            val carpeta = File(context.filesDir, "miniaturas")

            if (!carpeta.exists() && !carpeta.mkdirs()) {
                error("No se pudo crear la carpeta de miniaturas.")
            }

            val archivo = File(
                carpeta,
                "miniatura_${UUID.randomUUID()}.jpg"
            )

            try {
                val imagen = context.contentResolver
                    .openInputStream(uri)?.use { entrada ->
                        BitmapFactory.decodeStream(entrada)
                    } ?: error("No se pudo leer la imagen.")

                archivo.outputStream().use { salida ->
                    if (!imagen.compress(
                            android.graphics.Bitmap.CompressFormat.JPEG,
                            90,
                            salida
                        )
                    ) {
                        error("No se pudo guardar la miniatura.")
                    }
                }

                Uri.fromFile(archivo).toString()

            } catch (e: Exception) {
                archivo.delete()
                throw e
            }
        }


    // Abre el formulario para crear o editar.

    fun abrirFormulario(
        contenido: ContenidoEducativo,
        video: VideoRegistrado? = null
    ) {
        contenidoSeleccionado = contenido
        videoEditar = video

        titulo = video?.titulo ?: contenido.titulo
        descripcion = video?.descripcion ?: contenido.descripcion

        autor = video?.autor.orEmpty()
        duracionTexto = video?.let {
            formatearDuracion(it.duracion)
        }.orEmpty()

        // Limpiar selección anterior del MP4
        archivoSeleccionado = null
        nombreArchivo = ""

        // Limpiar selección anterior de la miniatura
        miniaturaSeleccionada = null
        nombreMiniatura = ""

        mensaje = ""
    }



    fun cerrarFormulario() {
        contenidoSeleccionado = null
        videoEditar = null

        archivoSeleccionado = null
        nombreArchivo = ""

        miniaturaSeleccionada = null
        nombreMiniatura = ""

        autor = ""
        duracionTexto = ""
    }


    BackHandler(
        enabled = contenidoSeleccionado != null && !guardando
    ) {
        cerrarFormulario()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(fondoGestion)
    ) {

        // ENCABEZADO
        TopAppBar(
            title = {
                Text(
                    text = if (contenidoSeleccionado == null) {
                        "Gestión de Videos"
                    } else {
                        "Asignar video"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            navigationIcon = {
                IconButton(
                    enabled = !guardando,
                    onClick = {
                        if (contenidoSeleccionado != null) {
                            cerrarFormulario()
                        } else {
                            onVolverClick()
                        }
                    }
                ) {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = azulGestion,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )

        if (contenidoSeleccionado == null) {

            // LISTA DE CÁPSULAS DEL GENERADOR
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Cápsulas educativas",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF172B4D)
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Selecciona una cápsula creada por el Generador para asignarle su video.",
                    color = grisGestion,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )

                Spacer(Modifier.height(20.dp))

                if (mensaje.isNotBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE7F3EA)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = mensaje,
                            modifier = Modifier.padding(14.dp),
                            color = Color(0xFF166534)
                        )
                    }

                    Spacer(Modifier.height(14.dp))
                }

                if (contenidos.isEmpty()) {

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {

                            Icon(
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = null,
                                modifier = Modifier.size(46.dp),
                                tint = azulGestion
                            )

                            Spacer(Modifier.height(12.dp))

                            Text(
                                text = "Todavía no hay cápsulas",
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(6.dp))

                            Text(
                                text = "Las cápsulas aparecerán aquí cuando el Generador las cree.",
                                color = grisGestion
                            )
                        }
                    }

                } else {

                    LazyColumn(
                        verticalArrangement =
                            Arrangement.spacedBy(14.dp),
                        contentPadding =
                            PaddingValues(bottom = 24.dp)
                    ) {

                        items(
                            items = contenidos,
                            key = { it.id }
                        ) { contenido ->

                            val video = videos.firstOrNull {
                                it.contenidoId == contenido.id
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 2.dp
                                )
                            ) {

                                Column(
                                    modifier = Modifier.padding(18.dp)
                                ) {

                                    Row(
                                        verticalAlignment =
                                            Alignment.CenterVertically,
                                        horizontalArrangement =
                                            Arrangement.spacedBy(10.dp)
                                    ) {

                                        Surface(
                                            color = Color(0xFFEAF0FF),
                                            shape =
                                                RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(
                                                imageVector =
                                                    Icons.Default.VideoLibrary,
                                                contentDescription = null,
                                                tint = azulGestion,
                                                modifier =
                                                    Modifier.padding(10.dp)
                                            )
                                        }

                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {

                                            Text(
                                                text = contenido.titulo,
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF172B4D)
                                            )

                                            Text(
                                                text = contenido.categoria,
                                                fontSize = 12.sp,
                                                color = grisGestion
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(14.dp))

                                    Text(
                                        text = contenido.descripcion,
                                        fontSize = 14.sp,
                                        color = grisGestion,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(Modifier.height(12.dp))

                                    Surface(
                                        color = Color(0xFFF0F4FA),
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Text(
                                            text = if (
                                                contenido.puntoId != null
                                            ) {
                                                "Cápsula con QR"
                                            } else {
                                                "Cápsula sin QR"
                                            },
                                            modifier = Modifier.padding(
                                                horizontal = 12.dp,
                                                vertical = 7.dp
                                            ),
                                            fontSize = 12.sp,
                                            color = azulGestion
                                        )
                                    }

                                    Spacer(Modifier.height(14.dp))

                                    Row(
                                        verticalAlignment =
                                            Alignment.CenterVertically,
                                        horizontalArrangement =
                                            Arrangement.spacedBy(8.dp)
                                    ) {

                                        Icon(
                                            imageVector = if (video != null) {
                                                Icons.Default.CheckCircle
                                            } else {
                                                Icons.Default.PlayArrow
                                            },
                                            contentDescription = null,
                                            tint = if (video != null) {
                                                Color(0xFF15803D)
                                            } else {
                                                Color(0xFFB45309)
                                            }
                                        )

                                        Text(
                                            text = if (video != null) {
                                                "Video asignado"
                                            } else {
                                                "Pendiente de video"
                                            },
                                            fontWeight = FontWeight.Medium,
                                            color = if (video != null) {
                                                Color(0xFF15803D)
                                            } else {
                                                Color(0xFFB45309)
                                            }
                                        )
                                    }

                                    if (video != null) {

                                        Spacer(Modifier.height(6.dp))

                                        Text(
                                            text = video.titulo,
                                            color = grisGestion,
                                            fontSize = 13.sp
                                        )

                                        if (video.autor.isNotBlank()) {
                                            Text(
                                                text = "Autor: ${video.autor}",
                                                color = grisGestion,
                                                fontSize = 12.sp
                                            )
                                        }

                                        if (video.duracion > 0L) {
                                            Text(
                                                text = "Duración: ${
                                                    formatearDuracion(
                                                        video.duracion
                                                    )
                                                }",
                                                color = grisGestion,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(14.dp))

                                    Button(
                                        onClick = {
                                            abrirFormulario(
                                                contenido,
                                                video
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = azulGestion
                                        )
                                    ) {
                                        Text(
                                            text = if (video == null) {
                                                "Asignar video MP4"
                                            } else {
                                                "Editar o reemplazar video"
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } else {

            // FORMULARIO DE ASIGNACIÓN
            val contenido = contenidoSeleccionado!!
            val videoActual = videoEditar

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement =
                    Arrangement.spacedBy(16.dp)
            ) {

                // CÁPSULA SELECCIONADA
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {

                        Text(
                            text = "Cápsula seleccionada",
                            color = grisGestion,
                            fontSize = 13.sp
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = contenido.titulo,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF172B4D)
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = contenido.categoria,
                            color = azulGestion
                        )
                    }
                }

                // INFORMACIÓN DEL VIDEO
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(14.dp)
                    ) {

                        Text(
                            text = "Información del video",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = titulo,
                            onValueChange = { titulo = it },
                            label = { Text("Título del video") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = descripcion,
                            onValueChange = { descripcion = it },
                            label = { Text("Descripción") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // NUEVO: AUTOR
                        OutlinedTextField(
                            value = autor,
                            onValueChange = { autor = it },
                            label = { Text("Autor del video") },
                            placeholder = {
                                Text("Ejemplo: Fernando Zapata")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // NUEVO: DURACIÓN MANUAL
                        OutlinedTextField(
                            value = duracionTexto,
                            onValueChange = { duracionTexto = it },
                            label = { Text("Duración del video") },
                            placeholder = {
                                Text("Ejemplo: 2:35")
                            },
                            supportingText = {
                                Text("Formato: minutos:segundos")
                            },
                            isError = duracionTexto.isNotBlank() &&
                                    convertirDuracion(duracionTexto) == null,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )


                        HorizontalDivider()

                        // SELECCIONAR VIDEO MP4
                        Text(
                            text = "Archivo MP4",
                            fontWeight = FontWeight.Bold
                        )

                        Button(
                            onClick = {
                                selectorVideo.launch(
                                    arrayOf("video/mp4")
                                )
                            },
                            enabled = !guardando,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = azulGestion
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = null
                            )

                            Spacer(Modifier.width(8.dp))

                            Text("Seleccionar video MP4")
                        }

                        Text(
                            text = when {
                                archivoSeleccionado != null ->
                                    "Seleccionado: $nombreArchivo"

                                videoActual != null ->
                                    "Ya hay un video asignado. Puedes conservarlo o reemplazarlo."

                                else ->
                                    "Selecciona un archivo MP4 del dispositivo."
                            },
                            color = if (archivoSeleccionado != null) {
                                Color(0xFF15803D)
                            } else {
                                grisGestion
                            },
                            fontSize = 13.sp
                        )

                        // SELECCIONAR MINIATURA
                        HorizontalDivider()

                        Text(
                            text = "Miniatura del video",
                            fontWeight = FontWeight.Bold
                        )

                        Button(
                            onClick = {
                                selectorMiniatura.launch(
                                    arrayOf("image/jpeg", "image/png")
                                )
                            },
                            enabled = !guardando,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = azulGestion
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null
                            )

                            Spacer(Modifier.width(8.dp))

                            Text("Seleccionar miniatura")
                        }

                        Text(
                            text = when {
                                miniaturaSeleccionada != null ->
                                    "Seleccionada: $nombreMiniatura"

                                videoActual?.urlMiniatura?.isNotBlank() == true ->
                                    "Ya hay una miniatura asignada. Puedes conservarla o cambiarla."

                                else ->
                                    "Selecciona una imagen JPG o PNG."
                            },
                            color = if (miniaturaSeleccionada != null) {
                                Color(0xFF15803D)
                            } else {
                                grisGestion
                            },
                            fontSize = 13.sp
                        )
                    }


                    }

                if (mensaje.isNotBlank()) {
                    Text(
                        text = mensaje,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                // GUARDAR VIDEO
                        Button(
                            enabled = !guardando &&
                                    titulo.isNotBlank() &&
                                    autor.isNotBlank() &&
                                    convertirDuracion(duracionTexto) != null &&
                                    (archivoSeleccionado != null || videoActual != null),

                            onClick = {
                                val uri = archivoSeleccionado
                                val uriMiniatura = miniaturaSeleccionada
                                val tituloGuardar = titulo.trim()
                                val descripcionGuardar = descripcion.trim()
                                val autorGuardar = autor.trim()
                                val duracionGuardar = convertirDuracion(duracionTexto)

                                if (duracionGuardar != null) {
                                    guardando = true
                                    mensaje = ""

                                    scope.launch {
                                        var nuevaRuta: String? = null
                                        var nuevaRutaMiniatura: String? = null

                                        try {
                                            // Copia un MP4 nuevo o conserva el anterior.
                                            val rutaGuardar =
                                                if (uri != null) {
                                                    copiarVideo(uri).also {
                                                        nuevaRuta = it
                                                    }
                                                } else {
                                                    videoActual?.urlVideo
                                                        ?: error("Selecciona un MP4.")
                                                }

                                            // Copia una miniatura nueva o conserva la anterior.
                                            val rutaMiniaturaGuardar =
                                                if (uriMiniatura != null) {
                                                    copiarMiniatura(uriMiniatura).also {
                                                        nuevaRutaMiniatura = it
                                                    }
                                                } else {
                                                    videoActual?.urlMiniatura.orEmpty()
                                                }

                                            withContext(Dispatchers.IO) {
                                                if (videoActual == null) {

                                                    // REGISTRAR NUEVO VIDEO
                                                    videoDao.registrar(
                                                        VideoRegistrado(
                                                            titulo = tituloGuardar,
                                                            descripcion = descripcionGuardar,
                                                            urlVideo = rutaGuardar,
                                                            contenidoId = contenido.id,
                                                            autor = autorGuardar,
                                                            duracion = duracionGuardar,
                                                            urlMiniatura = rutaMiniaturaGuardar
                                                        )
                                                    )

                                                } else {

                                                    // ACTUALIZAR VIDEO EXISTENTE
                                                    videoDao.actualizar(
                                                        id = videoActual.id,
                                                        titulo = tituloGuardar,
                                                        descripcion = descripcionGuardar,
                                                        urlVideo = rutaGuardar,
                                                        contenidoId = contenido.id,
                                                        autor = autorGuardar,
                                                        duracion = duracionGuardar,
                                                        urlMiniatura = rutaMiniaturaGuardar
                                                    )
                                                }
                                            }

                                            cerrarFormulario()
                                            mensaje = "Video guardado correctamente."

                                        } catch (e: Exception) {

                                            // Elimina solamente los archivos nuevos
                                            // si ocurre un error al guardar.
                                            withContext(Dispatchers.IO) {
                                                nuevaRuta?.let { ruta ->
                                                    Uri.parse(ruta).path?.let { path ->
                                                        File(path).delete()
                                                    }
                                                }

                                                nuevaRutaMiniatura?.let { ruta ->
                                                    Uri.parse(ruta).path?.let { path ->
                                                        File(path).delete()
                                                    }
                                                }
                                            }

                                            mensaje = "Error al guardar: ${e.message}"

                                        } finally {
                                            guardando = false
                                        }
                                    }
                                }
                            },

                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),

                            shape = RoundedCornerShape(12.dp),

                            colors = ButtonDefaults.buttonColors(
                                containerColor = azulGestion
                            )
                        ) {
                            Text(
                                if (guardando) {
                                    "Guardando video..."
                                } else {
                                    "Guardar video"
                                }
                            )
                        }


                        OutlinedButton(
                    onClick = { cerrarFormulario() },
                    enabled = !guardando,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancelar y volver a las cápsulas")
                }
            }
        }
    }
}