
package cl.triskeledu.aprendogame.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import cl.triskeledu.aprendogame.model.Capsula

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.draw.scale

@Composable
fun ResultadoQRScreen(
    capsula: Capsula,
    puntosGanados: Int,
    onVolverMapa: () -> Unit,
    onTomarSelfie: () -> Unit,
    onVerVideo: () -> Unit
) {

    // Controla el tamaño del trofeo.
    val escalaTrofeo = remember {
        Animatable(0f)
    }

// Inicia la animación cuando se abre la pantalla.
    LaunchedEffect(Unit) {
        escalaTrofeo.snapTo(0f)

        kotlinx.coroutines.delay(500)

        escalaTrofeo.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = 0.35f,
                stiffness = 150f
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Celebración del descubrimiento.
        Text(
            text = "🏆",
            fontSize = 75.sp,
            modifier = Modifier.scale(escalaTrofeo.value)
        )

        Text(
            text = if (puntosGanados > 0) {
                "¡QR descubierto!"
            } else {
                "¡QR reconocido!"
            },
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        // Puntos reales obtenidos.
        Text(
            text = "+$puntosGanados puntos",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF20A65A)
        )

        if (puntosGanados == 0) {
            Text(
                text = "Ya habías descubierto este QR. " +
                        "No se sumaron puntos nuevamente.",
                textAlign = TextAlign.Center
            )
        }

        // Información real de Room.
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    text = capsula.titulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Text(
                    text = capsula.descripcion
                )

                Text(
                    text = "Video educativo",
                    color = Color.Gray
                )
            }
        }

        // Aviso de bonificación turística.
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    text = "📸 Bonificación turística",
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Si este QR corresponde a un hito " +
                            "turístico, puedes obtener 100 puntos " +
                            "adicionales mediante una selfie válida."
                )
            }
        }

        // Abre la pantalla 9 para reproducir el video dentro de la app.
        Button(
            onClick = onVerVideo,
            enabled = capsula.urlVideo.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text("Ver video")
        }

        // Abre la pantalla de selfie turística.
        // Los puntos se otorgarán después de validar y guardar la foto.
        OutlinedButton(
            onClick = onTomarSelfie,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text("Tomar selfie (+100 pts)")
        }

        // Regresa al mapa.
        OutlinedButton(
            onClick = onVolverMapa,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text("Volver al mapa")
        }
    }
}
