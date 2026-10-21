package cl.triskeledu.aprendogame.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.triskeledu.aprendogame.model.Rol
import cl.triskeledu.aprendogame.ui.imagenPredeterminadaPunto
import cl.triskeledu.aprendogame.viewmodel.GameViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

private val AzulTurismo = Color(0xFF2359D1)
private val FondoTurismo = Color(0xFFF7F8FC)

/** Detalle compartido por los tres roles; solo el cliente puede escanear QR. */
@Composable
fun DetallePuntoScreen(
    puntoId: Int,
    rol: Rol,
    gameViewModel: GameViewModel,
    onVolver: () -> Unit,
    onEscanearQr: () -> Unit
) {
    val puntos by gameViewModel.puntos.collectAsStateWithLifecycle()
    val capsulas by gameViewModel.capsulas.collectAsStateWithLifecycle()
    val descubrimientos by gameViewModel.descubrimientosQr.collectAsStateWithLifecycle()
    val punto = puntos.firstOrNull { it.id == puntoId }
    val qrDelPunto = capsulas.filter {
        it.puntoId == puntoId && it.codigoQr.isNotBlank() &&
                it.estado.equals("PUBLICADA", ignoreCase = true)
    }
    val descubiertos = descubrimientos.map { it.capsulaId }.toSet()
    val context = LocalContext.current

    // Si el administrador seleccionó una fotografía, tiene prioridad sobre drawable.
    // La lectura se hace fuera del hilo principal para evitar congelar la pantalla.
    val imagenGaleria by produceState<androidx.compose.ui.graphics.ImageBitmap?>(
        initialValue = null,
        key1 = punto?.imagenUri
    ) {
        value = withContext(Dispatchers.IO) {
            try {
                val ruta = punto?.imagenUri?.takeIf { it.isNotBlank() }
                if (ruta == null) null else context.contentResolver
                    .openInputStream(Uri.parse(ruta))?.use { stream ->
                        BitmapFactory.decodeStream(stream)?.asImageBitmap()
                    }
            } catch (_: Exception) {
                null
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(FondoTurismo)
            .verticalScroll(rememberScrollState())
    ) {
        if (punto == null) {
            IconButton(onClick = onVolver) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Text("El punto turístico no está disponible.", modifier = Modifier.padding(20.dp))
        } else {
            Box(modifier = Modifier.fillMaxWidth().height(255.dp)) {
                val imagenInicial = imagenPredeterminadaPunto(punto.id)
                when {
                    imagenGaleria != null -> Image(
                        bitmap = imagenGaleria!!,
                        contentDescription = "Fotografía de ${punto.nombre}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    imagenInicial != null -> Image(
                        painter = painterResource(imagenInicial),
                        contentDescription = "Fotografía de ${punto.nombre}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    else -> Box(
                        modifier = Modifier.fillMaxSize().background(Color(0xFFDCE8FA)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Place, null, tint = AzulTurismo,
                            modifier = Modifier.size(72.dp))
                    }
                }
                IconButton(
                    onClick = onVolver,
                    modifier = Modifier.padding(16.dp).size(44.dp)
                        .background(Color.White.copy(alpha = 0.95f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver al mapa", tint = Color(0xFF1B2943))
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(punto.nombre, style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold, color = Color(0xFF17233D))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = Color(0xFFE6EEFF), shape = RoundedCornerShape(50.dp)) {
                        Text("Hito turístico", modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            color = AzulTurismo, style = MaterialTheme.typography.labelMedium)
                    }
                    val distancia = gameViewModel.calcularDistanciaMetros(punto)
                    Surface(color = Color.White, shape = RoundedCornerShape(50.dp)) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = AzulTurismo,
                                modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(3.dp))
                            Text(
                                when {
                                    distancia == null -> "Ubicación no disponible"
                                    distancia < 1000f -> "${distancia.toInt()} m"
                                    else -> "${String.format(Locale.getDefault(), "%.1f", distancia / 1000f)} km"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF40516D)
                            )
                        }
                    }
                }

                Text("Acerca de este lugar", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = Color(0xFF17233D))
                Text(punto.descripcion, style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF52617A))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Estado de los QR", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold, color = Color(0xFF17233D))
                        if (qrDelPunto.isEmpty()) {
                            Text("No hay QR publicados para este lugar.",
                                color = Color(0xFF52617A))
                        } else {
                            qrDelPunto.forEachIndexed { indice, capsula ->
                                val disponible = capsula.id in descubiertos
                                Surface(
                                    color = Color(0xFFF3F6FC),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(Icons.Default.QrCodeScanner, null, tint = AzulTurismo)
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("QR ${indice + 1}", fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF17233D))
                                            Text(capsula.titulo,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF52617A))
                                        }
                                        Text(
                                            if (rol == Rol.CLIENTE) {
                                                if (disponible) "Disponible" else "Pendiente"
                                            } else "Publicado",
                                            color = if (rol == Rol.CLIENTE && !disponible)
                                                Color(0xFFAC6C17) else Color(0xFF16764B),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (rol == Rol.CLIENTE) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF1FF))
                    ) {
                        Column(modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Recompensas", style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold, color = Color(0xFF17233D))
                            Text("+10 puntos por descubrir un QR nuevo",
                                color = Color(0xFF40516D))
                            Text("+100 puntos por selfie válida, según las reglas del sistema",
                                color = Color(0xFF40516D))
                        }
                    }
                    Button(
                        onClick = onEscanearQr,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulTurismo)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                        Spacer(Modifier.width(10.dp))
                        Text("Escanear QR", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}



