package cl.triskeledu.aprendogame.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.triskeledu.aprendogame.data.local.AppDatabase
import cl.triskeledu.aprendogame.data.repository.GameRepository
import cl.triskeledu.aprendogame.data.repository.GameRepositoryImpl
import cl.triskeledu.aprendogame.model.PuntoInteres
import cl.triskeledu.aprendogame.model.Usuario
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel principal del juego conectado al Repositorio de persistencia Room.
 * Provee el estado reactivo consumido por la interfaz de usuario en Compose.
 */
class GameViewModel(
    application: Application,
    private val repository: GameRepository = GameRepositoryImpl(AppDatabase.getInstance(application))
) : AndroidViewModel(application) {

    // Lista reactiva de puntos turísticos proveniente de la base de datos SQLite/Room
    val puntos: StateFlow<List<PuntoInteres>> = repository.getPuntos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Perfil y puntajes del usuario observados en tiempo real
    val usuario: StateFlow<Usuario?> = repository.getUsuario()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Marca un punto como completado y persiste el cambio en Room
    fun marcarCompletado(id: Int) {
        viewModelScope.launch {
            repository.marcarPuntoCompletado(id, puntosRecompensa = 100)
        }
    }
}