package cl.triskeledu.aprendogame.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.triskeledu.aprendogame.R
import kotlinx.coroutines.delay

@Composable
fun InicioScreen(
    onComenzarClick: () -> Unit,
    onCuentaClick: () -> Unit
) {
    // Animación continua y suave del ícono de ubicación.
    val movimiento = rememberInfiniteTransition(label = "Icono flotante")
    val desplazamiento by movimiento.animateFloat(
        initialValue = 0f,
        targetValue = -9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Desplazamiento vertical"
    )

    // Hace aparecer los elementos en orden al abrir la pantalla.
    var mostrarEncabezado by remember { mutableStateOf(false) }
    var mostrarTarjeta1 by remember { mutableStateOf(false) }
    var mostrarTarjeta2 by remember { mutableStateOf(false) }
    var mostrarTarjeta3 by remember { mutableStateOf(false) }
    var mostrarBotones by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        mostrarEncabezado = true
        delay(170)
        mostrarTarjeta1 = true
        delay(170)
        mostrarTarjeta2 = true
        delay(170)
        mostrarTarjeta3 = true
        delay(170)
        mostrarBotones = true
    }

    // Detectan la pulsación de cada botón sin alterar sus acciones.
    val interaccionComenzar = remember { MutableInteractionSource() }
    val comenzarPresionado by interaccionComenzar.collectIsPressedAsState()
    val escalaComenzar by animateFloatAsState(
        targetValue = if (comenzarPresionado) 0.96f else 1f,
        animationSpec = tween(120),
        label = "Pulsación comenzar"
    )

    val interaccionCuenta = remember { MutableInteractionSource() }
    val cuentaPresionada by interaccionCuenta.collectIsPressedAsState()
    val escalaCuenta by animateFloatAsState(
        targetValue = if (cuentaPresionada) 0.96f else 1f,
        animationSpec = tween(120),
        label = "Pulsación cuenta"
    )

    // Animaciones continuas discretas para que se noten después de la entrada.
    val pulsoBotones = rememberInfiniteTransition(label = "Pulso botones")
    val pulsoComenzar by pulsoBotones.animateFloat(
        initialValue = 1f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulso comenzar"
    )
    val pulsoCuenta by pulsoBotones.animateFloat(
        initialValue = 1.015f,
        targetValue = 0.985f,
        animationSpec = infiniteRepeatable(
            animation = tween(1350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulso cuenta"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.santiago),
            contentDescription = "Santiago Urbano",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xCC09121F),
                            Color(0xDD0D1D35),
                            Color(0xFA091426)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            AnimatedVisibility(
                visible = mostrarEncabezado,
                enter = fadeIn(tween(650)) + slideInVertically(
                    animationSpec = tween(650), initialOffsetY = { it / 5 }
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 24.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0x3338BDF8),
                        modifier = Modifier
                            .size(68.dp)
                            .graphicsLayer { translationY = desplazamiento.dp.toPx() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Logo",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Aprendo IA Jugando",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Santiago Urbano",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF38BDF8),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Recorre hitos culturales, escanea códigos QR y aprende sobre Inteligencia Artificial.",
                        fontSize = 14.sp,
                        color = Color(0xFFCBD5E1),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnimatedVisibility(
                    visible = mostrarTarjeta1,
                    enter = fadeIn(tween(450)) + slideInVertically(tween(450)) { it / 3 }
                ) {
                    ItemBeneficio(
                        icono = Icons.Default.LocationOn,
                        colorIcono = Color(0xFF38BDF8),
                        titulo = "Recorre puntos turísticos",
                        subtitulo = "Usa el mapa interactivo y visita lugares icónicos"
                    )
                }

                AnimatedVisibility(
                    visible = mostrarTarjeta2,
                    enter = fadeIn(tween(450)) + slideInVertically(tween(450)) { it / 3 }
                ) {
                    ItemBeneficio(
                        icono = Icons.Default.PlayArrow,
                        colorIcono = Color(0xFF818CF8),
                        titulo = "Cápsulas de video IA",
                        subtitulo = "Aprende conceptos clave con explicaciones breves"
                    )
                }

                AnimatedVisibility(
                    visible = mostrarTarjeta3,
                    enter = fadeIn(tween(450)) + slideInVertically(tween(450)) { it / 3 }
                ) {
                    ItemBeneficio(
                        icono = Icons.Default.CheckCircle,
                        colorIcono = Color(0xFF4ADE80),
                        titulo = "Valida con Selfie y gana puntos",
                        subtitulo = "Acumula puntos y desbloquea el ranking"
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(
                visible = mostrarBotones,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 3 }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onComenzarClick,
                        interactionSource = interaccionComenzar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .scale(escalaComenzar * pulsoComenzar),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        Text(
                            text = "Comenzar Aventura",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = onCuentaClick,
                        interactionSource = interaccionCuenta,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .scale(escalaCuenta * pulsoCuenta),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0x22FFFFFF)
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF38BDF8), Color(0xFF818CF8))
                            )
                        )
                    ) {
                        Text(
                            text = "Ya tengo una cuenta",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemBeneficio(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    colorIcono: Color,
    titulo: String,
    subtitulo: String
) {
    // Cada tarjeta respira suavemente mientras está visible.
    val transicion = rememberInfiniteTransition(label = "Tarjeta $titulo")
    val escalaTarjeta by transicion.animateFloat(
        initialValue = 0.985f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Respiracion tarjeta"
    )
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0x401E293B),
        modifier = Modifier.fillMaxWidth().scale(escalaTarjeta)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = colorIcono.copy(alpha = 0.2f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = colorIcono,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = titulo,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitulo,
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

