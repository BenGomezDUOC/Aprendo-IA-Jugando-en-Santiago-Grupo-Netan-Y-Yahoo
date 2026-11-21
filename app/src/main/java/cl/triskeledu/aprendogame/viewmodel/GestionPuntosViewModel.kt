
package cl.triskeledu.aprendogame.viewmodel

import android.app.Application
import android.location.Address
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.triskeledu.aprendogame.data.local.AppDatabase
import cl.triskeledu.aprendogame.location.ServicioGeografico
import cl.triskeledu.aprendogame.location.UbicacionGeografica
import cl.triskeledu.aprendogame.model.PuntoInteres
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Guarda todos los estados del formulario.
data class GestionPuntosUiState(
    val nombre: String = "",
    val descripcion: String = "",
    val direccion: String = "",
    val radio: String = "50",
    val imagenUri: String? = null,
    val ubicacion: UbicacionGeografica? = null,
    val ubicacionConfirmada: Boolean = false,
    val resultadosDireccion: List<Address> = emptyList(),
    val mostrarDirecciones: Boolean = false,
    val buscandoUbicacion: Boolean = false,
    val guardando: Boolean = false,
    val mensaje: String = "",
    val mensajeError: Boolean = false,
    val puntoEnEdicion: PuntoInteres? = null
) {

    val ocupado: Boolean
        get() = buscandoUbicacion || guardando

    val radioMetros: Double?
        get() = radio.trim()
            .replace(",", ".")
            .toDoubleOrNull()
            ?.takeIf { it.isFinite() && it > 0.0 }
}

class GestionPuntosViewModel(
    application: Application
) : AndroidViewModel(application) {

    // -------------------------------------------------
    // BASE DE DATOS Y SERVICIO GEOGRÁFICO
    // -------------------------------------------------

    private val database =
        AppDatabase.getInstance(application)

    private val puntoDao =
        database.puntoInteresDao()

    private val servicioGeografico =
        ServicioGeografico(application)

    // -------------------------------------------------
    // ESTADO DEL FORMULARIO
    // -------------------------------------------------

    private val _estado =
        MutableStateFlow(GestionPuntosUiState())

    val estado = _estado.asStateFlow()

    // Observa los puntos turísticos registrados en Room.
    val puntos = puntoDao.getPuntos().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // -------------------------------------------------
    // ACTUALIZAR CAMPOS DEL FORMULARIO
    // -------------------------------------------------

    fun cambiarNombre(valor: String) {
        _estado.update {
            if (it.ocupado) it else it.copy(nombre = valor)
        }
    }

    fun cambiarDescripcion(valor: String) {
        _estado.update {
            if (it.ocupado) it else it.copy(descripcion = valor)
        }
    }

    fun cambiarDireccion(valor: String) {

        _estado.update {

            if (it.ocupado) it

            else it.copy(
                direccion = valor,

                // Invalida la ubicación anterior.
                ubicacion = null,
                ubicacionConfirmada = false,
                resultadosDireccion = emptyList(),
                mostrarDirecciones = false,
                mensaje = "",
                mensajeError = false
            )
        }
    }

    fun cambiarRadio(valor: String) {

        _estado.update {

            if (it.ocupado) it

            else it.copy(
                radio = valor,
                ubicacionConfirmada = false
            )
        }
    }

    fun cambiarFotografia(uri: String) {

        _estado.update {

            if (it.ocupado) it

            else it.copy(
                imagenUri = uri,
                mensaje = "Fotografía seleccionada.",
                mensajeError = false
            )
        }
    }

    fun confirmarUbicacion(confirmada: Boolean) {

        _estado.update {

            if (it.ocupado) it

            else it.copy(
                ubicacionConfirmada =
                    confirmada &&
                            it.ubicacion != null &&
                            it.radioMetros != null
            )
        }
    }

    // -------------------------------------------------
    // MENSAJES
    // -------------------------------------------------

    fun informar(
        texto: String,
        error: Boolean = false
    ) {

        _estado.update {
            it.copy(
                mensaje = texto,
                mensajeError = error
            )
        }
    }

    // -------------------------------------------------
    // BUSCAR DIRECCIÓN
    // -------------------------------------------------

    fun buscarUbicacion() {

        val actual = _estado.value

        if (actual.ocupado) return

        if (actual.direccion.isBlank()) {

            informar(
                "Ingresa la dirección del lugar turístico.",
                true
            )

            return
        }

        _estado.update {

            it.copy(
                buscandoUbicacion = true,
                ubicacion = null,
                ubicacionConfirmada = false,
                resultadosDireccion = emptyList(),
                mostrarDirecciones = false,
                mensaje = "Buscando dirección...",
                mensajeError = false
            )
        }

        viewModelScope.launch {

            try {

                val resultados =
                    servicioGeografico.buscarDireccion(
                        actual.direccion
                    )

                when {

                    resultados.isEmpty() -> {

                        informar(
                            "No se encontró la dirección. Comprueba la calle, el número y la comuna.",
                            true
                        )
                    }

                    resultados.size == 1 -> {

                        cargarUbicacion(
                            resultados.first()
                        )
                    }

                    else -> {

                        _estado.update {

                            it.copy(
                                resultadosDireccion = resultados,
                                mostrarDirecciones = true,
                                mensaje = "Selecciona la dirección correcta.",
                                mensajeError = false
                            )
                        }
                    }
                }

            } catch (e: TimeoutCancellationException) {

                informar(
                    "La búsqueda tardó demasiado. Vuelve a intentarlo.",
                    true
                )

            } catch (e: CancellationException) {

                throw e

            } catch (e: Exception) {

                informar(
                    "No se pudo buscar la dirección. Comprueba tu conexión a Internet.",
                    true
                )

            } finally {

                _estado.update {
                    it.copy(buscandoUbicacion = false)
                }
            }
        }
    }

    // -------------------------------------------------
    // SELECCIONAR UNA DIRECCIÓN
    // -------------------------------------------------

    fun seleccionarDireccion(resultado: Address) {

        val actual = _estado.value

        if (actual.ocupado ||
            !actual.mostrarDirecciones ||
            resultado !in actual.resultadosDireccion
        ) return

        _estado.update {

            it.copy(
                mostrarDirecciones = false,
                buscandoUbicacion = true,
                ubicacion = null,
                ubicacionConfirmada = false,
                resultadosDireccion = emptyList()
            )
        }

        viewModelScope.launch {

            try {

                cargarUbicacion(resultado)

            } finally {

                _estado.update {
                    it.copy(buscandoUbicacion = false)
                }
            }
        }
    }

    fun cerrarDirecciones() {

        _estado.update {

            it.copy(
                mostrarDirecciones = false,
                resultadosDireccion = emptyList(),
                mensaje = ""
            )
        }
    }

    // -------------------------------------------------
    // OBTENER COORDENADAS Y ALTITUD
    // -------------------------------------------------

    private suspend fun cargarUbicacion(
        resultado: Address
    ) {

        informar(
            "Obteniendo coordenadas y altitud..."
        )

        try {

            val nuevaUbicacion =
                servicioGeografico.obtenerUbicacion(
                    resultado
                )

            _estado.update {

                it.copy(
                    ubicacion = nuevaUbicacion,
                    direccion = nuevaUbicacion.direccion
                        .ifBlank { it.direccion },
                    ubicacionConfirmada = false,
                    mensaje = "Ubicación encontrada. Comprueba el marcador en el mapa.",
                    mensajeError = false
                )
            }

        } catch (e: CancellationException) {

            throw e

        } catch (e: Exception) {

            informar(
                "No se pudo obtener la altitud. Comprueba tu conexión y vuelve a intentarlo.",
                true
            )
        }
    }

    // -------------------------------------------------
    // EDITAR PUNTO TURÍSTICO
    // -------------------------------------------------

    fun editarPunto(punto: PuntoInteres) {

        if (_estado.value.ocupado) return

        _estado.value = GestionPuntosUiState(

            nombre = punto.nombre,

            descripcion = punto.descripcion,

            direccion = punto.direccion,

            radio = punto.radioToleranciaMetros
                .toString(),

            imagenUri = punto.imagenUri,

            ubicacion = UbicacionGeografica(
                direccion = punto.direccion,
                latitud = punto.latitud,
                longitud = punto.longitud,
                altitud = punto.altitud
            ),

            ubicacionConfirmada =
                punto.direccion.isNotBlank(),

            puntoEnEdicion = punto,

            mensaje = "Editando: ${punto.nombre}"
        )
    }

    // -------------------------------------------------
    // LIMPIAR FORMULARIO
    // -------------------------------------------------

    fun limpiarFormulario() {

        if (_estado.value.ocupado) return

        _estado.value = GestionPuntosUiState()
    }

    // -------------------------------------------------
    // REGISTRAR O ACTUALIZAR PUNTO TURÍSTICO
    // -------------------------------------------------

    fun guardarPunto() {

        val actual = _estado.value

        if (actual.ocupado) return

        val datos = actual.ubicacion

        val radioValidado = actual.radioMetros

        // Validaciones del formulario.
        val error = when {

            actual.nombre.isBlank() ->
                "Ingresa el nombre del lugar."

            actual.descripcion.isBlank() ->
                "Ingresa la descripción."

            actual.direccion.isBlank() ->
                "Ingresa la dirección."

            datos == null ->
                "Primero debes buscar la ubicación."

            radioValidado == null ->
                "El radio GPS debe ser mayor que cero."

            !actual.ubicacionConfirmada ->
                "Debes confirmar la ubicación en el mapa."

            else -> null
        }

        if (error != null) {

            informar(error, true)

            return
        }

        if (datos == null || radioValidado == null) return

        _estado.update {

            it.copy(
                guardando = true,
                mensaje = "",
                mensajeError = false
            )
        }

        viewModelScope.launch {

            try {

                val puntoAnterior =
                    actual.puntoEnEdicion

                // Recupera el estado más reciente
                // para conservar el progreso existente.
                val puntoExistente = puntoAnterior?.let {

                    puntoDao.getPuntoPorId(it.id)
                        ?: error("El punto ya no existe.")
                }

                val puntoParaGuardar = PuntoInteres(

                    id = puntoExistente?.id ?: 0,

                    nombre = actual.nombre.trim(),

                    descripcion =
                        actual.descripcion.trim(),

                    direccion =
                        actual.direccion.trim(),

                    latitud = datos.latitud,

                    longitud = datos.longitud,

                    altitud = datos.altitud,

                    completado =
                        puntoExistente?.completado
                            ?: false,

                    radioToleranciaMetros =
                        radioValidado,

                    imagenUri = actual.imagenUri
                )

                if (puntoExistente != null) {

                    puntoDao.actualizar(
                        puntoParaGuardar
                    )

                } else {

                    puntoDao.crearPunto(
                        puntoParaGuardar
                    )
                }

                // Limpia los campos después de guardar.
                _estado.value = GestionPuntosUiState(

                    mensaje = if (
                        puntoExistente != null
                    ) {
                        "Punto turístico actualizado correctamente."
                    } else {
                        "Punto turístico registrado correctamente."
                    }
                )

            } catch (e: CancellationException) {

                throw e

            } catch (e: Exception) {

                informar(
                    "No se pudo guardar el punto turístico.",
                    true
                )

            } finally {

                _estado.update {
                    it.copy(guardando = false)
                }
            }
        }
    }
}
