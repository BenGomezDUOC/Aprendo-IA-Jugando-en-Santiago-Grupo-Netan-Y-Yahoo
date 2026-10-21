package cl.triskeledu.aprendogame.data.local

import cl.triskeledu.aprendogame.model.Rol
import cl.triskeledu.aprendogame.model.Usuario
import cl.triskeledu.aprendogame.security.PasswordUtils

/**
 * Crea las cuentas iniciales de Triskeledu.
 *
 * Cada cuenta se identifica mediante tipoCuentaSistema.
 *
 * Si ya existe, conserva su nombre, correo,
 * contraseña, avatar, puntos y progreso.
 */
object CuentasPredefinidas {

    suspend fun crearSiNoExisten(database: AppDatabase) {

        val usuarioDao = database.usuarioDao()

        // -----------------------------------------
        // 1. CUENTA DE ADMINISTRADOR
        // -----------------------------------------

        val adminExistente =
            usuarioDao.buscarCuentaSistema("ADMIN_PRINCIPAL")

        // Solo crea la cuenta si todavía no existe.
        if (adminExistente == null) {

            val administrador = Usuario(
                nombre = "Administrador",
                alias = "Admin",
                correo = "admin@triskeledu.cl",
                contrasena = PasswordUtils.crearHash("123"),
                rol = Rol.ADMINISTRADOR,
                tipoCuentaSistema = "ADMIN_PRINCIPAL"
            )

            usuarioDao.registrarJugador(administrador)
        }

        // -----------------------------------------
        // 2. CUENTA DE GENERADOR DE CONTENIDO
        // -----------------------------------------

        val generadorExistente =
            usuarioDao.buscarCuentaSistema("GENERADOR_PRINCIPAL")

        // Solo crea la cuenta si todavía no existe.
        if (generadorExistente == null) {

            val generador = Usuario(
                nombre = "Contenido",
                alias = "GenCont",
                correo = "contenido@triskeledu.cl",
                contrasena = PasswordUtils.crearHash("123"),
                rol = Rol.GENERADOR,
                tipoCuentaSistema = "GENERADOR_PRINCIPAL"
            )

            usuarioDao.registrarJugador(generador)
        }
    }
}



