package cl.triskeledu.aprendogame.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.triskeledu.aprendogame.model.PuntoInteres
import cl.triskeledu.aprendogame.viewmodel.GameViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun MapaScreen(
    viewModel: GameViewModel = viewModel(),
    onEscanearQrClick: (PuntoInteres) -> Unit = {}
) {
    val context = LocalContext.current
    val puntos by viewModel.puntos.collectAsStateWithLifecycle()
    val ubicacionActual by viewModel.ubicacionActual.collectAsStateWithLifecycle()
    val puntoSeleccionado by viewModel.puntoSeleccionado.collectAsStateWithLifecycle()

    var tienePermisoUbicacion by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcherPermiso = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        val concedido = permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        tienePermisoUbicacion = concedido
        if (concedido) {
            viewModel.iniciarRastreoGPS(context)
        }
    }

    LaunchedEffect(Unit) {
        if (tienePermisoUbicacion) {
            viewModel.iniciarRastreoGPS(context)
        } else {
            launcherPermiso.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    // Generador de iconos nativos personalizados (Pin circular estilizado sin emojis)
    fun crearPinDrawable(completado: Boolean): BitmapDrawable {
        val diametro = 84
        val bitmap = Bitmap.createBitmap(diametro, diametro, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paintSombra = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(80, 0, 0, 0)
        }
        val paintBorde = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
        }
        val paintFondo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            // Verde si está completado, azul Triskeledu si está pendiente
            color = if (completado) android.graphics.Color.parseColor("#16A34A") else android.graphics.Color.parseColor("#2563EB")
        }
        val paintCentro = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
        }

        val centro = diametro / 2f
        canvas.drawCircle(centro, centro + 2f, 34f, paintSombra)
        canvas.drawCircle(centro, centro, 32f, paintBorde)
        canvas.drawCircle(centro, centro, 28f, paintFondo)
        canvas.drawCircle(centro, centro, 10f, paintCentro)

        return BitmapDrawable(context.resources, bitmap)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Mapa centrado a pantalla completa
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(15.2)
                    controller.setCenter(GeoPoint(-33.4372, -70.6506))

                    val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)
                    locationOverlay.enableMyLocation()
                    overlays.add(locationOverlay)

                    mapViewRef = this
                }
            },
            update = { mapView ->
                while (mapView.overlays.size > 1) {
                    mapView.overlays.removeAt(mapView.overlays.size - 1)
                }

                puntos.forEach { punto ->
                    val marker = Marker(mapView).apply {
                        position = GeoPoint(punto.latitud, punto.longitud)
                        title = punto.nombre
                        snippet = punto.descripcion
                        icon = crearPinDrawable(punto.completado)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        setOnMarkerClickListener { _, _ ->
                            viewModel.seleccionarPunto(punto)
                            true
                        }
                    }
                    mapView.overlays.add(marker)
                }
                mapView.invalidate()
            }
        )

        DisposableEffect(Unit) {
            onDispose {
                mapViewRef?.onDetach()
            }
        }

        // Barra superior de estado (Limpia y sobria)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .align(Alignment.TopCenter),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Santiago Turístico IA",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    val completados = puntos.count { it.completado }
                    Text(
                        text = "$completados de ${puntos.size} puntos completados",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF38BDF8)
                    )
                }
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Botón flotante para recentrar en el usuario
        FloatingActionButton(
            onClick = {
                ubicacionActual?.let { loc ->
                    mapViewRef?.controller?.animateTo(GeoPoint(loc.latitude, loc.longitude))
                } ?: run {
                    mapViewRef?.controller?.animateTo(GeoPoint(-33.4372, -70.6506))
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = if (puntoSeleccionado != null) 230.dp else 24.dp),
            containerColor = Color(0xFF2563EB),
            contentColor = Color.White
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "Mi Ubicación")
        }

        // Tarjeta emergente inferior al tocar un marcador
        AnimatedVisibility(
            visible = puntoSeleccionado != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            enter = slideInVertically { it },
            exit = slideOutVertically { it }
        ) {
            puntoSeleccionado?.let { punto ->
                val distancia = viewModel.calcularDistanciaMetros(punto)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = punto.nombre,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            IconButton(onClick = { viewModel.seleccionarPunto(null) }) {
                                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = punto.descripcion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFCBD5E1)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (punto.completado) Icons.Default.CheckCircle else Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (punto.completado) Color(0xFF4ADE80) else Color(0xFFFBBF24),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (punto.completado) "Completado" else "Pendiente de visita",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (punto.completado) Color(0xFF4ADE80) else Color(0xFFFBBF24)
                            )
                            if (distancia != null) {
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "• A aprox. ${distancia.toInt()} m",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onEscanearQrClick(punto) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Escanear QR")
                            }

                            if (!punto.completado) {
                                Button(
                                    onClick = {
                                        viewModel.marcarCompletado(punto.id)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Validar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
