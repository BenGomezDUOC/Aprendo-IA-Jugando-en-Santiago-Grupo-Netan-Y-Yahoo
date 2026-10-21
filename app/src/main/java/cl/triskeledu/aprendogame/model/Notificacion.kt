
package cl.triskeledu.aprendogame.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notificaciones",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["usuarioId"])
    ]
)
data class Notificacion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val usuarioId: Int,

    val titulo: String,

    val mensaje: String,

    val tipo: String,

    val rutaDestino: String,

    val fechaTimestamp: Long = System.currentTimeMillis(),

    val leida: Boolean = false
)
