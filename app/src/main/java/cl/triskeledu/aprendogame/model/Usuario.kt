
package cl.triskeledu.aprendogame.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representa a un usuario de Triskeledu.
 * Permite registrar jugadores y almacenar sus puntos.
 */
@Entity(
    tableName = "usuarios",
    indices = [
        Index(value = ["correo"], unique = true)
    ]
)
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val nombre: String,
    val alias: String = "",
    val correo: String = "",
    val telefono: String = "",
    val contrasena: String = "",
    val direccion: String = "",
    val avatar: Int = 1,

    val rol: Rol = Rol.CLIENTE,
    val puntosAcumulados: Int = 0
)
