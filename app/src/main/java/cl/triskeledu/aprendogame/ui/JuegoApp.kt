package cl.triskeledu.aprendogame.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.triskeledu.aprendogame.model.EstadoJuego
import cl.triskeledu.aprendogame.viewmodel.GameViewModel

@Composable
fun JuegoApp(viewModel: GameViewModel = viewModel()) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // El 'when' decide qué pantalla mostrar según el estado actual.
        when (val e = estado) {
            is EstadoJuego.Cargando -> {
                CircularProgressIndicator()
                Text("Cargando preguntas...")
            }
            is EstadoJuego.Jugando -> {
                Text("Pregunta ${e.numero} de ${e.total}", style = MaterialTheme.typography.labelLarge)
                Text(e.pregunta.texto, style = MaterialTheme.typography.headlineSmall)
                e.pregunta.opciones.forEachIndexed { i, opcion ->
                    Button(onClick = { viewModel.responder(i) }, modifier = Modifier.fillMaxWidth()) {
                        Text(opcion)
                    }
                }
            }
            is EstadoJuego.Terminado -> {
                Text("¡Terminaste!", style = MaterialTheme.typography.headlineMedium)
                Text("Puntaje: ${e.puntaje} de ${e.total}")
                Button(onClick = viewModel::iniciar) { Text("Jugar de nuevo") }
            }
        }
    }
}