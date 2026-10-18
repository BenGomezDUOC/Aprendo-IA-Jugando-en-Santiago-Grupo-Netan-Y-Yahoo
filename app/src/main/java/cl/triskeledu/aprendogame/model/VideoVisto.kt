package cl.triskeledu.aprendogame.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "videos_vistos",

    foreignKeys = [
        // Relaciona el video visto con el jugador.
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        ),

        // Relaciona el video visto con su cápsula.
        ForeignKey(
            entity = Capsula::class,
            parentColumns = ["id"],
            childColumns = ["capsulaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],

    indices = [
        Index(value = ["usuarioId"]),
        Index(value = ["capsulaId"]),

        // Evita registrar varias veces el mismo
        // video visto por un mismo jugador.
        Index(
            value = ["usuarioId", "capsulaId"],
            unique = true
        )
    ]
)
data class VideoVisto(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Jugador que visualizó el video.
    val usuarioId: Int,

    // Cápsula a la que pertenece el video.
    val capsulaId: Int,

    // Fecha y hora de visualización.
    val fechaTimestamp: Long = System.currentTimeMillis()
)