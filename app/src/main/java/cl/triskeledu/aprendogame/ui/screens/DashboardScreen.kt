package cl.triskeledu.aprendogame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.triskeledu.aprendogame.model.VideoRegistrado
import cl.triskeledu.aprendogame.viewmodel.ColeccionViewModel
import cl.triskeledu.aprendogame.viewmodel.MapaViewModel
import cl.triskeledu.aprendogame.viewmodel.NotificacionesViewModel
import cl.triskeledu.aprendogame.viewmodel.SessionViewModel
import java.util.Locale

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import cl.triskeledu.aprendogame.data.local.AppDatabase

private val FondoInicio = Color(0xFFF5F7FB) // Mismo fondo de SeleccionJugadorScreen
private val AzulProfundo = Color(0xFF102747)
private val AzulAccion = Color(0xFF246BEE)
private val GrisTexto = Color(0xFF64748B)

@Composable
fun DashboardScreen(
    sessionViewModel: SessionViewModel,
    coleccionViewModel: ColeccionViewModel,
    mapaViewModel: MapaViewModel,
    notificacionesViewModel: NotificacionesViewModel,
    onMapaClick: () -> Unit,
    onEscanerClick: () -> Unit,
    onColeccionClick: () -> Unit,
    onVideoAdicionalClick: (Int) -> Unit,
    onVideoRegistradoClick: (VideoRegistrado) -> Unit = {},
    onProgresoClick: () -> Unit,
    onContenidosEducativosClick: () -> Unit,
    onNotificacionesClick: () -> Unit = {}
) {
    // Conservamos los parámetros de video para no romper llamadas anteriores.
    // Los videos ahora se muestran exclusivamente en ContenidosEducativosScreen.
    val usuario by sessionViewModel.usuario.collectAsStateWithLifecycle()
    val historialVisitas by sessionViewModel.historialVisitas.collectAsStateWithLifecycle()
    val qrDescubiertos by coleccionViewModel.qrDescubiertos.collectAsStateWithLifecycle()
    val capsulas by coleccionViewModel.capsulas.collectAsStateWithLifecycle()
    val descubrimientos by coleccionViewModel.descubrimientosQr.collectAsStateWithLifecycle()
    val videosVistos by coleccionViewModel.videosVistos.collectAsStateWithLifecycle()
    val videosAdicionalesVistos by coleccionViewModel.videosAdicionalesVistos.collectAsStateWithLifecycle()
    val puntosTuristicos by mapaViewModel.puntos.collectAsStateWithLifecycle()
    val notificacionesNoLeidas by notificacionesViewModel.notificacionesNoLeidas.collectAsStateWithLifecycle()

    // Accedemos a Room para consultar los videos administrativos.
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

    val capsulasDisponibles = capsulas.filter {
        it.estado == "PUBLICADA" && it.codigoQr.isNotBlank()
    }
    val totalQrDisponibles = capsulasDisponibles.map { it.codigoQr }.toSet().size
    val idsCapsulasDisponibles = capsulasDisponibles.map { it.id }.toSet()
    val qrDescubiertosDisponibles = descubrimientos.map { it.capsulaId }.toSet()
        .intersect(idsCapsulasDisponibles).size

    // QR que el jugador realmente ha descubierto.
    val idsQrDescubiertos = descubrimientos
        .map { it.capsulaId }
        .toSet()

// Videos QR vistos: deben tener QR descubierto y reproducción registrada.
    val totalVideosQr = videosVistos
        .map { it.capsulaId }
        .distinct()
        .count { it in idsQrDescubiertos }

// Videos independientes vistos.
    val totalVideosIndependientes = videosAdicionalesVistos
        .map { it.videoAdicionalId }
        .distinct()
        .count { identificador ->

            when {
                // Videos originales sin QR.
                identificador == 1 || identificador == 2 -> true

                // Videos creados por el administrador.
                identificador < 0 -> {
                    val video = videosRegistrados.firstOrNull {
                        it.id == -identificador
                    }

                    if (video == null) {
                        false
                    } else {
                        val contenido = contenidos.firstOrNull {
                            it.id == video.contenidoId
                        }

                        // Verificamos si el video tiene un QR asociado.
                        val tieneQr = contenido != null && capsulas.any {
                            it.codigoQr.startsWith(
                                "TRISKELEDU-CONTENIDO-${contenido.id}-"
                            )
                        }

                        !tieneQr
                    }
                }

                else -> false
            }
        }

// Total de videos que cumplen las reglas.
    val totalVideosVistos = totalVideosQr + totalVideosIndependientes

    val idsLugaresDisponibles = puntosTuristicos.map { it.id }.toSet()
    val lugaresVisitados = historialVisitas.filter { it.completada }
        .map { it.puntoId }.toSet().intersect(idsLugaresDisponibles).size
    val progreso = if (totalQrDisponibles > 0) {
        (qrDescubiertosDisponibles.toFloat() / totalQrDisponibles).coerceIn(0f, 1f)
    } else 0f
    val porcentajeProgreso = String.format(Locale.US, "%.2f", progreso * 100)
        .replace(".", ",")

    Column(
        modifier = Modifier.fillMaxSize()
            .background(FondoInicio)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = "¡Hola, ${usuario?.nombre ?: "Explorador"}! 👋",
                    fontSize = 26.sp, fontWeight = FontWeight.ExtraBold,
                    color = AzulProfundo
                )
                Text(
                    text = "Tu aventura por Santiago continúa aquí.",
                    fontSize = 14.sp, color = GrisTexto
                )
            }
            IconButton(onClick = onNotificacionesClick) {
                BadgedBox(
                    badge = {
                        if (notificacionesNoLeidas > 0) {
                            Badge { Text("$notificacionesNoLeidas") }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notificaciones",
                        tint = AzulProfundo
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onProgresoClick),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AzulProfundo),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
        ) {
            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("TUS PUNTOS", color = Color(0xFFB8D2FF),
                            fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(5.dp))
                        Text("${usuario?.puntosAcumulados ?: 0} pts",
                            color = Color.White, fontSize = 34.sp,
                            fontWeight = FontWeight.ExtraBold)
                    }
                    Surface(shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF27466E)) {
                        Icon(Icons.Default.Stars, contentDescription = null,
                            tint = Color(0xFFFFD166),
                            modifier = Modifier.padding(13.dp).size(32.dp))
                    }
                }
                HorizontalDivider(color = Color(0xFF355271))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Meta: 10.000 puntos", color = Color(0xFFD9E7FF),
                        fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Text("Ver progreso  →", color = Color(0xFF93C5FD),
                        fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(9.dp),
            modifier = Modifier.fillMaxWidth()) {
            IndicadorDashboard("${qrDescubiertos}", "QR escaneados",
                Icons.Default.QrCodeScanner, AzulAccion, Modifier.weight(1f))
            IndicadorDashboard("$totalVideosVistos", "Videos vistos",
                Icons.Default.PlayCircle, Color(0xFF8054DA), Modifier.weight(1f))
            IndicadorDashboard("$lugaresVisitados", "Lugares",
                Icons.Default.Place, Color(0xFF159F79), Modifier.weight(1f))
        }

        TituloDashboard("Tu exploración")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("QR descubiertos", color = AzulProfundo,
                        fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                        modifier = Modifier.weight(1f))
                    Text("$qrDescubiertosDisponibles / $totalQrDisponibles",
                        color = AzulAccion, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                LinearProgressIndicator(
                    progress = { progreso },
                    modifier = Modifier.fillMaxWidth().height(9.dp),
                    color = AzulAccion, trackColor = Color(0xFFE5ECF7)
                )
                Text(
                    text = when {
                        totalQrDisponibles == 0 -> "Todavía no hay códigos QR disponibles."
                        qrDescubiertosDisponibles == totalQrDisponibles -> "¡Exploración completada! Descubriste todos los QR."
                        else -> "Has completado el $porcentajeProgreso% de los QR. ¡Sigue explorando!"
                    },
                    color = GrisTexto, fontSize = 12.sp
                )
            }
        }

        TituloDashboard("Accesos rápidos")
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AccesoDashboard("Mapa", Icons.Default.Map, onMapaClick, Modifier.weight(1f))
            AccesoDashboard("Escanear QR", Icons.Default.QrCodeScanner,
                onEscanerClick, Modifier.weight(1f))
            AccesoDashboard("Colección", Icons.AutoMirrored.Filled.MenuBook,
                onColeccionClick, Modifier.weight(1f))
        }

        TituloDashboard("Aprende y descubre")
        Card(
            modifier = Modifier.fillMaxWidth()
                .clickable(onClick = onContenidosEducativosClick),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B3966)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Surface(shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF335782)) {
                    Icon(Icons.Default.SmartDisplay, contentDescription = null,
                        tint = Color(0xFFB9D7FF),
                        modifier = Modifier.padding(13.dp).size(31.dp))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("Contenidos Educativos", color = Color.White,
                        fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text("Videos nuevos, vistos y desbloqueados por QR",
                        color = Color(0xFFD5E4F9), fontSize = 12.sp,
                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = "Abrir contenidos",
                    tint = Color.White)
            }
        }

        TituloDashboard("Punto recomendado")
        Card(shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE7EFFD)),
            modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                Surface(shape = RoundedCornerShape(13.dp),
                    color = Color(0xFFD2E3FC)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null,
                        tint = AzulAccion,
                        modifier = Modifier.padding(12.dp).size(25.dp))
                }
                Column {
                    Text("Plaza de Armas", color = AzulProfundo,
                        fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Explora este lugar y descubre sus códigos QR.",
                        color = Color(0xFF50627B), fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun TituloDashboard(titulo: String) {
    Text(titulo, color = AzulProfundo, fontWeight = FontWeight.Bold, fontSize = 18.sp)
}

@Composable
private fun IndicadorDashboard(
    valor: String,
    etiqueta: String,
    icono: ImageVector,
    colorIcono: Color,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.fillMaxSize().padding(11.dp),
            verticalArrangement = Arrangement.Center) {
            Icon(icono, contentDescription = null, tint = colorIcono,
                modifier = Modifier.size(23.dp))
            Spacer(Modifier.height(8.dp))
            Text(valor, color = AzulProfundo, fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold)
            Text(etiqueta, color = GrisTexto, fontSize = 10.sp,
                lineHeight = 13.sp, maxLines = 2)
        }
    }
}

@Composable
private fun AccesoDashboard(
    titulo: String,
    icono: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.height(92.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Icon(icono, contentDescription = null, tint = AzulAccion,
                modifier = Modifier.size(27.dp))
            Spacer(Modifier.height(8.dp))
            Text(titulo, color = AzulProfundo, fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold, maxLines = 2)
        }
    }
}


