package cl.triskeledu.aprendogame.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad de Room que representa una cápsula educativa de Inteligencia Artificial.
 * Se asocia a un punto del mapa y contiene el código QR que Mary debe escanear
 * y el video/puntos que Juan Pablo reproducirá y otorgará.
 */
@Entity(
    tableName = "capsulas",
    foreignKeys = [
        ForeignKey(
            entity = PuntoInteres::class,
            parentColumns = ["id"],
            childColumns = ["puntoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["puntoId"]),
        Index(value = ["codigoQr"], unique = true)
    ]
)
data class Capsula(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,
    val codigoQr: String,
    val urlVideo: String,
    val puntos: Int,
    val puntoId: Int,
    val descripcion: String = ""
)