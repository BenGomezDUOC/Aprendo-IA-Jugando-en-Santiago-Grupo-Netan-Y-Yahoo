package cl.triskeledu.aprendogame.ui.screens

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.triskeledu.aprendogame.R
import cl.triskeledu.aprendogame.model.Usuario

@Composable
fun RegistroJugador(
    onCrearCuenta: (Usuario, (Boolean) -> Unit) -> Unit = { _, _ -> }
) {
    val fondo = Color(0xFF081C33)
    val tarjeta = Color(0xFF132F4D)
    val celeste = Color(0xFF38BDF8)
    val textoSecundario = Color(0xFFB8CBE0)

    var nombre by rememberSaveable { mutableStateOf("") }
    var alias by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var direccion by rememberSaveable { mutableStateOf("") }
    var avatarSeleccionado by rememberSaveable { mutableIntStateOf(1) }
    var mostrarErrores by rememberSaveable { mutableStateOf(false) }

    // Las contraseñas no se incluyen en el estado restaurable.
    var contrasena by remember { mutableStateOf("") }
    var confirmarContrasena by remember { mutableStateOf("") }
    var mostrarContrasena by rememberSaveable { mutableStateOf(false) }
    var mostrarConfirmacion by rememberSaveable { mutableStateOf(false) }
    var guardando by remember { mutableStateOf(false) }
    var errorRegistro by remember { mutableStateOf(false) }

    val errorNombre = when {
        nombre.isBlank() -> "Ingresa tu nombre completo."
        nombre.trim().length < 3 -> "El nombre debe tener al menos 3 caracteres."
        else -> null
    }
    val errorAlias = when {
        alias.isBlank() -> "Ingresa un alias."
        alias.trim().length < 3 -> "El alias debe tener al menos 3 caracteres."
        else -> null
    }
    val errorCorreo = when {
        correo.isBlank() -> "Ingresa tu correo electrónico."
        !Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches() -> "Ingresa un correo electrónico válido."
        else -> null
    }
    val digitosTelefono = telefono.filter { it.isDigit() }
    val errorTelefono = when {
        telefono.isBlank() -> "Ingresa tu teléfono."
        digitosTelefono.length < 8 || digitosTelefono.length > 15 -> "Ingresa un teléfono válido (8 a 15 dígitos)."
        else -> null
    }
    val errorContrasena = when {
        contrasena.isBlank() -> "Ingresa una contraseña."
        contrasena.length < 8 -> "La contraseña debe tener al menos 8 caracteres."
        else -> null
    }
    val errorConfirmacion = when {
        confirmarContrasena.isBlank() -> "Confirma tu contraseña."
        confirmarContrasena != contrasena -> "Las contraseñas no coinciden."
        else -> null
    }
    val formularioValido = listOf(
        errorNombre, errorAlias, errorCorreo, errorTelefono,
        errorContrasena, errorConfirmacion
    ).all { it == null }

    @Composable
    fun Campo(
        valor: String,
        cambiar: (String) -> Unit,
        etiqueta: String,
        icono: androidx.compose.ui.graphics.vector.ImageVector,
        error: String?,
        tipo: KeyboardType = KeyboardType.Text,
        ocultar: Boolean = false,
        visible: Boolean = false,
        alternarVisibilidad: (() -> Unit)? = null
    ) {
        OutlinedTextField(
            value = valor,
            onValueChange = { cambiar(it); errorRegistro = false },
            label = { Text(etiqueta) },
            leadingIcon = { Icon(icono, contentDescription = null) },
            trailingIcon = if (ocultar && alternarVisibilidad != null) {
                {
                    IconButton(onClick = alternarVisibilidad) {
                        Icon(
                            if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (visible) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                }
            } else null,
            visualTransformation = if (ocultar && !visible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = tipo),
            singleLine = true,
            enabled = !guardando,
            isError = mostrarErrores && error != null,
            supportingText = if (mostrarErrores && error != null) {
                { Text(error) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = celeste,
                unfocusedLabelColor = textoSecundario,
                focusedLeadingIconColor = celeste,
                unfocusedLeadingIconColor = textoSecundario,
                focusedTrailingIconColor = celeste,
                unfocusedTrailingIconColor = textoSecundario,
                focusedBorderColor = celeste,
                unfocusedBorderColor = Color(0xFF66809C),
                cursorColor = celeste,
                focusedContainerColor = tarjeta,
                unfocusedContainerColor = tarjeta,
                errorTextColor = Color.White,
                errorContainerColor = tarjeta
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF153B5C), fondo, Color(0xFF061426))))
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1B587A)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = celeste, modifier = Modifier.size(34.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text("Crear nuevo jugador", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        Spacer(Modifier.height(6.dp))
        Text("Únete a la aventura por Santiago", fontSize = 14.sp, color = textoSecundario)
        Spacer(Modifier.height(22.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xD9112943)),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Tus datos", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Completa los campos para crear tu cuenta", color = textoSecundario, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                Campo(nombre, { nombre = it }, "Nombre completo *", Icons.Default.Person, errorNombre)
                Campo(alias, { alias = it }, "Alias / Nombre de usuario *", Icons.Default.AccountCircle, errorAlias)
                Campo(correo, { correo = it }, "Correo electrónico *", Icons.Default.Email, errorCorreo, KeyboardType.Email)
                Campo(telefono, { telefono = it }, "Teléfono *", Icons.Default.Phone, errorTelefono, KeyboardType.Phone)
                Campo(contrasena, { contrasena = it }, "Contraseña (mínimo 8 caracteres) *", Icons.Default.Lock, errorContrasena, KeyboardType.Password, true, mostrarContrasena, { mostrarContrasena = !mostrarContrasena })
                Campo(confirmarContrasena, { confirmarContrasena = it }, "Confirmar contraseña *", Icons.Default.Lock, errorConfirmacion, KeyboardType.Password, true, mostrarConfirmacion, { mostrarConfirmacion = !mostrarConfirmacion })
                Campo(direccion, { direccion = it }, "Dirección (opcional)", Icons.Default.LocationOn, null)
            }
        }

        Spacer(Modifier.height(20.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xD9112943)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(18.dp)) {
                Text("Selecciona tu avatar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(R.drawable.avatar1, R.drawable.avatar2, R.drawable.avatar3).forEachIndexed { index, imagen ->
                        val numero = index + 1
                        val seleccionado = avatarSeleccionado == numero
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .border(if (seleccionado) 3.dp else 1.dp, if (seleccionado) celeste else Color(0xFF607D98), CircleShape)
                                .clickable(enabled = !guardando) { avatarSeleccionado = numero }
                        ) {
                            Image(
                                painter = painterResource(imagen),
                                contentDescription = "Avatar $numero",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        if (errorRegistro) {
            Text(
                "No se pudo crear la cuenta. Verifica los datos o si el correo ya está registrado.",
                color = Color(0xFFFF9D9D), fontSize = 13.sp
            )
            Spacer(Modifier.height(12.dp))
        }
        Button(
            onClick = {
                mostrarErrores = true
                if (!guardando && formularioValido) {
                    guardando = true
                    errorRegistro = false
                    val nuevoJugador = Usuario(
                        nombre = nombre.trim(),
                        alias = alias.trim(),
                        correo = correo.trim().lowercase(),
                        telefono = telefono.trim(),
                        contrasena = contrasena,
                        direccion = direccion.trim(),
                        avatar = avatarSeleccionado
                    )
                    onCrearCuenta(nuevoJugador) { exito ->
                        if (!exito) {
                            guardando = false
                            errorRegistro = true
                        }
                    }
                }
            },
            enabled = !guardando,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
        ) {
            if (guardando) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
            } else {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Crear jugador", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
        Spacer(Modifier.height(14.dp))
        Text("* Campos obligatorios · Solo la dirección es opcional", color = textoSecundario, fontSize = 11.sp)
        Spacer(Modifier.height(18.dp))
    }
}

