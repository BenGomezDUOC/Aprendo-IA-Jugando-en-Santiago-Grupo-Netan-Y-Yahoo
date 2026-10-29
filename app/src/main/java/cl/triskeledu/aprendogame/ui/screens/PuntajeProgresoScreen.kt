package cl.triskeledu.aprendogame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.triskeledu.aprendogame.viewmodel.ColeccionViewModel
import cl.triskeledu.aprendogame.viewmodel.SessionViewModel
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import java.util.Locale

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import cl.triskeledu.aprendogame.data.local.AppDatabase

private const val META_PUNTOS = 10000

@Composable
fun PuntajeProgresoScreen(
    sessionViewModel: SessionViewModel,
    coleccionViewModel: ColeccionViewModel,
    onVolverClick: () -> Unit,
    onHistorialClick: () -> Unit
) {
    // Recupera los datos reales del jugador activo.
    val usuario by sessionViewModel.usuario.collectAsStateWithLifecycle()
    val descubrimientos by coleccionViewModel.descubrimientosQr.collectAsStateWithLifecycle()
    val visitas by sessionViewModel.historialVisitas.collectAsStateWithLifecycle()
    val capsulas by coleccionViewModel.capsulas.collectAsStateWithLifecycle()
    val videosVistos by coleccionViewModel.videosVistos.collectAsStateWithLifecycle()
    val videosAdicionales by coleccionViewModel.videosAdicionalesVistos.collectAsStateWithLifecycle()

    // Recupera los videos y contenidos creados por el administrador.
    val context = LocalContext.current

    val database = remember(context) {
        AppDatabase.getInstance(context.applicationContext)
    }

    val videosRegistrados by remember(database) {
        database.videoRegistradoDao().obtenerTodos()
    }.collectAsState(initial = emptyList())

    val contenidos by remember(database) {
        database.contenidoEducativoDao().obtenerPublicados()
    }.collectAsState(initial = emptyList())

    // Total de puntos guardados en Room.
    val puntosAcumulados = usuario?.puntosAcumulados ?: 0

    // Los QR y las selfies solo se cuentan una vez por lugar.
    val totalQr = descubrimientos.size

    // Cuenta una selfie validada por cada QR diferente.
    val selfiesValidadas = visitas
        .filter {
            it.completada &&
                    !it.rutaFotoSelfie.isNullOrBlank() &&
                    it.capsulaId != null
        }
        .mapNotNull { it.capsulaId }
        .distinct()
        .size

    // Identificadores de los QR realmente descubiertos.
    val idsQrDescubiertos = descubrimientos
        .map { it.capsulaId }
        .toSet()

// Videos con QR: cuentan solo si se descubrió el QR
// y existe un registro de reproducción de su cápsula.
    val totalVideosQr = videosVistos
        .map { it.capsulaId }
        .distinct()
        .count { it in idsQrDescubiertos }

// Videos independientes originales y administrativos.
    val totalVideosIndependientes = videosAdicionales
        .map { it.videoAdicionalId }
        .distinct()
        .count { identificador ->

            when {
                // Los dos videos independientes originales.
                identificador == 1 || identificador == 2 -> true

                // Los videos creados por el administrador.
                identificador < 0 -> {
                    val idVideo = -identificador

                    val video = videosRegistrados.firstOrNull {
                        it.id == idVideo
                    }

                    if (video == null) {
                        false
                    } else {
                        val contenido = contenidos.firstOrNull {
                            it.id == video.contenidoId
                        }

                        // Busca si realmente se generó un QR para este contenido.
                        val tieneQr = contenido != null && capsulas.any {
                            it.codigoQr.startsWith(
                                "TRISKELEDU-CONTENIDO-${contenido.id}-"
                            )
                        }

                        // Solo cuenta como independiente si NO tiene QR.
                        !tieneQr
                    }
                }

                else -> false
            }
        }

// Total de videos válidamente vistos.
    val totalVideos = totalVideosQr + totalVideosIndependientes

    // Desglose de las recompensas actuales.
    val puntosQr = totalQr * 10
    val puntosSelfies = selfiesValidadas * 100

    // Progreso general de puntos sobre la meta fija de 10.000.
    val progresoPuntos = (
            puntosAcumulados.toFloat() / META_PUNTOS
            ).coerceIn(0f, 1f)

    val porcentajePuntos = String.format(
        Locale.US,
        "%.2f",
        progresoPuntos * 100
    ).replace(".", ",")

    // QR disponibles, incluidos los que agregue el administrador.
    val capsulasDisponibles = capsulas.filter {
        it.estado == "PUBLICADA" && it.codigoQr.isNotBlank()
    }
    val totalQrDisponibles = capsulasDisponibles.map { it.codigoQr }.toSet().size
    val idsQrDisponibles = capsulasDisponibles.map { it.id }.toSet()
    val qrDescubiertosDisponibles = descubrimientos.map { it.capsulaId }
        .toSet().intersect(idsQrDisponibles).size

    val progresoExploracion = if (totalQrDisponibles > 0) {
        (qrDescubiertosDisponibles.toFloat() / totalQrDisponibles).coerceIn(0f, 1f)
    } else {
        0f
    }

    val porcentajeExploracion = String.format(
        Locale.US,
        "%.2f",
        progresoExploracion * 100
    ).replace(".", ",")

    val progresoPuntosAnimado by animateFloatAsState(
        targetValue = progresoPuntos,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "progresoPuntos"
    )
    val progresoExploracionAnimado by animateFloatAsState(
        targetValue = progresoExploracion,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "progresoExploracion"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolverClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver"
                )
            }

            Text(
                text = "Mi Progreso",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tus logros explorando Santiago",
            fontSize = 14.sp,
            color = Color(0xFF64748B)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Tarjeta principal del puntaje.
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E293B)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Stars,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "$puntosAcumulados",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Text(
                    text = "de ${String.format(Locale.US, "%,d", META_PUNTOS).replace(",", ".")} puntos",
                    fontSize = 15.sp,
                    color = Color(0xFFCBD5E1)
                )

                Spacer(modifier = Modifier.height(18.dp))

                LinearProgressIndicator(
                    progress = { progresoPuntosAnimado },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp),
                    color = Color(0xFF38BDF8),
                    trackColor = Color(0xFF475569)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (puntosAcumulados >= META_PUNTOS) "¡Meta alcanzada!" else "Progreso general · $porcentajePuntos%",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Mis actividades",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricaProgreso(
                valor = totalQr.toString(),
                etiqueta = "QR",
                icono = Icons.Default.QrCodeScanner,
                modifier = Modifier.weight(1f)
            )

            MetricaProgreso(
                valor = totalVideos.toString(),
                etiqueta = "Videos",
                icono = Icons.Default.PlayCircle,
                modifier = Modifier.weight(1f)
            )

            MetricaProgreso(
                valor = selfiesValidadas.toString(),
                etiqueta = "Selfies",
                icono = Icons.Default.CameraAlt,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Exploración turística",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$qrDescubiertosDisponibles de $totalQrDisponibles QR descubiertos",
                        fontSize = 13.sp,
                        color = Color(0xFF334155)
                    )

                    Text(
                        text = "$porcentajeExploracion%",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { progresoExploracionAnimado },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(9.dp),
                    color = Color(0xFF2563EB),
                    trackColor = Color(0xFFE2E8F0)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = when {
                        totalQrDisponibles == 0 -> "No hay QR disponibles todavía."
                        qrDescubiertosDisponibles == totalQrDisponibles -> "¡Todos los QR disponibles descubiertos!"
                        else -> "Faltan ${totalQrDisponibles - qrDescubiertosDisponibles} QR por descubrir."
                    },
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Desglose de puntos",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(12.dp))

        DesglosePuntos(
            titulo = "Códigos QR",
            detalle = "$totalQr descubiertos × 10 puntos",
            puntos = puntosQr,
            icono = Icons.Default.QrCodeScanner
        )

        Spacer(modifier = Modifier.height(10.dp))

        DesglosePuntos(
            titulo = "Selfies turísticas",
            detalle = "$selfiesValidadas validadas × 100 puntos",
            puntos = puntosSelfies,
            icono = Icons.Default.CameraAlt
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onHistorialClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2563EB)
            )
        ) {
            Text(
                text = "Ver Historial de Actividades",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun MetricaProgreso(
    valor: String,
    etiqueta: String,
    icono: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = Color(0xFF2563EB)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = valor,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = etiqueta,
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
private fun DesglosePuntos(
    titulo: String,
    detalle: String,
    puntos: Int,
    icono: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = Color(0xFF2563EB)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Text(
                    text = detalle,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }

            Text(
                text = "+$puntos pts",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF059669)
            )
        }
    }
}




