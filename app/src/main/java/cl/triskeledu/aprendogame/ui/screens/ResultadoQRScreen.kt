
package cl.triskeledu.aprendogame.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.triskeledu.aprendogame.R
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

    val db = remember(context) {
        AppDatabase.getInstance(context.applicationContext)
    }

    // Evita regresar al escáner y leer el mismo QR.
    BackHandler {
        onVolverMapa()
    }

    // -------------------------------------------------
    // IDENTIFICAR EL TIPO DE QR
    // -------------------------------------------------

    val esQrAdministrativo = capsula.codigoQr.startsWith(
        "TRISKELEDU-CONTENIDO-"
    )

    // Los QR administrativos contienen el ID
    // de su contenido educativo.
    val contenidoId = remember(capsula.codigoQr) {

        Regex("^TRISKELEDU-CONTENIDO-(\\d+)-")
            .find(capsula.codigoQr)
            ?.groupValues
            ?.getOrNull(1)
            ?.toIntOrNull()
    }

    // -------------------------------------------------
    // VIDEOS REGISTRADOS POR EL ADMINISTRADOR
    // -------------------------------------------------

    val videosRegistrados by remember(db) {

        db.videoRegistradoDao().obtenerTodos()

    }.collectAsState(initial = emptyList())

    val videoAsociado = remember(
        videosRegistrados,
        contenidoId,
        capsula.urlVideo
    ) {

        if (contenidoId == null) {

            null

        } else {

            val candidatos = videosRegistrados.filter {
                it.contenidoId == contenidoId &&
                        it.urlVideo.isNotBlank()
            }

            candidatos.firstOrNull {
                it.urlVideo == capsula.urlVideo
            } ?: candidatos.singleOrNull()
        }
    }

    // -------------------------------------------------
    // VIDEOS ORIGINALES ALMACENADOS EN RES/RAW
    // -------------------------------------------------

    val videoLocal = if (!esQrAdministrativo) {

        when (capsula.puntoId) {

            1 -> R.raw.video_plaza_armas
            2 -> R.raw.video_cerro_santa_lucia
            3 -> R.raw.video_cerro_san_cristobal
            4 -> R.raw.video_palacio_de_la_moneda
            5 -> R.raw.video_parque_forestal
            6 -> R.raw.video_barrio_bellavista

            else -> null
        }

    } else {

        null
    }

    // -------------------------------------------------
    // MINIATURAS ORIGINALES EN RES/DRAWABLE
    // -------------------------------------------------

    val miniaturaLocal = if (!esQrAdministrativo) {

        when (capsula.puntoId) {

            1 -> R.drawable.miniatura_plaza_armas
            2 -> R.drawable.miniatura_santa_lucia
            3 -> R.drawable.miniatura_san_cristobal
            4 -> R.drawable.miniatura_la_moneda
            5 -> R.drawable.miniatura_parque_forestal
            6 -> R.drawable.miniatura_bellavista

            else -> null
        }

    } else {

        null
    }

    // -------------------------------------------------
    // MINIATURAS REGISTRADAS POR EL ADMINISTRADOR
    // -------------------------------------------------

    val rutaMiniatura =
        videoAsociado?.urlMiniatura.orEmpty()

    val miniatura by produceState<Bitmap?>(
        initialValue = null,
        key1 = rutaMiniatura
    ) {

        value = withContext(Dispatchers.IO) {

            runCatching {

                if (rutaMiniatura.isBlank()) {

                    null

                } else {

                    val uri = Uri.parse(rutaMiniatura)

                    when (uri.scheme) {

                        "content" -> {

                            context.contentResolver
                                .openInputStream(uri)
                                ?.use(BitmapFactory::decodeStream)
                        }

                        "file" -> {

                            uri.path?.let(
                                BitmapFactory::decodeFile
                            )
                        }

                        else -> {

                            BitmapFactory.decodeFile(
                                rutaMiniatura
                            )
                        }
                    }
                }

            }.getOrNull()
        }
    }

    // -------------------------------------------------
    // DISPONIBILIDAD DEL VIDEO
    // -------------------------------------------------

    // El video puede estar en res/raw o tener una URL.
    // Ya no depende únicamente de capsula.urlVideo.
    val videoDisponible =
        videoLocal != null ||
                capsula.urlVideo.isNotBlank() ||
                !videoAsociado?.urlVideo.isNullOrBlank()

    // -------------------------------------------------
    // ANIMACIÓN DEL TROFEO
    // -------------------------------------------------

    val escalaTrofeo = remember {

        Animatable(0f)
    }

    LaunchedEffect(capsula.id) {

        escalaTrofeo.snapTo(0f)

        delay(200)

        escalaTrofeo.animateTo(
            1f,
            spring(
                dampingRatio = 0.4f,
                stiffness = 180f
            )
        )
    }

    val transicion = rememberInfiniteTransition(
        label = "trofeo"
    )

    val flotacion by transicion.animateFloat(

        initialValue = 1f,

        targetValue = 1.05f,

        animationSpec = infiniteRepeatable(
            tween(1300),
            RepeatMode.Reverse
        ),

        label = "flotacion"
    )

    val escalaTrofeoFinal =
        escalaTrofeo.value * flotacion

    // =================================================
    // INTERFAZ PRINCIPAL
    // =================================================

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FF))
    ) {

        // Título centrado sin flecha de retroceso.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = "Resultado del QR",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color(0xFF17243D),
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // TROFEO
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.height(122.dp)
            ) {

                if (puntosGanados > 0) {

                    Text(
                        text = "🎊  ✨     🎉",
                        fontSize = 24.sp,
                        modifier = Modifier.align(
                            Alignment.TopCenter
                        )
                    )
                }

                Text(
                    text = "🏆",
                    fontSize = 92.sp,
                    modifier = Modifier.scale(
                        escalaTrofeoFinal
                    )
                )
            }

            // RESULTADO DEL ESCANEO
            Text(
                text = if (puntosGanados > 0) {
                    "¡QR descubierto!"
                } else {
                    "¡QR reconocido!"
                },
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF17243D),
                textAlign = TextAlign.Center
            )

            Text(
                text = "+$puntosGanados puntos",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF16A34A)
            )

            if (puntosGanados == 0) {

                Text(
                    text = "Ya habías descubierto este QR. No se sumaron puntos nuevamente.",
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF475569)
                )
            }

            // -----------------------------------------
            // TARJETA DEL VIDEO EDUCATIVO
            // -----------------------------------------

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 3.dp
                )
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Box(
                        modifier = Modifier
                            .size(
                                width = 100.dp,
                                height = 92.dp
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE5EEFF)),
                        contentAlignment = Alignment.Center
                    ) {

                        when {

                            // MINIATURA DEL ADMINISTRADOR
                            miniatura != null -> {

                                Image(
                                    bitmap = miniatura!!.asImageBitmap(),
                                    contentDescription =
                                        "Miniatura de ${capsula.titulo}",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // MINIATURA ORIGINAL
                            miniaturaLocal != null -> {

                                Image(
                                    painter = painterResource(
                                        id = miniaturaLocal
                                    ),
                                    contentDescription =
                                        "Miniatura de ${capsula.titulo}",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // SI NO EXISTE UNA MINIATURA
                            else -> {

                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Video educativo",
                                    tint = azul,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement =
                            Arrangement.spacedBy(5.dp)
                    ) {

                        Text(
                            text = capsula.titulo,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF17243D)
                        )

                        Text(
                            text = capsula.descripcion,
                            fontSize = 12.sp,
                            color = Color(0xFF475569),
                            maxLines = 3
                        )

                        Text(
                            text = "Video educativo",
                            fontSize = 11.sp,
                            color = azul,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // -----------------------------------------
            // TARJETA DE SELFIE TURÍSTICA
            // -----------------------------------------

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF7E7)
                )
            ) {

                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement =
                        Arrangement.spacedBy(12.dp),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(
                        text = "🏆",
                        fontSize = 30.sp,
                        modifier = Modifier.scale(flotacion)
                    )

                    Column(
                        verticalArrangement =
                            Arrangement.spacedBy(6.dp)
                    ) {

                        Text(
                            text = "¿Es un hito turístico?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF17243D)
                        )

                        Text(
                            text = "Si este QR está en un lugar turístico, toma una selfie para ganar 100 puntos extra.",
                            fontSize = 12.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }

            // -----------------------------------------
            // BOTÓN VER VIDEO
            // -----------------------------------------

            Button(
                onClick = onVerVideo,

                // Acepta videos locales y administrativos.
                enabled = videoDisponible,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),

                shape = RoundedCornerShape(25.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = azul
                )
            ) {

                Text(
                    text = "Ver video",
                    color = Color.White
                )
            }

            // -----------------------------------------
            // BOTÓN TOMAR SELFIE
            // -----------------------------------------

            Button(
                onClick = onTomarSelfie,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF16A34A)
                )
            ) {

                Text(
                    text = "Tomar selfie (+100 pts)",
                    color = Color.White
                )
            }

            // -----------------------------------------
            // BOTÓN VOLVER AL MAPA
            // -----------------------------------------

            OutlinedButton(
                onClick = onVolverMapa,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    azul
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White
                )
            ) {

                Text(
                    text = "Volver al mapa",
                    color = azul
                )
            }
        }
    }
}
