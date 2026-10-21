
package cl.triskeledu.aprendogame.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.triskeledu.aprendogame.model.Usuario
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    // Obtiene un usuario mediante su ID.
    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    fun getUsuario(id: Int): Flow<Usuario?>

    // Obtiene un usuario directamente mediante su ID.
    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun getUsuarioDirecto(id: Int): Usuario?

    // Suma puntos únicamente al usuario indicado.
    @Query("""
        UPDATE usuarios
        SET puntosAcumulados = puntosAcumulados + :puntos
        WHERE id = :id
    """)
    suspend fun sumarPuntos(id: Int, puntos: Int)

    // Guarda o actualiza un usuario existente.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarOActualizar(usuario: Usuario)

    // Obtiene todos los jugadores con rol CLIENTE.
    @Query("""
        SELECT * FROM usuarios
        WHERE rol = 'CLIENTE'
        ORDER BY nombre ASC
    """)
    fun obtenerJugadores(): Flow<List<Usuario>>

    // Registra un jugador nuevo y devuelve su ID.
    @Insert
    suspend fun registrarJugador(usuario: Usuario): Long

    // Comprueba si existe un correo registrado.
    @Query("SELECT COUNT(*) FROM usuarios WHERE correo = :correo")
    suspend fun contarPorCorreo(correo: String): Int

    // Busca una cuenta mediante su correo.
    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): Usuario?

    // NUEVO: busca una cuenta predefinida por su identificador.
    // No depende del correo, nombre ni rol.
    @Query("""
        SELECT * FROM usuarios
        WHERE tipoCuentaSistema = :tipo
        LIMIT 1
    """)
    suspend fun buscarCuentaSistema(tipo: String): Usuario?

    // Obtiene todas las cuentas, independientemente del rol.
    @Query("SELECT * FROM usuarios ORDER BY nombre ASC")
    fun obtenerTodosLosUsuarios(): Flow<List<Usuario>>

    // Elimina un usuario por su correo.
    @Query("DELETE FROM usuarios WHERE correo = :correo")
    suspend fun eliminarPorCorreo(correo: String)

    // Elimina exclusivamente la cuenta inicial de demostración.
    // Devuelve la cantidad de registros eliminados.
    @Query("""
        DELETE FROM usuarios
        WHERE id = 1
          AND nombre = 'Explorador Santiago'
          AND correo = ''
          AND contrasena = ''
    """)
    suspend fun eliminarUsuarioInicial(): Int

    // Actualiza los datos de una cuenta predefinida
    // sin eliminar al usuario ni su progreso.
    @Query("""
        UPDATE usuarios
        SET nombre = :nombre,
            alias = :alias,
            contrasena = :contrasena,
            rol = :rol
        WHERE id = :id
    """)
    suspend fun actualizarCuentaPredefinida(
        id: Int,
        nombre: String,
        alias: String,
        contrasena: String,
        rol: String
    )

    // Edita los datos personales del usuario.
    // Conserva su ID, rol, puntos y progreso.
    @Query("""
        UPDATE usuarios
        SET nombre = :nombre,
            alias = :alias,
            correo = :correo,
            telefono = :telefono,
            direccion = :direccion,
            avatar = :avatar,
            contrasena = :contrasenaHash
        WHERE id = :usuarioId
    """)
    suspend fun actualizarPerfil(
        usuarioId: Int,
        nombre: String,
        alias: String,
        correo: String,
        telefono: String,
        direccion: String,
        avatar: Int,
        contrasenaHash: String
    ): Int

    // Comprueba si otro usuario ya utiliza el correo.
    // Excluye al usuario que está editando su perfil.
    @Query("""
        SELECT COUNT(*) FROM usuarios
        WHERE correo = :correo
          AND id != :usuarioId
    """)
    suspend fun contarCorreoDeOtroUsuario(
        correo: String,
        usuarioId: Int
    ): Int
}



