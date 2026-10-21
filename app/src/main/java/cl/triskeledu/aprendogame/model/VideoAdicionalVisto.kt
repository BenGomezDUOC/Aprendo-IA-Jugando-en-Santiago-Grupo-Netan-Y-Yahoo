
package cl.triskeledu.aprendogame.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "videos_adicionales_vistos",

    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],

    indices = [
        Index(value = ["usuarioId"]),

        Index(
            value = ["usuarioId", "videoAdicionalId"],
            unique = true
        )
    ]
)
data class VideoAdicionalVisto(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Usuario que vio el video.
    val usuarioId: Int,

    // 1: IA en nuestra vida cotidiana.
    // 2: Ética y uso responsable de la IA.
    val videoAdicionalId: Int,

    // Fecha y hora en que terminó de verlo.
    val fechaTimestamp: Long = System.currentTimeMillis()
)


