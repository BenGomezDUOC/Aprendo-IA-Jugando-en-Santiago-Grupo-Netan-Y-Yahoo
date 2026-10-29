package cl.triskeledu.aprendogame.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import cl.triskeledu.aprendogame.data.local.AppDatabase
import cl.triskeledu.aprendogame.data.repository.GameRepository
import cl.triskeledu.aprendogame.data.repository.GameRepositoryImpl
import cl.triskeledu.aprendogame.model.Usuario
import cl.triskeledu.aprendogame.model.VisitaRealizada
import cl.triskeledu.aprendogame.security.PasswordUtils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SessionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository =
        GameRepositoryImpl(AppDatabase.getInstance(application))

    // --------------------------------------------------
    // JUGADOR ACTIVO
    // --------------------------------------------------

    private val _usuarioActivoId = MutableStateFlow<Int?>(null)

    val usuarioActivoId: StateFlow<Int?> = _usuarioActivoId.asStateFlow()

    fun setUsuarioActivo(id: Int?) {
        _usuarioActivoId.value = id
    }

    fun cerrarSesion() {
        _usuarioActivoId.value = null
    }

    // --------------------------------------------------
    // USUARIO AUTENTICADO
    // --------------------------------------------------

    val usuario: StateFlow<Usuario?> =
        _usuarioActivoId.flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                repository.getUsuario(id)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // --------------------------------------------------
    // HISTORIAL DE VISITAS
    // --------------------------------------------------

    val historialVisitas: StateFlow<List<VisitaRealizada>> =
        _usuarioActivoId.flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyList())
            } else {
                repository.getHistorialVisitas(id)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --------------------------------------------------
    // EDICIÓN DEL PERFIL
    // --------------------------------------------------

    /**
     * Edita el perfil del usuario que inició sesión.
     *
     * Si no escribe una contraseña nueva,
     * conserva la contraseña actual.
     *
     * No modifica el ID, rol, puntos ni progreso.
     */
    fun editarPerfil(
        nombre: String,
        alias: String,
        correo: String,
        telefono: String,
        direccion: String,
        avatar: Int,
        contrasenaActual: String,
        contrasenaNueva: String,
        onResultado: (Boolean, String) -> Unit
    ) {

        // Obtiene el ID del usuario que inició sesión.
        val idJugador = _usuarioActivoId.value

        if (idJugador == null) {
            onResultado(false, "Debes iniciar sesión")
            return
        }

        viewModelScope.launch {

            try {

                // 1. Validar el nombre.
                if (nombre.isBlank()) {
                    onResultado(false, "El nombre es obligatorio")
                    return@launch
                }

                // 2. Validar el correo.
                val correoLimpio = correo.trim().lowercase()

                if (!android.util.Patterns.EMAIL_ADDRESS
                        .matcher(correoLimpio).matches()
                ) {
                    onResultado(false, "Ingresa un correo válido")
                    return@launch
                }

                // 3. Comprobar que otro usuario no tenga el correo.
                val correoOcupado = withContext(Dispatchers.IO) {
                    repository.contarCorreoDeOtroUsuario(
                        correo = correoLimpio,
                        usuarioId = idJugador
                    )
                }

                if (correoOcupado > 0) {
                    onResultado(false, "Este correo ya está registrado")
                    return@launch
                }

                // 4. Obtener los datos actuales del usuario.
                val usuarioActual = withContext(Dispatchers.IO) {
                    repository.getUsuarioDirecto(idJugador)
                }

                if (usuarioActual == null) {
                    onResultado(false, "No se encontró el usuario")
                    return@launch
                }

                // 5. Conservar la contraseña existente por defecto.
                var hashParaGuardar = usuarioActual.contrasena

                // Solo cambia la contraseña si escribe una nueva.
                if (contrasenaNueva.isNotEmpty()) {

                    if (contrasenaActual.isEmpty()) {
                        onResultado(
                            false,
                            "Escribe tu contraseña actual"
                        )
                        return@launch
                    }

                    // Verifica que la contraseña actual sea correcta.
                    val contrasenaCorrecta = withContext(Dispatchers.Default) {
                        PasswordUtils.verificarContrasena(
                            contrasenaActual,
                            usuarioActual.contrasena
                        )
                    }

                    if (!contrasenaCorrecta) {
                        onResultado(
                            false,
                            "La contraseña actual es incorrecta"
                        )
                        return@launch
                    }

                    if (contrasenaNueva.length < 8) {
                        onResultado(
                            false,
                            "La contraseña nueva debe tener al menos 8 caracteres"
                        )
                        return@launch
                    }

                    // Protege la contraseña nueva mediante hash.
                    hashParaGuardar = withContext(Dispatchers.Default) {
                        PasswordUtils.crearHash(contrasenaNueva)
                    }
                }

                // 6. Guardar los cambios en Room.
                val filasActualizadas = withContext(Dispatchers.IO) {
                    repository.actualizarPerfil(
                        usuarioId = idJugador,
                        nombre = nombre.trim(),
                        alias = alias.trim(),
                        correo = correoLimpio,
                        telefono = telefono.trim(),
                        direccion = direccion.trim(),
                        avatar = avatar,
                        contrasenaHash = hashParaGuardar
                    )
                }

                // 7. Informar el resultado.
                if (filasActualizadas > 0) {
                    onResultado(
                        true,
                        "Perfil actualizado correctamente"
                    )
                } else {
                    onResultado(
                        false,
                        "No se pudieron guardar los cambios"
                    )
                }

            } catch (e: Exception) {
                onResultado(
                    false,
                    "Ocurrió un error al actualizar el perfil"
                )
            }
        }
    }
}
