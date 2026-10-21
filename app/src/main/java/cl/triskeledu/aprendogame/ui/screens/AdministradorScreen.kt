package cl.triskeledu.aprendogame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.triskeledu.aprendogame.data.local.AppDatabase

private val AzulAdmin = Color(0xFF2563EB)
private val FondoAdmin = Color(0xFFF5F7FB)
private val TextoAdmin = Color(0xFF17253D)
private val TextoSecundarioAdmin = Color(0xFF64748B)

@Composable
fun AdministradorScreen(
    onGestionPuntosClick: () -> Unit,
    onGestionVideosClick: () -> Unit = {},
    onGestionQrClick: () -> Unit = {},
    onTodosVideosClick: () -> Unit = {}
) {
    val context = LocalContext.current

    val database = remember(context) {
        AppDatabase.getInstance(context.applicationContext)
    }

    val usuarioDao = remember(database) {
        database.usuarioDao()
    }

    // Observa los usuarios registrados en Room.
    // La interfaz se actualiza si cambia la lista.
    val usuarios by remember(usuarioDao) {
        usuarioDao.obtenerTodosLosUsuarios()
    }.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoAdmin),
        contentPadding = PaddingValues(
            start = 18.dp,
            end = 18.dp,
            top = 24.dp,
            bottom = 100.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ENCABEZADO
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFE6EDFF)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = AzulAdmin,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "Panel de Administrador",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoAdmin
                    )

                    Text(
                        text = "Gestiona el contenido de Triskeledu",
                        fontSize = 12.sp,
                        color = TextoSecundarioAdmin
                    )
                }
            }
        }

        // HERRAMIENTAS
        item {
            Spacer(Modifier.height(4.dp))

            Text(
                text = "Herramientas de administración",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextoAdmin
            )
        }

        item {
            HerramientaAdministrador(
                titulo = "Puntos turísticos",
                descripcion = "Administrar lugares de Santiago",
                icono = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = AzulAdmin,
                        modifier = Modifier.size(25.dp)
                    )
                },
                onClick = onGestionPuntosClick
            )
        }

        item {
            HerramientaAdministrador(
                titulo = "Asignar videos",
                descripcion = "Vincular archivos MP4 a las cápsulas",
                icono = {
                    Icon(
                        imageVector = Icons.Default.VideoLibrary,
                        contentDescription = null,
                        tint = AzulAdmin,
                        modifier = Modifier.size(25.dp)
                    )
                },
                onClick = onGestionVideosClick
            )
        }

        item {
            HerramientaAdministrador(
                titulo = "Códigos QR",
                descripcion = "Generar QR de cápsulas con video",
                icono = {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = null,
                        tint = AzulAdmin,
                        modifier = Modifier.size(25.dp)
                    )
                },
                onClick = onGestionQrClick
            )
        }

        item {
            HerramientaAdministrador(
                titulo = "Todos los videos",
                descripcion = "Consultar y reproducir la biblioteca educativa",
                icono = {
                    Icon(Icons.Default.VideoLibrary, contentDescription = null,
                        tint = AzulAdmin, modifier = Modifier.size(25.dp))
                },
                onClick = onTodosVideosClick
            )
        }

        // USUARIOS
        item {
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Usuarios registrados",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoAdmin
                )

                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = Color(0xFFE6EDFF)
                ) {
                    Text(
                        text = "${usuarios.size} usuarios",
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        ),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AzulAdmin
                    )
                }
            }
        }

        if (usuarios.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Text(
                        text = "No hay usuarios registrados.",
                        modifier = Modifier.padding(20.dp),
                        color = TextoSecundarioAdmin
                    )
                }
            }
        } else {
            items(
                items = usuarios,
                key = { usuario -> usuario.id }
            ) { usuario ->

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 1.dp
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            Surface(
                                modifier = Modifier.size(44.dp),
                                shape = CircleShape,
                                color = Color(0xFFE6EDFF)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = AzulAdmin,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = usuario.nombre,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextoAdmin
                                )

                                Text(
                                    text = "@${usuario.alias}",
                                    fontSize = 13.sp,
                                    color = TextoSecundarioAdmin
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF1F5F9)
                            ) {
                                Text(
                                    text = usuario.rol.toString(),
                                    modifier = Modifier.padding(
                                        horizontal = 9.dp,
                                        vertical = 6.dp
                                    ),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF475569)
                                )
                            }
                        }

                        HorizontalDivider(
                            color = Color(0xFFE2E8F0)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = TextoSecundarioAdmin,
                                modifier = Modifier.size(17.dp)
                            )

                            Text(
                                text = usuario.correo.ifBlank {
                                    "Correo no disponible"
                                },
                                fontSize = 13.sp,
                                color = Color(0xFF475569)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Tarjeta reutilizable para las tres herramientas.
@Composable
private fun HerramientaAdministrador(
    titulo: String,
    descripcion: String,
    icono: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFEEF3FF)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    icono()
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = titulo,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoAdmin
                )

                Text(
                    text = descripcion,
                    fontSize = 12.sp,
                    color = TextoSecundarioAdmin
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(15.dp)
            )
        }
    }
}