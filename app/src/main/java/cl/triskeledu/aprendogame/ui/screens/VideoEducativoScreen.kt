

package cl.triskeledu.aprendogame.ui.screens

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

import cl.triskeledu.aprendogame.R
import cl.triskeledu.aprendogame.model.Capsula

import cl.triskeledu.aprendogame.model.VideoRegistrado

@Composable
fun VideoEducativoScreen(
    capsula: Capsula? = null,
    yaVisto: Boolean,
    onVideoTerminado: () -> Unit,
    onVolverColeccion: () -> Unit,
    videoAdicionalId: Int? = null,
    videoRegistrado: VideoRegistrado? = null
)  {

    // COLORES DE LA INTERFAZ
    val azulOscuro = Color(0xFF203653)
    val azulPrincipal = Color(0xFF2563EB)
    val fondoPantalla = Color(0xFFF5F7FC)
    val textoPrincipal = Color(0xFF1E293B)
    val textoSecundario = Color(0xFF64748B)

    // IDENTIFICADOR DEL VIDEO
    val puntoId = capsula?.puntoId

    // ESTADOS DEL REPRODUCTOR
    var videoTerminado by remember(capsula?.id, videoAdicionalId, videoRegistrado?.id) {
        mutableStateOf(false)
    }

    var videoIniciado by remember(capsula?.id, videoAdicionalId, videoRegistrado?.id) {
        mutableStateOf(false)
    }

    var videoPreparado by remember(capsula?.id, videoAdicionalId, videoRegistrado?.id) {
        mutableStateOf(false)
    }

    var videoViewInstance by remember(capsula?.id, videoAdicionalId, videoRegistrado?.id) {
        mutableStateOf<VideoView?>(null)
    }

    // EVITA NOTIFICAR DOS VECES EL MISMO FINAL
    var finalRegistrado by remember(capsula?.id, videoAdicionalId, videoRegistrado?.id) {
        mutableStateOf(false)
    }

    // INFORMACIÓN DE LOS OCHO VIDEOS
    val tituloVideo = videoRegistrado?.titulo ?: when (videoAdicionalId) {
        1 -> "IA en nuestra vida cotidiana"
        2 -> "Ética y uso responsable de la IA"

        else -> when (puntoId) {
            1 -> "¿Qué es la Inteligencia Artificial?"
            2 -> "Reconocimiento de imágenes"
            3 -> "IA y medioambiente"
            4 -> "Aprendizaje automático"
            5 -> "Redes neuronales"
            6 -> "Inteligencia Artificial generativa"
            else -> capsula?.titulo ?: "Video educativo"
        }
    }

    val autor = when (videoAdicionalId) {
        1 -> "Magdiel Reynoso"
        2 -> "Benjamín Gómez"

        else -> when (puntoId) {
            1 -> "Magdiel Reynoso"
            2 -> "Fernando Zapata"
            3 -> "Juan Pablo Taulis"
            4 -> "Benjamín Gómez"
            5 -> "Juan Pablo Taulis"
            6 -> "Fernando Zapata"
            else -> "Triskeledu"
        }
    }

    val duracion = when (videoAdicionalId) {
        1 -> "0:46"
        2 -> "0:47"

        else -> when (puntoId) {
            1 -> "0:44"
            2 -> "0:45"
            3 -> "0:47"
            4 -> "0:48"
            5 -> "0:48"
            6 -> "0:46"
            else -> "--:--"
        }
    }

    val tema = when (videoAdicionalId) {
        1 -> "IA en la vida cotidiana"
        2 -> "Ética de la Inteligencia Artificial"

        else -> when (puntoId) {
            1 -> "Introducción a la IA"
            2 -> "Reconocimiento de imágenes"
            3 -> "IA y medioambiente"
            4 -> "Aprendizaje automático"
            5 -> "Redes neuronales"
            6 -> "IA generativa"
            else -> "Fundamentos de IA"
        }
    }

    val lugar = if (videoRegistrado != null) {
        "Nuevo contenido educativo"
    } else when (videoAdicionalId) {
        1, 2 -> "Contenido educativo adicional"

        else -> when (puntoId) {
            1 -> "Plaza de Armas"
            2 -> "Cerro Santa Lucía"
            3 -> "Cerro San Cristóbal"
            4 -> "Palacio de La Moneda"
            5 -> "Parque Forestal"
            6 -> "Barrio Bellavista"
            else -> "Triskeledu"
        }
    }

    val descripcionVideo = videoRegistrado?.descripcion ?: when (videoAdicionalId) {
        1 -> "Descubre cómo utilizamos la inteligencia artificial en nuestra vida cotidiana, por ejemplo, en los asistentes virtuales, las recomendaciones y las aplicaciones móviles."

        2 -> "Aprende sobre la importancia de utilizar la inteligencia artificial de manera ética y responsable, respetando la privacidad, evitando los prejuicios y verificando la información."

        else -> when (puntoId) {
            1 -> "Aprende qué es la inteligencia artificial, cómo reconoce imágenes, comprende preguntas y ayuda a estudiar la historia de Santiago."

            2 -> "Descubre cómo la visión artificial permite reconocer objetos, plantas y monumentos mediante patrones aprendidos de imágenes."

            3 -> "Conoce cómo la inteligencia artificial analiza datos ambientales, estudia la calidad del aire y ayuda a proteger la naturaleza."

            4 -> "Aprende cómo funciona el machine learning y cómo los modelos utilizan datos para identificar patrones y realizar predicciones."

            5 -> "Descubre cómo funcionan las redes neuronales artificiales y cómo pueden reconocer plantas, voces y otros patrones."

            6 -> "Aprende cómo la inteligencia artificial generativa puede crear textos, imágenes y sonidos, y por qué debemos utilizarla responsablemente."

            else -> capsula?.descripcion?.ifBlank {
                "Descubre nuevos conocimientos sobre Inteligencia Artificial."
            } ?: "Descubre nuevos conocimientos sobre Inteligencia Artificial."
        }
    }

    // SELECCIONA EL ARCHIVO MP4 LOCAL
    val videoLocal = if (videoRegistrado != null) {
        null
    } else when (videoAdicionalId) {
        1 -> R.raw.video_ia_cotidiana
        2 -> R.raw.video_etica_ia

        else -> when (puntoId) {
            1 -> R.raw.video_plaza_armas
            2 -> R.raw.video_cerro_santa_lucia
            3 -> R.raw.video_cerro_san_cristobal
            4 -> R.raw.video_palacio_de_la_moneda
            5 -> R.raw.video_parque_forestal
            6 -> R.raw.video_barrio_bellavista
            else -> null
        }
    }

    // URL ALTERNATIVA SI NO HAY VIDEO LOCAL
    val urlVideo = videoRegistrado?.urlVideo
        ?: capsula?.urlVideo.orEmpty()

    // REGISTRA LA FINALIZACIÓN UNA SOLA VEZ
    fun finalizarVideo() {
        videoTerminado = true
        videoIniciado = false

        if (!yaVisto && !finalRegistrado) {
            finalRegistrado = true
            onVideoTerminado()
        }
    }

    // DETENER EL VIDEO AL SALIR DE LA PANTALLA
    DisposableEffect(capsula?.id, videoAdicionalId, videoRegistrado?.id)  {
        onDispose {
            videoViewInstance?.stopPlayback()
            videoViewInstance = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(fondoPantalla)
    ) {

        // ENCABEZADO CON FLECHA PARA REGRESAR A LA PANTALLA ANTERIOR.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(azulOscuro)
                .height(64.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = {
                    videoViewInstance?.stopPlayback()
                    onVolverColeccion()
                },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver a la pantalla anterior",
                    tint = Color.White
                )
            }
            Text(
                text = "Video Educativo",
                color = Color.White,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            // TARJETA DEL REPRODUCTOR
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {

                if (videoLocal != null || urlVideo.isNotBlank()) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {

                        AndroidView(
                            factory = { context ->

                                VideoView(context).apply {

                                    videoViewInstance = this

                                    if (videoLocal != null) {

                                        val uri = Uri.parse(
                                            "android.resource://${context.packageName}/raw/" +
                                                    context.resources.getResourceEntryName(videoLocal)
                                        )

                                        setVideoURI(uri)

                                    } else {

                                        setVideoURI(Uri.parse(urlVideo))
                                    }

                                    val controles = MediaController(context)

                                    setMediaController(controles)
                                    controles.setAnchorView(this)

                                    setOnPreparedListener {
                                        videoPreparado = true
                                    }

                                    setOnCompletionListener {
                                        finalizarVideo()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // DETECTA CUANDO EL VIDEO LLEGA AL FINAL
                        LaunchedEffect(
                            videoIniciado,
                            videoViewInstance
                        ) {

                            if (
                                videoIniciado &&
                                !yaVisto &&
                                !videoTerminado
                            ) {

                                while (true) {

                                    kotlinx.coroutines.delay(400)

                                    val reproductor = videoViewInstance

                                    if (reproductor != null &&
                                        reproductor.isPlaying
                                    ) {

                                        val duracionReal = reproductor.duration
                                        val posicionActual = reproductor.currentPosition

                                        if (
                                            duracionReal > 0 &&
                                            posicionActual >= duracionReal - 400
                                        ) {

                                            finalizarVideo()
                                            break
                                        }
                                    }
                                }
                            }
                        }

                        // BOTÓN CENTRAL DE REPRODUCCIÓN
                        if (!videoIniciado) {

                            IconButton(
                                onClick = {

                                    videoViewInstance?.let { reproductor ->

                                        if (videoPreparado) {

                                            if (videoTerminado) {
                                                reproductor.seekTo(0)
                                                videoTerminado = false
                                            }

                                            reproductor.start()
                                            videoIniciado = true
                                        }
                                    }
                                },
                                enabled = videoPreparado,
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(
                                        color = Color(0xCC203653),
                                        shape = RoundedCornerShape(50)
                                    )
                            ) {

                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Reproducir video",
                                    tint = Color.White,
                                    modifier = Modifier.size(42.dp)
                                )
                            }
                        }
                    }

                } else {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = "Video todavía no disponible",
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // TARJETA DE INFORMACIÓN
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

                    Text(
                        text = lugar.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = azulPrincipal
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = tituloVideo,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        color = textoPrincipal,
                        lineHeight = 29.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Autor: $autor",
                        fontSize = 14.sp,
                        color = textoSecundario
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = "Duración: $duracion",
                        fontSize = 14.sp,
                        color = textoSecundario
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(
                        color = Color(0xFFE8EDF5)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = descripcionVideo,
                        fontSize = 14.sp,
                        color = textoPrincipal,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TARJETA DEL TEMA
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFEAF2FF)
                )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "Tema",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = azulOscuro
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = Color(0xFFE6DCFF),
                        shape = RoundedCornerShape(20.dp)
                    ) {

                        Text(
                            text = tema,
                            modifier = Modifier.padding(
                                horizontal = 14.dp,
                                vertical = 10.dp
                            ),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF673AB7)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // MENSAJE DE VIDEO VISTO
            if (yaVisto || videoTerminado) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFFE2F6E7),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "¡Has visto este video!",
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

