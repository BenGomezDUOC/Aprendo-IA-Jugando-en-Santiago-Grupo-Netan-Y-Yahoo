package cl.triskeledu.aprendogame.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.triskeledu.aprendogame.R
import cl.triskeledu.aprendogame.data.local.AppDatabase
import cl.triskeledu.aprendogame.model.Usuario
import kotlinx.coroutines.flow.Flow

private val AzulFondo = Color(0xFF081B31)
private val AzulTarjeta = Color(0xFF173858)
private val AzulBoton = Color(0xFF2563EB)
private val Celeste = Color(0xFF38BDF8)

@Composable
fun SeleccionJugadorScreen(
    jugadoresFlow: Flow<List<Usuario>>,
    onIniciarSesion: (Usuario, String, (Boolean) -> Unit) -> Unit,
    onCrearJugador: () -> Unit
) {
    val context = LocalContext.current
    // Objetos de base de datos: remember, NO rememberSaveable.
    val database = remember(context) {
        AppDatabase.getInstance(context.applicationContext)
    }
    val jugadores by jugadoresFlow.collectAsState(initial = emptyList())

    var jugadorSeleccionadoId by rememberSaveable { mutableStateOf<Int?>(null) }
    var mostrarDialogo by rememberSaveable { mutableStateOf(false) }
    // No persistimos contraseñas en el estado restaurable.
    var contrasenaIngresada by remember { mutableStateOf("") }
    var mostrarContrasena by rememberSaveable { mutableStateOf(false) }
    var errorContrasena by remember { mutableStateOf("") }
    var verificando by remember { mutableStateOf(false) }
    val jugadorSeleccionado = jugadores.find { it.id == jugadorSeleccionadoId }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF102D4B), AzulFondo, Color(0xFF06101E))
                )
            )
    ) {
        // Detalles luminosos sin necesitar imágenes ni recursos nuevos.
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(210.dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0x332CA9E8), Color.Transparent)
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 32.dp, bottom = 20.dp)) {
                Text(
                    text = "¿Quién va a explorar?",
                    color = Color.White,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Selecciona tu jugador",
                    color = Celeste,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Continúa tu aventura por Santiago y descubre nuevos desafíos.",
                    color = Color(0xFFB7C9DC),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                items(items = jugadores, key = { it.id }) { jugador ->
                    val qrFlow = remember(jugador.id) {
                        database.descubrimientoQrDao().contarQrDescubiertos(jugador.id)
                    }
                    val qrDescubiertos by qrFlow.collectAsState(initial = 0)
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
                                contrasenaIngresada = ""
                                errorContrasena = ""
                                verificando = false
                                mostrarContrasena = false
                                mostrarDialogo = true
                            },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = AzulTarjeta.copy(alpha = 0.94f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(58.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF28567B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(imagenAvatar),
                                    contentDescription = "Avatar de ${jugador.nombre}",
                                    modifier = Modifier.size(53.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = jugador.nombre,
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(5.dp))
                                Text(
                                    text = "★ ${jugador.puntosAcumulados} puntos",
                                    color = Color(0xFFFFD47B),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(5.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.QrCode2, null, tint = Celeste, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "$qrDescubiertos QR descubiertos",
                                        color = Color(0xFFC7D8E9),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowForwardIos,
                                contentDescription = "Seleccionar jugador",
                                tint = Celeste,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(5.dp))
                    Button(
                        onClick = onCrearJugador,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulBoton)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(9.dp))
                        Text("Crear nuevo jugador", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (mostrarDialogo && jugadorSeleccionado != null) {
        AlertDialog(
            onDismissRequest = {
                if (!verificando) mostrarDialogo = false
                contrasenaIngresada = ""
                errorContrasena = ""
            },
            containerColor = Color(0xFF102B48),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFDBEAFE),
            title = { Text("Iniciar sesión", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Jugador: ${jugadorSeleccionado.nombre}")
                    Spacer(modifier = Modifier.height(15.dp))
                    OutlinedTextField(
                        value = contrasenaIngresada,
                        onValueChange = {
                            contrasenaIngresada = it
                            errorContrasena = ""
                        },
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        trailingIcon = {
                            IconButton(onClick = { mostrarContrasena = !mostrarContrasena }) {
                                Icon(
                                    if (mostrarContrasena) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (mostrarContrasena) "Ocultar contraseña" else "Mostrar contraseña"
                                )
                            }
                        },
                        visualTransformation = if (mostrarContrasena) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        isError = errorContrasena.isNotEmpty(),
                        supportingText = if (errorContrasena.isNotEmpty()) {
                            { Text(errorContrasena, color = Color(0xFFFFA6A6)) }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Celeste,
                            unfocusedLabelColor = Color(0xFFBED0E2),
                            focusedBorderColor = Celeste,
                            unfocusedBorderColor = Color(0xFF69839D)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (contrasenaIngresada.isBlank()) {
                            errorContrasena = "Ingresa tu contraseña."
                        } else {
                            verificando = true
                            // AppNavigation verifica el hash y devuelve el resultado.
                            onIniciarSesion(jugadorSeleccionado, contrasenaIngresada) { correcta ->
                                verificando = false
                                if (correcta) {
                                    mostrarDialogo = false
                                    contrasenaIngresada = ""
                                    errorContrasena = ""
                                } else {
                                    errorContrasena = "Contraseña incorrecta. Inténtalo nuevamente."
                                }
                            }
                        }
                    },
                    enabled = !verificando,
                    colors = ButtonDefaults.buttonColors(containerColor = AzulBoton)
                ) { Text(if (verificando) "Verificando..." else "Ingresar") }
            },
            dismissButton = {
                TextButton(onClick = {
                    if (!verificando) mostrarDialogo = false
                    contrasenaIngresada = ""
                    errorContrasena = ""
                }) { Text("Cancelar", color = Celeste) }
            }
        )
    }
}


