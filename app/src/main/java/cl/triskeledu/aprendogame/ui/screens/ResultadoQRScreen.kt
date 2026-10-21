package cl.triskeledu.aprendogame.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.triskeledu.aprendogame.data.local.AppDatabase
import cl.triskeledu.aprendogame.model.Capsula
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun ResultadoQRScreen(
    capsula: Capsula,
    puntosGanados: Int,
    onVolverMapa: () -> Unit,
    onTomarSelfie: () -> Unit,
    onVerVideo: () -> Unit,
    onVolverEscaner: () -> Unit = onVolverMapa
) {
    val azul = Color(0xFF2563EB)
    val context = LocalContext.current
    val db = remember(context) { AppDatabase.getInstance(context.applicationContext) }

    // Los QR administrativos guardan el ID del contenido educativo en su código.
    val contenidoId = remember(capsula.codigoQr) {
        Regex("^TRISKELEDU-CONTENIDO-(\\d+)-")
            .find(capsula.codigoQr)
            ?.groupValues?.getOrNull(1)?.toIntOrNull()
    }

    // Mismo origen de datos que ContenidosEducativosScreen: videos_registrados.
    val videosRegistrados by remember(db) {
        db.videoRegistradoDao().obtenerTodos()
    }.collectAsState(initial = emptyList())

    val videoAsociado = remember(videosRegistrados, contenidoId, capsula.urlVideo) {
        if (contenidoId == null) null
        else {
            val candidatos = videosRegistrados.filter { it.contenidoId == contenidoId }
            // Si hay varios videos para el mismo contenido, se prioriza el que
            // coincide con la URL del video que realmente abre esta cápsula.
            candidatos.firstOrNull { it.urlVideo == capsula.urlVideo }
                ?: candidatos.singleOrNull()
        }
    }
    val rutaMiniatura = videoAsociado?.urlMiniatura.orEmpty()
    val miniatura by produceState<Bitmap?>(initialValue = null, rutaMiniatura) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                if (rutaMiniatura.isBlank()) null
                else {
                    val uri = Uri.parse(rutaMiniatura)
                    when (uri.scheme) {
                        "content" -> context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
                        "file" -> uri.path?.let(BitmapFactory::decodeFile)
                        else -> BitmapFactory.decodeFile(rutaMiniatura)
                    }
                }
            }.getOrNull()
        }
    }

    val escalaTrofeo = remember { Animatable(0f) }
    LaunchedEffect(capsula.id) {
        escalaTrofeo.snapTo(0f)
        delay(200)
        escalaTrofeo.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = 180f))
    }
    val transicion = rememberInfiniteTransition(label = "trofeo")
    val flotacion by transicion.animateFloat(
        initialValue = 1f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1300), RepeatMode.Reverse),
        label = "flotacion"
    )
    val escalaTrofeoFinal = escalaTrofeo.value * flotacion

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F9FF))) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 8.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolverEscaner) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver a escanear QR", tint = azul)
            }
            Text("Resultado del QR", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.height(122.dp)) {
                if (puntosGanados > 0) {
                    Text("🎊  ✨     🎉", fontSize = 24.sp, modifier = Modifier.align(Alignment.TopCenter))
                }
                Text("🏆", fontSize = 92.sp, modifier = Modifier.scale(escalaTrofeoFinal))
            }
            Text(
                if (puntosGanados > 0) "¡QR descubierto!" else "¡QR reconocido!",
                fontSize = 26.sp, fontWeight = FontWeight.Bold,
                color = Color(0xFF17243D), textAlign = TextAlign.Center
            )
            Text(
                "+$puntosGanados puntos", fontSize = 25.sp,
                fontWeight = FontWeight.Bold, color = Color(0xFF16A34A)
            )
            if (puntosGanados == 0) {
                Text(
                    "Ya habías descubierto este QR. No se sumaron puntos nuevamente.",
                    fontSize = 13.sp, textAlign = TextAlign.Center,
                    color = Color(0xFF475569)
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier.size(width = 100.dp, height = 92.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE5EEFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (miniatura != null) {
                            Image(
                                bitmap = miniatura!!.asImageBitmap(),
                                contentDescription = "Miniatura de ${capsula.titulo}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.PlayArrow, "Video educativo", tint = azul,
                                modifier = Modifier.size(40.dp))
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(capsula.titulo, fontWeight = FontWeight.Bold,
                            fontSize = 15.sp, color = Color(0xFF17243D))
                        Text(capsula.descripcion, fontSize = 12.sp,
                            color = Color(0xFF475569), maxLines = 3)
                        Text("Video educativo", fontSize = 11.sp,
                            color = azul, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7E7))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🏆", fontSize = 30.sp, modifier = Modifier.scale(flotacion))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("¿Es un hito turístico?", fontWeight = FontWeight.Bold,
                            fontSize = 15.sp, color = Color(0xFF17243D))
                        Text(
                            "Si este QR está en un lugar turístico, toma una selfie para ganar 100 puntos extra.",
                            fontSize = 12.sp, color = Color(0xFF475569)
                        )
                    }
                }
            }

            Button(
                onClick = onVerVideo,
                enabled = capsula.urlVideo.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = azul)
            ) { Text("Ver video", color = Color.White) }

            Button(
                onClick = onTomarSelfie,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
            ) { Text("Tomar selfie (+100 pts)", color = Color.White) }

            OutlinedButton(
                onClick = onVolverMapa,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(25.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, azul),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
            ) { Text("Volver al mapa", color = azul) }
        }
    }
}




