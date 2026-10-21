
package cl.triskeledu.aprendogame.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.triskeledu.aprendogame.model.Notificacion
import cl.triskeledu.aprendogame.viewmodel.GameViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesScreen(
    gameViewModel: GameViewModel,
    onVolver: () -> Unit,
    onAbrirDestino: (String) -> Unit
) {
    val notificaciones by gameViewModel.notificaciones.collectAsState()
    val noLeidas by gameViewModel.notificacionesNoLeidas.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Notificaciones")
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    if (noLeidas > 0) {
                        IconButton(
                            onClick = {
                                gameViewModel.marcarTodasNotificacionesLeidas()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Marcar todas como leídas"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->

        if (notificaciones.isEmpty()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Todavía no tienes notificaciones",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Aquí aparecerán los avisos relacionados con tus actividades en Triskeledu.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

        } else {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = notificaciones,
                    key = { it.id }
                ) { notificacion ->

                    TarjetaNotificacion(
                        notificacion = notificacion,
                        onClick = {
                            gameViewModel.marcarNotificacionLeida(
                                notificacion.id
                            )
                            onAbrirDestino(notificacion.rutaDestino)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaNotificacion(
    notificacion: Notificacion,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                if (notificacion.leida) {
                    MaterialTheme.colorScheme.surfaceVariant
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = notificacion.titulo,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notificacion.mensaje,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = formatearFechaNotificacion(
                        notificacion.fechaTimestamp
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!notificacion.leida) {
                Spacer(modifier = Modifier.width(8.dp))

                Badge {
                    Text("Nueva")
                }
            }
        }
    }
}

private fun formatearFechaNotificacion(
    fechaTimestamp: Long
): String {
    val formato = SimpleDateFormat(
        "dd/MM/yyyy HH:mm",
        Locale.getDefault()
    )

    return formato.format(Date(fechaTimestamp))
}



