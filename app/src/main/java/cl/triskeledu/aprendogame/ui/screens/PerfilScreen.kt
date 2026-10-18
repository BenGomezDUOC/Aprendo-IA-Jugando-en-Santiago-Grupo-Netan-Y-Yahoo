package cl.triskeledu.aprendogame.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.triskeledu.aprendogame.R
import cl.triskeledu.aprendogame.viewmodel.GameViewModel

@Composable
fun PerfilScreen(
    gameViewModel: GameViewModel,
    onCerrarSesion: () -> Unit
) {
    val usuario by gameViewModel.usuario.collectAsStateWithLifecycle()
    val qrDescubiertos by gameViewModel.qrDescubiertos.collectAsStateWithLifecycle()
    val videosDescubiertos by gameViewModel.videosDescubiertos.collectAsStateWithLifecycle()

    val avatarDrawable = when (usuario?.avatar) {
        1 -> R.drawable.avatar1
        2 -> R.drawable.avatar2
        3 -> R.drawable.avatar3
        else -> R.drawable.avatar1
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Perfil del Jugador",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Avatar grande con marco
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(4.dp, Color(0xFF2563EB), CircleShape)
        ) {
            Image(
                painter = painterResource(id = avatarDrawable),
                contentDescription = "Avatar de ${usuario?.nombre ?: "Jugador"}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nombre y alias
        Text(
            text = usuario?.nombre ?: "Explorador",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )

        Text(
            text = "@${usuario?.alias ?: "explorador"}",
            fontSize = 15.sp,
            color = Color(0xFF64748B)
        )

        Text(
            text = usuario?.correo ?: "",
            fontSize = 13.sp,
            color = Color(0xFF94A3B8)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tarjeta resumen de logros
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Estadísticas del Juego",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Puntos totales", color = Color(0xFF64748B))
                    Text(
                        "${usuario?.puntosAcumulados ?: 0} pts",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB)
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF1F5F9))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Códigos QR escaneados", color = Color(0xFF64748B))
                    Text("$qrDescubiertos", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF1F5F9))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Cápsulas IA vistas", color = Color(0xFF64748B))
                    Text("$videosDescubiertos", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Botón cerrar sesión
        Button(
            onClick = onCerrarSesion,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar Sesión", fontWeight = FontWeight.SemiBold)
        }
    }
}
