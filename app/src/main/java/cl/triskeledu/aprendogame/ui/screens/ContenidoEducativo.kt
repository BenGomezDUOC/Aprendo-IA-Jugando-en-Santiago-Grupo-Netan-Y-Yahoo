
package cl.triskeledu.aprendogame.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contenidos_educativos",
    foreignKeys = [
        ForeignKey(
            entity = PuntoInteres::class,
            parentColumns = ["id"],
            childColumns = ["puntoId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["puntoId"]),
        Index(value = ["codigoQr"], unique = true)
    ]
)
data class ContenidoEducativo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val titulo: String,
    val descripcion: String,
    val categoria: String = "General",

    // BORRADOR o PUBLICADA
    val estado: String = "BORRADOR",

    // null significa que no tiene punto turístico.
    val puntoId: Int? = null,

    // null significa que todavía no tiene un QR asignado.
    val codigoQr: String? = null
)


