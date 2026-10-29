package cl.triskeledu.aprendogame.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.provider.Settings
import android.content.Intent
import android.net.Uri

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import cl.triskeledu.aprendogame.R
import cl.triskeledu.aprendogame.data.local.AppDatabase
import cl.triskeledu.aprendogame.viewmodel.ColeccionViewModel
import cl.triskeledu.aprendogame.viewmodel.SessionViewModel


import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.animation.core.*


@Composable
fun PerfilScreen(
    sessionViewModel: SessionViewModel,
    coleccionViewModel: ColeccionViewModel,
    onCerrarSesion: () -> Unit,
    onVolver: () -> Unit,
    onVerNotificaciones: () -> Unit
) {
    val usuario by sessionViewModel.usuario.collectAsStateWithLifecycle()
    val qrDescubiertos by coleccionViewModel.qrDescubiertos.collectAsStateWithLifecycle()
    val videosVistos by coleccionViewModel.videosVistos.collectAsStateWithLifecycle()
    val historialVisitas by sessionViewModel.historialVisitas.collectAsStateWithLifecycle()
    val videosAdicionales by coleccionViewModel.videosAdicionalesVistos.collectAsStateWithLifecycle()
    val descubrimientos by coleccionViewModel.descubrimientosQr.collectAsStateWithLifecycle()
    val capsulas by coleccionViewModel.capsulas.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val database = remember(context) { AppDatabase.getInstance(context.applicationContext) }
    val videosRegistrados by remember(database) {
        database.videoRegistradoDao().obtenerTodos()
    }.collectAsStateWithLifecycle(initialValue = emptyList())
    val contenidosPublicados by remember(database) {
        database.contenidoEducativoDao().obtenerPublicados()
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    // Cuenta videos distintos realmente vistos, no solo códigos QR escaneados.
    val idsQrEscaneados = descubrimientos.map { it.capsulaId }.toSet()
    val qrVistosValidos = videosVistos.map { it.capsulaId }.toSet()
        .intersect(idsQrEscaneados)
    val videosOriginalesVistos = videosAdicionales
        .map { it.videoAdicionalId }
        .filter { it in 1..2 }
        .toSet()
    val videosAdminIndependientes = videosAdicionales
        .map { it.videoAdicionalId }
        .filter { it < 0 }
        .map { -it }
        .toSet()
        .count { videoId ->
            val video = videosRegistrados.firstOrNull { it.id == videoId }
            val contenido = contenidosPublicados.firstOrNull { it.id == video?.contenidoId }
            video != null && (video.contenidoId == null || (contenido != null && contenido.puntoId == null))
        }
    val totalVideosVistos = qrVistosValidos.size +
            videosOriginalesVistos.size + videosAdminIndependientes

    // Controla qué ventana está abierta.
    var pantallaAbierta by remember { mutableStateOf("") }

    // Datos del formulario.
    var nombre by remember { mutableStateOf("") }
    var alias by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var avatar by remember { mutableIntStateOf(1) }

    var contrasenaActual by remember { mutableStateOf("") }
    var contrasenaNueva by remember { mutableStateOf("") }

    var guardando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }
    var guardadoCorrecto by remember { mutableStateOf(false) }


    // Guarda las preferencias de notificaciones del dispositivo.
    val preferenciasNotificaciones = remember(context) {
        context.getSharedPreferences(
            "preferencias_notificaciones",
            android.content.Context.MODE_PRIVATE
        )
    }

    // Cada cuenta utiliza su propio identificador.
    val usuarioId = usuario?.id

    // Lee la preferencia guardada.
    // Si el usuario nunca la ha activado, comienza en false.
    var notificaciones by remember(usuarioId) {
        mutableStateOf(
            usuarioId?.let { id ->
                preferenciasNotificaciones.getBoolean(
                    "notificaciones_usuario_$id",
                    false
                )
            } ?: false
        )
    }


    // Guarda la preferencia de notificaciones de la cuenta activa.
    fun guardarPreferenciaNotificaciones(activadas: Boolean) {
        notificaciones = activadas

        usuarioId?.let { id ->
            preferenciasNotificaciones.edit()
                .putBoolean("notificaciones_usuario_$id", activadas)
                .apply()
        }
    }

    // Solicita el permiso de notificaciones y recibe la respuesta.
    val solicitarPermisoNotificaciones =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { concedido ->
            guardarPreferenciaNotificaciones(concedido)
        }

    // Comprueba si Android permite mostrar notificaciones.
    fun tienePermisoNotificaciones(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
    }


    // Carga los datos cuando cambia la cuenta activa.
    LaunchedEffect(usuario?.id) {
        usuario?.let {
            nombre = it.nombre
            alias = it.alias
            correo = it.correo
            telefono = it.telefono
            direccion = it.direccion
            avatar = it.avatar
        }
    }

    // Abre el formulario con los datos actuales.
    fun abrirEdicion(destino: String) {
        usuario?.let {
            nombre = it.nombre
            alias = it.alias
            correo = it.correo
            telefono = it.telefono
            direccion = it.direccion
            avatar = it.avatar
        }

        contrasenaActual = ""
        contrasenaNueva = ""
        mensaje = ""
        guardadoCorrecto = false
        pantallaAbierta = destino
    }

    val avatarDrawable = when (usuario?.avatar) {
        1 -> R.drawable.avatar1
        2 -> R.drawable.avatar2
        3 -> R.drawable.avatar3
        else -> R.drawable.avatar1
    }

    // Animación continua del avatar: sube y baja suavemente.
    val movimientoAvatar = rememberInfiniteTransition(
        label = "movimientoAvatar"
    )

    val desplazamientoAvatar by movimientoAvatar.animateFloat(
        initialValue = 0f,
        targetValue = -18f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1500,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "desplazamientoAvatar"
    )

    // Cuenta únicamente las visitas completadas.
    val selfies = historialVisitas.count { it.completada }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        // ENCABEZADO
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onVolver,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver"
                )
            }

            Text(
                text = "Mi Perfil",
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(Modifier.height(25.dp))

        // FOTO Y DATOS
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {

            Image(
                painter = painterResource(avatarDrawable),
                contentDescription = "Avatar del usuario",
                modifier = Modifier
                    .size(85.dp)
                    .offset(y = desplazamientoAvatar.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color(0xFF2563EB), CircleShape)
                    .clickable { abrirEdicion("avatar") },
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.weight(1f)) {

                Text(
                    text = usuario?.nombre ?: "Usuario",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${usuario?.puntosAcumulados ?: 0} puntos",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )

                if (!usuario?.alias.isNullOrBlank()) {
                    Text(
                        text = "@${usuario?.alias}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            IconButton(
                onClick = { abrirEdicion("perfil") },
                enabled = usuario != null
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar perfil",
                    tint = Color(0xFF2563EB)
                )
            }
        }

        Spacer(Modifier.height(25.dp))

        // ESTADÍSTICAS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            EstadisticaPerfil(
                cantidad = qrDescubiertos,
                titulo = "QR",
                modifier = Modifier.weight(1f)
            )

            EstadisticaPerfil(
                cantidad = totalVideosVistos,
                titulo = "Videos",
                modifier = Modifier.weight(1f)
            )

            EstadisticaPerfil(
                cantidad = selfies,
                titulo = "Selfies",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(25.dp))

        // MENÚ
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {

            Column {

                OpcionPerfil(
                    icono = Icons.Default.Person,
                    titulo = "Editar perfil",
                    onClick = { abrirEdicion("perfil") }
                )

                HorizontalDivider()

                OpcionPerfil(
                    icono = Icons.Default.Face,
                    titulo = "Cambiar avatar",
                    onClick = { abrirEdicion("avatar") }
                )

                HorizontalDivider()


                // Abre el historial de notificaciones.
                OpcionPerfil(
                    icono = Icons.Default.Notifications,
                    titulo = "Ver notificaciones",
                    subtitulo = "Consultar avisos y actividades",
                    onClick = onVerNotificaciones
                )

                HorizontalDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Color(0xFF64748B)
                    )

                    Spacer(Modifier.width(14.dp))

                    Text(
                        "Notificaciones",
                        modifier = Modifier.weight(1f)
                    )


                    Switch(
                        checked = notificaciones,
                        enabled = usuarioId != null,
                        onCheckedChange = { activadas ->
                            if (!activadas) {
                                guardarPreferenciaNotificaciones(false)
                            } else if (tienePermisoNotificaciones()) {
                                guardarPreferenciaNotificaciones(true)
                            } else {
                                solicitarPermisoNotificaciones.launch(
                                    Manifest.permission.POST_NOTIFICATIONS
                                )
                            }
                        }
                    )

                } // Cierra la Row de notificaciones

                HorizontalDivider()

                OpcionPerfil(
                    icono = Icons.Default.Lock,
                    titulo = "Permisos",
                    subtitulo = "Cámara, Ubicación",
                    onClick = { pantallaAbierta = "permisos" }
                )

                HorizontalDivider()

                OpcionPerfil(
                    icono = Icons.Default.Info,
                    titulo = "Ayuda y soporte",
                    onClick = { pantallaAbierta = "ayuda" }
                )

                HorizontalDivider()

                OpcionPerfil(
                    icono = Icons.Default.ExitToApp,
                    titulo = "Cerrar sesión",
                    onClick = onCerrarSesion
                )
            }
        }

        if (mensaje.isNotBlank()) {
            Spacer(Modifier.height(14.dp))

            Text(
                text = mensaje,
                color = if (guardadoCorrecto)
                    Color(0xFF15803D)
                else
                    Color(0xFFDC2626),
                fontSize = 13.sp
            )
        }
    }

    // VENTANA EDITAR PERFIL
    if (pantallaAbierta == "perfil") {

        AlertDialog(
            onDismissRequest = {
                if (!guardando) pantallaAbierta = ""
            },
            title = { Text("Editar perfil") },
            text = {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = alias,
                        onValueChange = { alias = it },
                        label = { Text("Alias") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = correo,
                        onValueChange = { correo = it },
                        label = { Text("Correo electrónico") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        label = { Text("Teléfono") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = direccion,
                        onValueChange = { direccion = it },
                        label = { Text("Dirección") },
                        singleLine = true
                    )

                    HorizontalDivider()

                    Text(
                        "Cambiar contraseña (opcional)",
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = contrasenaActual,
                        onValueChange = { contrasenaActual = it },
                        label = { Text("Contraseña actual") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = contrasenaNueva,
                        onValueChange = { contrasenaNueva = it },
                        label = { Text("Contraseña nueva") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )

                    Text(
                        "Deja ambas vacías si no deseas cambiarla.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    if (mensaje.isNotBlank() && !guardadoCorrecto) {
                        Text(
                            mensaje,
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {

                TextButton(
                    enabled = !guardando,
                    onClick = {
                        guardando = true
                        mensaje = ""

                        sessionViewModel.editarPerfil(
                            nombre = nombre,
                            alias = alias,
                            correo = correo,
                            telefono = telefono,
                            direccion = direccion,
                            avatar = avatar,
                            contrasenaActual = contrasenaActual,
                            contrasenaNueva = contrasenaNueva
                        ) { exito, resultado ->

                            guardando = false
                            mensaje = resultado
                            guardadoCorrecto = exito

                            if (exito) {
                                pantallaAbierta = ""
                                contrasenaActual = ""
                                contrasenaNueva = ""
                            }
                        }
                    }
                ) {
                    Text(if (guardando) "Guardando..." else "Guardar")
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !guardando,
                    onClick = { pantallaAbierta = "" }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // VENTANA CAMBIAR AVATAR
    if (pantallaAbierta == "avatar") {

        AlertDialog(
            onDismissRequest = {
                if (!guardando) pantallaAbierta = ""
            },
            title = { Text("Cambiar avatar") },
            text = {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    listOf(
                        1 to R.drawable.avatar1,
                        2 to R.drawable.avatar2,
                        3 to R.drawable.avatar3
                    ).forEach { (numero, drawable) ->

                        Image(
                            painter = painterResource(drawable),
                            contentDescription = "Avatar $numero",
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .border(
                                    if (avatar == numero) 3.dp else 1.dp,
                                    if (avatar == numero)
                                        Color(0xFF2563EB)
                                    else
                                        Color.LightGray,
                                    CircleShape
                                )
                                .clickable { avatar = numero },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            },
            confirmButton = {

                TextButton(
                    enabled = !guardando,
                    onClick = {
                        guardando = true
                        mensaje = ""

                        sessionViewModel.editarPerfil(
                            nombre = nombre,
                            alias = alias,
                            correo = correo,
                            telefono = telefono,
                            direccion = direccion,
                            avatar = avatar,
                            contrasenaActual = "",
                            contrasenaNueva = ""
                        ) { exito, resultado ->

                            guardando = false
                            mensaje = resultado
                            guardadoCorrecto = exito

                            if (exito) pantallaAbierta = ""
                        }
                    }
                ) {
                    Text(if (guardando) "Guardando..." else "Guardar")
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !guardando,
                    onClick = { pantallaAbierta = "" }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // VENTANA DE PERMISOS
    if (pantallaAbierta == "permisos") {

        val permisoCamara = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        val permisoUbicacion = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

        AlertDialog(
            onDismissRequest = { pantallaAbierta = "" },
            title = { Text("Permisos de la aplicación") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Cámara: " +
                                if (permisoCamara) "Permitida" else "No permitida"
                    )

                    Text(
                        "Ubicación: " +
                                if (permisoUbicacion) "Permitida" else "No permitida"
                    )

                    Text(
                        "Puedes revisar o cambiar los permisos desde los ajustes de Android."
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(intent)
                        pantallaAbierta = ""
                    }
                ) {
                    Text("Abrir ajustes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { pantallaAbierta = "" }
                ) {
                    Text("Cerrar")
                }
            }
        )
    }

    // VENTANA DE AYUDA
    if (pantallaAbierta == "ayuda") {

        AlertDialog(
            onDismissRequest = { pantallaAbierta = "" },
            title = { Text("Ayuda y soporte") },
            text = {
                Text(
                    "Triskeledu es una aplicación educativa para explorar Santiago, descubrir códigos QR, ver cápsulas y registrar visitas.\n\n" +
                            "Si tienes problemas con la cámara o ubicación, revisa los permisos de la aplicación."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { pantallaAbierta = "" }
                ) {
                    Text("Entendido")
                }
            }
        )
    }
}

// TARJETA DE ESTADÍSTICAS
@Composable
private fun EstadisticaPerfil(
    cantidad: Int,
    titulo: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                "$cantidad",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Text(
                titulo,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

// OPCIÓN DEL MENÚ
@Composable
private fun OpcionPerfil(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    subtitulo: String = "",
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = Color(0xFF64748B)
        )

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {

            Text(
                titulo,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )

            if (subtitulo.isNotBlank()) {
                Text(
                    subtitulo,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.LightGray
        )
    }
}