package cl.triskeledu.aprendogame.data.local

import androidx.room.TypeConverter
import cl.triskeledu.aprendogame.model.Rol

/**
 * Convertidores de tipo para tipos de datos personalizados en Room.
 */
class Converters {
    @TypeConverter
    fun fromRol(rol: Rol): String = rol.name

    @TypeConverter
    fun toRol(value: String): Rol = try {
        Rol.valueOf(value)
    } catch (e: IllegalArgumentException) {
        Rol.CLIENTE
    }
}
