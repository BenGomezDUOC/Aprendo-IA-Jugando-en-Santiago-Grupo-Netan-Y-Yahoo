package cl.triskeledu.aprendogame.data.local

import cl.triskeledu.aprendogame.model.Rol
import cl.triskeledu.aprendogame.model.Usuario
import cl.triskeledu.aprendogame.security.PasswordUtils

/**
 * Crea las cuentas institucionales de Triskeledu.
 *
 * Cada cuenta se identifica mediante tipoCuentaSistema.
 *
 * Si la cuenta ya existe, sincroniza su contraseña con
 * el valor canónico ("123") para garantizar el acceso.
 * El progreso, avatar y puntos del usuario se conservan.
 */
object CuentasPredefinidas {

    private const val CONTRASENA_ADMIN     = "123"
    private const val CONTRASENA_GENERADOR = "123"

    suspend fun crearSiNoExisten(database: AppDatabase) {

        val usuarioDao = database.usuarioDao()

        // -----------------------------------------
        // 1. CUENTA DE ADMINISTRADOR
        // -----------------------------------------

        val adminExistente =
            usuarioDao.buscarCuentaSistema("ADMIN_PRINCIPAL")

        if (adminExistente == null) {
            // Primera instalación: crea la cuenta desde cero.
            val administrador = Usuario(
                nombre = "Administrador",
                alias = "Admin",
                correo = "admin@triskeledu.cl",
                contrasena = PasswordUtils.crearHash(CONTRASENA_ADMIN),
                rol = Rol.ADMINISTRADOR,
                tipoCuentaSistema = "ADMIN_PRINCIPAL"
            )
            usuarioDao.registrarJugador(administrador)
        } else {
            // La cuenta ya existe: sincroniza la contraseña canónica.
            usuarioDao.actualizarCuentaPredefinida(
                id         = adminExistente.id,
                nombre     = adminExistente.nombre,
                alias      = adminExistente.alias,
                contrasena = PasswordUtils.crearHash(CONTRASENA_ADMIN),
                rol        = Rol.ADMINISTRADOR.name
            )
        }

        // -----------------------------------------
        // 2. CUENTA DE GENERADOR DE CONTENIDO
        // -----------------------------------------

        val generadorExistente =
            usuarioDao.buscarCuentaSistema("GENERADOR_PRINCIPAL")

        if (generadorExistente == null) {
            // Primera instalación: crea la cuenta desde cero.
            val generador = Usuario(
                nombre = "Contenido",
                alias = "GenCont",
                correo = "contenido@triskeledu.cl",
                contrasena = PasswordUtils.crearHash(CONTRASENA_GENERADOR),
                rol = Rol.GENERADOR,
                tipoCuentaSistema = "GENERADOR_PRINCIPAL"
            )
            usuarioDao.registrarJugador(generador)
        } else {
            // La cuenta ya existe: sincroniza la contraseña canónica.
            usuarioDao.actualizarCuentaPredefinida(
                id         = generadorExistente.id,
                nombre     = generadorExistente.nombre,
                alias      = generadorExistente.alias,
                contrasena = PasswordUtils.crearHash(CONTRASENA_GENERADOR),
                rol        = Rol.GENERADOR.name
            )
        }
    }
}
