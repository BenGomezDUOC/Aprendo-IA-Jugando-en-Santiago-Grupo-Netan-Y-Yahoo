
package cl.triskeledu.aprendogame.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Video registrado por el Administrador.
 *
 * Puede asociarse a un contenido creado
 * por el Generador.
 */
@Entity(
    tableName = "videos_registrados",
    foreignKeys = [
        ForeignKey(
            entity = ContenidoEducativo::class,
            parentColumns = ["id"],
            childColumns = ["contenidoId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["contenidoId"])
    ]
)
data class VideoRegistrado(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val titulo: String,

    val descripcion: String = "",

    // Dirección del archivo MP4.
    val urlVideo: String,

    // Cápsula creada por el Generador.
    val contenidoId: Int? = null,

    // Nombre ingresado manualmente por el Administrador.
    val autor: String = "",

    // Duración ingresada manualmente.
    // Se guarda en milisegundos.
    val duracion: Long = 0L,

    val urlMiniatura: String = ""
)


