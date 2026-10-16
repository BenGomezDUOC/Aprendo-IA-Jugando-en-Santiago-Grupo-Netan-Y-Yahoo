
package cl.triskeledu.aprendogame.navigation

import android.widget.Toast

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext

import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import cl.triskeledu.aprendogame.data.local.AppDatabase
import cl.triskeledu.aprendogame.data.local.CuentasPredefinidas
import cl.triskeledu.aprendogame.model.Rol
import cl.triskeledu.aprendogame.security.PasswordUtils

import cl.triskeledu.aprendogame.ui.screens.AdministradorScreen
import cl.triskeledu.aprendogame.ui.screens.ContenidoScreen
import cl.triskeledu.aprendogame.ui.screens.EscanerQRScreen
import cl.triskeledu.aprendogame.ui.screens.InicioScreen
import cl.triskeledu.aprendogame.ui.screens.MapaScreen
import cl.triskeledu.aprendogame.ui.screens.RegistroJugador
import cl.triskeledu.aprendogame.ui.screens.SeleccionJugadorScreen

import cl.triskeledu.aprendogame.viewmodel.GameViewModel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Navegación principal de Triskeledu.
 *
 * Administra:
 * 1. Inicio.
 * 2. Registro de jugadores.
 * 3. Selección e inicio de sesión.
 * 4. Acceso según el rol del usuario.
 * 5. Identificación del jugador activo en el mapa.
 */
@Composable
fun AppNavigation() {

    // Controla la navegación entre pantallas.
    val navController = rememberNavController()

    // Contexto de Android.
    val context = LocalContext.current

    // Base de datos Room.
    val database = remember(context) {
        AppDatabase.getInstance(context)
    }

    // DAO de usuarios.
    val usuarioDao = remember(database) {
        database.usuarioDao()
    }

    // Elimina el usuario inicial de demostración.
    // Conserva las cuentas reales y crea las cuentas predefinidas.
    LaunchedEffect(database) {
        withContext(Dispatchers.IO) {
            usuarioDao.eliminarUsuarioInicial()
            CuentasPredefinidas.crearSiNoExisten(database)
        }
    }

    // Obtiene todos los usuarios registrados.
    val jugadoresFlow = remember(usuarioDao) {
        usuarioDao.obtenerTodosLosUsuarios()
    }

    // Permite ejecutar operaciones de Room sin bloquear la interfaz.
    val scope = rememberCoroutineScope()

    // Guarda el ID del usuario que inició sesión.
    // Se conserva ante determinadas recreaciones de la pantalla.
    var jugadorActivoId by rememberSaveable {
        mutableStateOf<Int?>(null)
    }

    // --------------------------------------------------
    // NAVEGACIÓN
    // --------------------------------------------------

    NavHost(
        navController = navController,
        startDestination = "inicio"
    ) {

        // --------------------------------------------------
        // PANTALLA 1: INICIO
        // --------------------------------------------------

        composable("inicio") {

            InicioScreen(
                onComenzarClick = {
                    navController.navigate("registro")
                },

                onCuentaClick = {
                    navController.navigate("seleccion_jugador")
                }
            )
        }

        // --------------------------------------------------
        // PANTALLA 2: REGISTRO DE JUGADOR
        // --------------------------------------------------

        composable("registro") {

            RegistroJugador(

                onCrearCuenta = { nuevoJugador, onResultado ->

                    scope.launch {

                        try {

                            val resultado = withContext(Dispatchers.IO) {

                                // Verifica que el correo no esté registrado.
                                val correoExistente =
                                    usuarioDao.contarPorCorreo(
                                        nuevoJugador.correo
                                    )

                                if (correoExistente > 0) {

                                    null

                                } else {

                                    // Protege la contraseña.
                                    val contrasenaProtegida =
                                        PasswordUtils.crearHash(
                                            nuevoJugador.contrasena
                                        )

                                    // Guarda la contraseña protegida.
                                    val jugadorSeguro = nuevoJugador.copy(
                                        contrasena = contrasenaProtegida
                                    )

                                    // Registra el nuevo jugador.
                                    usuarioDao.registrarJugador(
                                        jugadorSeguro
                                    )
                                }
                            }

                            if (resultado == null) {

                                Toast.makeText(
                                    context,
                                    "Este correo ya está registrado",
                                    Toast.LENGTH_LONG
                                ).show()

                                onResultado(false)

                            } else {

                                // El registro no inicia sesión.
                                jugadorActivoId = null

                                Toast.makeText(
                                    context,
                                    "¡Jugador registrado correctamente!",
                                    Toast.LENGTH_LONG
                                ).show()

                                onResultado(true)

                                // Solicita iniciar sesión.
                                navController.navigate("seleccion_jugador")
                            }

                        } catch (e: Exception) {

                            Toast.makeText(
                                context,
                                "No se pudo registrar el jugador",
                                Toast.LENGTH_LONG
                            ).show()

                            onResultado(false)
                        }
                    }
                }
            )
        }

        // --------------------------------------------------
        // PANTALLA 3: SELECCIÓN E INICIO DE SESIÓN
        // --------------------------------------------------

        composable("seleccion_jugador") {

            SeleccionJugadorScreen(

                jugadoresFlow = jugadoresFlow,

                onIniciarSesion = { jugador, contrasena ->

                    scope.launch {

                        try {

                            // Consulta nuevamente el usuario en Room.
                            val usuarioActual = withContext(Dispatchers.IO) {
                                usuarioDao.getUsuarioDirecto(jugador.id)
                            }

                            // Verifica la contraseña.
                            val contrasenaCorrecta =
                                withContext(Dispatchers.Default) {

                                    usuarioActual != null &&
                                            PasswordUtils.verificarContrasena(
                                                contrasena = contrasena,
                                                hashGuardado = usuarioActual.contrasena
                                            )
                                }

                            if (
                                contrasenaCorrecta &&
                                usuarioActual != null
                            ) {

                                // Guarda el ID del usuario autenticado.
                                jugadorActivoId = usuarioActual.id

                                Toast.makeText(
                                    context,
                                    "¡Bienvenido, ${usuarioActual.nombre}!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // Determina el destino según su rol.
                                val destino = when (usuarioActual.rol) {

                                    Rol.CLIENTE -> "cliente"

                                    Rol.GENERADOR -> "contenido"

                                    Rol.ADMINISTRADOR -> "administrador"
                                }

                                navController.navigate(destino)

                            } else {

                                jugadorActivoId = null

                                Toast.makeText(
                                    context,
                                    "Contraseña incorrecta",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                        } catch (e: Exception) {

                            jugadorActivoId = null

                            Toast.makeText(
                                context,
                                "No se pudo iniciar sesión",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                },

                onCrearJugador = {
                    navController.navigate("registro")
                }
            )
        }

        // --------------------------------------------------
        // PANTALLA: MAPA DEL JUGADOR
        // --------------------------------------------------

        composable("cliente") {

            val idJugador = jugadorActivoId

            if (idJugador == null) {

                // Impide entrar al mapa sin un jugador autenticado.
                LaunchedEffect(Unit) {
                    navController.navigate("seleccion_jugador") {
                        popUpTo("cliente") {
                            inclusive = true
                        }
                    }
                }

            } else {

                // Obtiene el ViewModel asociado a esta pantalla.
                val gameViewModel: GameViewModel = viewModel()

                // Conecta el ID autenticado con el ViewModel.
                LaunchedEffect(idJugador) {
                    gameViewModel.establecerUsuarioActivo(idJugador)
                }

                // Muestra el mapa del jugador.
                MapaScreen(
                    onEscanearQrClick = { _ ->
                        navController.navigate("escanear_qr")
                    }
                )
            }
        }

        // --------------------------------------------------
        // PANTALLA: ESCÁNER QR
        // --------------------------------------------------

        composable("escanear_qr") {

            if (jugadorActivoId == null) {

                LaunchedEffect(Unit) {
                    navController.navigate("seleccion_jugador") {
                        popUpTo("escanear_qr") {
                            inclusive = true
                        }
                    }
                }

            } else {

                EscanerQRScreen()
            }
        }

        // --------------------------------------------------
        // PANTALLA: ADMINISTRADOR
        // --------------------------------------------------

        composable("administrador") {

            AdministradorScreen()
        }

        // --------------------------------------------------
        // PANTALLA: GENERADOR DE CONTENIDO
        // --------------------------------------------------

        composable("contenido") {

            ContenidoScreen()
        }
    }
}




