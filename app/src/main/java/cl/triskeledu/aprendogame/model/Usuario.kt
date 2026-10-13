package cl.triskeledu.aprendogame.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room que representa al usuario local de la aplicación.
 * Permite gestionar perfiles (Cliente, Administrador, Generador) y acumular puntajes de gamificación.
 */
@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey
    val id: Int = 1,
    val nombre: String,
    val rol: Rol = Rol.CLIENTE,
    val puntosAcumulados: Int = 0
)
