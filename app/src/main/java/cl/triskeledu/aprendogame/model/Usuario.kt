
package cl.triskeledu.aprendogame.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representa a un usuario de Triskeledu.
 *
 * Guarda sus datos personales, rol, avatar y puntos.
 *
 * tipoCuentaSistema identifica únicamente las
 * cuentas predefinidas de la aplicación.
 */
@Entity(
    tableName = "usuarios",
    indices = [
        Index(value = ["correo"], unique = true),
        Index(value = ["tipoCuentaSistema"], unique = true)
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
    val puntosAcumulados: Int = 0,

    // Solo las cuentas predefinidas tienen un valor.
    // Los demás usuarios tienen null.
    val tipoCuentaSistema: String? = null
)

