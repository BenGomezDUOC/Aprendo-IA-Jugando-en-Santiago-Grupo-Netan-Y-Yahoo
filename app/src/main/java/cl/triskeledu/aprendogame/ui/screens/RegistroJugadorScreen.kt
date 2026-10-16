
package cl.triskeledu.aprendogame.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    // Datos del formulario
    var nombre by remember { mutableStateOf("") }
    var alias by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }

    // Avatar elegido
    var avatarSeleccionado by remember { mutableIntStateOf(1) }

    // Indica si se está registrando al jugador
    var guardando by remember { mutableStateOf(false) }

    // Color principal
    val azul = Color(0xFF1765B3)

    // Validación básica del formulario
    val formularioCompleto =
        nombre.isNotBlank() &&
                alias.isNotBlank() &&
                correo.contains("@") &&
                telefono.isNotBlank() &&
                contrasena.length >= 8

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // Título
        Text(
            text = "Crear Nuevo Jugador",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF173B65)
        )

        Text(
            text = "Únete a la comunidad de jugadores",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Nombre completo
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre completo") },
            singleLine = true,
            enabled = !guardando,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        // Alias
        OutlinedTextField(
            value = alias,
            onValueChange = { alias = it },
            label = { Text("Alias") },
            singleLine = true,
            enabled = !guardando,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        // Correo electrónico
        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            singleLine = true,
            enabled = !guardando,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        // Teléfono
        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = { Text("Teléfono") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone
            ),
            singleLine = true,
            enabled = !guardando,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        // Contraseña
        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            singleLine = true,
            enabled = !guardando,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        // Dirección opcional
        OutlinedTextField(
            value = direccion,
            onValueChange = { direccion = it },
            label = { Text("Dirección (opcional)") },
            singleLine = true,
            enabled = !guardando,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Selección de avatar
        Text(
            text = "Selecciona tu avatar",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth()
        )

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
                val numeroAvatar = indice + 1

                Image(
                    painter = painterResource(id = imagen),
                    contentDescription = "Avatar $numeroAvatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(82.dp)
                        .border(
                            width = if (
                                avatarSeleccionado == numeroAvatar
                            ) 4.dp else 1.dp,
                            color = if (
                                avatarSeleccionado == numeroAvatar
                            ) azul else Color.LightGray,
                            shape = CircleShape
                        )
                        .clickable(enabled = !guardando) {
                            avatarSeleccionado = numeroAvatar
                        }
                )
            }
        }

        Text(
            text = "Avatar seleccionado: $avatarSeleccionado",
            fontSize = 13.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Crear jugador
        Button(
            onClick = {
                if (!guardando && formularioCompleto) {
                    // Bloquea nuevos intentos de registro
                    guardando = true

                    // Crea el objeto con los datos del formulario
                    val nuevoJugador = Usuario(
                        nombre = nombre.trim(),
                        alias = alias.trim(),
                        correo = correo.trim().lowercase(),
                        telefono = telefono.trim(),
                        contrasena = contrasena,
                        direccion = direccion.trim(),
                        avatar = avatarSeleccionado
                    )

                    // Envía los datos y espera el resultado
                    onCrearCuenta(nuevoJugador) { registroExitoso ->
                        if (!registroExitoso) {
                            // Permite volver a intentar si hubo un error
                            guardando = false
                        }
                    }
                }
            },
            enabled = formularioCompleto && !guardando,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = azul
            )
        ) {
            if (guardando) {
                Text(
                    text = "Guardando jugador...",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "Crear Jugador",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text(
            text = "Al registrarte aceptas nuestros Términos y Condiciones",
            fontSize = 11.sp,
            color = Color.Gray
        )
    }
}
