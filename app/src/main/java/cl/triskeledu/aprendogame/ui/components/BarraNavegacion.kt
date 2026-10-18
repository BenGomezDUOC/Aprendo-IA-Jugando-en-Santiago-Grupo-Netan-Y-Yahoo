package cl.triskeledu.aprendogame.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector


import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import cl.triskeledu.aprendogame.ui.components.BarraNavegacion

// Representa una opción de la barra inferior.
data class OpcionNavegacion(
    val titulo: String,
    val ruta: String,
    val icono: ImageVector
)

@Composable
fun BarraNavegacion(
    rutaActual: String,
    onNavegar: (String) -> Unit
) {

    // Las cinco opciones de navegación del jugador.
    val opciones = listOf(
        OpcionNavegacion("Inicio", "cliente", Icons.Filled.Home),
        OpcionNavegacion("Mapa", "mapa", Icons.Filled.Map),
        OpcionNavegacion("Escáner", "escanear_qr", Icons.Filled.QrCodeScanner),
        OpcionNavegacion("Colección", "coleccion", Icons.Filled.CollectionsBookmark),
        OpcionNavegacion("Perfil", "perfil", Icons.Filled.Person)
    )

    NavigationBar {

        // Recorre las opciones para crear un botón por cada una.
        opciones.forEach { opcion ->

            NavigationBarItem(
                selected = rutaActual == opcion.ruta,

                onClick = {
                    onNavegar(opcion.ruta)
                },

                icon = {
                    Icon(
                        imageVector = opcion.icono,
                        contentDescription = opcion.titulo
                    )
                },

                label = {
                    Text(opcion.titulo)
                }
            )
        }
    }
}