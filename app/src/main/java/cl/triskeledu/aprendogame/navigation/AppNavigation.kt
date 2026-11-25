package cl.triskeledu.aprendogame.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import cl.triskeledu.aprendogame.data.local.AppDatabase
import cl.triskeledu.aprendogame.data.local.CuentasPredefinidas
import cl.triskeledu.aprendogame.data.local.SessionManager
import cl.triskeledu.aprendogame.model.Capsula
import cl.triskeledu.aprendogame.model.VideoRegistrado
import cl.triskeledu.aprendogame.model.Rol
import cl.triskeledu.aprendogame.security.PasswordUtils
import cl.triskeledu.aprendogame.data.repository.RemoteBackendRepository
import cl.triskeledu.aprendogame.data.remote.dto.RegistroBackendRequestDto
import cl.triskeledu.aprendogame.ui.components.BarraNavegacion
import cl.triskeledu.aprendogame.ui.screens.*
import cl.triskeledu.aprendogame.viewmodel.ColeccionViewModel
import cl.triskeledu.aprendogame.viewmodel.EscanerQRViewModel
import cl.triskeledu.aprendogame.viewmodel.MapaViewModel
import cl.triskeledu.aprendogame.viewmodel.NotificacionesViewModel
import cl.triskeledu.aprendogame.viewmodel.SelfieTuristicaViewModel
import cl.triskeledu.aprendogame.viewmodel.SessionViewModel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    val context = LocalContext.current

    // Base de datos Room.
    val database = remember(context) {
        AppDatabase.getInstance(context)
    }

    val usuarioDao = remember(database) {
        database.usuarioDao()
    }

    // Prepara las cuentas iniciales sin duplicarlas.
    LaunchedEffect(database) {
        withContext(Dispatchers.IO) {
            usuarioDao.eliminarUsuarioInicial()
            CuentasPredefinidas.crearSiNoExisten(database)
        }
    }

    val jugadoresFlow = remember(usuarioDao) {
        usuarioDao.obtenerTodosLosUsuarios()
    }

    val scope = rememberCoroutineScope()
    val remoteBackendRepository = remember { RemoteBackendRepository() }

    val sessionManager = remember(context) {
        SessionManager(context.applicationContext)
    }

    // Recupera el usuario de la sesión persistida.
    var jugadorActivoId by rememberSaveable {
        mutableStateOf<Int?>(
            sessionManager.obtenerUsuarioActivo()
        )
    }

    // ViewModels especializados por área funcional.
    val sessionViewModel: SessionViewModel = viewModel()
    val coleccionViewModel: ColeccionViewModel = viewModel()
    val mapaViewModel: MapaViewModel = viewModel()
    val escanerViewModel: EscanerQRViewModel = viewModel()
    val notificacionesViewModel: NotificacionesViewModel = viewModel()
    val selfieViewModel: SelfieTuristicaViewModel = viewModel()

    // Comprueba la validez de la sesión persistida.
    var sesionInicializada by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {

        val guardadoId = sessionManager.obtenerUsuarioActivo()

        if (guardadoId != null) {

            val existe = withContext(Dispatchers.IO) {
                usuarioDao.getUsuarioDirecto(guardadoId)
            }

            if (existe != null) {

                jugadorActivoId = existe.id

                sessionViewModel.setUsuarioActivo(existe.id)
                coleccionViewModel.setUsuarioActivo(existe.id)
                escanerViewModel.setUsuarioActivo(existe.id)
                notificacionesViewModel.setUsuarioActivo(existe.id)
                selfieViewModel.setUsuarioActivo(existe.id)

            } else {

                sessionManager.cerrarSesion()

                jugadorActivoId = null

                sessionViewModel.cerrarSesion()
                coleccionViewModel.setUsuarioActivo(null)
                escanerViewModel.setUsuarioActivo(null)
                notificacionesViewModel.setUsuarioActivo(null)
                selfieViewModel.setUsuarioActivo(null)
            }
        }

        sesionInicializada = true
    }

    // ID del punto elegido desde el mapa.
    var puntoDetalleId by rememberSaveable {
        mutableStateOf<Int?>(null)
    }

    val usuarioActual by sessionViewModel.usuario.collectAsState()

    // Cápsula descubierta al escanear un QR.
    var resultadoCapsula by remember {
        mutableStateOf<Capsula?>(null)
    }

    var resultadoPuntos by remember {
        mutableIntStateOf(0)
    }

    // Video adicional seleccionado por el cliente.
    var videoAdicionalSeleccionado by remember {
        mutableStateOf<Int?>(null)
    }

    // Video administrativo seleccionado.
    var videoRegistradoSeleccionado by remember {
        mutableStateOf<VideoRegistrado?>(null)
    }

    // Videos seleccionados por el personal.
    var capsulaPersonalSeleccionada by remember {
        mutableStateOf<Capsula?>(null)
    }

    var adicionalPersonalSeleccionado by remember {
        mutableStateOf<Int?>(null)
    }

    var tipoVideoPersonal by remember {
        mutableStateOf("registrado")
    }


    // --------------------------------------------------
    // SINCRONIZACIÓN DEL USUARIO ACTIVO
    // --------------------------------------------------

    LaunchedEffect(jugadorActivoId) {

        val id = jugadorActivoId

        if (id == null) {

            sessionViewModel.cerrarSesion()
            coleccionViewModel.setUsuarioActivo(null)
            escanerViewModel.setUsuarioActivo(null)
            notificacionesViewModel.setUsuarioActivo(null)
            selfieViewModel.setUsuarioActivo(null)

        } else {

            sessionViewModel.setUsuarioActivo(id)
            coleccionViewModel.setUsuarioActivo(id)
            escanerViewModel.setUsuarioActivo(id)
            notificacionesViewModel.setUsuarioActivo(id)
            selfieViewModel.setUsuarioActivo(id)
        }
    }


    // --------------------------------------------------
    // RUTAS DEL ESCÁNER QR
    // --------------------------------------------------

    // -1 significa que no existe un punto turístico
    // seleccionado: escáner general.
    val rutaEscanerGeneral = "escanear_qr?puntoId=-1"

    val rutaEscaner = "escanear_qr?puntoId={puntoId}"

    val entradaActual by
    navController.currentBackStackEntryAsState()

    val rutaRegistrada = entradaActual?.destination?.route

    // Mantiene la selección de la barra inferior.
    val rutaActual = if (rutaRegistrada == rutaEscaner) {
        "escanear_qr"
    } else {
        rutaRegistrada
    }

    // Comprueba si el escaneo viene desde un punto.
    val escaneoDesdePunto =
        rutaActual == "escanear_qr" &&
                (
                        entradaActual?.arguments
                            ?.getInt("puntoId", -1) ?: -1
                        ) != -1

    val rutasJugador = listOf(
        "cliente",
        "mapa",
        "detalle_punto",
        "escanear_qr",
        "coleccion",
        "perfil"
    )

    // Solo muestra la barra inferior para el cliente.
    val mostrarBarraJugador =
        jugadorActivoId != null &&
                usuarioActual?.id == jugadorActivoId &&
                usuarioActual?.rol == Rol.CLIENTE &&
                rutaActual in rutasJugador


    // --------------------------------------------------
    // FUNCIONES DE NAVEGACIÓN
    // --------------------------------------------------

    // Abre el escáner general sin asociarlo a un lugar.
    fun abrirEscanerGeneral() {

        if (rutaActual == "escanear_qr" && escaneoDesdePunto) {

            navController.navigate(rutaEscanerGeneral) {

                popUpTo(rutaEscaner) {
                    inclusive = true
                }

                launchSingleTop = true
            }

        } else {

            navController.navigate(rutaEscanerGeneral) {
                launchSingleTop = true
            }
        }
    }

    // Regresa al mapa evitando volver al escáner.
    fun volverAlMapa() {

        if (navController.currentDestination?.route == "mapa") {
            return
        }

        if (!navController.popBackStack("mapa", false)) {

            val rutaOrigen =
                navController.currentDestination?.route

            navController.navigate("mapa") {

                if (rutaOrigen != null) {
                    popUpTo(rutaOrigen) {
                        inclusive = true
                    }
                }

                launchSingleTop = true
            }
        }
    }

    // Regresa a la pantalla correspondiente al rol.
    fun volverDesdePerfil() {

        if (!navController.popBackStack()) {

            val destino = when (usuarioActual?.rol) {

                Rol.ADMINISTRADOR -> "administrador"
                Rol.GENERADOR -> "contenido"
                else -> "cliente"
            }

            navController.navigate(destino) {
                launchSingleTop = true
            }
        }
    }

    // Cierra la sesión de cualquiera de los tres roles.
    fun cerrarSesionActual() {

        sessionManager.cerrarSesion()

        jugadorActivoId = null

        sessionViewModel.cerrarSesion()
        coleccionViewModel.setUsuarioActivo(null)
        escanerViewModel.setUsuarioActivo(null)
        notificacionesViewModel.setUsuarioActivo(null)
        selfieViewModel.setUsuarioActivo(null)

        resultadoCapsula = null
        resultadoPuntos = 0
        puntoDetalleId = null

        navController.navigate("inicio") {

            popUpTo(navController.graph.id) {
                inclusive = true
            }

            launchSingleTop = true
        }
    }


    // ==================================================
    // NAVEGACIÓN PRINCIPAL
    // ==================================================

    Scaffold(

        bottomBar = {

            if (mostrarBarraJugador) {

                BarraNavegacion(

                    rutaActual = rutaActual ?: "cliente",

                    onNavegar = { ruta ->

                        if (ruta == "escanear_qr") {

                            if (
                                ruta != rutaActual ||
                                escaneoDesdePunto
                            ) {
                                abrirEscanerGeneral()
                            }

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


            // ==========================================
            // PANTALLA 1: INICIO
            // ==========================================

            composable("inicio") {

                // Si existe una sesión válida,
                // abre directamente la pantalla del rol.
                LaunchedEffect(
                    sesionInicializada,
                    usuarioActual
                ) {

                    if (
                        sesionInicializada &&
                        jugadorActivoId != null &&
                        usuarioActual != null
                    ) {

                        val destino = when (usuarioActual?.rol) {

                            Rol.CLIENTE -> "cliente"
                            Rol.GENERADOR -> "contenido"
                            Rol.ADMINISTRADOR -> "administrador"
                            null -> "cliente"
                        }

                        navController.navigate(destino) {

                            popUpTo("inicio") {
                                inclusive = true
                            }
                        }
                    }
                }

                InicioScreen(

                    onComenzarClick = {
                        navController.navigate("registro")
                    },

                    onCuentaClick = {
                        navController.navigate(
                            "seleccion_jugador"
                        )
                    }
                )
            }


            // ==========================================
            // PANTALLA 2: REGISTRO
            // ==========================================

            composable("registro") {

                RegistroJugador(

                    onCrearCuenta = { nuevoJugador, onResultado ->

                        scope.launch {

                            try {

                                // Comprueba si el correo existe.
                                val correoExistente =
                                    withContext(Dispatchers.IO) {

                                        usuarioDao.contarPorCorreo(
                                            nuevoJugador.correo
                                        )
                                    }

                                if (correoExistente > 0) {

                                    Toast.makeText(
                                        context,
                                        "Este correo electrónico ya está registrado",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    onResultado(false)

                                    return@launch
                                }

                                // Primero crea la cuenta en MySQL. Si el backend
                                // falla, no genera una cuenta Room sin sincronizar.
                                val registro = remoteBackendRepository.registrar(
                                    RegistroBackendRequestDto(
                                        nombre = nuevoJugador.nombre.trim(),
                                        alias = nuevoJugador.alias.trim(),
                                        correo = nuevoJugador.correo.trim().lowercase(),
                                        contrasena = nuevoJugador.contrasena,
                                        telefono = nuevoJugador.telefono.trim(),
                                        direccion = nuevoJugador.direccion.trim(),
                                        avatar = nuevoJugador.avatar,
                                        rol = "CLIENTE"
                                    )
                                ).getOrThrow()

                                // El registro no devuelve JWT: se obtiene con login.
                                val sesion = remoteBackendRepository.login(
                                    nuevoJugador.correo.trim().lowercase(),
                                    nuevoJugador.contrasena
                                ).getOrThrow()
                                check(sesion.usuario.id == registro.id) {
                                    "La sesión no pertenece a la cuenta registrada"
                                }

                                val nuevoId = withContext(Dispatchers.IO) {
                                    usuarioDao.registrarJugador(
                                        nuevoJugador.copy(
                                            correo = nuevoJugador.correo.trim().lowercase(),
                                            contrasena = PasswordUtils.crearHash(nuevoJugador.contrasena)
                                        )
                                    ).toInt()
                                }

                                sessionManager.guardarSesionBackend(
                                    usuarioLocalId = nuevoId,
                                    usuarioBackendId = sesion.usuario.id,
                                    token = sesion.token,
                                    rol = sesion.usuario.rol
                                )
                                jugadorActivoId = nuevoId
                                onResultado(true)
                                Toast.makeText(context, "¡Bienvenido, ${nuevoJugador.nombre}!", Toast.LENGTH_SHORT).show()
                                navController.navigate("cliente") {
                                    popUpTo("registro") { inclusive = true }
                                    launchSingleTop = true
                                }

                            } catch (e: Exception) {

                                Toast.makeText(
                                    context,
                                    "Registro fallido: ${e.message ?: "verifica el backend"}",
                                    Toast.LENGTH_LONG
                                ).show()

                                onResultado(false)
                            }
                        }
                    }
                )
            }


            // ==========================================
            // PANTALLA 3: INICIO DE SESIÓN
            // ==========================================

            composable("seleccion_jugador") {

                SeleccionJugadorScreen(

                    jugadoresFlow = jugadoresFlow,

                    onIniciarSesion = {
                            jugador,
                            contrasena,
                            onResultado ->

                        scope.launch {

                            try {

                                val usuarioEncontrado =
                                    withContext(Dispatchers.IO) {

                                        usuarioDao.getUsuarioDirecto(
                                            jugador.id
                                        )
                                    }

                                val contrasenaLimpia =
                                    contrasena.trim()

                                val contrasenaCorrecta =
                                    withContext(Dispatchers.Default) {

                                        if (usuarioEncontrado == null) {

                                            false

                                        } else {

                                            PasswordUtils.verificarContrasena(
                                                contrasena = contrasenaLimpia,
                                                hashGuardado =
                                                    usuarioEncontrado.contrasena
                                            )
                                        }
                                    }

                                if (
                                    contrasenaCorrecta &&
                                    usuarioEncontrado != null
                                ) {

                                    // Las cuentas predefinidas funcionan con
                                    // credenciales locales; los demás usuarios
                                    // deben autenticarse también en Spring Boot.
                                        val loginResult = remoteBackendRepository.login(
                                            usuarioEncontrado.correo.trim().lowercase(),
                                            contrasenaLimpia
                                        )
                                        val sesion = if (loginResult.isSuccess) {
                                            loginResult.getOrThrow()
                                        } else {
                                            // Si la cuenta existía solo localmente (creada antes del backend),
                                            // la sincronizamos automáticamente en MySQL y obtenemos la sesión.
                                            remoteBackendRepository.registrar(
                                                RegistroBackendRequestDto(
                                                    nombre = usuarioEncontrado.nombre.trim(),
                                                    alias = usuarioEncontrado.alias.trim(),
                                                    correo = usuarioEncontrado.correo.trim().lowercase(),
                                                    contrasena = contrasenaLimpia,
                                                    telefono = usuarioEncontrado.telefono.trim().ifEmpty { "912345678" },
                                                    direccion = usuarioEncontrado.direccion.trim(),
                                                    avatar = usuarioEncontrado.avatar,
                                                    rol = usuarioEncontrado.rol.name
                                                )
                                            )
                                            remoteBackendRepository.login(
                                                usuarioEncontrado.correo.trim().lowercase(),
                                                contrasenaLimpia
                                            ).getOrThrow()
                                        }

                                        check(sesion.usuario.rol == usuarioEncontrado.rol.name) {
                                            "El rol del backend no coincide con el usuario local"
                                        }

                                        sessionManager.guardarSesionBackend(
                                            usuarioLocalId = usuarioEncontrado.id,
                                            usuarioBackendId = sesion.usuario.id,
                                            token = sesion.token,
                                            rol = sesion.usuario.rol
                                        )


                                    jugadorActivoId =
                                        usuarioEncontrado.id

                                    onResultado(true)

                                    Toast.makeText(
                                        context,
                                        "¡Bienvenido, ${usuarioEncontrado.nombre}!",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    val destino =
                                        when (usuarioEncontrado.rol) {

                                            Rol.CLIENTE -> "cliente"
                                            Rol.GENERADOR -> "contenido"
                                            Rol.ADMINISTRADOR -> "administrador"
                                        }

                                    navController.navigate(destino) {
                                        popUpTo("seleccion_jugador") {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }

                                } else {

                                    jugadorActivoId = null

                                    onResultado(false)
                                }

                            } catch (e: Exception) {

                                jugadorActivoId = null

                                onResultado(false)

                                Toast.makeText(
                                    context,
                                    "Inicio de sesión fallido: ${e.message ?: "verifica el backend"}",
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


            // ==========================================
            // PANTALLA 4: DASHBOARD DEL CLIENTE
            // ==========================================

            composable("cliente") {

                if (jugadorActivoId == null) {

                    LaunchedEffect(Unit) {

                        navController.navigate(
                            "seleccion_jugador"
                        ) {

                            popUpTo("cliente") {
                                inclusive = true
                            }
                        }
                    }

                } else if (
                    usuarioActual?.id == jugadorActivoId &&
                    usuarioActual?.rol == Rol.CLIENTE
                ) {

                    DashboardScreen(

                        sessionViewModel = sessionViewModel,
                        coleccionViewModel = coleccionViewModel,
                        mapaViewModel = mapaViewModel,
                        notificacionesViewModel =
                            notificacionesViewModel,

                        onMapaClick = {

                            navController.navigate("mapa") {
                                launchSingleTop = true
                            }
                        },

                        // Escáner general: acepta cualquier
                        // QR válido y publicado de Triskeledu.
                        onEscanerClick = {

                            abrirEscanerGeneral()
                        },

                        onColeccionClick = {

                            navController.navigate("coleccion") {
                                launchSingleTop = true
                            }
                        },

                        onVideoAdicionalClick = { numeroVideo ->

                            videoAdicionalSeleccionado =
                                numeroVideo

                            navController.navigate(
                                "video_adicional"
                            )
                        },

                        onVideoRegistradoClick = { video ->

                            videoRegistradoSeleccionado = video

                            navController.navigate(
                                "video_registrado"
                            )
                        },

                        onProgresoClick = {

                            navController.navigate(
                                "puntaje_progreso"
                            ) {
                                launchSingleTop = true
                            }
                        },

                        onContenidosEducativosClick = {

                            navController.navigate(
                                "contenidos_educativos"
                            ) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }


            // ==========================================
            // CONTENIDOS EDUCATIVOS DEL JUGADOR
            // ==========================================

            composable("contenidos_educativos") {

                if (
                    jugadorActivoId != null &&
                    usuarioActual?.id == jugadorActivoId &&
                    usuarioActual?.rol == Rol.CLIENTE
                ) {

                    ContenidosEducativosScreen(

                        coleccionViewModel =
                            coleccionViewModel,

                        onVolver = {
                            navController.popBackStack()
                        },

                        onVideoAdicionalClick = { numeroVideo ->

                            videoAdicionalSeleccionado =
                                numeroVideo

                            navController.navigate(
                                "video_adicional"
                            )
                        },

                        onVideoRegistradoClick = { video ->

                            videoRegistradoSeleccionado = video

                            navController.navigate(
                                "video_registrado"
                            )
                        },

                        onCapsulaClick = { capsula ->

                            resultadoCapsula = capsula

                            navController.navigate(
                                "video_educativo"
                            )
                        }
                    )
                }
            }


            // ==========================================
            // PANTALLA 12: PUNTAJE Y PROGRESO
            // ==========================================

            composable("puntaje_progreso") {

                if (jugadorActivoId == null) {

                    LaunchedEffect(Unit) {

                        navController.navigate(
                            "seleccion_jugador"
                        ) {

                            popUpTo("puntaje_progreso") {
                                inclusive = true
                            }
                        }
                    }

                } else if (

                    usuarioActual?.id == jugadorActivoId &&
                    usuarioActual?.rol == Rol.CLIENTE

                ) {

                    PuntajeProgresoScreen(

                        sessionViewModel = sessionViewModel,
                        coleccionViewModel = coleccionViewModel,

                        onVolverClick = {
                            navController.popBackStack()
                        },

                        onHistorialClick = {

                            navController.navigate(
                                "historial_actividades"
                            ) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }


            // ==========================================
            // PANTALLA 13: HISTORIAL DE ACTIVIDADES
            // ==========================================

            composable("historial_actividades") {

                if (jugadorActivoId == null) {

                    LaunchedEffect(Unit) {

                        navController.navigate(
                            "seleccion_jugador"
                        ) {

                            popUpTo("historial_actividades") {
                                inclusive = true
                            }
                        }
                    }

                } else if (

                    usuarioActual?.id == jugadorActivoId &&
                    usuarioActual?.rol == Rol.CLIENTE

                ) {

                    HistorialActividadesScreen(

                        sessionViewModel = sessionViewModel,
                        coleccionViewModel = coleccionViewModel,
                        mapaViewModel = mapaViewModel,

                        onVolverClick = {
                            navController.popBackStack()
                        }
                    )
                }
            }


            // ==========================================
            // MAPA
            // ==========================================

            composable("mapa") {

                if (jugadorActivoId == null) {

                    LaunchedEffect(Unit) {

                        navController.navigate(
                            "seleccion_jugador"
                        ) {

                            popUpTo("mapa") {
                                inclusive = true
                            }
                        }
                    }

                } else {

                    MapaScreen(

                        viewModel = mapaViewModel,

                        onVerDetallesClick = { punto ->

                            // Guarda el punto seleccionado.
                            puntoDetalleId = punto.id

                            navController.navigate(
                                "detalle_punto"
                            )
                        },

                        onVolverClick = if (

                            usuarioActual?.rol ==
                            Rol.ADMINISTRADOR ||

                            usuarioActual?.rol ==
                            Rol.GENERADOR

                        ) {

                            {
                                navController.popBackStack()
                            }

                        } else {

                            null
                        }
                    )
                }
            }


            // ==========================================
            // DETALLE DEL PUNTO TURÍSTICO
            // ==========================================

            composable("detalle_punto") {

                val id = puntoDetalleId
                val rol = usuarioActual?.rol

                if (
                    jugadorActivoId != null &&
                    usuarioActual?.id == jugadorActivoId &&
                    id != null &&
                    rol != null
                ) {

                    DetallePuntoScreen(

                        puntoId = id,
                        rol = rol,
                        mapaViewModel = mapaViewModel,
                        coleccionViewModel = coleccionViewModel,

                        onVolver = {
                            navController.popBackStack()
                        },

                        // Envía el ID del punto turístico
                        // seleccionado al escáner.
                        onEscanearQr = {

                            if (rol == Rol.CLIENTE) {

                                navController.navigate(
                                    "escanear_qr?puntoId=$id"
                                ) {
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }
            }


            // ==========================================
            // ESCÁNER QR
            // ==========================================

            composable(

                route = rutaEscaner,

                arguments = listOf(

                    navArgument("puntoId") {

                        type = NavType.IntType

                        // -1 representa el escáner general.
                        defaultValue = -1
                    }
                )

            ) { entrada ->

                // Si existe un ID, el QR debe pertenecer
                // al punto turístico seleccionado.
                // Si es null, se permite cualquier QR válido.
                val puntoIdEsperado = entrada.arguments
                    ?.getInt("puntoId", -1)
                    ?.takeIf { it != -1 }

                if (jugadorActivoId == null) {

                    LaunchedEffect(Unit) {

                        navController.navigate(
                            "seleccion_jugador"
                        ) {

                            popUpTo(rutaEscaner) {
                                inclusive = true
                            }
                        }
                    }

                } else if (

                    usuarioActual?.id == jugadorActivoId &&
                    usuarioActual?.rol == Rol.CLIENTE

                ) {

                    // Reinicia los estados del escáner cuando
                    // cambia el punto turístico esperado.
                    key(puntoIdEsperado) {

                        EscanerQRScreen(

                            escanerViewModel =
                                escanerViewModel,

                            puntoIdEsperado =
                                puntoIdEsperado,

                            onVolverMapa = {

                                volverAlMapa()
                            },

                            onResultadoQr = { capsula, puntos ->

                                resultadoCapsula = capsula
                                resultadoPuntos = puntos

                                // Elimina el escáner de la pila
                                // para evitar regresar a la cámara
                                // y escanear el mismo QR.
                                navController.navigate(
                                    "resultado_qr"
                                ) {

                                    popUpTo(rutaEscaner) {
                                        inclusive = true
                                    }

                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            }


            // ==========================================
            // ADMINISTRADOR
            // ==========================================

            composable("administrador") {

                if (jugadorActivoId == null) {

                    LaunchedEffect(Unit) {

                        navController.navigate(
                            "seleccion_jugador"
                        ) {

                            popUpTo("administrador") {
                                inclusive = true
                            }
                        }
                    }

                } else if (

                    usuarioActual?.id == jugadorActivoId &&
                    usuarioActual?.rol == Rol.ADMINISTRADOR

                ) {

                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {

                        AdministradorScreen(

                            onGestionPuntosClick = {

                                navController.navigate(
                                    "gestion_puntos"
                                ) {
                                    launchSingleTop = true
                                }
                            },

                            onGestionVideosClick = {

                                navController.navigate(
                                    "gestion_videos"
                                ) {
                                    launchSingleTop = true
                                }
                            },

                            onGestionQrClick = {

                                navController.navigate(
                                    "gestion_qr"
                                ) {
                                    launchSingleTop = true
                                }
                            },

                            onTodosVideosClick = {

                                navController.navigate(
                                    "todos_videos"
                                )
                            }
                        )

                        // Acceso al mapa.
                        ExtendedFloatingActionButton(

                            onClick = {
                                navController.navigate("mapa")
                            },

                            icon = {
                                Icon(
                                    Icons.Default.Map,
                                    contentDescription = null
                                )
                            },

                            text = {
                                Text("Ver mapa")
                            },

                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        )

                        // Acceso al perfil.
                        ExtendedFloatingActionButton(

                            onClick = {

                                navController.navigate("perfil") {
                                    launchSingleTop = true
                                }
                            },

                            icon = {

                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null
                                )
                            },

                            text = {
                                Text("Mi Perfil")
                            },

                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        )
                    }

                } else if (

                    usuarioActual?.id == jugadorActivoId

                ) {

                    LaunchedEffect(Unit) {

                        navController.navigate(
                            "seleccion_jugador"
                        ) {

                            popUpTo("administrador") {
                                inclusive = true
                            }
                        }
                    }
                }
            }


            // ==========================================
            // GESTIÓN DE PUNTOS TURÍSTICOS
            // ==========================================

            composable("gestion_puntos") {

                if (
                    usuarioActual?.id == jugadorActivoId &&
                    usuarioActual?.rol == Rol.ADMINISTRADOR
                ) {

                    GestionPuntosScreen(

                        onVolverClick = {
                            navController.popBackStack()
                        }
                    )
                }
            }


            // ==========================================
            // GESTIÓN DE VIDEOS
            // ==========================================

            composable("gestion_videos") {

                if (
                    jugadorActivoId != null &&
                    usuarioActual?.id == jugadorActivoId &&
                    usuarioActual?.rol == Rol.ADMINISTRADOR
                ) {

                    GestionVideosScreen(

                        onVolverClick = {
                            navController.popBackStack()
                        }
                    )
                }
            }


            // ==========================================
            // GESTIÓN DE CÓDIGOS QR
            // ==========================================

            composable("gestion_qr") {

                if (
                    jugadorActivoId != null &&
                    usuarioActual?.id == jugadorActivoId &&
                    usuarioActual?.rol == Rol.ADMINISTRADOR
                ) {

                    GestionQrScreen(

                        onVolverClick = {
                            navController.popBackStack()
                        }
                    )
                }
            }


            // ==========================================
            // GENERADOR DE CONTENIDO
            // ==========================================

            composable("contenido") {

                if (jugadorActivoId == null) {

                    LaunchedEffect(Unit) {

                        navController.navigate(
                            "seleccion_jugador"
                        ) {

                            popUpTo("contenido") {
                                inclusive = true
                            }
                        }
                    }

                } else if (

                    usuarioActual?.id == jugadorActivoId &&
                    usuarioActual?.rol == Rol.GENERADOR

                ) {

                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {

                        ContenidoScreen(

                            onTodosVideosClick = {

                                navController.navigate(
                                    "todos_videos"
                                )
                            }
                        )

                        // Acceso al mapa.
                        ExtendedFloatingActionButton(

                            onClick = {
                                navController.navigate("mapa")
                            },

                            icon = {

                                Icon(
                                    Icons.Default.Map,
                                    contentDescription = null
                                )
                            },

                            text = {
                                Text("Ver mapa")
                            },

                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        )

                        // Acceso al perfil.
                        ExtendedFloatingActionButton(

                            onClick = {

                                navController.navigate("perfil") {
                                    launchSingleTop = true
                                }
                            },

                            icon = {

                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null
                                )
                            },

                            text = {
                                Text("Mi Perfil")
                            },

                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        )
                    }

                } else if (

                    usuarioActual?.id == jugadorActivoId

                ) {

                    LaunchedEffect(Unit) {

                        navController.navigate(
                            "seleccion_jugador"
                        ) {

                            popUpTo("contenido") {
                                inclusive = true
                            }
                        }
                    }
                }
            }


            // ==========================================
            // BIBLIOTECA DE VIDEOS DEL PERSONAL
            // ==========================================

            composable("todos_videos") {

                if (
                    jugadorActivoId != null &&
                    usuarioActual?.id == jugadorActivoId &&
                    usuarioActual?.rol in listOf(
                        Rol.ADMINISTRADOR,
                        Rol.GENERADOR
                    )
                ) {

                    TodosVideosScreen(

                        onVolver = {
                            navController.popBackStack()
                        },

                        onAbrirVideo = { video ->

                            tipoVideoPersonal = "registrado"

                            videoRegistradoSeleccionado = video

                            navController.navigate(
                                "video_personal"
                            )
                        },

                        onAbrirCapsula = { capsula ->

                            tipoVideoPersonal = "capsula"

                            capsulaPersonalSeleccionada =
                                capsula

                            navController.navigate(
                                "video_personal"
                            )
                        },

                        onAbrirAdicional = { id ->

                            tipoVideoPersonal = "adicional"

                            adicionalPersonalSeleccionado = id

                            navController.navigate(
                                "video_personal"
                            )
                        }
                    )
                }
            }


            // ==========================================
            // VIDEO DEL PERSONAL
            // ==========================================

            composable("video_personal") {

                if (
                    jugadorActivoId != null &&
                    usuarioActual?.id == jugadorActivoId &&
                    usuarioActual?.rol in listOf(
                        Rol.ADMINISTRADOR,
                        Rol.GENERADOR
                    )
                ) {

                    val capsula =
                        if (tipoVideoPersonal == "capsula") {
                            capsulaPersonalSeleccionada
                        } else {
                            null
                        }

                    val adicional =
                        if (tipoVideoPersonal == "adicional") {
                            adicionalPersonalSeleccionado
                        } else {
                            null
                        }

                    val registrado =
                        if (tipoVideoPersonal == "registrado") {
                            videoRegistradoSeleccionado
                        } else {
                            null
                        }

                    if (
                        capsula != null ||
                        adicional != null ||
                        registrado != null
                    ) {

                        VideoEducativoScreen(

                            capsula = capsula,

                            videoAdicionalId = adicional,

                            videoRegistrado = registrado,

                            yaVisto = false,

                            // El personal no obtiene puntos.
                            onVideoTerminado = {},

                            onVolverColeccion = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }


            // ==========================================
            // COLECCIÓN
            // ==========================================

            composable("coleccion") {

                if (jugadorActivoId == null) {

                    LaunchedEffect(Unit) {

                        navController.navigate(
                            "seleccion_jugador"
                        ) {

                            popUpTo("coleccion") {
                                inclusive = true
                            }
                        }
                    }

                } else if (
                    usuarioActual?.id == jugadorActivoId &&
                    usuarioActual?.rol == Rol.CLIENTE
                ) {

                    ColeccionScreen(

                        coleccionViewModel =
                            coleccionViewModel,

                        // Video descubierto mediante QR.
                        onCapsulaClick = { capsula ->

                            resultadoCapsula = capsula

                            navController.navigate(
                                "video_educativo"
                            )
                        },

                        // Videos adicionales.
                        onVideoAdicionalClick = { numeroVideo ->

                            videoAdicionalSeleccionado =
                                numeroVideo

                            navController.navigate(
                                "video_adicional"
                            )
                        },

                        // Videos creados por el administrador.
                        onVideoRegistradoClick = { video ->

                            videoRegistradoSeleccionado = video

                            navController.navigate(
                                "video_registrado"
                            )
                        },

                        onVolver = {
                            navController.popBackStack()
                        }
                    )
                }
            }


            // ==========================================
            // RESULTADO DEL QR
            // ==========================================

            composable("resultado_qr") {

                val capsula = resultadoCapsula

                if (jugadorActivoId == null) {

                    LaunchedEffect(Unit) {

                        navController.navigate(
                            "seleccion_jugador"
                        ) {

                            popUpTo("resultado_qr") {
                                inclusive = true
                            }
                        }
                    }

                } else if (capsula == null) {

                    LaunchedEffect(Unit) {

                        // Si no existe un resultado,
                        // regresa al mapa y no a la cámara.
                        volverAlMapa()
                    }

                } else {

                    ResultadoQRScreen(

                        capsula = capsula,

                        puntosGanados = resultadoPuntos,

                        onVolverMapa = {

                            volverAlMapa()
                        },

                        onTomarSelfie = {

                            navController.navigate(
                                "selfie_turistica"
                            )
                        },

                        onVerVideo = {

                            navController.navigate(
                                "video_educativo"
                            )
                        },

                        // Se conserva por compatibilidad.
                        // La pantalla ya no tiene la flecha.
                        // Si se usa, vuelve al mapa.
                        onVolverEscaner = {

                            volverAlMapa()
                        }
                    )
                }
            }


            // ==========================================
            // VIDEO EDUCATIVO
            // ==========================================

            composable("video_educativo") {

                val capsula = resultadoCapsula

                val idJugador = jugadorActivoId

                val videosVistos by
                coleccionViewModel.videosVistos
                    .collectAsState()

                if (
                    idJugador == null ||
                    capsula == null
                ) {

                    LaunchedEffect(Unit) {

                        navController.navigate("cliente") {

                            popUpTo("video_educativo") {
                                inclusive = true
                            }
                        }
                    }

                } else {

                    val yaVisto = videosVistos.any {

                        it.capsulaId == capsula.id
                    }

                    VideoEducativoScreen(

                        capsula = capsula,

                        yaVisto = yaVisto,

                        onVideoTerminado = {

                            coleccionViewModel.registrarVideoVisto(
                                capsula.id
                            )
                        },

                        onVolverColeccion = {
                            navController.popBackStack()
                        }
                    )
                }
            }


            // ==========================================
            // VIDEO ADICIONAL
            // ==========================================

            composable("video_adicional") {

                val numeroVideo =
                    videoAdicionalSeleccionado

                val idJugador = jugadorActivoId

                if (
                    idJugador == null ||
                    numeroVideo == null ||
                    numeroVideo !in 1..2
                ) {

                    LaunchedEffect(Unit) {

                        navController.navigate("cliente") {

                            popUpTo("video_adicional") {
                                inclusive = true
                            }
                        }
                    }

                } else {

                    val videosAdicionalesVistos by
                    coleccionViewModel
                        .videosAdicionalesVistos
                        .collectAsState()

                    val yaVisto =
                        videosAdicionalesVistos.any {

                            it.videoAdicionalId == numeroVideo
                        }

                    VideoEducativoScreen(

                        capsula = null,

                        videoAdicionalId = numeroVideo,

                        yaVisto = yaVisto,

                        onVideoTerminado = {

                            coleccionViewModel
                                .registrarVideoAdicionalVisto(
                                    numeroVideo
                                )
                        },

                        onVolverColeccion = {
                            navController.popBackStack()
                        }
                    )
                }
            }


            // ==========================================
            // SELFIE TURÍSTICA
            // ==========================================

            composable("selfie_turistica") {

                val capsula = resultadoCapsula

                val idJugador = jugadorActivoId

                if (
                    idJugador == null ||
                    capsula == null
                ) {

                    LaunchedEffect(Unit) {

                        navController.navigate("cliente") {

                            popUpTo("selfie_turistica") {
                                inclusive = true
                            }
                        }
                    }

                } else {

                    SelfieTuristicaScreen(

                        onVolver = {
                            navController.popBackStack()
                        },

                        onFotoGuardada = {
                                ruta,
                                informarResultado ->

                            selfieViewModel
                                .validarYRegistrarSelfie(

                                    rutaFoto = ruta,

                                    codigoQrEsperado =
                                        capsula.codigoQr,

                                    puntoId = capsula.puntoId,

                                    capsulaId = capsula.id,

                                    onResultado = {
                                            exito,
                                            mensaje ->

                                        informarResultado(
                                            exito,
                                            mensaje
                                        )
                                    }
                                )
                        }
                    )
                }
            }


            // ==========================================
            // PANTALLA 14: NOTIFICACIONES
            // ==========================================

            composable("notificaciones") {

                if (jugadorActivoId == null) {

                    LaunchedEffect(Unit) {

                        navController.navigate(
                            "seleccion_jugador"
                        ) {

                            popUpTo("notificaciones") {
                                inclusive = true
                            }
                        }
                    }

                } else if (

                    usuarioActual?.id == jugadorActivoId

                ) {

                    NotificacionesScreen(

                        notificacionesViewModel =
                            notificacionesViewModel,

                        onVolver = {

                            volverDesdePerfil()
                        },

                        onAbrirDestino = { ruta ->

                            // Rutas permitidas según el rol.
                            val destinosPermitidos =
                                when (usuarioActual?.rol) {

                                    Rol.CLIENTE -> setOf(

                                        "cliente",
                                        "mapa",
                                        "escanear_qr",
                                        "coleccion",
                                        "perfil",
                                        "puntaje_progreso",
                                        "historial_actividades",
                                        "contenidos_educativos"
                                    )

                                    Rol.GENERADOR -> setOf(

                                        "contenido",
                                        "perfil",
                                        "todos_videos",
                                        "mapa"
                                    )

                                    Rol.ADMINISTRADOR -> setOf(

                                        "administrador",
                                        "perfil",
                                        "gestion_puntos",
                                        "gestion_videos",
                                        "gestion_qr",
                                        "todos_videos",
                                        "mapa"
                                    )

                                    else -> emptySet()
                                }

                            if (ruta in destinosPermitidos) {

                                if (ruta == "escanear_qr") {

                                    abrirEscanerGeneral()

                                } else {

                                    navController.navigate(ruta) {
                                        launchSingleTop = true
                                    }
                                }

                            } else {

                                Toast.makeText(
                                    context,
                                    "Esta notificación no tiene una pantalla disponible",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                }
            }


            // ==========================================
            // VIDEO REGISTRADO POR EL ADMINISTRADOR
            // ==========================================

            composable("video_registrado") {

                val video = videoRegistradoSeleccionado

                // Recupera los contenidos publicados.
                val contenidos by remember(database) {

                    database.contenidoEducativoDao()
                        .obtenerPublicados()

                }.collectAsState(
                    initial = emptyList()
                )

                // Recupera las cápsulas educativas.
                val capsulas by
                coleccionViewModel.capsulas
                    .collectAsState()

                // Recupera los descubrimientos del jugador.
                val descubrimientos by
                coleccionViewModel.descubrimientosQr
                    .collectAsState()

                val videosVistos by
                coleccionViewModel.videosVistos
                    .collectAsState()

                val videosAdicionalesVistos by
                coleccionViewModel
                    .videosAdicionalesVistos
                    .collectAsState()

                if (
                    jugadorActivoId == null ||
                    usuarioActual?.rol != Rol.CLIENTE ||
                    video == null
                ) {

                    LaunchedEffect(Unit) {

                        navController.navigate("cliente") {

                            popUpTo("video_registrado") {
                                inclusive = true
                            }

                            launchSingleTop = true
                        }
                    }

                } else {

                    // Busca el contenido asociado al video.
                    val contenido = contenidos.firstOrNull {

                        it.id == video.contenidoId
                    }

                    // Busca el QR generado para ese contenido.
                    val capsulaQr = contenido?.let {

                        val prefijo =
                            "TRISKELEDU-CONTENIDO-${it.id}-"

                        capsulas.firstOrNull { capsula ->

                            capsula.codigoQr.startsWith(prefijo) &&
                                    capsula.estado == "PUBLICADA"
                        }
                    }

                    // Comprueba si el contenido requiere QR.
                    val requiereQr =
                        contenido?.puntoId != null

                    // Comprueba si el jugador descubrió el QR.
                    val qrDescubierto =
                        capsulaQr != null &&
                                descubrimientos.any {

                                    it.capsulaId == capsulaQr.id
                                }

                    // Identifica los videos sin QR.
                    val identificadorVideo = -video.id

                    // Comprueba si el video ya fue visto.
                    val yaVisto = if (requiereQr) {

                        capsulaQr != null &&
                                videosVistos.any {

                                    it.capsulaId == capsulaQr.id
                                }

                    } else {

                        videosAdicionalesVistos.any {

                            it.videoAdicionalId ==
                                    identificadorVideo
                        }
                    }

                    if (requiereQr && !qrDescubierto) {

                        // No permite reproducir un video
                        // antes de descubrir su QR.
                        LaunchedEffect(
                            video.id,
                            qrDescubierto
                        ) {

                            Toast.makeText(
                                context,
                                "Primero debes escanear el QR de este video",
                                Toast.LENGTH_LONG
                            ).show()

                            navController.popBackStack()
                        }

                    } else {

                        VideoEducativoScreen(

                            videoRegistrado = video,

                            yaVisto = yaVisto,

                            onVideoTerminado = {

                                if (requiereQr) {

                                    // Video asociado a un QR.
                                    if (
                                        capsulaQr != null &&
                                        qrDescubierto
                                    ) {

                                        coleccionViewModel
                                            .registrarVideoVisto(
                                                capsulaQr.id
                                            )
                                    }

                                } else {

                                    // Video sin QR.
                                    coleccionViewModel
                                        .registrarVideoAdministradorVisto(
                                            video.id
                                        )
                                }
                            },

                            onVolverColeccion = {

                                navController.popBackStack()
                            }
                        )
                    }
                }
            }


            // ==========================================
            // PANTALLA 15: MI PERFIL
            // ==========================================

            composable("perfil") {

                if (jugadorActivoId == null) {

                    LaunchedEffect(Unit) {

                        navController.navigate(
                            "seleccion_jugador"
                        ) {

                            popUpTo("perfil") {
                                inclusive = true
                            }
                        }
                    }

                } else if (

                    usuarioActual?.id == jugadorActivoId

                ) {

                    // Perfil compartido por los tres roles.
                    PerfilScreen(

                        sessionViewModel = sessionViewModel,

                        coleccionViewModel =
                            coleccionViewModel,

                        onCerrarSesion = {

                            cerrarSesionActual()
                        },

                        onVolver = {

                            volverDesdePerfil()
                        },

                        onVerNotificaciones = {

                            navController.navigate(
                                "notificaciones"
                            ) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

        } // Fin del NavHost.

    } // Fin del Scaffold.
}

