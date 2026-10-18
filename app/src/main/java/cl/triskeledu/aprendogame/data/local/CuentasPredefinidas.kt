package cl.triskeledu.aprendogame.data.local

import cl.triskeledu.aprendogame.model.Rol
import cl.triskeledu.aprendogame.model.Usuario
import cl.triskeledu.aprendogame.security.PasswordUtils

/**
 * Crea las cuentas predefinidas del sistema.
 *
 * No modifica los clientes registrados.
 * No vuelve a insertar una cuenta si su correo ya existe.
 */
object CuentasPredefinidas {

    suspend fun crearSiNoExisten(database: AppDatabase) {

        val usuarioDao = database.usuarioDao()

        // -----------------------------------------
        // 1. CUENTA DE ADMINISTRADOR
        // -----------------------------------------

        val correoAdmin = "admin@triskeledu.cl"
        val hashAdmin = PasswordUtils.crearHash("Admin12345!")
        val adminExistente = usuarioDao.buscarPorCorreo(correoAdmin)

        if (adminExistente == null) {
            val administrador = Usuario(
                nombre = "Administrador",
                alias = "Admin",
                correo = correoAdmin,
                contrasena = hashAdmin,
                rol = Rol.ADMINISTRADOR
            )
            usuarioDao.registrarJugador(administrador)
        } else if (!PasswordUtils.verificarContrasena("Admin12345!", adminExistente.contrasena)) {
            usuarioDao.insertarOActualizar(
                adminExistente.copy(contrasena = hashAdmin)
            )
        }

        // -----------------------------------------
        // 2. CUENTA DE GENERADOR DE CONTENIDO
        // -----------------------------------------

        val correoGenerador = "generador@triskeledu.cl"
        val hashGenerador = PasswordUtils.crearHash("Generador12345!")
        val generadorExistente = usuarioDao.buscarPorCorreo(correoGenerador)

        if (generadorExistente == null) {
            val generador = Usuario(
                nombre = "Generador de contenido",
                alias = "Generador",
                correo = correoGenerador,
                contrasena = hashGenerador,
                rol = Rol.GENERADOR
            )
            usuarioDao.registrarJugador(generador)
        } else if (!PasswordUtils.verificarContrasena("Generador12345!", generadorExistente.contrasena)) {
            usuarioDao.insertarOActualizar(
                generadorExistente.copy(contrasena = hashGenerador)
            )
        }
    }
}
