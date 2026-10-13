package cl.triskeledu.aprendogame.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.triskeledu.aprendogame.viewmodel.GameViewModel

@Composable
fun JuegoApp(viewModel: GameViewModel = viewModel()) {
    val puntos by viewModel.puntos.collectAsStateWithLifecycle()
    // Función de orden superior: cuenta cuántos puntos están completados.
    val completados = puntos.count { it.completado }

    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp)) {
        Text("Aprendo IA Jugando en Santiago", style = MaterialTheme.typography.headlineSmall)
        Text("Completados: $completados de ${puntos.size}")
        Spacer(Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(puntos, key = { it.id }) { punto ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(punto.nombre, style = MaterialTheme.typography.titleMedium)
                        Text(punto.descripcion)
                        Text("Lat ${punto.latitud}, Lon ${punto.longitud}, Alt ${punto.altitud} m")
                        Button(
                            onClick = { viewModel.marcarCompletado(punto.id) },
                            enabled = !punto.completado,
                        ) {
                            Text(if (punto.completado) "Completado" else "Marcar completado")
                        }
                    }
                }
            }
        }
    }
}