package cl.triskeledu.aprendogame.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.triskeledu.aprendogame.R
import cl.triskeledu.aprendogame.model.Usuario

@Composable
fun RegistroJugador(
    onCrearCuenta: (Usuario, (Boolean) -> Unit) -> Unit = { _, _ -> }
) {
    // Datos esenciales para iniciar el juego
    var nombre by remember { mutableStateOf("") }
    var alias by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var avatarSeleccionado by remember { mutableIntStateOf(1) }
    var guardando by remember { mutableStateOf(false) }

    val azulPrincipal = Color(0xFF2563EB)

    val formularioValido = nombre.trim().isNotBlank() &&
            alias.trim().isNotBlank() &&
            contrasena.length >= 4

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Crear Cuenta de Jugador",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Text(
            text = "Configura tu perfil para comenzar a explorar",
            fontSize = 14.sp,
            color = Color(0xFF64748B)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Selección de Avatar
        Text(
            text = "Elige tu avatar",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF334155),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val avatares = listOf(
                R.drawable.avatar1,
                R.drawable.avatar2,
                R.drawable.avatar3
            )

            avatares.forEachIndexed { indice, imagen ->
                val numero = indice + 1
                val seleccionado = avatarSeleccionado == numero

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(
                            width = if (seleccionado) 3.5.dp else 1.dp,
                            color = if (seleccionado) azulPrincipal else Color(0xFFCBD5E1),
                            shape = CircleShape
                        )
                        .clickable(enabled = !guardando) {
                            avatarSeleccionado = numero
                        }
                ) {
                    Image(
                        painter = painterResource(id = imagen),
                        contentDescription = "Avatar $numero",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Campos del Formulario
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF64748B))
            },
            singleLine = true,
            enabled = !guardando,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = alias,
            onValueChange = { alias = it },
            label = { Text("Alias / Nombre de usuario") },
            singleLine = true,
            enabled = !guardando,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña (mínimo 4 caracteres)") },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF64748B))
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            enabled = !guardando,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (!guardando && formularioValido) {
                    guardando = true
                    // Genera un identificador de correo único interno para Room
                    val correoInterno = "${alias.trim().lowercase()}@aprendogame.local"
                    val nuevoJugador = Usuario(
                        nombre = nombre.trim(),
                        alias = alias.trim(),
                        correo = correoInterno,
                        contrasena = contrasena,
                        avatar = avatarSeleccionado
                    )
                    onCrearCuenta(nuevoJugador) { exito ->
                        if (!exito) {
                            guardando = false
                        }
                    }
                }
            },
            enabled = formularioValido && !guardando,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = azulPrincipal)
        ) {
            if (guardando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.5.dp
                )
            } else {
                Text(
                    text = "Comenzar a Jugar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
