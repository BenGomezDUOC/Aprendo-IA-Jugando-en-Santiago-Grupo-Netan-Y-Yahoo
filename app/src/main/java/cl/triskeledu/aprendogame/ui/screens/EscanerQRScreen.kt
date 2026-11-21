
package cl.triskeledu.aprendogame.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

import cl.triskeledu.aprendogame.data.local.AppDatabase
import cl.triskeledu.aprendogame.data.repository.GameRepositoryImpl
import cl.triskeledu.aprendogame.model.Capsula
import cl.triskeledu.aprendogame.model.DescubrimientoQr
import cl.triskeledu.aprendogame.viewmodel.EscanerQRViewModel

import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference


// =====================================================
// TIPOS DE ERROR DEL ESCÁNER
// =====================================================

private enum class TipoErrorQr {
    NO_VALIDO,
    NO_PUBLICADO,
    OTRO_PUNTO
}


// =====================================================
// PANTALLA PRINCIPAL DEL ESCÁNER QR
// =====================================================

@Composable
fun EscanerQRScreen(
    escanerViewModel: EscanerQRViewModel,
    onResultadoQr: (Capsula, Int) -> Unit,
    puntoIdEsperado: Int? = null,
    onVolverMapa: () -> Unit = {}
) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val repository = remember(context) {
        GameRepositoryImpl(
            AppDatabase.getInstance(context.applicationContext)
        )
    }

    // Avisos generales del escáner.
    val snackbarHostState = remember { SnackbarHostState() }

    var mensaje by remember {
        mutableStateOf("")
    }

    // Comprueba el permiso real de Android.
    fun tienePermiso(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    var permisoCamara by remember {
        mutableStateOf(tienePermiso())
    }

    var mostrarDialogoPermiso by remember {
        mutableStateOf(false)
    }

    var solicitudEnCurso by remember {
        mutableStateOf(false)
    }

    var permisoSolicitadoAntes by rememberSaveable {
        mutableStateOf(false)
    }

    // Evita procesar varios QR simultáneamente.
    val qrBloqueado = remember {
        AtomicBoolean(false)
    }

    // Evita navegar varias veces al resultado.
    val resultadoEncontrado = remember {
        AtomicBoolean(false)
    }

    // Controla las ventanas de error.
    var errorQr by remember {
        mutableStateOf<TipoErrorQr?>(null)
    }

    // Nombre del punto turístico seleccionado.
    var nombrePunto by remember(puntoIdEsperado) {
        mutableStateOf("")
    }

    // Evita volver a detectar inmediatamente un QR rechazado.
    val qrRechazado = remember {
        AtomicReference<String?>(null)
    }

    val fotogramasSinQr = remember {
        AtomicInteger(0)
    }

    // Recupera el nombre del punto turístico seleccionado.
    LaunchedEffect(puntoIdEsperado) {

        nombrePunto = puntoIdEsperado?.let { id ->

            try {
                repository.getPuntoPorId(id)?.nombre
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.e(
                    "TriskeleduQR",
                    "Error al obtener el punto turístico",
                    error
                )
                null
            }

        }.orEmpty()
    }

    // Muestra únicamente avisos generales.
    LaunchedEffect(mensaje) {

        if (mensaje.isNotBlank()) {

            snackbarHostState.showSnackbar(
                message = mensaje,
                duration = SnackbarDuration.Short
            )

            mensaje = ""
        }
    }


    // -------------------------------------------------
    // PERMISOS DE CÁMARA
    // -------------------------------------------------

    val solicitarPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->

        solicitudEnCurso = false
        permisoCamara = concedido

        // El aviso personalizado aparece después
        // de responder a la solicitud oficial de Android.
        mostrarDialogoPermiso = !concedido
    }

    LaunchedEffect(Unit) {

        if (!tienePermiso() && !permisoSolicitadoAntes) {

            permisoSolicitadoAntes = true
            solicitudEnCurso = true

            solicitarPermiso.launch(
                Manifest.permission.CAMERA
            )

        } else if (!tienePermiso()) {

            mostrarDialogoPermiso = true
        }
    }

    // Comprueba el permiso al regresar desde Ajustes.
    DisposableEffect(lifecycleOwner, context) {

        val observador = LifecycleEventObserver { _, evento ->

            if (evento == Lifecycle.Event.ON_RESUME) {

                val concedido = tienePermiso()

                permisoCamara = concedido

                mostrarDialogoPermiso =
                    !concedido &&
                            permisoSolicitadoAntes &&
                            !solicitudEnCurso
            }
        }

        lifecycleOwner.lifecycle.addObserver(observador)

        onDispose {

            lifecycleOwner.lifecycle.removeObserver(observador)
        }
    }

    // Abre los ajustes de Android de esta aplicación.
    val abrirAjustes: () -> Unit = {

        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${context.packageName}")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(intent)
    }


    // -------------------------------------------------
    // MOSTRAR ERROR Y PAUSAR EL ESCANEO
    // -------------------------------------------------

    fun mostrarErrorQr(
        tipoError: TipoErrorQr,
        textoLeido: String
    ) {

        // Mantiene bloqueado el procesamiento mientras
        // la ventana de error permanece visible.
        qrBloqueado.set(true)

        qrRechazado.set(
            extraerCodigoQr(textoLeido)
        )

        fotogramasSinQr.set(0)

        mensaje = ""
        snackbarHostState.currentSnackbarData?.dismiss()

        errorQr = tipoError
    }


    // -------------------------------------------------
    // VALIDACIÓN Y PROCESAMIENTO DEL QR
    // -------------------------------------------------

    val procesarQr: (String) -> Unit = { textoLeido ->

        val codigoQr = extraerCodigoQr(textoLeido)

        val usuarioId = escanerViewModel.usuarioActivoId.value

        when {

            usuarioId == null -> {

                mensaje =
                    "Debes iniciar sesión para registrar QR."

                qrBloqueado.set(false)
            }

            codigoQr.isBlank() -> {

                mostrarErrorQr(
                    TipoErrorQr.NO_VALIDO,
                    textoLeido
                )
            }

            else -> {

                scope.launch {

                    try {

                        // Busca el QR en Room.
                        // Incluye los QR nuevos del administrador.
                        val capsula = repository.validarQr(codigoQr)

                        when {

                            // VALIDACIÓN 1:
                            // El QR debe existir en Triskeledu.
                            capsula == null -> {

                                mostrarErrorQr(
                                    TipoErrorQr.NO_VALIDO,
                                    textoLeido
                                )
                            }

                            // VALIDACIÓN 2:
                            // La cápsula debe estar publicada.
                            !capsula.estado.equals(
                                "PUBLICADA",
                                ignoreCase = true
                            ) -> {

                                mostrarErrorQr(
                                    TipoErrorQr.NO_PUBLICADO,
                                    textoLeido
                                )
                            }

                            // VALIDACIÓN 3:
                            // Si viene desde el mapa, el QR
                            // debe pertenecer al punto seleccionado.
                            puntoIdEsperado != null &&
                                    capsula.puntoId != puntoIdEsperado -> {

                                mostrarErrorQr(
                                    TipoErrorQr.OTRO_PUNTO,
                                    textoLeido
                                )
                            }

                            // VALIDACIÓN CORRECTA.
                            else -> {

                                // Registra el descubrimiento.
                                val resultado =
                                    repository.registrarDescubrimientoQr(
                                        DescubrimientoQr(
                                            usuarioId = usuarioId,
                                            capsulaId = capsula.id,
                                            fechaTimestamp =
                                                System.currentTimeMillis()
                                        )
                                    )

                                // 10 puntos solamente si el QR
                                // se descubre por primera vez.
                                val puntosGanados =
                                    if (resultado == -1L) 0 else 10

                                resultadoEncontrado.set(true)

                                // Abre la pantalla de resultado
                                // únicamente si el QR es válido.
                                onResultadoQr(
                                    capsula,
                                    puntosGanados
                                )
                            }
                        }

                    } catch (error: CancellationException) {

                        throw error

                    } catch (error: Exception) {

                        Log.e(
                            "TriskeleduQR",
                            "Error al registrar QR",
                            error
                        )

                        mensaje =
                            "No se pudo registrar el QR: ${error.message}"

                        qrBloqueado.set(false)
                    }
                }
            }
        }
    }


    // -------------------------------------------------
    // INTERFAZ PRINCIPAL
    // -------------------------------------------------

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF29292B))
    ) {

        // Cámara automática si existe permiso.
        if (permisoCamara && errorQr == null) {

            CamaraQrEnVivo(
                modifier = Modifier.fillMaxSize(),
                lifecycleOwner = lifecycleOwner,
                qrBloqueado = qrBloqueado,
                resultadoEncontrado = resultadoEncontrado,
                qrRechazado = qrRechazado,
                fotogramasSinQr = fotogramasSinQr,
                onQrDetectado = procesarQr,
                onError = { error ->

                    mensaje = error
                }
            )

            // Cuatro esquinas blancas del escáner.
            MarcoEscaneoQr(
                modifier = Modifier
                    .size(230.dp)
                    .align(Alignment.Center)
            )
        }

        // Ventana personalizada de permiso de cámara.
        if (
            !permisoCamara &&
            mostrarDialogoPermiso &&
            !solicitudEnCurso
        ) {

            VentanaPermisoCamara(
                modifier = Modifier.fillMaxSize(),
                onAbrirAjustes = abrirAjustes
            )
        }

        // Ventanas de error del código QR.
        errorQr?.let { error ->

            if (permisoCamara) {

                VentanaErrorQr(
                    modifier = Modifier.fillMaxSize(),
                    error = error,
                    nombrePunto = nombrePunto,

                    onReintentar = {

                        // Cierra la ventana y reactiva la cámara.
                        // Conserva el QR rechazado para evitar
                        // que vuelva a detectarse inmediatamente.
                        fotogramasSinQr.set(0)
                        qrBloqueado.set(false)
                        errorQr = null
                    },

                    onVolverMapa = onVolverMapa
                )
            }
        }

        // Mensajes flotantes para errores generales.
        if (errorQr == null) {

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 20.dp
                    )
            ) { data ->

                Snackbar(
                    snackbarData = data,
                    shape = RoundedCornerShape(16.dp),
                    containerColor = Color(0xFF263746),
                    contentColor = Color.White
                )
            }
        }
    }
}


// =====================================================
// VENTANA DE PERMISO DE CÁMARA
// =====================================================

@Composable
private fun VentanaPermisoCamara(
    modifier: Modifier = Modifier,
    onAbrirAjustes: () -> Unit
) {

    Box(
        modifier = modifier
            .background(Color(0xFF29292B))
            .padding(horizontal = 22.dp),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .background(
                    color = Color(0xFF0D0D0F),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(
                    horizontal = 22.dp,
                    vertical = 28.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            IconoCamaraDesactivada(
                modifier = Modifier.size(54.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Permiso de cámara necesario",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 29.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "Para escanear códigos QR, necesitas permitir el acceso a la cámara.",
                color = Color.White,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(22.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFF2C2C2F),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(
                        horizontal = 16.dp,
                        vertical = 20.dp
                    ),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "Puedes activar el permiso desde los ajustes de la aplicación.",
                    color = Color.White,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onAbrirAjustes,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF4F4F4),
                    contentColor = Color.Black
                )
            ) {

                Text(
                    text = "Abrir ajustes",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}


// =====================================================
// VENTANAS DE ERROR DEL QR
// =====================================================

@Composable
private fun VentanaErrorQr(
    modifier: Modifier = Modifier,
    error: TipoErrorQr,
    nombrePunto: String,
    onReintentar: () -> Unit,
    onVolverMapa: () -> Unit
) {

    val otroPunto = error == TipoErrorQr.OTRO_PUNTO

    Box(
        modifier = modifier
            .background(Color(0xFF29292B))
            .padding(horizontal = 22.dp),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .background(
                    color = Color(0xFF0D0D0F),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(
                    horizontal = 22.dp,
                    vertical = 28.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Ícono rojo según el tipo de error.
            IconoErrorQr(
                otroPunto = otroPunto,
                modifier = Modifier.size(54.dp)
            )

            Spacer(Modifier.height(18.dp))

            // Título de la ventana.
            Text(
                text = if (otroPunto) {
                    "QR incorrecto"
                } else {
                    "QR no válido"
                },
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 29.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(22.dp))

            // Explicación del error.
            Text(
                text = when (error) {

                    TipoErrorQr.NO_VALIDO ->
                        "El código QR escaneado no pertenece a Triskeledu."

                    TipoErrorQr.NO_PUBLICADO ->
                        "El código QR escaneado no está disponible."

                    TipoErrorQr.OTRO_PUNTO ->
                        "El código QR escaneado no pertenece al punto turístico seleccionado."
                },
                color = Color.White,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(22.dp))

            // Recuadro gris informativo.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFF2C2C2F),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                if (otroPunto) {

                    // Solo se muestra el punto seleccionado.
                    // NO se revela a qué lugar pertenece
                    // el código QR rechazado.
                    Text(
                        text = "Punto turístico seleccionado",
                        color = Color(0xFFBDBDBD),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    if (nombrePunto.isNotBlank()) {

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = nombrePunto,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(Modifier.height(10.dp))
                }

                Text(
                    text = if (otroPunto) {
                        "Escanea un código QR correspondiente a este lugar."
                    } else {
                        "Escanea un código QR válido para continuar con tu descubrimiento."
                    },
                    color = Color.White,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(24.dp))

            // Botón para regresar a la cámara.
            Button(
                onClick = onReintentar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF4F4F4),
                    contentColor = Color.Black
                )
            ) {

                Text(
                    text = "Volver a escanear",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Solo aparece en el error de punto turístico.
            if (otroPunto) {

                Spacer(Modifier.height(8.dp))

                TextButton(
                    onClick = onVolverMapa
                ) {

                    Text(
                        text = "Volver al mapa",
                        color = Color(0xFFD1D5DB),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}


// =====================================================
// ÍCONO DE ERROR DEL CÓDIGO QR
// =====================================================

@Composable
private fun IconoErrorQr(
    otroPunto: Boolean,
    modifier: Modifier = Modifier
) {

    Canvas(modifier = modifier) {

        val color = Color(0xFFF87171)
        val grosor = 3.dp.toPx()

        val ancho = size.width
        val alto = size.height

        val centroX = ancho / 2f
        val centroY = alto / 2f

        if (otroPunto) {

            // Marcador de ubicación.
            val pin = Path().apply {

                moveTo(
                    centroX,
                    alto * 0.92f
                )

                cubicTo(
                    ancho * 0.40f, alto * 0.77f,
                    ancho * 0.14f, alto * 0.53f,
                    ancho * 0.14f, alto * 0.40f
                )

                cubicTo(
                    ancho * 0.14f, alto * 0.02f,
                    ancho * 0.86f, alto * 0.02f,
                    ancho * 0.86f, alto * 0.40f
                )

                cubicTo(
                    ancho * 0.86f, alto * 0.53f,
                    ancho * 0.60f, alto * 0.77f,
                    centroX, alto * 0.92f
                )

                close()
            }

            drawPath(
                path = pin,
                color = color,
                style = Stroke(width = grosor)
            )

        } else {

            // Círculo para QR no válido.
            drawCircle(
                color = color,
                radius = ancho * 0.42f,
                center = Offset(centroX, centroY),
                style = Stroke(width = grosor)
            )
        }

        // Dibuja una X dentro del ícono.
        val posicionY = if (otroPunto) {
            alto * 0.38f
        } else {
            centroY
        }

        val distancia = ancho * 0.13f

        drawLine(
            color = color,
            start = Offset(
                centroX - distancia,
                posicionY - distancia
            ),
            end = Offset(
                centroX + distancia,
                posicionY + distancia
            ),
            strokeWidth = grosor,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color,
            start = Offset(
                centroX + distancia,
                posicionY - distancia
            ),
            end = Offset(
                centroX - distancia,
                posicionY + distancia
            ),
            strokeWidth = grosor,
            cap = StrokeCap.Round
        )
    }
}


// =====================================================
// ÍCONO DE CÁMARA DESACTIVADA
// =====================================================

@Composable
private fun IconoCamaraDesactivada(
    modifier: Modifier = Modifier
) {

    Canvas(modifier = modifier) {

        val ancho = size.width
        val alto = size.height
        val color = Color(0xFFD8D8D8)
        val grosor = 3.dp.toPx()

        // Cuerpo de la cámara.
        drawRoundRect(
            color = color,
            topLeft = Offset(
                ancho * 0.17f,
                alto * 0.30f
            ),
            size = Size(
                ancho * 0.66f,
                alto * 0.46f
            ),
            cornerRadius = CornerRadius(
                5.dp.toPx()
            ),
            style = Stroke(width = grosor)
        )

        // Parte superior de la cámara.
        drawRoundRect(
            color = color,
            topLeft = Offset(
                ancho * 0.35f,
                alto * 0.22f
            ),
            size = Size(
                ancho * 0.30f,
                alto * 0.13f
            ),
            cornerRadius = CornerRadius(
                2.dp.toPx()
            ),
            style = Stroke(width = grosor)
        )

        // Lente.
        drawCircle(
            color = color,
            radius = ancho * 0.12f,
            center = Offset(
                ancho * 0.50f,
                alto * 0.53f
            ),
            style = Stroke(width = grosor)
        )

        // Fondo de la línea diagonal.
        drawLine(
            color = Color(0xFF0D0D0F),
            start = Offset(
                ancho * 0.19f,
                alto * 0.16f
            ),
            end = Offset(
                ancho * 0.81f,
                alto * 0.84f
            ),
            strokeWidth = grosor * 2.6f,
            cap = StrokeCap.Round
        )

        // Línea diagonal visible.
        drawLine(
            color = color,
            start = Offset(
                ancho * 0.19f,
                alto * 0.16f
            ),
            end = Offset(
                ancho * 0.81f,
                alto * 0.84f
            ),
            strokeWidth = grosor,
            cap = StrokeCap.Round
        )
    }
}


// =====================================================
// CÁMARA EN VIVO CON CAMERAX Y ML KIT
// =====================================================

@Composable
private fun CamaraQrEnVivo(
    modifier: Modifier = Modifier,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    qrBloqueado: AtomicBoolean,
    resultadoEncontrado: AtomicBoolean,
    qrRechazado: AtomicReference<String?>,
    fotogramasSinQr: AtomicInteger,
    onQrDetectado: (String) -> Unit,
    onError: (String) -> Unit
) {

    val context = LocalContext.current

    val callbackQr by rememberUpdatedState(onQrDetectado)
    val callbackError by rememberUpdatedState(onError)

    val previewView = remember(context) {

        PreviewView(context).apply {

            implementationMode =
                PreviewView.ImplementationMode.COMPATIBLE

            scaleType =
                PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(context, lifecycleOwner) {

        val executor =
            Executors.newSingleThreadExecutor()

        val opciones = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        val lectorQr =
            BarcodeScanning.getClient(opciones)

        val proveedorFuturo =
            ProcessCameraProvider.getInstance(context)

        var proveedorCamara: ProcessCameraProvider? = null

        var analizador: ImageAnalysis? = null

        var vistaPrevia: Preview? = null

        val activo = AtomicBoolean(true)

        val iniciarCamara = Runnable {

            if (!activo.get()) return@Runnable

            try {

                val proveedor = proveedorFuturo.get()

                if (!activo.get()) return@Runnable

                proveedorCamara = proveedor

                val preview = Preview.Builder()
                    .build()
                    .also {

                        it.surfaceProvider =
                            previewView.surfaceProvider
                    }

                vistaPrevia = preview

                val analisis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(
                        ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                    )
                    .build()

                analizador = analisis

                analisis.setAnalyzer(executor) { imageProxy ->

                    if (
                        !activo.get() ||
                        qrBloqueado.get() ||
                        resultadoEncontrado.get()
                    ) {

                        imageProxy.close()

                    } else {

                        analizarImagenQr(
                            imageProxy = imageProxy,
                            lectorQr = lectorQr,
                            activo = activo,
                            qrBloqueado = qrBloqueado,
                            qrRechazado = qrRechazado,
                            fotogramasSinQr = fotogramasSinQr,

                            onQrDetectado = { texto ->

                                ContextCompat
                                    .getMainExecutor(context)
                                    .execute {

                                        if (activo.get()) {

                                            callbackQr(texto)
                                        }
                                    }
                            }
                        )
                    }
                }

                proveedor.unbindAll()

                proveedor.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analisis
                )

            } catch (error: Exception) {

                Log.e(
                    "TriskeleduQR",
                    "Error al iniciar cámara",
                    error
                )

                if (activo.get()) {

                    callbackError(
                        "No se pudo iniciar la cámara: ${error.message}"
                    )
                }
            }
        }

        proveedorFuturo.addListener(
            iniciarCamara,
            ContextCompat.getMainExecutor(context)
        )

        // Libera la cámara al salir de la pantalla
        // o cuando aparece una ventana de error.
        onDispose {

            activo.set(false)

            analizador?.clearAnalyzer()

            vistaPrevia?.let { preview ->

                proveedorCamara?.unbind(preview)
            }

            analizador?.let { analisis ->

                proveedorCamara?.unbind(analisis)
            }

            lectorQr.close()

            executor.shutdown()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}


// =====================================================
// ANÁLISIS DE CÓDIGOS QR CON ML KIT
// =====================================================

private fun analizarImagenQr(
    imageProxy: ImageProxy,
    lectorQr: com.google.mlkit.vision.barcode.BarcodeScanner,
    activo: AtomicBoolean,
    qrBloqueado: AtomicBoolean,
    qrRechazado: AtomicReference<String?>,
    fotogramasSinQr: AtomicInteger,
    onQrDetectado: (String) -> Unit
) {

    try {

        // Conserva la conversión que ya funcionaba.
        val bitmap = imageProxy.toBitmap()

        val imagen = InputImage.fromBitmap(
            bitmap,
            imageProxy.imageInfo.rotationDegrees
        )

        lectorQr.process(imagen)

            .addOnSuccessListener { codigos ->

                if (!activo.get() || qrBloqueado.get()) {

                    return@addOnSuccessListener
                }

                val textos = codigos.mapNotNull { codigo ->

                    codigo.rawValue?.takeIf {
                        it.isNotBlank()
                    }
                }

                // Comprueba si el QR rechazado sigue
                // apareciendo delante de la cámara.
                val rechazado = qrRechazado.get()

                if (rechazado != null) {

                    val sigueVisible = textos.any { texto ->

                        extraerCodigoQr(texto) == rechazado
                    }

                    if (sigueVisible) {

                        fotogramasSinQr.set(0)

                    } else if (
                        fotogramasSinQr.incrementAndGet() >= 3
                    ) {

                        // Permite volver a intentar ese QR
                        // cuando se haya retirado de la cámara.
                        qrRechazado.compareAndSet(
                            rechazado,
                            null
                        )

                        fotogramasSinQr.set(0)
                    }
                }

                // Busca un QR diferente al rechazado.
                val texto = textos.firstOrNull {

                    extraerCodigoQr(it) != qrRechazado.get()
                }

                if (
                    !texto.isNullOrBlank() &&
                    qrBloqueado.compareAndSet(false, true)
                ) {

                    onQrDetectado(texto)
                }
            }

            .addOnFailureListener { error ->

                Log.e(
                    "TriskeleduQR",
                    "Error al analizar QR",
                    error
                )
            }

            .addOnCompleteListener {

                bitmap.recycle()

                imageProxy.close()
            }

    } catch (error: Exception) {

        Log.e(
            "TriskeleduQR",
            "Error al convertir imagen",
            error
        )

        imageProxy.close()
    }
}


// =====================================================
// MARCO DE CUATRO ESQUINAS BLANCAS
// =====================================================

@Composable
private fun MarcoEscaneoQr(
    modifier: Modifier = Modifier
) {

    Canvas(modifier = modifier) {

        val largo = 38.dp.toPx()
        val grosor = 5.dp.toPx()

        val ancho = size.width
        val alto = size.height

        val color = Color.White

        // Esquina superior izquierda.
        drawLine(
            color,
            Offset(0f, largo),
            Offset(0f, 0f),
            grosor
        )

        drawLine(
            color,
            Offset(0f, 0f),
            Offset(largo, 0f),
            grosor
        )

        // Esquina superior derecha.
        drawLine(
            color,
            Offset(ancho - largo, 0f),
            Offset(ancho, 0f),
            grosor
        )

        drawLine(
            color,
            Offset(ancho, 0f),
            Offset(ancho, largo),
            grosor
        )

        // Esquina inferior izquierda.
        drawLine(
            color,
            Offset(0f, alto - largo),
            Offset(0f, alto),
            grosor
        )

        drawLine(
            color,
            Offset(0f, alto),
            Offset(largo, alto),
            grosor
        )

        // Esquina inferior derecha.
        drawLine(
            color,
            Offset(ancho - largo, alto),
            Offset(ancho, alto),
            grosor
        )

        drawLine(
            color,
            Offset(ancho, alto - largo),
            Offset(ancho, alto),
            grosor
        )
    }
}


// =====================================================
// EXTRAER CÓDIGO QR SIMPLE O JSON
// =====================================================

private fun extraerCodigoQr(texto: String): String {

    return try {

        val json = JSONObject(texto)

        json.optString(
            "codigoQr",
            texto
        ).trim()

    } catch (error: Exception) {

        texto.trim()
    }
}
