
package cl.triskeledu.aprendogame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.triskeledu.aprendogame.viewmodel.GameViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import cl.triskeledu.aprendogame.data.local.AppDatabase



// Representa una actividad que mostraremos en el historial.
// No es una entidad de Room: solo organiza los datos para la pantalla.
private data class ActividadHistorial(
    val id: String,
    val tipo: String,
    val titulo: String,
    val descripcion: String,
    val fechaTimestamp: Long,
    val puntos: Int
)

@Composable
fun HistorialActividadesScreen(
    gameViewModel: GameViewModel,
    onVolverClick: () -> Unit
) {
    // Registros del jugador activo, recuperados desde Room.
    val descubrimientos by
    gameViewModel.descubrimientosQr.collectAsStateWithLifecycle()

    val videosVistos by
    gameViewModel.videosVistos.collectAsStateWithLifecycle()

    val videosAdicionales by
    gameViewModel.videosAdicionalesVistos.collectAsStateWithLifecycle()

    val visitas by
    gameViewModel.historialVisitas.collectAsStateWithLifecycle()

    val capsulas by
    gameViewModel.capsulas.collectAsStateWithLifecycle()

    val puntosTuristicos by
    gameViewModel.puntos.collectAsStateWithLifecycle()

    // Consulta los videos creados por el administrador.
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

// Identificadores de los QR escaneados por el jugador.
    val idsQrDescubiertos = descubrimientos
        .map { it.capsulaId }
        .toSet()

    // Relaciona cada cápsula y lugar con su identificador.
    val capsulasPorId = capsulas.associateBy { it.id }
    val lugaresPorId = puntosTuristicos.associateBy { it.id }

    // Unimos los registros de diferentes tablas en una sola lista.
    val actividades = buildList {

        // 1. Descubrimientos de códigos QR: +10 puntos.
        descubrimientos.forEach { descubrimiento ->

            val capsula = capsulasPorId[descubrimiento.capsulaId]
            val lugar = capsula?.let {
                lugaresPorId[it.puntoId]
            }

            add(
                ActividadHistorial(
                    id = "qr_${descubrimiento.id}",
                    tipo = "QR",
                    titulo = "QR escaneado",
                    descripcion = lugar?.nombre
                        ?: capsula?.titulo
                        ?: "Lugar turístico",
                    fechaTimestamp = descubrimiento.fechaTimestamp,
                    puntos = 10
                )
            )
        }

        // 2. Videos turísticos vistos: no otorgan puntos.
        videosVistos
            .filter { it.capsulaId in idsQrDescubiertos }
            .distinctBy { it.capsulaId }
            .forEach { video ->

            val capsula = capsulasPorId[video.capsulaId]

            add(
                ActividadHistorial(
                    id = "video_${video.id}",
                    tipo = "Videos",
                    titulo = "Video visto",
                    descripcion = capsula?.titulo
                        ?: "Video educativo",
                    fechaTimestamp = video.fechaTimestamp,
                    puntos = 0
                )
            )
        }

        // 3. Videos independientes vistos: no otorgan puntos.
// Excluye registros antiguos de videos protegidos por QR.
        videosAdicionales
            .distinctBy { it.videoAdicionalId }
            .forEach { video ->

                val identificador = video.videoAdicionalId

                val nombreVideo = when (identificador) {
                    1 -> "IA en nuestra vida cotidiana"
                    2 -> "Ética y uso responsable de la IA"

                    else -> {
                        val registrado = videosRegistrados.firstOrNull {
                            it.id == -identificador
                        }

                        registrado?.titulo ?: "Video educativo adicional"
                    }
                }

                val esVideoIndependiente = when {
                    identificador == 1 || identificador == 2 -> true

                    identificador < 0 -> {
                        val registrado = videosRegistrados.firstOrNull {
                            it.id == -identificador
                        }

                        if (registrado == null) {
                            false
                        } else {
                            val contenido = contenidos.firstOrNull {
                                it.id == registrado.contenidoId
                            }

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

                if (esVideoIndependiente) {
                    add(
                        ActividadHistorial(
                            id = "adicional_${video.id}",
                            tipo = "Videos",
                            titulo = "Video visto",
                            descripcion = nombreVideo,
                            fechaTimestamp = video.fechaTimestamp,
                            puntos = 0
                        )
                    )
                }
            }

        // 4. Selfies turísticas validadas: +100 puntos.
        visitas
            .filter {
                it.completada &&
                        !it.rutaFotoSelfie.isNullOrBlank()
            }
            .forEach { visita ->

                val lugar = lugaresPorId[visita.puntoId]

                add(
                    ActividadHistorial(
                        id = "selfie_${visita.id}",
                        tipo = "Selfies",
                        titulo = "Selfie turística",
                        descripcion = lugar?.nombre
                            ?: "Lugar turístico",
                        fechaTimestamp = visita.fechaTimestamp,
                        puntos = 100
                    )
                )
            }
    }.sortedByDescending { it.fechaTimestamp }

    // Controla el filtro seleccionado.
    var filtroSeleccionado by remember {
        mutableStateOf("Todos")
    }

    val actividadesFiltradas = if (filtroSeleccionado == "Todos") {
        actividades
    } else {
        actividades.filter { it.tipo == filtroSeleccionado }
    }

    // Agrupa las actividades por día, manteniendo el orden cronológico.
    val formatoFecha = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale("es", "CL"))
    }

    val formatoHora = remember {
        SimpleDateFormat("HH:mm", Locale("es", "CL"))
    }

    val actividadesPorFecha = actividadesFiltradas.groupBy {
        formatoFecha.format(Date(it.fechaTimestamp))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Encabezado.
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
                text = "Historial de Actividades",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Revisa tus descubrimientos y logros en Santiago",
            fontSize = 13.sp,
            color = Color(0xFF64748B)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Filtros de actividades.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("Todos", "QR", "Videos", "Selfies").forEach { filtro ->

                val seleccionado = filtroSeleccionado == filtro

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            filtroSeleccionado = filtro
                        },
                    shape = RoundedCornerShape(20.dp),
                    color = if (seleccionado) {
                        Color(0xFF2563EB)
                    } else {
                        Color.White
                    }
                ) {
                    Box(
                        modifier = Modifier.padding(
                            horizontal = 4.dp,
                            vertical = 10.dp
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = filtro,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (seleccionado) {
                                Color.White
                            } else {
                                Color(0xFF475569)
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (actividadesFiltradas.isEmpty()) {

            // Mensaje cuando todavía no hay registros.
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Todavía no hay actividades",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Explora Santiago para comenzar tu historial.",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

        } else {

            actividadesPorFecha.forEach { (fecha, registros) ->

                Text(
                    text = fecha,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )

                Spacer(modifier = Modifier.height(10.dp))

                registros.forEach { actividad ->

                    TarjetaActividadHistorial(
                        actividad = actividad,
                        hora = formatoHora.format(
                            Date(actividad.fechaTimestamp)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun TarjetaActividadHistorial(
    actividad: ActividadHistorial,
    hora: String
) {
    val icono: ImageVector = when (actividad.tipo) {
        "QR" -> Icons.Default.QrCodeScanner
        "Videos" -> Icons.Default.PlayCircle
        else -> Icons.Default.CameraAlt
    }

    val colorIcono = when (actividad.tipo) {
        "QR" -> Color(0xFF059669)
        "Videos" -> Color(0xFF2563EB)
        else -> Color(0xFF7C3AED)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = colorIcono.copy(alpha = 0.12f)
            ) {
                Box(
                    modifier = Modifier.size(42.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = colorIcono
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = actividad.titulo,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = actividad.descripcion,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )

                Text(
                    text = hora,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            if (actividad.puntos > 0) {
                Text(
                    text = "+${actividad.puntos} pts",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF059669)
                )
            }
        }
    }
}


