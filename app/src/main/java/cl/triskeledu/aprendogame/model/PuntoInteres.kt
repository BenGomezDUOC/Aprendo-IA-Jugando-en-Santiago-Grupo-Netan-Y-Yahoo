
package cl.triskeledu.aprendogame.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room que representa un punto turístico
 * georreferenciado de Santiago.
 */
@Entity(tableName = "puntos_interes")
data class PuntoInteres(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val nombre: String,

    val descripcion: String,

    // Coordenadas obtenidas automáticamente.
    val latitud: Double,

    val longitud: Double,

    // Altitud aproximada del terreno en metros.
    val altitud: Double,

    val completado: Boolean = false,

    // Radio predeterminado de tolerancia GPS.
    val radioToleranciaMetros: Double = 50.0,

    val imagenUri: String? = null,

    // Dirección registrada por el administrador.
    @ColumnInfo(defaultValue = "''")
    val direccion: String = ""
)

