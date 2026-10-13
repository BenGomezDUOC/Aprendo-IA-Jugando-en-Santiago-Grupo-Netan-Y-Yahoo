package cl.triskeledu.aprendogame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun InicioScreen(
    onClienteClick: () -> Unit,
    onAdministradorClick: () -> Unit,
    onContenidoClick: () -> Unit
) {
    Column( // Es el contenedor que apila los elementos uno debajo del otro de forma vertical.
        modifier = Modifier
            .fillMaxSize() // Modifier.fillMaxSize(): Ocupa todo el ancho y alto disponible en la pantalla
            .background(Color(0xFF1E88E5)) // Color de fondo temático accesible
            .padding(24.dp), // Modifier.padding: Dejar espacios para que los elementos no queden pegados
        verticalArrangement = Arrangement.Center, // Colocar los elementos en el centro vertical
        horizontalAlignment = Alignment.CenterHorizontally // Centrar los elementos horizontalmente
    ) {
        Text(text = "Aprendo IA Jugando en Santiago", color = Color.White)
        Text(text = "Descubre, aprende y gana puntos escaneando códigos QR en la ciudad.", color = Color.White)

        Button(
            onClick = {
                onClienteClick()
            }
        ) {
            Text("Ir a Cliente")
        }

        Button(
            onClick = {
                onAdministradorClick()
            }
        ) {
            Text("Ir a Administrador")
        }

        Button(
            onClick = {
                onContenidoClick()
            }
        ) {
            Text("Ir a Generador de Contenido")
        }
    }
}
