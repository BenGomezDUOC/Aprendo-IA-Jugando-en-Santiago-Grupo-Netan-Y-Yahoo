package cl.triskeledu.aprendogame.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad de Room que registra cada validación en terreno:
 * selfie tomada (evidencia), punto visitado, cápsula consumida y timestamp.
 */
@Entity(
    tableName = "visitas_realizadas",
    foreignKeys = [
        ForeignKey(
            entity = PuntoInteres::class,
            parentColumns = ["id"],
            childColumns = ["puntoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["puntoId"])]
)
data class VisitaRealizada(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val puntoId: Int,
    val usuarioId: Int = 1,
    val capsulaId: Int? = null,
    val rutaFotoSelfie: String? = null,
    val fechaTimestamp: Long = System.currentTimeMillis(),
    val completada: Boolean = true
)
