
package cl.triskeledu.aprendogame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    // --------------------------------------------------
    // DATOS DEL JUGADOR DESDE ROOM
    // --------------------------------------------------

    val usuario by gameViewModel.usuario.collectAsStateWithLifecycle()

    val historialVisitas by
    gameViewModel.historialVisitas.collectAsStateWithLifecycle()

    val qrDescubiertos by
    gameViewModel.qrDescubiertos.collectAsStateWithLifecycle()

    val videosDescubiertos by
    gameViewModel.videosDescubiertos.collectAsStateWithLifecycle()

    // Lista de todos los puntos turísticos disponibles.
    val puntosTuristicos by
    gameViewModel.puntos.collectAsStateWithLifecycle()

    // --------------------------------------------------
    // CÁLCULOS DEL DASHBOARD
    // --------------------------------------------------

    // Cuenta los lugares diferentes con visitas completadas.
    val idsLugaresVisitados = historialVisitas
        .filter { it.completada }
        .map { it.puntoId }
        .toSet()

    // Solo considera lugares que siguen existiendo en Room.
    val idsLugaresDisponibles = puntosTuristicos
        .map { it.id }
        .toSet()

    val lugaresVisitados = idsLugaresVisitados
        .intersect(idsLugaresDisponibles)
        .size

    // Total de lugares turísticos registrados.
    val totalLugares = idsLugaresDisponibles.size

    // Total de puntos turísticos disponibles en Room.
    val totalPuntosTuristicos = puntosTuristicos.size

// Calcula el avance según los QR descubiertos.
// Este cálculo supone un QR por punto turístico.
    val puntosDescubiertos = qrDescubiertos

    val progreso = if (totalPuntosTuristicos > 0) {
        (puntosDescubiertos.toFloat() / totalPuntosTuristicos)
            .coerceIn(0f, 1f)
    } else {
        0f
    }

    // Convierte el progreso a porcentaje.
    val porcentajeProgreso = (progreso * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FB))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        // --------------------------------------------------
        // BIENVENIDA
        // --------------------------------------------------

        Text(
            text = "¡Hola, explorador!",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF102747)
        )

        Text(
            text = "Sigue explorando Santiago",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(20.dp))

        // --------------------------------------------------
        // PUNTOS TOTALES
        // --------------------------------------------------

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF5145E5)
            )
        ) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text = "🏆 ${usuario?.puntosAcumulados ?: 0}",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "Puntos totales",
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --------------------------------------------------
        // INDICADORES DEL JUGADOR
        // --------------------------------------------------

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            TarjetaIndicador(
                cantidad = qrDescubiertos.toString(),
                titulo = "QR encontrados",
                modifier = Modifier.weight(1f)
            )

            TarjetaIndicador(
                cantidad = videosDescubiertos.toString(),
                titulo = "Videos descubiertos",
                modifier = Modifier.weight(1f)
            )

            TarjetaIndicador(
                cantidad = lugaresVisitados.toString(),
                titulo = "Lugares visitados",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --------------------------------------------------
        // PROGRESO
        // --------------------------------------------------

        Text(
            text = "Tu progreso",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF102747)
        )

        Spacer(modifier = Modifier.height(10.dp))

        LinearProgressIndicator(
            progress = { progreso },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp),
            color = Color(0xFF246BEE),
            trackColor = Color(0xFFE0E5EE)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = puntosDescubiertos.toString() +
                    " de " + totalPuntosTuristicos +
                    " puntos descubiertos (" +
                    porcentajeProgreso + "%)",
            fontSize = 13.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --------------------------------------------------
        // DESAFÍO ACTUAL
        // --------------------------------------------------

        Text(
            text = "Desafío actual",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF102747)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFD782)
            )
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "📍 Visita Plaza de Armas",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF102747)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Explora este lugar y descubre su código QR.",
                    fontSize = 13.sp,
                    color = Color(0xFF102747)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --------------------------------------------------
        // ACCESOS RÁPIDOS
        // --------------------------------------------------

        Text(
            text = "Accesos rápidos",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF102747)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            TarjetaIndicador(
                cantidad = "🗺️",
                titulo = "Mapa",
                modifier = Modifier.weight(1f),
                onClick = onMapaClick
            )

            TarjetaIndicador(
                cantidad = "📷",
                titulo = "Escáner QR",
                modifier = Modifier.weight(1f),
                onClick = onEscanerClick
            )

            TarjetaIndicador(
                cantidad = "📚",
                titulo = "Colección",
                modifier = Modifier.weight(1f),
                onClick = onColeccionClick
            )
        }
    }
}

// --------------------------------------------------
// TARJETA REUTILIZABLE
// --------------------------------------------------

@Composable
fun TarjetaIndicador(
    cantidad: String,
    titulo: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {

    Card(
        modifier = modifier
            .height(105.dp)
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = cantidad,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF102747)
            )

            Text(
                text = titulo,
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

