package cl.triskeledu.aprendogame.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "descubrimientos_qr",

    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        ),
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

        // Un jugador solo puede descubrir
        // una misma cápsula una vez.
        Index(
            value = ["usuarioId", "capsulaId"],
            unique = true
        )
    ]
)
data class DescubrimientoQr(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Jugador que encontró el QR.
    val usuarioId: Int,

    // Cápsula educativa descubierta.
    val capsulaId: Int,

    // Fecha y hora del descubrimiento.
    val fechaTimestamp: Long = System.currentTimeMillis()
)