package cl.triskeledu.aprendogame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import cl.triskeledu.aprendogame.model.Capsula
import cl.triskeledu.aprendogame.viewmodel.GameViewModel

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.ui.draw.clip
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import cl.triskeledu.aprendogame.data.local.AppDatabase
import cl.triskeledu.aprendogame.model.VideoRegistrado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import cl.triskeledu.aprendogame.R



@Composable
fun ColeccionScreen(
    gameViewModel: GameViewModel,
    onCapsulaClick: (Capsula) -> Unit,
    onVideoAdicionalClick: (Int) -> Unit,
    onVideoRegistradoClick: (VideoRegistrado) -> Unit,
    onVolver: () -> Unit
) {

    // Observa los videos y contenidos del Administrador en Room.
    val context = LocalContext.current
    val database = remember(context) { AppDatabase.getInstance(context.applicationContext) }
    val videosRegistrados by remember(database) {
        database.videoRegistradoDao().obtenerTodos()
    }.collectAsStateWithLifecycle(initialValue = emptyList())
    val contenidosAdministrativos by remember(database) {
        database.contenidoEducativoDao().obtenerTodos()
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    // Cápsulas educativas registradas en Room.
    val capsulas by gameViewModel.capsulas.collectAsStateWithLifecycle()

    // Descubrimientos QR del usuario activo.
    val descubrimientos by
    gameViewModel.descubrimientosQr.collectAsStateWithLifecycle()

    // Videos turísticos que el usuario terminó de ver.
    val videosVistos by
    gameViewModel.videosVistos.collectAsStateWithLifecycle()

    // Videos adicionales que el usuario terminó de ver.
    val videosAdicionales by
    gameViewModel.videosAdicionalesVistos.collectAsStateWithLifecycle()

    // Pestaña seleccionada:
    // 0 = Todos, 1 = Videos, 2 = Turísticos.
    var pestanaSeleccionada by remember { mutableIntStateOf(0) }

    // Relaciona cada descubrimiento con su cápsula.
    val descubrimientosTuristicos = descubrimientos.mapNotNull { descubrimiento ->
        val capsula = capsulas.find {
            it.id == descubrimiento.capsulaId
        }

        if (capsula != null) {
            Pair(descubrimiento, capsula)
        } else {
            null
        }
    }

    // Identifica los videos turísticos completados.
    val idsVideosVistos = videosVistos
        .map { it.capsulaId }
        .toSet()

    val cantidadTuristicos = descubrimientosTuristicos
        .distinctBy { (_, capsula) -> capsula.id }
        .size

    // Solo los videos independientes originales completados.
    // Los identificadores negativos de videos administrativos con QR
    // no deben contarse como videos independientes.
    val adicionalesValidos = videosAdicionales
        .filter { it.videoAdicionalId in 1..2 }
        .distinctBy { it.videoAdicionalId }

    // Un QR descubierto no equivale a un video visto.
    val turisticosConVideoVisto = descubrimientosTuristicos
        .filter { (_, capsula) -> capsula.id in idsVideosVistos }
        .distinctBy { (_, capsula) -> capsula.id }

    val cantidadVideos = turisticosConVideoVisto.size + adicionalesValidos.size

    // Todos incluye cada QR descubierto y cada video independiente visto.
    val cantidadTodos = cantidadTuristicos + adicionalesValidos.size

    // Filtramos antes de dibujar para que la pestaña Videos
    // muestre solo reproducciones completadas.
    val turisticosAMostrar = if (pestanaSeleccionada == 1) {
        turisticosConVideoVisto
    } else {
        descubrimientosTuristicos.distinctBy { (_, capsula) -> capsula.id }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
    ) {

        Text(
            text = "Colección de Descubrimientos",
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Tu historial de aprendizaje y descubrimientos",
            fontSize = 14.sp,
            color = Color(0xFF64748B)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Pestañas de la colección.
        TabRow(
            selectedTabIndex = pestanaSeleccionada,
            containerColor = Color.White,
            contentColor = Color(0xFF2563EB)
        ) {

            Tab(
                selected = pestanaSeleccionada == 0,
                onClick = { pestanaSeleccionada = 0 },
                text = {
                    Text("Todos ($cantidadTodos)")
                }
            )

            Tab(
                selected = pestanaSeleccionada == 1,
                onClick = { pestanaSeleccionada = 1 },
                text = {
                    Text("Videos ($cantidadVideos)")
                }
            )

            Tab(
                selected = pestanaSeleccionada == 2,
                onClick = { pestanaSeleccionada = 2 },
                text = {
                    Text("Turísticos ($cantidadTuristicos)")
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Comprueba si hay elementos para mostrar.
        val hayTuristicos = turisticosAMostrar.isNotEmpty()
        val hayAdicionales = adicionalesValidos.isNotEmpty()

        val mostrarTuristicos = pestanaSeleccionada in listOf(0, 1, 2)
        val mostrarAdicionales = pestanaSeleccionada in listOf(0, 1)

        val hayContenido =
            (mostrarTuristicos && hayTuristicos) ||
                    (mostrarAdicionales && hayAdicionales)

        if (!hayContenido) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (pestanaSeleccionada) {
                        2 -> "Todavía no has descubierto códigos QR."
                        1 -> "Todavía no tienes videos en tu colección."
                        else -> "Tu colección está vacía. ¡Comienza a explorar!"
                    },
                    color = Color(0xFF64748B)
                )
            }

        } else {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {

                // Descubrimientos turísticos registrados.
                if (mostrarTuristicos) {

                    items(
                        items = turisticosAMostrar,
                        key = { (descubrimiento, _) ->
                            "qr_${descubrimiento.id}"
                        }
                    ) { (descubrimiento, capsula) ->

                        val videoVisto = capsula.id in idsVideosVistos

                        // QR administrativo: TRISKELEDU-CONTENIDO-ID-...
                        // Se identifica por el contenido asociado, nunca por el título.
                        val contenidoAsociado = contenidosAdministrativos.firstOrNull { contenido ->
                            capsula.codigoQr.startsWith("TRISKELEDU-CONTENIDO-${contenido.id}-")
                        }
                        val videoAsociado = contenidoAsociado?.let { contenido ->
                            videosRegistrados.firstOrNull { video ->
                                video.contenidoId == contenido.id && video.urlVideo.isNotBlank()
                            }
                        }


                        TarjetaColeccion(
                            imagenResId = imagenTuristica(capsula.puntoId),
                            urlMiniatura = videoAsociado?.urlMiniatura.orEmpty(),
                            titulo = capsula.titulo,
                            descripcion = capsula.descripcion,
                            fecha = "QR capturado: ${
                                formatearFechaColeccion(
                                    descubrimiento.fechaTimestamp
                                )
                            }",
                            estado = if (videoVisto) {
                                "✓ Video visto"
                            } else {
                                "Video disponible"
                            },
                            completado = videoVisto,
                            onClick = {
                                onCapsulaClick(capsula)
                            }
                        )
                    } // Cierra items de videos turísticos
                } // Cierra if (mostrarTuristicos)



                // Dos videos educativos sin QR ni puntos.
                if (mostrarAdicionales) {

                    items(
                        items = adicionalesValidos,
                        key = { video ->
                            "adicional_${video.videoAdicionalId}"
                        }
                    ) { video ->

                        val titulo = when (video.videoAdicionalId) {
                            1 -> "IA en nuestra vida cotidiana"
                            2 -> "Ética y uso responsable de la IA"
                            else -> "Video educativo"
                        }

                        val autor = when (video.videoAdicionalId) {
                            1 -> "Magdiel Reynoso"
                            2 -> "Benjamín Gómez"
                            else -> ""
                        }

                        TarjetaColeccion(
                            imagenResId = imagenVideoAdicional(video.videoAdicionalId),
                            titulo = titulo,
                            descripcion = "Video educativo adicional · $autor",
                            fecha = "Visto: ${
                                formatearFechaColeccion(
                                    video.fechaTimestamp
                                )
                            }",
                            estado = "✓ Completado · 0 puntos",
                            completado = true,
                            onClick = {
                                onVideoAdicionalClick(
                                    video.videoAdicionalId
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

// Tarjeta reutilizable para los elementos de la colección.
@Composable
private fun TarjetaColeccion(
    imagenResId: Int,
    urlMiniatura: String = "",
    titulo: String,
    descripcion: String,
    fecha: String,
    estado: String,
    completado: Boolean,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (completado) {
                Color(0xFFE2F6E7)
            } else {
                Color.White
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Icono provisional del video.

            MiniaturaColeccion(
                urlMiniatura = urlMiniatura,
                imagenResId = imagenResId,
                titulo = titulo
            )


            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = titulo,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = descripcion,
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = fecha,
                    fontSize = 12.sp,
                    color = Color(0xFF475569)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = estado,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (completado) {
                        Color(0xFF2E7D32)
                    } else {
                        Color(0xFF2563EB)
                    }
                )
            }
        }
    }
}

// Convierte el timestamp guardado en Room
// a una fecha legible para el usuario.
private fun formatearFechaColeccion(
    timestamp: Long
): String {

    val formato = SimpleDateFormat(
        "dd/MM/yyyy",
        Locale("es", "CL")
    )

    return formato.format(Date(timestamp))
}


private fun imagenTuristica(puntoId: Int): Int {
    return when (puntoId) {
        1 -> R.drawable.miniatura_plaza_armas
        2 -> R.drawable.miniatura_santa_lucia
        3 -> R.drawable.miniatura_san_cristobal
        4 -> R.drawable.miniatura_la_moneda
        5 -> R.drawable.miniatura_parque_forestal
        6 -> R.drawable.miniatura_bellavista
        else -> R.drawable.miniatura_plaza_armas
    }
}

private fun imagenVideoAdicional(videoId: Int): Int {
    return when (videoId) {
        1 -> R.drawable.miniatura_ia_cotidiana
        2 -> R.drawable.miniatura_etica_ia
        else -> R.drawable.miniatura_ia_cotidiana
    }
}

// Usa la misma estrategia de lectura de miniaturas que ContenidosEducativosScreen.
@Composable
private fun MiniaturaColeccion(
    urlMiniatura: String,
    imagenResId: Int,
    titulo: String
) {
    val context = LocalContext.current
    val bitmap by produceState<android.graphics.Bitmap?>(
        initialValue = null,
        key1 = urlMiniatura
    ) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                if (urlMiniatura.isBlank()) null else {
                    val uri = Uri.parse(urlMiniatura)
                    if (uri.scheme == "content") {
                        context.contentResolver.openInputStream(uri)?.use {
                            BitmapFactory.decodeStream(it)
                        }
                    } else {
                        BitmapFactory.decodeFile(
                            if (uri.scheme == "file") uri.path else urlMiniatura
                        )
                    }
                }
            }.getOrNull()
        }
    }

    val modifier = Modifier
        .size(width = 90.dp, height = 76.dp)
        .clip(RoundedCornerShape(10.dp))

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = "Miniatura de $titulo",
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Image(
            painter = painterResource(imagenResId),
            contentDescription = "Miniatura de $titulo",
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}



