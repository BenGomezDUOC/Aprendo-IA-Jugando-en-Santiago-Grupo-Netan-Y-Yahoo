
package cl.triskeledu.aprendogame.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
fun RegistroJugador() {

    // Guarda los datos que escribe el jugador
    var nombre by remember { mutableStateOf("") }
    var alias by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    // Guarda el número del avatar seleccionado
    var avatarSeleccionado by remember { mutableStateOf(1) }

    Column(
        modifier = Modifier.padding(24.dp),
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
    }
}

