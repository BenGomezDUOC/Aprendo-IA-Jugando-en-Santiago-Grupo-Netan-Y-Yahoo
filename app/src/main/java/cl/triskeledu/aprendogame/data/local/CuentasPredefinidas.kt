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

        if (usuarioDao.buscarPorCorreo(correoAdmin) == null) {

            val administrador = Usuario(
                nombre = "Administrador",
                alias = "Admin",
                correo = correoAdmin,
                contrasena = PasswordUtils.crearHash("Admin12345!"),
                rol = Rol.ADMINISTRADOR
            )

            usuarioDao.registrarJugador(administrador)
        }

        // -----------------------------------------
        // 2. CUENTA DE GENERADOR DE CONTENIDO
        // -----------------------------------------

        val correoGenerador = "generador@triskeledu.cl"

        if (usuarioDao.buscarPorCorreo(correoGenerador) == null) {

            val generador = Usuario(
                nombre = "Generador de contenido",
                alias = "Generador",
                correo = correoGenerador,
                contrasena = PasswordUtils.crearHash("Generador12345!"),
                rol = Rol.GENERADOR
            )

            usuarioDao.registrarJugador(generador)
        }
    }
}
