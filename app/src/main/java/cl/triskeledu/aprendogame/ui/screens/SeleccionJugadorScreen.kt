
package cl.triskeledu.aprendogame.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import cl.triskeledu.aprendogame.R
import cl.triskeledu.aprendogame.model.Usuario

import kotlinx.coroutines.flow.Flow

import androidx.compose.ui.platform.LocalContext
import cl.triskeledu.aprendogame.data.local.AppDatabase

/**
 * Pantalla 3: Selección de jugador.
 *
 * Muestra las cuentas registradas en Room:
 * - Avatar del usuario.
 * - Nombre.
 * - Puntos acumulados.
 *
 * Permite seleccionar una cuenta e ingresar su contraseña.
 */
@Composable
fun SeleccionJugadorScreen(
    jugadoresFlow: Flow<List<Usuario>>,
    onIniciarSesion: (Usuario, String) -> Unit,
    onCrearJugador: () -> Unit
) {

    // Accede a la base de datos Room.
    val context = LocalContext.current

    val database = remember(context) {
        AppDatabase.getInstance(context.applicationContext)
    }

    // Observa los usuarios almacenados en Room.
    val jugadores by jugadoresFlow.collectAsState(
        initial = emptyList()
    )

    // Conserva el ID del usuario seleccionado.
    var jugadorSeleccionadoId by rememberSaveable {
        mutableStateOf<Int?>(null)
    }

    // Controla la ventana para ingresar la contraseña.
    var mostrarDialogo by rememberSaveable {
        mutableStateOf(false)
    }

    // La contraseña no se guarda en el estado restaurable.
    var contrasenaIngresada by remember {
        mutableStateOf("")
    }

    // Recupera el usuario seleccionado.
    val jugadorSeleccionado = jugadores.find {
        it.id == jugadorSeleccionadoId
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FB))
            .padding(20.dp)
    ) {

        Text(
            text = "Selecciona tu jugador",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF102747)
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {

            // Tarjetas de los jugadores registrados.
            items(
                items = jugadores,
                key = { jugador -> jugador.id }
            ) { jugador ->

                // Observa cuántos QR ha descubierto este jugador.
                val qrDescubiertosFlow = remember(jugador.id) {
                    database.descubrimientoQrDao()
                        .contarQrDescubiertos(jugador.id)
                }

                val qrDescubiertos by qrDescubiertosFlow.collectAsState(
                    initial = 0
                )

                // Selecciona el recurso según el avatar
                // que tiene guardado este usuario en Room.
                val imagenAvatar = when (jugador.avatar) {
                    1 -> R.drawable.avatar1
                    2 -> R.drawable.avatar2
                    3 -> R.drawable.avatar3
                    else -> R.drawable.avatar1
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {

                            jugadorSeleccionadoId = jugador.id

                            // Limpia la contraseña anterior.
                            contrasenaIngresada = ""

                            mostrarDialogo = true
                        },
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
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // Muestra el avatar guardado del jugador.
                        Image(
                            painter = painterResource(
                                id = imagenAvatar
                            ),
                            contentDescription = "Avatar de ${jugador.nombre}",
                            modifier = Modifier.size(48.dp),
                            contentScale = ContentScale.Fit
                        )

                        Spacer(
                            modifier = Modifier.size(14.dp)
                        )

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            Text(
                                text = jugador.nombre,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF102747)
                            )

                            Text(
                                text = "${jugador.puntosAcumulados} puntos",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )

                            // Pendiente de conectar con el conteo
                            // de QR descubiertos por usuario.
                            Text(
                                text = "QR encontrados: $qrDescubiertos",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }

                        // Marca visualmente el jugador seleccionado.
                        if (jugadorSeleccionadoId == jugador.id) {

                            Text(
                                text = "✓",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF246BEE)
                            )
                        }
                    }
                }
            }

            // Tarjeta para registrar un jugador nuevo.
            item {

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onCrearJugador()
                        },
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
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Crear jugador",
                            tint = Color(0xFF246BEE),
                            modifier = Modifier.size(28.dp)
                        )

                        Spacer(
                            modifier = Modifier.size(14.dp)
                        )

                        Text(
                            text = "Crear nuevo jugador",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF102747)
                        )
                    }
                }
            }
        }
    }

    // --------------------------------------------------
    // VENTANA PARA INTRODUCIR LA CONTRASEÑA
    // --------------------------------------------------

    if (mostrarDialogo && jugadorSeleccionado != null) {

        AlertDialog(
            onDismissRequest = {
                mostrarDialogo = false
                contrasenaIngresada = ""
            },

            title = {
                Text("Iniciar sesión")
            },

            text = {

                Column {

                    Text(
                        text = "Jugador: ${jugadorSeleccionado.nombre}"
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    OutlinedTextField(
                        value = contrasenaIngresada,
                        onValueChange = {
                            contrasenaIngresada = it
                        },
                        label = {
                            Text("Contraseña")
                        },
                        visualTransformation =
                            PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },

            confirmButton = {

                Button(
                    onClick = {

                        // AppNavigation verifica la contraseña.
                        onIniciarSesion(
                            jugadorSeleccionado,
                            contrasenaIngresada
                        )
                    },
                    enabled = contrasenaIngresada.isNotBlank()
                ) {
                    Text("Ingresar")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        mostrarDialogo = false
                        contrasenaIngresada = ""
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
