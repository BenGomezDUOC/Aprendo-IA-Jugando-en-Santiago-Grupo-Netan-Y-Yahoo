package cl.triskeledu.aprendogame.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room que representa un punto turístico georreferenciado de Santiago.
 * Fernando la utilizará para pintar marcadores en el mapa y validar cercanía por GPS.
 */
@Entity(tableName = "puntos_interes")
data class PuntoInteres(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val descripcion: String,
    val latitud: Double,
    val longitud: Double,
    val altitud: Double,
    val completado: Boolean = false,
    val radioToleranciaMetros: Double = 50.0
)