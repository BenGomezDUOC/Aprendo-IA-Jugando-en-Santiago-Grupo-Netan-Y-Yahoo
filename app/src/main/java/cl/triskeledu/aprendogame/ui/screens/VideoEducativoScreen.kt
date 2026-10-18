package cl.triskeledu.aprendogame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

import cl.triskeledu.aprendogame.model.Capsula

@Composable
fun VideoEducativoScreen(
    capsula: Capsula,
    yaVisto: Boolean,
    onVideoTerminado: () -> Unit,
    onVolverColeccion: () -> Unit
) {
    val context = LocalContext.current

    // Prepara el reproductor una sola vez por video.
    val reproductor = remember(context, capsula.id, capsula.urlVideo) {
        ExoPlayer.Builder(context).build().apply {
            if (capsula.urlVideo.isNotBlank()) {
                setMediaItem(MediaItem.fromUri(capsula.urlVideo))
                prepare()
            }
        }
    }

    // Guarda el estado de visualización que muestra la pantalla.
    var videoTerminado by remember(capsula.id) {
        mutableStateOf(false)
    }

    // Escucha cuándo termina la reproducción.
    DisposableEffect(reproductor) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    videoTerminado = true
                    onVideoTerminado()
                }
            }
        }

        reproductor.addListener(listener)

        onDispose {
            reproductor.removeListener(listener)
            reproductor.release()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        // REPRODUCTOR
        if (capsula.urlVideo.isNotBlank()) {
            AndroidView(
                factory = { vistaContext ->
                    PlayerView(vistaContext).apply {
                        player = reproductor
                        useController = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .background(
                        Color(0xFFE8EDF5),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("Video todavía no disponible")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // TÍTULO
        Text(
            text = capsula.titulo,
            fontSize = 24.sp,
            color = Color(0xFF202020)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Autor: Triskeledu",
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // DESCRIPCIÓN
        Text(
            text = capsula.descripcion.ifBlank {
                "Descubre nuevos conocimientos sobre Inteligencia Artificial."
            },
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(20.dp))

        // TEMA
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color(0xFFEAF4FF),
                    RoundedCornerShape(14.dp)
                )
                .padding(16.dp)
        ) {
            Text(
                text = "Tema",
                color = Color(0xFF263B66)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = Color(0xFFE7D9FF),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "Fundamentos de IA",
                    modifier = Modifier.padding(
                        horizontal = 14.dp,
                        vertical = 8.dp
                    ),
                    color = Color(0xFF673AB7)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // VIDEO VISTO
        if (yaVisto || videoTerminado) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color(0xFFE2F6E7),
                        RoundedCornerShape(12.dp)
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
                    color = Color(0xFF2E7D32)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // VOLVER A LA COLECCIÓN
        OutlinedButton(
            onClick = onVolverColeccion,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver a la colección")
        }
    }
}
