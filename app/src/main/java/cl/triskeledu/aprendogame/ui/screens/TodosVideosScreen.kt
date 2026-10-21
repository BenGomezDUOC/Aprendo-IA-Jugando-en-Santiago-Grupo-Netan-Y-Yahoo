package cl.triskeledu.aprendogame.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import cl.triskeledu.aprendogame.R
import cl.triskeledu.aprendogame.model.Capsula
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.triskeledu.aprendogame.data.local.AppDatabase
import cl.triskeledu.aprendogame.model.VideoRegistrado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun TodosVideosScreen(
    onVolver: () -> Unit,
    onAbrirVideo: (VideoRegistrado) -> Unit,
    onAbrirCapsula: (Capsula) -> Unit,
    onAbrirAdicional: (Int) -> Unit
) {
    val context = LocalContext.current
    val database = remember(context) { AppDatabase.getInstance(context.applicationContext) }
    val videos by remember(database) { database.videoRegistradoDao().obtenerTodos() }
        .collectAsState(initial = emptyList())

    val capsulas by remember(database) { database.capsulaDao().getTodasLasCapsulas() }
        .collectAsState(initial = emptyList())
    // Solo las seis cápsulas originales tienen un MP4 local asignado por puntoId.
    // Las cápsulas nuevas con video propio aparecen mediante VideoRegistrado.
    val codigosOriginales = setOf(
        "QR_PLAZA_ARMAS", "QR_SANTA_LUCIA", "QR_SAN_CRISTOBAL",
        "QR_LA_MONEDA", "QR_PARQUE_FORESTAL", "QR_BELLAVISTA"
    )
    val capsulasOriginales = capsulas.filter { it.codigoQr in codigosOriginales && it.puntoId in 1..6 }
    val videosConArchivo = videos.filter { it.urlVideo.isNotBlank() }
    val total = capsulasOriginales.size + 2 + videosConArchivo.size

    Column(Modifier.fillMaxSize().background(Color(0xFFF5F7FC))) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF203653)).padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver al panel principal", tint = Color.White)
            }
            Text("Todos los videos", fontSize = 20.sp, fontWeight = FontWeight.Bold,
                color = Color.White)
        }
        Text("Todos los videos educativos: $total",
            modifier = Modifier.padding(16.dp), color = Color(0xFF475569))
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Text("Videos de puntos turísticos (${capsulasOriginales.size})", fontWeight = FontWeight.Bold) }
            items(capsulasOriginales, key = { "capsula_${it.id}" }) { capsula ->
                FilaVideoBiblioteca(
                    titulo = capsula.titulo,
                    descripcion = capsula.descripcion,
                    miniaturaRes = when (capsula.puntoId) {
                        1 -> R.drawable.miniatura_plaza_armas
                        2 -> R.drawable.miniatura_santa_lucia
                        3 -> R.drawable.miniatura_san_cristobal
                        4 -> R.drawable.miniatura_la_moneda
                        5 -> R.drawable.miniatura_parque_forestal
                        6 -> R.drawable.miniatura_bellavista
                        else -> R.drawable.miniatura_plaza_armas
                    },
                    onClick = { onAbrirCapsula(capsula) }
                )
            }
            item { Text("Videos educativos adicionales (2)", fontWeight = FontWeight.Bold) }
            items(listOf(1, 2), key = { "adicional_$it" }) { id ->
                FilaVideoBiblioteca(
                    titulo = if (id == 1) "IA en nuestra vida cotidiana" else "Ética y uso responsable de la IA",
                    descripcion = "Video educativo sin código QR",
                    miniaturaRes = if (id == 1) R.drawable.miniatura_ia_cotidiana else R.drawable.miniatura_etica_ia,
                    onClick = { onAbrirAdicional(id) }
                )
            }
            item { Text("Videos registrados por el Administrador (${videosConArchivo.size})", fontWeight = FontWeight.Bold) }
            items(videosConArchivo, key = { "registrado_${it.id}" }) { video ->
                FilaVideoBiblioteca(
                    titulo = video.titulo,
                    descripcion = video.descripcion,
                    miniaturaUrl = video.urlMiniatura,
                    onClick = { onAbrirVideo(video) }
                )
            }
        }
    }
}

@Composable
private fun FilaVideoBiblioteca(
    titulo: String,
    descripcion: String,
    miniaturaRes: Int? = null,
    miniaturaUrl: String = "",
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (miniaturaRes != null) {
                Image(
                    painter = painterResource(miniaturaRes),
                    contentDescription = "Miniatura del video",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(width = 108.dp, height = 85.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                MiniaturaBiblioteca(miniaturaUrl)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(titulo, fontWeight = FontWeight.Bold, color = Color(0xFF17253D),
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(descripcion.ifBlank { "Sin descripción" }, fontSize = 12.sp,
                    color = Color(0xFF64748B), maxLines = 3,
                    overflow = TextOverflow.Ellipsis)
                Text("Ver video", fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold, color = Color(0xFF2563EB))
            }
        }
    }
}

@Composable
private fun MiniaturaBiblioteca(url: String) {
    val context = LocalContext.current
    val bitmap by produceState<android.graphics.Bitmap?>(initialValue = null, key1 = url) {
        value = withContext(Dispatchers.IO) {
            if (url.isBlank()) null else runCatching {
                val uri = Uri.parse(url)
                if (uri.scheme == "content") {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                } else {
                    BitmapFactory.decodeFile(if (uri.scheme == "file") uri.path else url)
                }
            }.getOrNull()
        }
    }
    Box(
        modifier = Modifier.size(width = 108.dp, height = 85.dp)
            .clip(RoundedCornerShape(12.dp)).background(Color(0xFFE6EDFF)),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(bitmap = bitmap!!.asImageBitmap(), contentDescription = "Miniatura del video",
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Icon(Icons.Default.PlayArrow, contentDescription = "Video",
                tint = Color(0xFF2563EB), modifier = Modifier.size(34.dp))
        }
    }
}
