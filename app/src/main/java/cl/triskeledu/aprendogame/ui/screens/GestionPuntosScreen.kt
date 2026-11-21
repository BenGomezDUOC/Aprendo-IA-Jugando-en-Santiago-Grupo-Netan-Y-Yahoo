
package cl.triskeledu.aprendogame.ui.screens

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

import cl.triskeledu.aprendogame.ui.imagenPredeterminadaPunto
import cl.triskeledu.aprendogame.viewmodel.GestionPuntosViewModel

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

import java.util.Locale

@Composable
fun GestionPuntosScreen(
    onVolverClick: () -> Unit
) {

    val context = LocalContext.current

    // -------------------------------------------------
    // VIEWMODEL
    // -------------------------------------------------

    val gestionViewModel: GestionPuntosViewModel =
        viewModel()

    // Observa el estado del formulario.
    val estado by gestionViewModel.estado
        .collectAsStateWithLifecycle()

    // Observa los puntos registrados en Room.
    val puntos by gestionViewModel.puntos
        .collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()

    val scrollState = rememberScrollState()

    val ocupado = estado.ocupado

    val radioMetros = estado.radioMetros

    // -------------------------------------------------
    // SELECCIONAR FOTOGRAFÍA
    // -------------------------------------------------

    val seleccionarFoto =
        rememberLauncherForActivityResult(

            contract =
                ActivityResultContracts.OpenDocument()

        ) { uri: Uri? ->

            if (uri != null) {

                try {

                    context.contentResolver
                        .takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )

                    gestionViewModel.cambiarFotografia(
                        uri.toString()
                    )

                } catch (e: Exception) {

                    gestionViewModel.informar(
                        "No se pudo obtener permiso para la fotografía.",
                        true
                    )
                }
            }
        }

    // -------------------------------------------------
    // VISTA PREVIA DE LA FOTOGRAFÍA
    // -------------------------------------------------

    val imagenSeleccionada = estado.imagenUri

    val vistaPrevia by produceState<ImageBitmap?>(

        initialValue = null,

        key1 = imagenSeleccionada

    ) {

        value = null

        value = withContext(Dispatchers.IO) {

            try {

                imagenSeleccionada?.let { ruta ->

                    context.contentResolver
                        .openInputStream(
                            Uri.parse(ruta)
                        )
                        ?.use {

                            BitmapFactory.decodeStream(it)
                                ?.asImageBitmap()
                        }
                }

            } catch (e: CancellationException) {

                throw e

            } catch (_: Exception) {

                null
            }
        }
    }

    // -------------------------------------------------
    // MOSTRAR DIRECCIONES ENCONTRADAS
    // -------------------------------------------------

    if (estado.mostrarDirecciones) {

        AlertDialog(

            onDismissRequest = {

                gestionViewModel.cerrarDirecciones()
            },

            title = {

                Text("Seleccionar ubicación")
            },

            text = {

                Column(

                    modifier = Modifier
                        .heightIn(max = 350.dp)
                        .verticalScroll(
                            rememberScrollState()
                        ),

                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        "Encontramos varias ubicaciones. Selecciona la que corresponda al punto turístico."
                    )

                    estado.resultadosDireccion
                        .forEach { resultado ->

                            OutlinedButton(

                                onClick = {

                                    gestionViewModel
                                        .seleccionarDireccion(
                                            resultado
                                        )
                                },

                                modifier =
                                    Modifier.fillMaxWidth()

                            ) {

                                Text(
                                    resultado.getAddressLine(0)
                                        ?: "${resultado.latitude}, ${resultado.longitude}"
                                )
                            }
                        }
                }
            },

            confirmButton = {

                TextButton(

                    onClick = {

                        gestionViewModel.cerrarDirecciones()
                    }

                ) {

                    Text("Cancelar")
                }
            }
        )
    }

    // =================================================
    // INTERFAZ PRINCIPAL
    // =================================================

    Column(

        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp),

        verticalArrangement =
            Arrangement.spacedBy(12.dp)

    ) {

        // -------------------------------------------------
        // ENCABEZADO
        // -------------------------------------------------

        Row(

            modifier = Modifier.fillMaxWidth(),

            verticalAlignment =
                Alignment.CenterVertically

        ) {

            IconButton(
                onClick = onVolverClick
            ) {

                Icon(
                    imageVector =
                        Icons.AutoMirrored.Filled.ArrowBack,

                    contentDescription =
                        "Volver al Administrador"
                )
            }

            Text(

                text = "Gestión de Puntos Turísticos",

                style =
                    MaterialTheme.typography.titleLarge,

                modifier = Modifier.weight(1f)
            )
        }

        Text(

            text = if (estado.puntoEnEdicion == null) {

                "Registrar un nuevo lugar turístico"

            } else {

                "Editar punto turístico"
            },

            style =
                MaterialTheme.typography.titleMedium
        )

        // -------------------------------------------------
        // NOMBRE DEL LUGAR
        // -------------------------------------------------

        OutlinedTextField(

            value = estado.nombre,

            onValueChange =
                gestionViewModel::cambiarNombre,

            label = {

                Text("Nombre del lugar")
            },

            placeholder = {

                Text("Ejemplo: Cerro San Cristóbal")
            },

            modifier = Modifier.fillMaxWidth(),

            singleLine = true,

            enabled = !ocupado
        )

        // -------------------------------------------------
        // DESCRIPCIÓN
        // -------------------------------------------------

        OutlinedTextField(

            value = estado.descripcion,

            onValueChange =
                gestionViewModel::cambiarDescripcion,

            label = {

                Text("Descripción")
            },

            modifier = Modifier.fillMaxWidth(),

            minLines = 3,

            enabled = !ocupado
        )

        // -------------------------------------------------
        // DIRECCIÓN
        // -------------------------------------------------

        OutlinedTextField(

            value = estado.direccion,

            onValueChange =
                gestionViewModel::cambiarDireccion,

            label = {

                Text("Dirección")
            },

            placeholder = {

                Text("Calle, número y comuna")
            },

            supportingText = {

                Text(
                    "Ingresa la dirección exacta del punto turístico."
                )
            },

            modifier = Modifier.fillMaxWidth(),

            minLines = 1,

            maxLines = 3,

            enabled = !ocupado
        )

        // -------------------------------------------------
        // BUSCAR UBICACIÓN
        // -------------------------------------------------

        Button(

            onClick =
                gestionViewModel::buscarUbicacion,

            modifier =
                Modifier.fillMaxWidth(),

            enabled = !ocupado &&
                    estado.direccion.isNotBlank()

        ) {

            if (estado.buscandoUbicacion) {

                CircularProgressIndicator(

                    modifier = Modifier.size(20.dp),

                    strokeWidth = 2.dp,

                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(
                    modifier = Modifier.width(10.dp)
                )

                Text("Buscando ubicación...")

            } else {

                Text("Buscar ubicación")
            }
        }

        // -------------------------------------------------
        // RADIO DE TOLERANCIA GPS
        // -------------------------------------------------

        OutlinedTextField(

            value = estado.radio,

            onValueChange =
                gestionViewModel::cambiarRadio,

            label = {

                Text("Radio de tolerancia GPS en metros")
            },

            supportingText = {

                Text("Valor predeterminado: 50 metros.")
            },

            modifier = Modifier.fillMaxWidth(),

            singleLine = true,

            enabled = !ocupado,

            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            )
        )

        // -------------------------------------------------
        // DATOS GEOGRÁFICOS AUTOMÁTICOS
        // -------------------------------------------------

        estado.ubicacion?.let { datos ->

            Card(

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(16.dp),

                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFEAF2FF)
                )

            ) {

                Column(

                    modifier = Modifier.padding(14.dp),

                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)

                ) {

                    Text(

                        text = "Ubicación encontrada",

                        style =
                            MaterialTheme.typography.titleMedium,

                        color = Color(0xFF2563EB)
                    )

                    if (datos.direccion.isNotBlank()) {

                        Text(
                            text = datos.direccion,
                            color = Color(0xFF17243D)
                        )
                    }

                    HorizontalDivider()

                    Text(

                        text = "Latitud: ${
                            String.format(
                                Locale.US,
                                "%.6f",
                                datos.latitud
                            )
                        }",

                        color = Color(0xFF17243D)
                    )

                    Text(

                        text = "Longitud: ${
                            String.format(
                                Locale.US,
                                "%.6f",
                                datos.longitud
                            )
                        }",

                        color = Color(0xFF17243D)
                    )

                    Text(

                        text = "Altitud aproximada: ${datos.altitud} m",

                        color = Color(0xFF17243D)
                    )

                    Text(

                        text = if (radioMetros != null) {
                            "Radio GPS: $radioMetros m"
                        } else {
                            "Radio GPS: inválido"
                        },

                        color = Color(0xFF17243D)
                    )

                    // Mapa con marcador y radio GPS.
                    VistaPreviaPuntoTuristico(

                        latitud = datos.latitud,

                        longitud = datos.longitud,

                        nombre = estado.nombre.ifBlank {
                            "Punto turístico"
                        },

                        radio = radioMetros ?: 0.0
                    )

                    Text(

                        text = "Comprueba que el marcador corresponda al lugar que deseas registrar.",

                        style =
                            MaterialTheme.typography.bodySmall,

                        color = Color(0xFF17243D)
                    )

                    Row(

                        verticalAlignment =
                            Alignment.CenterVertically

                    ) {

                        Checkbox(

                            checked =
                                estado.ubicacionConfirmada,

                            enabled =
                                !ocupado && radioMetros != null,

                            onCheckedChange =
                                gestionViewModel::confirmarUbicacion
                        )

                        Text(

                            text = "Confirmo que la ubicación es correcta.",

                            color = Color(0xFF17243D)
                        )
                    }

                    if (estado.ubicacionConfirmada) {

                        Text(

                            text = "✓ Ubicación confirmada",

                            color = Color(0xFF16A34A)
                        )
                    }
                }
            }
        }

        // -------------------------------------------------
        // FOTOGRAFÍA DEL PUNTO
        // -------------------------------------------------

        Text(

            text = "Fotografía del lugar",

            style = MaterialTheme.typography.titleMedium
        )

        val fotoInicial =
            estado.puntoEnEdicion?.id?.let {

                imagenPredeterminadaPunto(it)
            }

        Card(

            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)

        ) {

            when {

                vistaPrevia != null -> {

                    Image(

                        bitmap = vistaPrevia!!,

                        contentDescription =
                            "Fotografía seleccionada",

                        modifier =
                            Modifier.fillMaxSize(),

                        contentScale =
                            ContentScale.Crop
                    )
                }

                fotoInicial != null &&
                        estado.imagenUri == null -> {

                    Image(

                        painter =
                            painterResource(fotoInicial),

                        contentDescription =
                            "Fotografía predeterminada",

                        modifier =
                            Modifier.fillMaxSize(),

                        contentScale =
                            ContentScale.Crop
                    )
                }

                else -> {

                    Box(

                        modifier = Modifier.fillMaxSize(),

                        contentAlignment = Alignment.Center

                    ) {

                        Text(
                            "Sin fotografía seleccionada"
                        )
                    }
                }
            }
        }

        OutlinedButton(

            onClick = {

                seleccionarFoto.launch(
                    arrayOf("image/*")
                )
            },

            modifier = Modifier.fillMaxWidth(),

            enabled = !ocupado

        ) {

            Text(

                if (estado.imagenUri == null) {

                    "Seleccionar fotografía"

                } else {

                    "Cambiar fotografía"
                }
            )
        }

        // -------------------------------------------------
        // GUARDAR O ACTUALIZAR
        // -------------------------------------------------

        Button(

            enabled = !ocupado,

            modifier = Modifier.fillMaxWidth(),

            onClick = gestionViewModel::guardarPunto

        ) {

            Text(

                when {

                    estado.guardando ->
                        "Guardando..."

                    estado.puntoEnEdicion != null ->
                        "Guardar cambios"

                    else ->
                        "Guardar punto turístico"
                }
            )
        }

        // -------------------------------------------------
        // CANCELAR EDICIÓN
        // -------------------------------------------------

        if (estado.puntoEnEdicion != null) {

            OutlinedButton(

                onClick =
                    gestionViewModel::limpiarFormulario,

                enabled = !ocupado,

                modifier = Modifier.fillMaxWidth()

            ) {

                Text("Cancelar edición")
            }
        }

        // -------------------------------------------------
        // MENSAJES
        // -------------------------------------------------

        if (estado.mensaje.isNotBlank()) {

            Text(

                text = estado.mensaje,

                color = if (estado.mensajeError) {

                    MaterialTheme.colorScheme.error

                } else {

                    MaterialTheme.colorScheme.primary
                }
            )
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // =================================================
        // PUNTOS TURÍSTICOS REGISTRADOS
        // =================================================

        Text(

            text = "Puntos turísticos registrados (${puntos.size})",

            style =
                MaterialTheme.typography.titleMedium
        )

        if (puntos.isEmpty()) {

            Text(
                "No hay puntos turísticos registrados."
            )

        } else {

            puntos.forEach { punto ->

                Card(

                    modifier = Modifier.fillMaxWidth(),

                    colors = CardDefaults.cardColors(

                        containerColor =
                            MaterialTheme.colorScheme.surface
                    )

                ) {

                    Column(

                        modifier = Modifier.padding(16.dp),

                        verticalArrangement =
                            Arrangement.spacedBy(6.dp)

                    ) {

                        Text(

                            text = punto.nombre,

                            style =
                                MaterialTheme.typography.titleMedium
                        )

                        Text(punto.descripcion)

                        Text(

                            text = if (
                                punto.direccion.isBlank()
                            ) {

                                "Dirección: pendiente de registrar"

                            } else {

                                "Dirección: ${punto.direccion}"
                            }
                        )

                        Text(
                            "Latitud: ${punto.latitud}"
                        )

                        Text(
                            "Longitud: ${punto.longitud}"
                        )

                        Text(
                            "Altitud: ${punto.altitud} m"
                        )

                        Text(
                            "Radio GPS: ${punto.radioToleranciaMetros} m"
                        )

                        // Carga los datos en el formulario.
                        OutlinedButton(

                            onClick = {

                                gestionViewModel.editarPunto(
                                    punto
                                )

                                scope.launch {

                                    scrollState.animateScrollTo(0)
                                }
                            },

                            modifier =
                                Modifier.fillMaxWidth(),

                            enabled = !ocupado

                        ) {

                            Text("Editar")
                        }
                    }
                }
            }
        }
    }
}

// =====================================================
// MAPA DE VISTA PREVIA DEL PUNTO TURÍSTICO
// =====================================================

@Composable
private fun VistaPreviaPuntoTuristico(

    latitud: Double,

    longitud: Double,

    nombre: String,

    radio: Double

) {

    val context = LocalContext.current

    val lifecycleOwner =
        LocalLifecycleOwner.current

    val mapa = remember(context) {

        Configuration.getInstance().userAgentValue =
            context.packageName

        MapView(context).apply {

            setTileSource(
                TileSourceFactory.MAPNIK
            )

            setMultiTouchControls(true)

            controller.setZoom(17.0)
        }
    }

    // Controla el ciclo de vida del mapa.
    DisposableEffect(mapa, lifecycleOwner) {

        val observador =
            LifecycleEventObserver { _, evento ->

                when (evento) {

                    Lifecycle.Event.ON_RESUME -> {

                        mapa.onResume()
                    }

                    Lifecycle.Event.ON_PAUSE -> {

                        mapa.onPause()
                    }

                    else -> Unit
                }
            }

        lifecycleOwner.lifecycle
            .addObserver(observador)

        onDispose {

            lifecycleOwner.lifecycle
                .removeObserver(observador)

            mapa.onPause()

            mapa.onDetach()
        }
    }

    AndroidView(

        factory = {
            mapa
        },

        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(12.dp)),

        update = { mapView ->

            val posicion = GeoPoint(
                latitud,
                longitud
            )

            mapView.overlays.clear()

            // Círculo del radio GPS.
            if (
                radio.isFinite() &&
                radio > 0.0 &&
                radio <= 100000.0
            ) {

                val circulo = Polygon(mapView)

                circulo.points =
                    Polygon.pointsAsCircle(
                        posicion,
                        radio
                    )

                circulo.fillPaint.color =
                    android.graphics.Color.argb(
                        45, 37, 99, 235
                    )

                circulo.outlinePaint.color =
                    android.graphics.Color.rgb(
                        37, 99, 235
                    )

                circulo.outlinePaint.strokeWidth =
                    3f

                mapView.overlays.add(circulo)
            }

            // Marcador del punto turístico.
            val marcador = Marker(mapView)

            marcador.position = posicion

            marcador.title = nombre

            marcador.setAnchor(
                Marker.ANCHOR_CENTER,
                Marker.ANCHOR_BOTTOM
            )

            mapView.overlays.add(marcador)

            mapView.controller.setCenter(
                posicion
            )

            mapView.invalidate()
        }
    )
}
