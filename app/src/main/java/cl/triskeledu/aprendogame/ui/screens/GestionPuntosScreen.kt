package cl.triskeledu.aprendogame.ui.screens

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import cl.triskeledu.aprendogame.ui.imagenPredeterminadaPunto
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.triskeledu.aprendogame.data.local.AppDatabase
import cl.triskeledu.aprendogame.model.PuntoInteres
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun GestionPuntosScreen(
    onVolverClick: () -> Unit
) {
    val context = LocalContext.current

    // Base de datos Room.
    val database = remember(context) {
        AppDatabase.getInstance(context.applicationContext)
    }

    // DAO para consultar, crear y editar puntos turísticos.
    val puntoDao = remember(database) {
        database.puntoInteresDao()
    }

    // Observa los puntos guardados en Room.
    // La lista se actualiza cuando cambia la base de datos.
    val puntos by remember(puntoDao) {
        puntoDao.getPuntos()
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Datos del formulario.
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var latitud by remember { mutableStateOf("") }
    var longitud by remember { mutableStateOf("") }
    var altitud by remember { mutableStateOf("") }
    var radio by remember { mutableStateOf("50") }
    // Ruta de la foto elegida en la galería. Null usa la foto inicial de drawable.
    var imagenUri by remember { mutableStateOf<String?>(null) }

    // Estado del formulario. Se declara antes del selector que lo utiliza.
    var guardando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }

    // OpenDocument permite conservar el acceso a la foto después de reiniciar.
    val seleccionarFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                imagenUri = uri.toString()
                mensaje = "Fotografía seleccionada. Pulsa Guardar para confirmar."
            } catch (e: Exception) {
                mensaje = "No se pudo obtener permiso para la fotografía."
            }
        }
    }

    // Previsualización de la fotografía de la galería.
    val vistaPrevia by produceState<androidx.compose.ui.graphics.ImageBitmap?>(
        initialValue = null, key1 = imagenUri
    ) {
        value = withContext(Dispatchers.IO) {
            try {
                imagenUri?.let { ruta ->
                    context.contentResolver.openInputStream(Uri.parse(ruta))?.use {
                        BitmapFactory.decodeStream(it)?.asImageBitmap()
                    }
                }
            } catch (_: Exception) { null }
        }
    }

    // null = crear un punto nuevo.
    // Con un punto seleccionado = editar ese punto.
    var puntoEnEdicion by remember {
        mutableStateOf<PuntoInteres?>(null)
    }

    // Limpia los campos y vuelve al modo de registro.
    fun limpiarFormulario() {
        nombre = ""
        descripcion = ""
        latitud = ""
        longitud = ""
        altitud = ""
        radio = "50"
        imagenUri = null
        puntoEnEdicion = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // Flecha para regresar al panel del administrador.
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = onVolverClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver al Administrador"
                )
            }

            Text(
                text = "Gestión de Puntos Turísticos",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Cambia el título según la operación.
        Text(
            text = if (puntoEnEdicion == null) {
                "Registrar un nuevo lugar turístico"
            } else {
                "Editar punto turístico"
            },
            style = MaterialTheme.typography.titleMedium
        )

        // -----------------------------
        // FORMULARIO
        // -----------------------------

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre del lugar") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        OutlinedTextField(
            value = latitud,
            onValueChange = { latitud = it },
            label = { Text("Latitud") },
            placeholder = { Text("Ejemplo: -33.4372") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = longitud,
            onValueChange = { longitud = it },
            label = { Text("Longitud") },
            placeholder = { Text("Ejemplo: -70.6506") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = altitud,
            onValueChange = { altitud = it },
            label = { Text("Altitud en metros") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = radio,
            onValueChange = { radio = it },
            label = {
                Text("Radio de tolerancia GPS en metros")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // -----------------------------
        // FOTOGRAFÍA DEL PUNTO
        // -----------------------------
        Text("Fotografía del lugar", style = MaterialTheme.typography.titleMedium)

        val fotoInicial = puntoEnEdicion?.id?.let { imagenPredeterminadaPunto(it) }
        Card(modifier = Modifier.fillMaxWidth().height(190.dp)) {
            when {
                vistaPrevia != null -> Image(
                    bitmap = vistaPrevia!!,
                    contentDescription = "Fotografía seleccionada",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                fotoInicial != null && imagenUri == null -> Image(
                    painter = painterResource(fotoInicial),
                    contentDescription = "Fotografía predeterminada",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Sin fotografía seleccionada")
                }
            }
        }
        OutlinedButton(
            onClick = { seleccionarFoto.launch(arrayOf("image/*")) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !guardando
        ) {
            Text(if (imagenUri == null) "Seleccionar fotografía" else "Cambiar fotografía")
        }
        if (imagenUri == null && puntoEnEdicion != null && fotoInicial != null) {
            Text("Se utilizará la fotografía inicial del lugar.")
        }

        // -----------------------------
        // GUARDAR O ACTUALIZAR
        // -----------------------------

        Button(
            enabled = !guardando,
            modifier = Modifier.fillMaxWidth(),
            onClick = {

                // Convierte los textos a números.
                val lat = latitud.trim()
                    .replace(",", ".")
                    .toDoubleOrNull()

                val lon = longitud.trim()
                    .replace(",", ".")
                    .toDoubleOrNull()

                val alt = altitud.trim()
                    .replace(",", ".")
                    .toDoubleOrNull()

                val radioMetros = radio.trim()
                    .replace(",", ".")
                    .toDoubleOrNull()

                // Comprueba los datos antes de guardarlos.
                when {

                    nombre.isBlank() || descripcion.isBlank() -> {
                        mensaje = "Ingresa el nombre y la descripción."
                    }

                    lat == null ||
                            !lat.isFinite() ||
                            lat !in -90.0..90.0 -> {
                        mensaje = "La latitud debe estar entre -90 y 90."
                    }

                    lon == null ||
                            !lon.isFinite() ||
                            lon !in -180.0..180.0 -> {
                        mensaje = "La longitud debe estar entre -180 y 180."
                    }

                    alt == null || !alt.isFinite() -> {
                        mensaje = "Ingresa una altitud válida."
                    }

                    radioMetros == null ||
                            !radioMetros.isFinite() ||
                            radioMetros <= 0.0 -> {
                        mensaje = "El radio GPS debe ser mayor que cero."
                    }

                    else -> {

                        guardando = true
                        mensaje = ""

                        scope.launch {
                            try {

                                // Conserva el ID cuando se edita.
                                // Conserva también el estado completado.
                                val puntoParaGuardar = PuntoInteres(
                                    id = puntoEnEdicion?.id ?: 0,
                                    nombre = nombre.trim(),
                                    descripcion = descripcion.trim(),
                                    latitud = lat,
                                    longitud = lon,
                                    altitud = alt,
                                    completado = puntoEnEdicion?.completado
                                        ?: false,
                                    radioToleranciaMetros = radioMetros,
                                    imagenUri = imagenUri
                                )

                                val editando = puntoEnEdicion != null

                                // Ejecuta la operación en Room.
                                withContext(Dispatchers.IO) {

                                    if (editando) {

                                        // Actualiza el registro existente.
                                        // No crea un ID nuevo.
                                        puntoDao.actualizar(
                                            puntoParaGuardar
                                        )

                                    } else {

                                        // Registra un punto nuevo.
                                        puntoDao.crearPunto(
                                            puntoParaGuardar
                                        )
                                    }
                                }

                                limpiarFormulario()

                                mensaje = if (editando) {
                                    "Punto turístico actualizado correctamente."
                                } else {
                                    "Punto turístico registrado correctamente."
                                }

                            } catch (e: Exception) {

                                mensaje =
                                    "No se pudo guardar el punto turístico."

                            } finally {

                                guardando = false
                            }
                        }
                    }
                }
            }
        ) {
            Text(
                when {
                    guardando -> "Guardando..."
                    puntoEnEdicion != null -> "Guardar cambios"
                    else -> "Guardar punto turístico"
                }
            )
        }

        // -----------------------------
        // CANCELAR EDICIÓN
        // -----------------------------

        if (puntoEnEdicion != null) {

            OutlinedButton(
                onClick = {
                    limpiarFormulario()
                    mensaje = ""
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !guardando
            ) {
                Text("Cancelar edición")
            }
        }

        // Mensaje de resultado.
        if (mensaje.isNotEmpty()) {
            Text(
                text = mensaje,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // -----------------------------
        // LISTADO DE PUNTOS
        // -----------------------------

        Text(
            text = "Puntos turísticos registrados (${puntos.size})",
            style = MaterialTheme.typography.titleMedium
        )

        if (puntos.isEmpty()) {

            Text("No hay puntos turísticos registrados.")

        } else {

            puntos.forEach { punto ->

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {

                        Text(
                            text = punto.nombre,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = punto.descripcion
                        )

                        Text(
                            text = "Latitud: ${punto.latitud}"
                        )

                        Text(
                            text = "Longitud: ${punto.longitud}"
                        )

                        Text(
                            text = "Altitud: ${punto.altitud} m"
                        )

                        Text(
                            text = "Radio GPS: ${punto.radioToleranciaMetros} m"
                        )

                        // Carga el punto en el formulario superior.
                        OutlinedButton(
                            onClick = {

                                puntoEnEdicion = punto

                                nombre = punto.nombre
                                descripcion = punto.descripcion
                                latitud = punto.latitud.toString()
                                longitud = punto.longitud.toString()
                                altitud = punto.altitud.toString()
                                radio = punto.radioToleranciaMetros.toString()
                                imagenUri = punto.imagenUri

                                mensaje = "Editando: ${punto.nombre}"

                                // Sube al formulario automáticamente.
                                scope.launch {
                                    scrollState.animateScrollTo(0)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !guardando
                        ) {
                            Text("Editar")
                        }
                    }
                }
            }
        }
    }
}