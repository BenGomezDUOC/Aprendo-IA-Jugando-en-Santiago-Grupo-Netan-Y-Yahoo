package cl.triskeledu.aprendogame.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cl.triskeledu.aprendogame.R

@Composable
fun RegistroJugador(
    // Se ejecuta al presionar "Crear cuenta". Por defecto no hace nada,
    // así las llamadas actuales a RegistroJugador() siguen compilando.
    onCrearCuenta: (String, String, String, Int) -> Unit = { _, _, _, _ -> },
    onIrAlMapaClick: () -> Unit = {},
) {

    // Guarda los datos que escribe el jugador
    var nombre by remember { mutableStateOf("") }
    var alias by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    // Guarda el número del avatar seleccionado
    var avatarSeleccionado by remember { mutableStateOf(1) }

    // El botón solo se activa cuando los campos básicos están completos
    val formularioCompleto = nombre.isNotBlank() && alias.isNotBlank() && contrasena.length >= 4

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState()) // Permite bajar si el teclado tapa el botón
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Campo Nombre
        OutlinedTextField(
            value = nombre,
            onValueChange = { nuevoNombre ->
                nombre = nuevoNombre
            },
            label = {
                Text("Nombre")
            }
        )

        // Campo Alias
        OutlinedTextField(
            value = alias,
            onValueChange = { aliasNuevo ->
                alias = aliasNuevo
            },
            label = {
                Text("Alias")
            }
        )

        // Campo Contraseña
        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasenaNueva ->
                contrasena = contrasenaNueva
            },
            label = {
                Text("Contraseña")
            },
            visualTransformation = PasswordVisualTransformation()
        )

        // Título de selección de avatar
        Text("Selecciona tu avatar")

        // Coloca los tres avatares uno al lado del otro
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Avatar 1
            Image(
                painter = painterResource(id = R.drawable.avatar1),
                contentDescription = "Avatar 1",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .border(
                        width = if (avatarSeleccionado == 1) 3.dp else 0.dp,
                        color = if (avatarSeleccionado == 1)
                            Color.Blue
                        else
                            Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable {
                        avatarSeleccionado = 1
                    }
            )

            // Avatar 2
            Image(
                painter = painterResource(id = R.drawable.avatar2),
                contentDescription = "Avatar 2",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .border(
                        width = if (avatarSeleccionado == 2) 3.dp else 0.dp,
                        color = if (avatarSeleccionado == 2)
                            Color.Blue
                        else
                            Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable {
                        avatarSeleccionado = 2
                    }
            )

            // Avatar 3
            Image(
                painter = painterResource(id = R.drawable.avatar3),
                contentDescription = "Avatar 3",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .border(
                        width = if (avatarSeleccionado == 3) 3.dp else 0.dp,
                        color = if (avatarSeleccionado == 3)
                            Color.Blue
                        else
                            Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable {
                        avatarSeleccionado = 3
                    }
            )
        }

        // Muestra el número del avatar que eligió el jugador
        Text(
            text = "Avatar seleccionado: $avatarSeleccionado"
        )

        // Botón para crear la cuenta (desactivado hasta completar los campos)
        Button(
            onClick = {
                onCrearCuenta(nombre.trim(), alias.trim(), contrasena, avatarSeleccionado)
            },
            enabled = formularioCompleto,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text("Crear cuenta")
        }

        // BOTÓN TEMPORAL: Probar mapa directamente (sin tener que llenar datos)
        androidx.compose.material3.OutlinedButton(
            onClick = { onIrAlMapaClick() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("🗺️ [Prueba] Entrar directo al Mapa")
        }
    }
}