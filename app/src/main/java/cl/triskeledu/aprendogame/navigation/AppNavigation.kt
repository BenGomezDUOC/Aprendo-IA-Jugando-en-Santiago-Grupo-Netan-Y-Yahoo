
package cl.triskeledu.aprendogame.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.triskeledu.aprendogame.ui.screens.AdministradorScreen
import cl.triskeledu.aprendogame.ui.screens.ContenidoScreen
import cl.triskeledu.aprendogame.ui.screens.EscanerQRScreen
import cl.triskeledu.aprendogame.ui.screens.InicioScreen
import cl.triskeledu.aprendogame.ui.screens.MapaScreen
import cl.triskeledu.aprendogame.ui.screens.RegistroJugador

@Composable

fun AppNavigation(){
    val navController = rememberNavController()

    NavHost( //Contiene las rutas disponibles (el nombre o direccion de cada pantalla, define que pantalla existen)
        navController = navController,
        startDestination = "inicio" //Define que pantalla aparece primero(la ruta)
    ){
        //Declaracion de las pantallas

        composable ("inicio"){ //Ruta o apodo de la pantalla
            InicioScreen( //Pantalla real lo que se dibuja
                onComenzarClick = {
                    // 2. Recibe el aviso
                    // 3. Le dice al controlador que navegue a la ruta "cliente"
                    navController.navigate("registro")
                },
                onCuentaClick = {
                    navController.navigate("cliente")
                }
            )
        }

        composable("registro"){ //Ruta o apodo de la pantalla
            RegistroJugador(
                onCrearCuenta = { _, _, _, _ ->
                    navController.navigate("cliente")
                },
                onIrAlMapaClick = {
                    navController.navigate("cliente")
                }
            )
        }

        // Pantalla Principal del Cliente: Mapa y Geolocalización (Módulo Fernando)
        composable("cliente"){
            MapaScreen(
                onEscanearQrClick = { punto ->
                    navController.navigate("escanear_qr")
                }
            )
        }

        composable("escanear_qr"){
            EscanerQRScreen()
        }

        composable ("administrador"){
            AdministradorScreen()
        }

        composable ("contenido"){
            ContenidoScreen()
        }
    }
}


