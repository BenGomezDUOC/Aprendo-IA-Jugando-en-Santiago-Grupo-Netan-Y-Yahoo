package cl.triskeledu.aprendogame.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

    // Configuración base de OSMDroid (necesaria para caching y User-Agent)
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Vista del Mapa OpenStreetMap
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(14.5)
                    // Coordenada central: Santiago Centro
                    controller.setCenter(GeoPoint(-33.4429, -70.6539))

                    // Capa de ubicación en el mapa
                    val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)
                    locationOverlay.enableMyLocation()
                    overlays.add(locationOverlay)

                    mapViewRef = this
                }
            },
            update = { mapView ->
                // Limpiar marcadores previos excepto la capa de ubicación (índice 0)
                while (mapView.overlays.size > 1) {
                    mapView.overlays.removeAt(mapView.overlays.size - 1)
                }

                // Agregar marcadores para cada Punto de Interés
                puntos.forEach { punto ->
                    val marker = Marker(mapView).apply {
                        position = GeoPoint(punto.latitud, punto.longitud)
                        title = punto.nombre
                        snippet = punto.descripcion
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
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

        // Limpieza del ciclo de vida del MapView
        DisposableEffect(Unit) {
            onDispose {
                mapViewRef?.onDetach()
            }
        }

        // Header superior tipo "Outdoor UX" con alto contraste
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E293B) // Azul oscuro alto contraste
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
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
                        text = "Progreso: $completados / ${puntos.size} descubiertos",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Botón flotante para re-centrar en el usuario
        FloatingActionButton(
            onClick = {
                ubicacionActual?.let { loc ->
                    mapViewRef?.controller?.animateTo(GeoPoint(loc.latitude, loc.longitude))
                } ?: run {
                    // Si aún no hay GPS, centrar en Plaza de Armas
                    mapViewRef?.controller?.animateTo(GeoPoint(-33.4372, -70.6506))
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = if (puntoSeleccionado != null) 230.dp else 24.dp),
            containerColor = Color(0xFF0284C7),
            contentColor = Color.White
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "Mi Ubicación")
        }

        // Tarjeta emergente inferior al tocar un marcador (BottomSheet / Card)
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
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF0F172A) // Alto contraste Outdoor
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = punto.nombre,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
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

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (punto.completado) Icons.Default.CheckCircle else Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (punto.completado) Color(0xFF4ADE80) else Color(0xFFFBBF24),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (punto.completado) "¡Lugar ya completado!" else "Pendiente de visita",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (punto.completado) Color(0xFF4ADE80) else Color(0xFFFBBF24)
                            )
                            if (distancia != null) {
                                Spacer(modifier = Modifier.width(12.dp))
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
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2563EB)
                                ),
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
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF16A34A)
                                    ),
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
