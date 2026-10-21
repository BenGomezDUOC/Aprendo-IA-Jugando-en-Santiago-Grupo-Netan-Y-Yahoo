package cl.triskeledu.aprendogame.validation

import java.util.regex.Pattern

/**
 * Validador centralizado y desacoplado de la interfaz de usuario (IE 2.2.1).
 * Gestiona las reglas de negocio y validación de los formularios de la aplicación
 * (Registro de Jugador, Autenticación y Gestión de Puntos).
 */
object FormularioValidator {

    private val PATRON_CORREO: Pattern = Pattern.compile(
        "[a-zA-Z0-9+._%\\-]{1,256}" +
        "@" +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
        "(" +
        "\\." +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
        ")+"
    )

    fun validarNombre(nombre: String): String? {
        val trimmed = nombre.trim()
        return when {
            trimmed.isBlank() -> "Ingresa tu nombre completo."
            trimmed.length < 3 -> "El nombre debe tener al menos 3 caracteres."
            trimmed.length > 50 -> "El nombre no puede exceder 50 caracteres."
            else -> null
        }
    }

    fun validarAlias(alias: String): String? {
        val trimmed = alias.trim()
        return when {
            trimmed.isBlank() -> "Ingresa un alias."
            trimmed.length < 3 -> "El alias debe tener al menos 3 caracteres."
            trimmed.length > 20 -> "El alias no puede exceder 20 caracteres."
            else -> null
        }
    }

    fun validarCorreo(correo: String): String? {
        val trimmed = correo.trim()
        return when {
            trimmed.isBlank() -> "Ingresa tu correo electrónico."
            !PATRON_CORREO.matcher(trimmed).matches() -> "Ingresa un correo electrónico válido."
            else -> null
        }
    }

    fun validarTelefono(telefono: String): String? {
        val trimmed = telefono.trim()
        val digitos = trimmed.filter { it.isDigit() }
        return when {
            trimmed.isBlank() -> "Ingresa tu teléfono."
            digitos.length < 8 || digitos.length > 15 -> "Ingresa un teléfono válido (8 a 15 dígitos)."
            else -> null
        }
    }

    fun validarContrasena(contrasena: String): String? {
        return when {
            contrasena.isBlank() -> "Ingresa una contraseña."
            contrasena.length < 8 -> "La contraseña debe tener al menos 8 caracteres."
            else -> null
        }
    }

    fun validarConfirmacion(contrasena: String, confirmarContrasena: String): String? {
        return when {
            confirmarContrasena.isBlank() -> "Confirma tu contraseña."
            confirmarContrasena != contrasena -> "Las contraseñas no coinciden."
            else -> null
        }
    }

    data class EstadoFormularioRegistro(
        val errorNombre: String?,
        val errorAlias: String?,
        val errorCorreo: String?,
        val errorTelefono: String?,
        val errorContrasena: String?,
        val errorConfirmacion: String?
    ) {
        val esValido: Boolean
            get() = errorNombre == null &&
                    errorAlias == null &&
                    errorCorreo == null &&
                    errorTelefono == null &&
                    errorContrasena == null &&
                    errorConfirmacion == null
    }

    fun validarFormularioCompleto(
        nombre: String,
        alias: String,
        correo: String,
        telefono: String,
        contrasena: String,
        confirmarContrasena: String
    ): EstadoFormularioRegistro {
        return EstadoFormularioRegistro(
            errorNombre = validarNombre(nombre),
            errorAlias = validarAlias(alias),
            errorCorreo = validarCorreo(correo),
            errorTelefono = validarTelefono(telefono),
            errorContrasena = validarContrasena(contrasena),
            errorConfirmacion = validarConfirmacion(contrasena, confirmarContrasena)
        )
    }
}
