package cl.triskeledu.aprendogame.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.triskeledu.aprendogame.ui.screens.JuegoApp
import cl.triskeledu.aprendogame.ui.screens.InicioScreen
import cl.triskeledu.aprendogame.ui.screens.AdministradorScreen
import cl.triskeledu.aprendogame.ui.screens.ContenidoScreen

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
                onClienteClick = {
                    // 2. Recibe el aviso
                    // 3. Le dice al controlador que navegue a la ruta "cliente"
                    navController.navigate("cliente")
                },
                onAdministradorClick = {
                    navController.navigate("administrador")
                },
                onContenidoClick = {
                    navController.navigate("contenido")
                }
            )
        }

        composable("cliente"){//Ruta o apodo de la pantalla
            JuegoApp() //Pantalla real lo que se dibuja
        }

        composable ("administrador"){
            AdministradorScreen()
        }

        composable ("contenido"){
            ContenidoScreen()
        }
    }
}
