package cl.triskeledu.aprendogame.ui.screens

// Herramientas para construir la pantalla
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.triskeledu.aprendogame.R

@Composable // Indica que esta función construye elementos visuales
fun InicioScreen(
    onComenzarClick: () -> Unit, // Acción para ir a la selección de jugador
    onCuentaClick: () -> Unit // Acción para ir al acceso de usuarios
) {

    Box( // Permite colocar la fotografía detrás y el contenido encima
        modifier = Modifier.fillMaxSize() // Ocupa todo el espacio disponible
    ) {

        Image( // Muestra la fotografía de Santiago
            painter = painterResource(id = R.drawable.santiago), // Busca la foto en drawable
            contentDescription = "Fotografía de Santiago", // Descripción de la imagen
            modifier = Modifier.fillMaxSize(), // Ocupa toda la pantalla
            contentScale = ContentScale.Crop // Llena la pantalla recortando lo que sobre
        )

        Box( // Coloca un degradado sobre la fotografía
            modifier = Modifier
                .fillMaxSize() // Ocupa toda la pantalla
                .background(
                    brush = Brush.verticalGradient( // Cambia el color de arriba hacia abajo
                        colors = listOf(
                            Color(0x00FFFFFF), // Arriba: transparente
                            Color(0x44112C50), // Centro: azul semitransparente
                            Color(0xEE102747) // Abajo: azul oscuro
                        )
                    )
                )
        )

        Column( // Organiza el contenido de arriba hacia abajo
            modifier = Modifier
                .fillMaxSize() // Ocupa toda la pantalla
                .padding(24.dp), // Deja espacio alrededor
            horizontalAlignment = Alignment.CenterHorizontally, // Centra horizontalmente
            verticalArrangement = Arrangement.SpaceBetween // Distribuye el contenido
        ) {

            Column( // Organiza el logo y el título
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(32.dp)) // Espacio superior

                Text(
                    text = "🎓", // Logo provisional hasta agregar el definitivo
                    fontSize = 55.sp // Tamaño del símbolo
                )

                Spacer(modifier = Modifier.height(12.dp)) // Espacio entre logo y título

                Text(
                    text = "Aprendo IA\nJugando en Santiago", // Título en dos líneas
                    fontSize = 30.sp, // Tamaño de la letra
                    fontWeight = FontWeight.Bold, // Letra en negrita
                    color = Color(0xFF102747), // Azul oscuro
                    textAlign = TextAlign.Center, // Centra el texto
                    lineHeight = 36.sp // Separación entre líneas
                )
            }

            Column( // Organiza la descripción y los beneficios
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Descubre, aprende y gana puntos escaneando códigos QR en la ciudad.",
                    fontSize = 17.sp, // Tamaño del texto
                    color = Color.White, // Texto blanco
                    textAlign = TextAlign.Center, // Texto centrado
                    lineHeight = 24.sp // Separación entre líneas
                )

                Spacer(modifier = Modifier.height(24.dp)) // Espacio antes de los beneficios

                Row( // Primer beneficio: icono y texto al lado
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow, // Icono de reproducción
                        contentDescription = null, // Icono decorativo
                        tint = Color.White, // Icono blanco
                        modifier = Modifier.size(28.dp)
                    )

                    Text(
                        text = "Aprende con videos cortos",
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row( // Segundo beneficio
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn, // Icono de ubicación
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )

                    Text(
                        text = "Recorre lugares icónicos",
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row( // Tercer beneficio
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle, // Icono de desafío
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )

                    Text(
                        text = "Gana puntos y desbloquea desafíos",
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }

            Column( // Organiza los dos botones inferiores
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Button( // Botón principal: Comenzar
                    onClick = { onComenzarClick() }, // Ejecuta la navegación
                    modifier = Modifier
                        .fillMaxWidth() // Ocupa el ancho disponible
                        .height(54.dp), // Altura del botón
                    shape = RoundedCornerShape(14.dp), // Esquinas redondeadas
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF246BEE) // Fondo azul
                    )
                ) {
                    Text(
                        text = "Comenzar",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp)) // Separación entre botones

                OutlinedButton( // Botón secundario
                    onClick = { onCuentaClick() }, // Ejecuta la acción de acceso
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White // Fondo blanco
                    )
                ) {
                    Text(
                        text = "Ya tengo una cuenta",
                        fontSize = 16.sp,
                        color = Color(0xFF102747) // Texto azul oscuro
                    )
                }

                Spacer(modifier = Modifier.height(16.dp)) // Espacio inferior
            }

        } // Cierra Column principal

    } // Cierra Box

} // Cierra InicioScreen