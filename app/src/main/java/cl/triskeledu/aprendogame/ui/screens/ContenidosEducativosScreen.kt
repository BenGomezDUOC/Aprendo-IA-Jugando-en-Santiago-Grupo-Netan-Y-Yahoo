
package cl.triskeledu.aprendogame.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.triskeledu.aprendogame.R
import cl.triskeledu.aprendogame.data.local.AppDatabase
import cl.triskeledu.aprendogame.model.Capsula
import cl.triskeledu.aprendogame.model.VideoRegistrado
import cl.triskeledu.aprendogame.viewmodel.GameViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val fondoBiblioteca = Color(0xFFF3F6FD)
private val azulBiblioteca = Color(0xFF152D52)
private val textoBiblioteca = Color(0xFF1C2C45)

// Representa un video dentro de la biblioteca.
private data class ElementoBiblioteca(
    val clave: String,
    val titulo: String,
    val descripcion: String,
    val visto: Boolean,
    val miniatura: Int? = null,
    val videoRegistrado: VideoRegistrado? = null,
    val capsula: Capsula? = null,
    val videoAdicionalId: Int? = null
)

@Composable
fun ContenidosEducativosScreen(
    gameViewModel: GameViewModel,
    onVolver: () -> Unit,
    onVideoAdicionalClick: (Int) -> Unit,
    onVideoRegistradoClick: (VideoRegistrado) -> Unit,
    onCapsulaClick: (Capsula) -> Unit
) {
    val context = LocalContext.current

    val db = remember(context) {
        AppDatabase.getInstance(context.applicationContext)
    }

    // Recupera los contenidos publicados por el administrador.
    val contenidos by remember(db) {
        db.contenidoEducativoDao().obtenerPublicados()
    }.collectAsState(initial = emptyList())

    // Recupera los videos registrados por el administrador.
    val videos by remember(db) {
        db.videoRegistradoDao().obtenerTodos()
    }.collectAsState(initial = emptyList())

    // Recupera las cápsulas originales y administrativas.
    val capsulas by gameViewModel.capsulas
        .collectAsStateWithLifecycle()

    // Recupera los QR escaneados por el jugador.
    val descubrimientos by gameViewModel.descubrimientosQr
        .collectAsStateWithLifecycle()

    // Recupera los videos de cápsulas terminados.
    val vistosQr by gameViewModel.videosVistos
        .collectAsStateWithLifecycle()

    // Recupera los videos adicionales terminados.
    val vistosAdicionales by gameViewModel.videosAdicionalesVistos
        .collectAsStateWithLifecycle()

    // Identificadores para comprobar rápidamente el progreso.
    val idsDescubiertos = descubrimientos
        .map { it.capsulaId }
        .toSet()

    val idsCapsulasVistas = vistosQr
        .map { it.capsulaId }
        .toSet()

    val idsAdicionalesVistos = vistosAdicionales
        .map { it.videoAdicionalId }
        .toSet()

    // Construye la lista de videos que verá el jugador.
    val elementos = buildList {

        // ----------------------------------------
        // 1. VIDEOS ORIGINALES SIN QR
        // ----------------------------------------
        // Siempre aparecen, aunque no se hayan visto.

        add(
            ElementoBiblioteca(
                clave = "adicional-1",
                titulo = "IA en nuestra vida cotidiana",
                descripcion = "Descubre cómo la inteligencia artificial está presente en tu día a día.",
                visto = 1 in idsAdicionalesVistos,
                miniatura = R.drawable.miniatura_ia_cotidiana,
                videoAdicionalId = 1
            )
        )

        add(
            ElementoBiblioteca(
                clave = "adicional-2",
                titulo = "Ética y uso responsable de la IA",
                descripcion = "Aprende a utilizar la inteligencia artificial de manera responsable.",
                visto = 2 in idsAdicionalesVistos,
                miniatura = R.drawable.miniatura_etica_ia,
                videoAdicionalId = 2
            )
        )

        // ----------------------------------------
        // 2. VIDEOS ORIGINALES CON QR
        // ----------------------------------------
        // Aparecen después de escanear el QR
        // y terminar de ver el video.

        // Se excluyen los QR administrativos para
        // evitar que sus videos aparezcan duplicados.
        capsulas.filter { capsula ->

            capsula.estado == "PUBLICADA" &&

                    !capsula.codigoQr.startsWith(
                        "TRISKELEDU-CONTENIDO-"
                    ) &&

                    capsula.id in idsDescubiertos &&

                    capsula.id in idsCapsulasVistas

        }.forEach { capsula ->

            // Miniatura según el punto turístico.
            val miniatura = when (capsula.puntoId) {
                1 -> R.drawable.miniatura_plaza_armas
                2 -> R.drawable.miniatura_santa_lucia
                3 -> R.drawable.miniatura_san_cristobal
                4 -> R.drawable.miniatura_la_moneda
                5 -> R.drawable.miniatura_parque_forestal
                6 -> R.drawable.miniatura_bellavista
                else -> R.drawable.miniatura_plaza_armas
            }

            add(
                ElementoBiblioteca(
                    clave = "capsula-${capsula.id}",
                    titulo = capsula.titulo,
                    descripcion = capsula.descripcion,
                    visto = true,
                    miniatura = miniatura,
                    capsula = capsula
                )
            )
        }

        // ----------------------------------------
        // 3. VIDEOS CREADOS POR EL ADMINISTRADOR
        // ----------------------------------------

        videos.forEach { video ->

            // Solo mostramos videos que tengan una URL.
            if (video.urlVideo.isBlank()) {
                return@forEach
            }

            // Busca el contenido educativo relacionado.
            val contenido = contenidos.firstOrNull {
                it.id == video.contenidoId
            }

            // Si no hay contenido asociado, se considera
            // video independiente, sin QR.
            if (contenido == null) {

                if (video.contenidoId == null) {
                    add(
                        ElementoBiblioteca(
                            clave = "admin-${video.id}",
                            titulo = video.titulo,
                            descripcion = video.descripcion,
                            visto = -video.id in idsAdicionalesVistos,
                            videoRegistrado = video
                        )
                    )
                }

                return@forEach
            }

            if (contenido.puntoId == null) {

                // SIN QR:
                // Aparece siempre como Nuevo o Visto.
                add(
                    ElementoBiblioteca(
                        clave = "admin-${video.id}",
                        titulo = video.titulo,
                        descripcion = video.descripcion,
                        visto = -video.id in idsAdicionalesVistos,
                        videoRegistrado = video
                    )
                )

            } else {

                // CON QR:
                // Busca la cápsula generada para el contenido.
                val prefijo =
                    "TRISKELEDU-CONTENIDO-${contenido.id}-"

                val capsulaQr = capsulas.firstOrNull { capsula ->
                    capsula.codigoQr.startsWith(prefijo) &&
                            capsula.estado == "PUBLICADA"
                }

                // Solo se desbloquea cuando el QR fue
                // escaneado y el video fue terminado.
                val desbloqueado =
                    capsulaQr != null &&
                            capsulaQr.id in idsDescubiertos &&
                            capsulaQr.id in idsCapsulasVistas

                if (desbloqueado) {

                    add(
                        ElementoBiblioteca(
                            clave = "admin-${video.id}",
                            titulo = video.titulo,
                            descripcion = video.descripcion,
                            visto = true,
                            videoRegistrado = video
                        )
                    )
                }
            }
        }
    }

    // ----------------------------------------
    // INTERFAZ DE CONTENIDOS EDUCATIVOS
    // ----------------------------------------

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(fondoBiblioteca)
    ) {

        // Encabezado de la biblioteca.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(azulBiblioteca)
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 18.dp,
                    bottom = 25.dp
                )
        ) {

            IconButton(
                onClick = onVolver,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    Icons.Default.SmartDisplay,
                    contentDescription = null,
                    tint = Color(0xFF91B8FF),
                    modifier = Modifier.size(33.dp)
                )

                Spacer(Modifier.width(12.dp))

                Text(
                    "Contenidos Educativos",
                    color = Color.White,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "Tu espacio para descubrir, aprender y volver a explorar.",
                color = Color(0xFFCFDCF5),
                fontSize = 13.sp
            )

            // Se eliminó el contador:
            // "vistos · disponibles".
        }

        // Lista de videos disponibles.
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {

            item {

                Text(
                    "Explora tus videos",
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = textoBiblioteca
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    "Los contenidos con QR aparecen cuando los completas.",
                    color = Color(0xFF64748B),
                    fontSize = 12.sp
                )
            }

            items(
                items = elementos,
                key = { it.clave }
            ) { elemento ->

                TarjetaContenidoBiblioteca(
                    elemento = elemento,
                    onClick = {

                        when {

                            // Video original sin QR.
                            elemento.videoAdicionalId != null -> {
                                onVideoAdicionalClick(
                                    elemento.videoAdicionalId
                                )
                            }

                            // Video original con QR.
                            elemento.capsula != null -> {
                                onCapsulaClick(elemento.capsula)
                            }

                            // Video creado por el administrador.
                            elemento.videoRegistrado != null -> {
                                onVideoRegistradoClick(
                                    elemento.videoRegistrado
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

// ------------------------------------------
// TARJETA INDIVIDUAL DE CADA VIDEO
// ------------------------------------------

@Composable
private fun TarjetaContenidoBiblioteca(
    elemento: ElementoBiblioteca,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {

            // Miniatura del video.
            Box(
                modifier = Modifier
                    .size(width = 116.dp, height = 108.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(Color(0xFFE1EBFF)),
                contentAlignment = Alignment.Center
            ) {

                if (elemento.miniatura != null) {

                    // Miniaturas de los videos originales.
                    Image(
                        painter = painterResource(
                            elemento.miniatura
                        ),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                } else {

                    // Miniaturas de videos administrativos.
                    val context = LocalContext.current

                    val ruta = elemento.videoRegistrado
                        ?.urlMiniatura
                        .orEmpty()

                    val imagen by produceState<android.graphics.Bitmap?>(
                        initialValue = null,
                        ruta
                    ) {

                        value = withContext(Dispatchers.IO) {

                            runCatching {

                                if (ruta.isBlank()) {

                                    null

                                } else {

                                    val uri = Uri.parse(ruta)

                                    if (uri.scheme == "content") {

                                        context.contentResolver
                                            .openInputStream(uri)
                                            ?.use {
                                                BitmapFactory.decodeStream(it)
                                            }

                                    } else {

                                        BitmapFactory.decodeFile(
                                            if (uri.scheme == "file") {
                                                uri.path
                                            } else {
                                                ruta
                                            }
                                        )
                                    }
                                }

                            }.getOrNull()
                        }
                    }

                    if (imagen != null) {

                        Image(
                            bitmap = imagen!!.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                    } else {

                        Icon(
                            Icons.Default.SmartDisplay,
                            contentDescription = null,
                            tint = Color(0xFF3C6AB4),
                            modifier = Modifier.size(43.dp)
                        )
                    }
                }

                // Ícono de reproducción.
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp),
                    color = Color(0xDD152D52),
                    shape = RoundedCornerShape(9.dp)
                ) {

                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Reproducir",
                        tint = Color.White,
                        modifier = Modifier
                            .padding(5.dp)
                            .size(18.dp)
                    )
                }
            }

            // Información del video.
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {

                Text(
                    elemento.titulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = textoBiblioteca,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    elemento.descripcion.ifBlank {
                        "Video educativo de inteligencia artificial"
                    },
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Indicador individual Nuevo / Visto.
                Surface(
                    color = if (elemento.visto) {
                        Color(0xFFE4F7EC)
                    } else {
                        Color(0xFFFFF3D6)
                    },
                    shape = RoundedCornerShape(50.dp)
                ) {

                    Row(
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 6.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {

                        Icon(
                            imageVector = if (elemento.visto) {
                                Icons.Default.CheckCircle
                            } else {
                                Icons.Default.Star
                            },
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (elemento.visto) {
                                Color(0xFF198354)
                            } else {
                                Color(0xFFD68B00)
                            }
                        )

                        Text(
                            text = if (elemento.visto) {
                                "Visto"
                            } else {
                                "Nuevo"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (elemento.visto) {
                                Color(0xFF198354)
                            } else {
                                Color(0xFF9A6200)
                            }
                        )
                    }
                }
            }
        }
    }
}



