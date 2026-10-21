package cl.triskeledu.aprendogame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.triskeledu.aprendogame.data.local.AppDatabase
import cl.triskeledu.aprendogame.model.ContenidoEducativo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val azulContenido = Color(0xFF2455A4)
private val fondoContenido = Color(0xFFF4F7FC)

private val categoriasContenido = listOf(
    "Fundamentos de IA",
    "IA en la vida cotidiana",
    "Aprendizaje automático",
    "Visión artificial",
    "Procesamiento del lenguaje natural",
    "Robótica",
    "Ética y uso responsable de la IA",
    "IA y turismo",
    "Otra"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContenidoScreen(onTodosVideosClick: () -> Unit = {}
) {

    val context = LocalContext.current

    val database = remember(context) {
        AppDatabase.getInstance(context.applicationContext)
    }

    val dao = remember(database) {
        database.contenidoEducativoDao()
    }

    val scope = rememberCoroutineScope()

    val contenidos by remember(dao) {
        dao.obtenerTodos()
    }.collectAsState(initial = emptyList())

    // Observa los puntos turísticos en Room.
    // Cuando Administrador crea o edita un lugar, la lista se actualiza sola.
    val puntoDao = remember(database) { database.puntoInteresDao() }
    val puntos by remember(puntoDao) {
        puntoDao.getPuntos()
    }.collectAsState(initial = emptyList())

    var mostrarFormulario by remember { mutableStateOf(false) }
    var editando by remember {
        mutableStateOf<ContenidoEducativo?>(null)
    }

    var conQr by remember { mutableStateOf(false) }
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var categoria by remember {
        mutableStateOf(categoriasContenido.first())
    }
    var otraCategoria by remember { mutableStateOf("") }
    var puntoId by remember { mutableStateOf<Int?>(null) }

    var menuCategoria by remember { mutableStateOf(false) }
    var menuPunto by remember { mutableStateOf(false) }

    var guardando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }

    fun abrirFormulario(
        tipoConQr: Boolean,
        contenido: ContenidoEducativo? = null
    ) {
        editando = contenido
        conQr = contenido?.puntoId != null ||
                (contenido == null && tipoConQr)

        titulo = contenido?.titulo.orEmpty()
        descripcion = contenido?.descripcion.orEmpty()

        val categoriaGuardada =
            contenido?.categoria ?: categoriasContenido.first()

        categoria = if (
            categoriaGuardada in categoriasContenido
        ) {
            categoriaGuardada
        } else {
            "Otra"
        }

        otraCategoria = if (
            categoriaGuardada in categoriasContenido
        ) {
            ""
        } else {
            categoriaGuardada
        }

        puntoId = contenido?.puntoId
        mensaje = ""
        mostrarFormulario = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(fondoContenido)
            .padding(16.dp)
    ) {

        Text(
            text = "Generador de Contenido",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF14233E)
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Crea y clasifica cápsulas educativas sobre inteligencia artificial.",
            color = Color(0xFF64748B)
        )

        // Botón para acceder a todos los videos educativos
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onTodosVideosClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = azulContenido
            )
        ) {
            Text("▶ Todos los videos educativos")
        }

        Spacer(Modifier.height(18.dp))

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                Text(
                    text = "Crear nueva cápsula",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(14.dp))

                Button(
                    onClick = { abrirFormulario(true) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = azulContenido
                    )
                ) {
                    Text("📍 Crear cápsula con QR")
                }

                Text(
                    text = "Asociada a un punto turístico. El Administrador asignará el video y el QR.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(Modifier.height(14.dp))

                OutlinedButton(
                    onClick = { abrirFormulario(false) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📚 Crear cápsula sin QR")
                }

                Text(
                    text = "Contenido educativo general, sin punto turístico ni QR.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        if (mensaje.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = mensaje,
                color = azulContenido
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Cápsulas creadas (${contenidos.size})",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        if (contenidos.isEmpty()) {

            Text(
                text = "Todavía no hay cápsulas creadas.",
                color = Color.Gray
            )

        } else {

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 28.dp)
            ) {

                items(
                    items = contenidos,
                    key = { it.id }
                ) { contenido ->

                    val nombrePunto = puntos.firstOrNull {
                        it.id == contenido.puntoId
                    }?.nombre

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            Text(
                                text = contenido.titulo,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = contenido.descripcion,
                                color = Color(0xFF475569)
                            )

                            Spacer(Modifier.height(10.dp))

                            Text(
                                text = "Categoría: ${contenido.categoria}",
                                color = azulContenido
                            )

                            Text(
                                text = if (contenido.puntoId != null) {
                                    "Tipo: Con QR"
                                } else {
                                    "Tipo: Sin QR"
                                }
                            )

                            if (contenido.puntoId != null) {
                                Text(
                                    text = "Punto turístico: " +
                                            (nombrePunto
                                                ?: "ID ${contenido.puntoId}")
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            OutlinedButton(
                                onClick = {
                                    abrirFormulario(
                                        contenido.puntoId != null,
                                        contenido
                                    )
                                }
                            ) {
                                Text("Editar cápsula")
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarFormulario) {

        AlertDialog(
            onDismissRequest = {
                if (!guardando) mostrarFormulario = false
            },

            shape = RoundedCornerShape(20.dp),

            title = {
                Text(
                    text = if (editando == null) {
                        if (conQr) {
                            "Nueva cápsula con QR"
                        } else {
                            "Nueva cápsula sin QR"
                        }
                    } else {
                        "Editar cápsula"
                    },
                    fontWeight = FontWeight.Bold
                )
            },

            text = {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 460.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Text(
                        text = if (conQr) {
                            "Esta cápsula estará asociada a un punto turístico."
                        } else {
                            "Esta cápsula no necesita punto turístico ni código QR."
                        },
                        color = Color.Gray,
                        fontSize = 13.sp
                    )

                    OutlinedTextField(
                        value = titulo,
                        onValueChange = { titulo = it },
                        label = { Text("Título de la cápsula") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción educativa") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    ExposedDropdownMenuBox(
                        expanded = menuCategoria,
                        onExpandedChange = {
                            menuCategoria = !menuCategoria
                        }
                    ) {

                        OutlinedTextField(
                            value = categoria,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Clasificación de IA") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = menuCategoria
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = menuCategoria,
                            onDismissRequest = {
                                menuCategoria = false
                            }
                        ) {

                            categoriasContenido.forEach { opcion ->

                                DropdownMenuItem(
                                    text = { Text(opcion) },
                                    onClick = {
                                        categoria = opcion
                                        menuCategoria = false
                                    }
                                )
                            }
                        }
                    }

                    if (categoria == "Otra") {

                        OutlinedTextField(
                            value = otraCategoria,
                            onValueChange = {
                                otraCategoria = it
                            },
                            label = {
                                Text("Especifica la categoría")
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (conQr) {

                        ExposedDropdownMenuBox(
                            expanded = menuPunto,
                            onExpandedChange = {
                                menuPunto = !menuPunto
                            }
                        ) {

                            OutlinedTextField(
                                value = puntos.firstOrNull {
                                    it.id == puntoId
                                }?.nombre ?: "Seleccionar punto turístico",
                                onValueChange = {},
                                readOnly = true,
                                label = {
                                    Text("Punto turístico")
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = menuPunto
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = menuPunto,
                                onDismissRequest = {
                                    menuPunto = false
                                }
                            ) {

                                puntos.forEach { punto ->

                                    DropdownMenuItem(
                                        text = {
                                            Text(punto.nombre)
                                        },
                                        onClick = {
                                            puntoId = punto.id
                                            menuPunto = false
                                        }
                                    )
                                }
                            }
                        }

                        if (puntos.isEmpty()) {
                            Text(
                                text = "No se encontraron puntos turísticos.",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            },

            confirmButton = {

                val categoriaValida =
                    categoria != "Otra" || otraCategoria.isNotBlank()

                Button(
                    enabled = !guardando &&
                            titulo.isNotBlank() &&
                            descripcion.isNotBlank() &&
                            categoriaValida &&
                            (!conQr || puntoId != null),

                    onClick = {

                        val categoriaFinal = if (
                            categoria == "Otra"
                        ) {
                            otraCategoria.trim()
                        } else {
                            categoria
                        }

                        val contenidoActual = editando
                        val puntoSeleccionado = if (conQr) {
                            puntoId
                        } else {
                            null
                        }

                        val tituloGuardar = titulo.trim()
                        val descripcionGuardar = descripcion.trim()

                        scope.launch {

                            guardando = true

                            try {

                                withContext(Dispatchers.IO) {

                                    if (contenidoActual == null) {

                                        dao.crear(
                                            ContenidoEducativo(
                                                titulo = tituloGuardar,
                                                descripcion = descripcionGuardar,
                                                categoria = categoriaFinal,
                                                estado = "PUBLICADA",
                                                puntoId = puntoSeleccionado
                                            )
                                        )

                                    } else {

                                        dao.actualizar(
                                            id = contenidoActual.id,
                                            titulo = tituloGuardar,
                                            descripcion = descripcionGuardar,
                                            categoria = categoriaFinal,
                                            puntoId = puntoSeleccionado
                                        )

                                        if (
                                            contenidoActual.estado != "PUBLICADA"
                                        ) {
                                            dao.cambiarEstado(
                                                contenidoActual.id,
                                                "PUBLICADA"
                                            )
                                        }
                                    }
                                }

                                mostrarFormulario = false
                                mensaje = "Cápsula guardada correctamente."

                            } catch (e: Exception) {

                                mensaje = "No se pudo guardar: " +
                                        (e.message ?: "Error desconocido")

                            } finally {
                                guardando = false
                            }
                        }
                    }
                ) {
                    Text(
                        if (guardando) {
                            "Guardando..."
                        } else {
                            "Guardar cápsula"
                        }
                    )
                }
            },

            dismissButton = {
                TextButton(
                    enabled = !guardando,
                    onClick = {
                        mostrarFormulario = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
