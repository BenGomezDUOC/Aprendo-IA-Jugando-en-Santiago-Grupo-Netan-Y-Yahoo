
package cl.triskeledu.aprendogame.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import cl.triskeledu.aprendogame.data.local.AppDatabase
import cl.triskeledu.aprendogame.data.local.CuentasPredefinidas
import cl.triskeledu.aprendogame.model.Capsula
import cl.triskeledu.aprendogame.model.Rol
import cl.triskeledu.aprendogame.security.PasswordUtils
import cl.triskeledu.aprendogame.ui.components.BarraNavegacion
import cl.triskeledu.aprendogame.ui.screens.AdministradorScreen
import cl.triskeledu.aprendogame.ui.screens.ContenidoScreen
import cl.triskeledu.aprendogame.ui.screens.DashboardScreen
import cl.triskeledu.aprendogame.ui.screens.EscanerQRScreen
import cl.triskeledu.aprendogame.ui.screens.InicioScreen
import cl.triskeledu.aprendogame.ui.screens.MapaScreen
import cl.triskeledu.aprendogame.ui.screens.RegistroJugador
import cl.triskeledu.aprendogame.ui.screens.ResultadoQRScreen
import cl.triskeledu.aprendogame.ui.screens.SeleccionJugadorScreen
import cl.triskeledu.aprendogame.ui.screens.SelfieTuristicaScreen
import cl.triskeledu.aprendogame.ui.screens.VideoEducativoScreen
import cl.triskeledu.aprendogame.viewmodel.GameViewModel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AppNavigation() {

    // Controla la navegación entre las pantallas.
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

    // Prepara las cuentas iniciales.
    LaunchedEffect(database) {
        withContext(Dispatchers.IO) {
            usuarioDao.eliminarUsuarioInicial()
            CuentasPredefinidas.crearSiNoExisten(database)
        }
    }

    // Usuarios registrados.
    val jugadoresFlow = remember(usuarioDao) {
        usuarioDao.obtenerTodosLosUsuarios()
    }

    // Permite ejecutar operaciones asíncronas.
    val scope = rememberCoroutineScope()

    // ID del jugador que inició sesión.
    var jugadorActivoId by rememberSaveable {
        mutableStateOf<Int?>(null)
    }

    // ViewModel compartido.
    val gameViewModel: GameViewModel = viewModel()

    // Cápsula descubierta al escanear el QR.
    var resultadoCapsula by remember {
        mutableStateOf<Capsula?>(null)
    }

    // Puntos obtenidos por el QR.
    var resultadoPuntos by remember {
        mutableIntStateOf(0)
    }

    // Mantiene sincronizado el jugador activo.
    LaunchedEffect(jugadorActivoId) {

        val id = jugadorActivoId

        if (id == null) {
            gameViewModel.cerrarSesion()
        } else {
            gameViewModel.establecerUsuarioActivo(id)
        }
    }

    // --------------------------------------------------
    // NAVEGACIÓN INFERIOR
    // --------------------------------------------------

    val entradaActual by navController.currentBackStackEntryAsState()

    val rutaActual = entradaActual?.destination?.route

    val rutasJugador = listOf(
        "cliente",
        "mapa",
        "escanear_qr",
        "coleccion",
        "perfil"
    )

    Scaffold(
        bottomBar = {

            if (
                jugadorActivoId != null &&
                rutaActual in rutasJugador
            ) {

                BarraNavegacion(
                    rutaActual = rutaActual ?: "cliente",

                    onNavegar = { ruta ->

                        if (
                            ruta == "coleccion" ||
                            ruta == "perfil"
                        ) {

                            Toast.makeText(
                                context,
                                "Esta pantalla estará disponible próximamente",
                                Toast.LENGTH_SHORT
                            ).show()

                        } else if (ruta != rutaActual) {

                            navController.navigate(ruta) {
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        }
    ) { paddingInterno ->

        NavHost(
            navController = navController,
            startDestination = "inicio",
            modifier = Modifier.padding(paddingInterno)
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

                                    val correoExistente =
                                        usuarioDao.contarPorCorreo(
                                            nuevoJugador.correo
                                        )

                                    if (correoExistente > 0) {

                                        null

                                    } else {

                                        val contrasenaProtegida =
                                            PasswordUtils.crearHash(
                                                nuevoJugador.contrasena
                                            )

                                        val jugadorSeguro =
                                            nuevoJugador.copy(
                                                contrasena = contrasenaProtegida
                                            )

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

                                    jugadorActivoId = null

                                    Toast.makeText(
                                        context,
                                        "¡Jugador registrado correctamente!",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    onResultado(true)

                                    navController.navigate(
                                        "seleccion_jugador"
                                    )
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

                                val usuarioActual =
                                    withContext(Dispatchers.IO) {
                                        usuarioDao.getUsuarioDirecto(
                                            jugador.id
                                        )
                                    }

                                val contrasenaCorrecta =
                                    withContext(Dispatchers.Default) {

                                        usuarioActual != null &&
                                                PasswordUtils.verificarContrasena(
                                                    contrasena = contrasena,
                                                    hashGuardado =
                                                        usuarioActual.contrasena
                                                )
                                    }

                                if (
                                    contrasenaCorrecta &&
                                    usuarioActual != null
                                ) {

                                    jugadorActivoId = usuarioActual.id

                                    Toast.makeText(
                                        context,
                                        "¡Bienvenido, ${usuarioActual.nombre}!",
                                        Toast.LENGTH_SHORT
                                    ).show()

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
            // PANTALLA 4: DASHBOARD DEL JUGADOR
            // --------------------------------------------------

            composable("cliente") {

                if (jugadorActivoId == null) {

                    LaunchedEffect(Unit) {

                        navController.navigate("seleccion_jugador") {
                            popUpTo("cliente") {
                                inclusive = true
                            }
                        }
                    }

                } else {

                    DashboardScreen(

                        gameViewModel = gameViewModel,

                        onMapaClick = {
                            navController.navigate("mapa") {
                                launchSingleTop = true
                            }
                        },

                        onEscanerClick = {
                            navController.navigate("escanear_qr") {
                                launchSingleTop = true
                            }
                        },

                        onColeccionClick = {

                            Toast.makeText(
                                context,
                                "La colección estará disponible próximamente",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }

            // --------------------------------------------------
            // PANTALLA: MAPA DEL JUGADOR
            // --------------------------------------------------

            composable("mapa") {

                if (jugadorActivoId == null) {

                    LaunchedEffect(Unit) {

                        navController.navigate("seleccion_jugador") {
                            popUpTo("mapa") {
                                inclusive = true
                            }
                        }
                    }

                } else {

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

                    EscanerQRScreen(

                        gameViewModel = gameViewModel,

                        onResultadoQr = { capsula, puntos ->

                            resultadoCapsula = capsula

                            resultadoPuntos = puntos

                            navController.navigate("resultado_qr")
                        }
                    )
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

            // --------------------------------------------------
            // PANTALLA 8: RESULTADO DEL QR
            // --------------------------------------------------

            composable("resultado_qr") {

                val capsula = resultadoCapsula

                if (jugadorActivoId == null) {

                    LaunchedEffect(Unit) {

                        navController.navigate("seleccion_jugador") {
                            popUpTo("resultado_qr") {
                                inclusive = true
                            }
                        }
                    }

                } else if (capsula == null) {

                    LaunchedEffect(Unit) {

                        navController.navigate("escanear_qr") {
                            popUpTo("resultado_qr") {
                                inclusive = true
                            }
                        }
                    }

                } else {

                    ResultadoQRScreen(

                        capsula = capsula,

                        puntosGanados = resultadoPuntos,

                        // Vuelve al mapa.
                        onVolverMapa = {

                            navController.navigate("mapa") {
                                popUpTo("resultado_qr") {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        },

                        // Abre la pantalla 10.
                        onTomarSelfie = {
                            navController.navigate("selfie_turistica")
                        },
                        // Abre la pantalla 9
                        onVerVideo = {
                            navController.navigate("video_educativo")
                        }
                    )
                }
            }


            // --------------------------------------------------
            // PANTALLA 9: VIDEO EDUCATIVO
            // --------------------------------------------------

            composable("video_educativo") {

                val capsula = resultadoCapsula
                val idJugador = jugadorActivoId

                // Consulta los videos vistos del jugador.
                val videosVistos by gameViewModel.videosVistos
                    .collectAsState()

                if (idJugador == null || capsula == null) {

                    // Si no hay jugador o cápsula,
                    // regresa al inicio del jugador.
                    LaunchedEffect(Unit) {
                        navController.navigate("cliente") {
                            popUpTo("video_educativo") {
                                inclusive = true
                            }
                        }
                    }

                } else {

                    // Comprueba si el jugador ya vio esta cápsula.
                    val yaVisto = videosVistos.any {
                        it.capsulaId == capsula.id
                    }

                    VideoEducativoScreen(
                        capsula = capsula,
                        yaVisto = yaVisto,

                        // Registra el video cuando termina.
                        onVideoTerminado = {
                            gameViewModel.registrarVideoVisto(
                                capsula.id
                            )
                        },

                        // Regresa a la pantalla anterior.
                        onVolverColeccion = {
                            navController.popBackStack()
                        }
                    )
                }
            }


            // --------------------------------------------------
            // PANTALLA 10: SELFIE TURÍSTICA
            // --------------------------------------------------

            composable("selfie_turistica") {

                val capsula = resultadoCapsula
                val idJugador = jugadorActivoId

                if (idJugador == null || capsula == null) {

                    LaunchedEffect(Unit) {

                        navController.navigate("cliente") {
                            popUpTo("selfie_turistica") {
                                inclusive = true
                            }
                        }
                    }

                } else {

                    SelfieTuristicaScreen(

                        // Vuelve al resultado del QR.
                        onVolver = {
                            navController.popBackStack()
                        },

                        // Recibe la ruta de la fotografía.

                        onFotoGuardada = { ruta, informarResultado ->

                            gameViewModel.validarYRegistrarSelfie(
                                rutaFoto = ruta,
                                codigoQrEsperado = capsula.codigoQr,
                                puntoId = capsula.puntoId,
                                capsulaId = capsula.id,

                                onResultado = { exito, mensaje ->
                                    informarResultado(exito, mensaje)
                                }
                            )
                        }

                    )
                }
            }

        } // Cierra NavHost

    } // Cierra Scaffold

} // Cierra AppNavigation






