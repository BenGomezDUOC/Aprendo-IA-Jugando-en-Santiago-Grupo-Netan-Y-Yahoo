package cl.triskeledu.aprendogame.ui

import androidx.annotation.DrawableRes
import cl.triskeledu.aprendogame.R

// Devuelve la fotografía inicial según el ID del punto turístico.
@DrawableRes
fun imagenPredeterminadaPunto(puntoId: Int): Int? {
    return when (puntoId) {
        1 -> R.drawable.plaza_de_armas
        2 -> R.drawable.cerro_santa_lucia
        3 -> R.drawable.cerro_san_cristobal
        4 -> R.drawable.palacio_la_moneda
        5 -> R.drawable.parque_forestal
        6 -> R.drawable.barrio_bellavista
        else -> null
    }
}
