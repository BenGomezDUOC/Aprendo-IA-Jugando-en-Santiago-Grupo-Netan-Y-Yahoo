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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.triskeledu.aprendogame.viewmodel.GameViewModel

@Composable
fun DashboardScreen(
    gameViewModel: GameViewModel,
    onMapaClick: () -> Unit,
    onEscanerClick: () -> Unit,
    onColeccionClick: () -> Unit
) {
    val usuario by gameViewModel.usuario.collectAsStateWithLifecycle()
    val historialVisitas by gameViewModel.historialVisitas.collectAsStateWithLifecycle()
    val qrDescubiertos by gameViewModel.qrDescubiertos.collectAsStateWithLifecycle()
    val videosDescubiertos by gameViewModel.videosDescubiertos.collectAsStateWithLifecycle()
    val puntosTuristicos by gameViewModel.puntos.collectAsStateWithLifecycle()

    val idsLugaresVisitados = historialVisitas
        .filter { it.completada }
        .map { it.puntoId }
        .toSet()

    val idsLugaresDisponibles = puntosTuristicos
        .map { it.id }
        .toSet()

    val lugaresVisitados = idsLugaresVisitados.intersect(idsLugaresDisponibles).size
    val totalPuntosTuristicos = puntosTuristicos.size

    val progreso = if (totalPuntosTuristicos > 0) {
        (qrDescubiertos.toFloat() / totalPuntosTuristicos).coerceIn(0f, 1f)
    } else {
        0f
    }
    val porcentajeProgreso = (progreso * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Bienvenido, ${usuario?.nombre ?: "Explorador"}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Text(
            text = "Resumen de tu progreso en Santiago",
            fontSize = 14.sp,
            color = Color(0xFF64748B)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Tarjeta Principal de Puntos
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Puntos Acumulados",
                        fontSize = 14.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${usuario?.puntosAcumulados ?: 0} pts",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF38BDF8)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x2238BDF8),
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Indicadores métricos limpios
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TarjetaMetrica(
                valor = qrDescubiertos.toString(),
                etiqueta = "QR escaneados",
                icono = Icons.Default.QrCodeScanner,
                colorIcono = Color(0xFF2563EB),
                modifier = Modifier.weight(1f)
            )

            TarjetaMetrica(
                valor = videosDescubiertos.toString(),
                etiqueta = "Cápsulas IA",
                icono = Icons.Default.PlayCircle,
                colorIcono = Color(0xFF7C3AED),
                modifier = Modifier.weight(1f)
            )

            TarjetaMetrica(
                valor = lugaresVisitados.toString(),
                etiqueta = "Lugares",
                icono = Icons.Default.LocationOn,
                colorIcono = Color(0xFF059669),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Progreso de exploración
        Text(
            text = "Avance de Exploración",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$qrDescubiertos de $totalPuntosTuristicos puntos descubiertos",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF334155)
                    )
                    Text(
                        text = "$porcentajeProgreso%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { progreso },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Color(0xFF2563EB),
                    trackColor = Color(0xFFE2E8F0)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Desafío activo
        Text(
            text = "Punto Recomendado",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFDBEAFE),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = Color(0xFF2563EB)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Plaza de Armas",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
                    Text(
                        text = "Visita este punto turístico para escanear su código QR.",
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Accesos directos sobrios
        Text(
            text = "Accesos Rápidos",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BotonAccesoRapido(
                titulo = "Ver Mapa",
                icono = Icons.Default.Map,
                onClick = onMapaClick,
                modifier = Modifier.weight(1f)
            )

            BotonAccesoRapido(
                titulo = "Escanear QR",
                icono = Icons.Default.QrCodeScanner,
                onClick = onEscanerClick,
                modifier = Modifier.weight(1f)
            )

            BotonAccesoRapido(
                titulo = "Cápsulas",
                icono = Icons.AutoMirrored.Filled.MenuBook,
                onClick = onColeccionClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TarjetaMetrica(
    valor: String,
    etiqueta: String,
    icono: ImageVector,
    colorIcono: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = colorIcono,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = valor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = etiqueta,
                fontSize = 11.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
private fun BotonAccesoRapido(
    titulo: String,
    icono: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(84.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = Color(0xFF2563EB),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = titulo,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B)
            )
        }
    }
}
