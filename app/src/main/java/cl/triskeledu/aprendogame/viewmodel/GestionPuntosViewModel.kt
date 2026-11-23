package cl.triskeledu.aprendogame.viewmodel

import android.app.Application
import android.location.Address
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.triskeledu.aprendogame.data.local.AppDatabase
import cl.triskeledu.aprendogame.data.local.SessionManager
import cl.triskeledu.aprendogame.data.remote.RetrofitClient
import cl.triskeledu.aprendogame.data.remote.dto.PuntoInteresBackendDto
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import cl.triskeledu.aprendogame.location.ServicioGeografico
import cl.triskeledu.aprendogame.location.UbicacionGeografica
import cl.triskeledu.aprendogame.model.PuntoInteres
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
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

    private val sesion = SessionManager(application)

    // Evita que la migración vuelva a crear en MySQL un punto eliminado.
    private val puntosEliminados = application.getSharedPreferences(
        "puntos_eliminados_mysql", android.content.Context.MODE_PRIVATE
    )

    private fun claveLocal(punto: PuntoInteres): String =
        "${punto.nombre.trim().lowercase(java.util.Locale.ROOT)}|${punto.latitud}|${punto.longitud}"


    // -------------------------------------------------
    // ESTADO DEL FORMULARIO
    // -------------------------------------------------

    private val _estado =
        MutableStateFlow(GestionPuntosUiState())

    val estado = _estado.asStateFlow()

    // La lista de Administración refleja únicamente los puntos confirmados en MySQL.
    // Se conservan los ID locales al actualizar la caché para no romper las relaciones Room.
    private val _puntos = MutableStateFlow<List<PuntoInteres>>(emptyList())
    val puntos = _puntos.asStateFlow()

    init {
        sincronizarPuntos()
    }

    /**
     * Migración idempotente Room -> MySQL y actualización de la caché.
     * Nunca borra registros de Room ni modifica registros existentes de MySQL.
     * Si se interrumpe, al reintentar verifica nuevamente el servidor antes de crear.
     */
    fun sincronizarPuntos() {
        viewModelScope.launch {
            try {
                val auth = sesion.obtenerAuthorizationHeader()
                    ?: error("Debes iniciar sesión en el backend para consultar MySQL.")
                val locales = puntoDao.getPuntos().first()
                val listaConfirmada = withContext(Dispatchers.IO) {
                    val api = RetrofitClient.localizacionesApi
                    val primera = api.obtenerPuntos(auth)
                    if (!primera.isSuccessful) error("No se pudieron consultar puntos de MySQL (HTTP ${primera.code()}).")
                    val remotos = primera.body().orEmpty().toMutableList()
                    val fallos = mutableListOf<String>()

                    // Nombre único: criterio conservador para no crear duplicados si
                    // alguien corrigió dirección o coordenadas después de crear el punto.
                    for (local in locales) {
                        if (puntosEliminados.getBoolean(claveLocal(local), false)) continue
                        val mismoNombre = remotos.filter {
                            it.nombre.trim().equals(local.nombre.trim(), ignoreCase = true)
                        }
                        if (mismoNombre.isNotEmpty()) continue
                        // No enviar URI content:// o file://: el backend exige URL multimedia válida.
                        val imagenRemota = local.imagenUri?.takeIf {
                            it.startsWith("/api/localizaciones/multimedia/") ||
                                    it.startsWith("http://10.0.2.2:8083/api/localizaciones/multimedia/")
                        }
                        val solicitud = PuntoInteresBackendDto(
                            id = 0L,
                            codigo = "",
                            nombre = local.nombre,
                            descripcion = local.descripcion,
                            direccion = local.direccion,
                            latitud = local.latitud,
                            longitud = local.longitud,
                            altitud = local.altitud,
                            radioToleranciaMetros = local.radioToleranciaMetros,
                            imagenUri = imagenRemota,
                            completado = false,
                            puntosRecompensa = 100,
                            qrEsperado = ""
                        )
                        try {
                            val creado = api.crearPunto(auth, solicitud)
                            if (creado.isSuccessful && creado.body() != null) {
                                remotos.add(creado.body()!!)
                            } else {
                                fallos.add("${local.nombre}: HTTP ${creado.code()}")
                            }
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            fallos.add("${local.nombre}: ${e.message ?: "sin conexión"}")
                        }
                    }
                    val refresco = api.obtenerPuntos(auth)
                    if (!refresco.isSuccessful) error("No se pudo verificar MySQL (HTTP ${refresco.code()}).")
                    Pair(refresco.body().orEmpty(), fallos)
                }

                val (remotosVerificados, fallos) = listaConfirmada
                val actuales = puntoDao.getPuntos().first().toMutableList()
                val visibles = mutableListOf<PuntoInteres>()
                for (remoto in remotosVerificados) {
                    val local = actuales.firstOrNull {
                        it.nombre.trim().equals(remoto.nombre.trim(), ignoreCase = true)
                    }
                    val actualizado = PuntoInteres(
                        id = local?.id ?: 0,
                        nombre = remoto.nombre,
                        descripcion = remoto.descripcion ?: "",
                        direccion = remoto.direccion ?: "",
                        latitud = remoto.latitud ?: continue,
                        longitud = remoto.longitud ?: continue,
                        altitud = remoto.altitud ?: 0.0,
                        radioToleranciaMetros = remoto.radioToleranciaMetros ?: 50.0,
                        imagenUri = remoto.imagenUri ?: local?.imagenUri,
                        completado = local?.completado ?: false
                    )
                    val cache = if (local == null) {
                        actualizado.copy(id = puntoDao.crearPunto(actualizado).toInt())
                    } else {
                        puntoDao.actualizar(actualizado)
                        actualizado
                    }
                    visibles.add(cache)
                    if (local == null) actuales.add(cache)
                }
                _puntos.value = visibles
                if (fallos.isNotEmpty()) {
                    informar("No se migraron todos los puntos: ${fallos.joinToString("; ")}", true)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("PUNTOS_MYSQL", "Error al sincronizar", e)
                informar("No se pudo sincronizar con MySQL: ${e.message}", true)
            }
        }
    }

    /** Elimina exclusivamente el punto indicado, una vez confirmado por el usuario.
     * Spring Boot impide borrar puntos que tengan contenidos o cápsulas asociados.
     */
    fun eliminarPunto(punto: PuntoInteres) {
        if (_estado.value.ocupado) return
        _estado.update { it.copy(guardando = true, mensaje = "", mensajeError = false) }
        viewModelScope.launch {
            try {
                val auth = sesion.obtenerAuthorizationHeader()
                    ?: error("Inicia sesión como Administrador para eliminar puntos en MySQL.")
                val idRemoto = withContext(Dispatchers.IO) {
                    val api = RetrofitClient.localizacionesApi
                    val consulta = api.obtenerPuntos(auth)
                    if (!consulta.isSuccessful) error("No se pudo consultar MySQL (HTTP ${consulta.code()}).")
                    val matches = consulta.body().orEmpty().filter { remoto ->
                        remoto.nombre.trim().equals(punto.nombre.trim(), ignoreCase = true) &&
                                remoto.latitud != null && remoto.longitud != null &&
                                kotlin.math.abs(remoto.latitud - punto.latitud) < 0.0001 &&
                                kotlin.math.abs(remoto.longitud - punto.longitud) < 0.0001
                    }
                    if (matches.size != 1) {
                        error("No se identificó de manera única el punto en MySQL. No se eliminó nada.")
                    }
                    val id = matches.single().id
                    val respuesta = api.eliminarPunto(auth, id)
                    if (!respuesta.isSuccessful) {
                        when (respuesta.code()) {
                            409 -> error("No puedes eliminar este punto porque tiene contenidos o cápsulas asociados.")
                            403 -> error("Tu usuario no tiene permiso para eliminar puntos.")
                            404 -> error("El punto ya no existe en MySQL. Actualiza la lista.")
                            else -> error("No se eliminó en MySQL (HTTP ${respuesta.code()}).")
                        }
                    }
                    id
                }
                // Primero marcar como eliminado: nunca volver a importarlo desde Room.
                puntosEliminados.edit().putBoolean(claveLocal(punto), true).apply()
                _puntos.value = _puntos.value.filterNot { it.id == punto.id }
                // La caché es secundaria. Si su borrado falla, la marca evita remigración.
                try {
                    puntoDao.eliminarPuntoPorId(punto.id)
                } catch (e: Exception) {
                    Log.w("PUNTOS_MYSQL", "Eliminado en MySQL, caché local conservada: $idRemoto", e)
                }
                _estado.update { actual ->
                    if (actual.puntoEnEdicion?.id == punto.id) GestionPuntosUiState(
                        guardando = true,
                        mensaje = "Punto eliminado de MySQL correctamente."
                    ) else actual.copy(mensaje = "Punto eliminado de MySQL correctamente.", mensajeError = false)
                }
                sincronizarPuntos()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("PUNTOS_MYSQL", "Fallo al eliminar punto", e)
                informar(e.message ?: "No se pudo eliminar el punto turístico.", true)
            } finally {
                _estado.update { it.copy(guardando = false) }
            }
        }
    }

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
                    // Si hay sesión remota, primero se confirma el cambio
                    // en MySQL; no mostrar éxito por actualizar solo Room.
                    val authorization = sesion.obtenerAuthorizationHeader()
                    if (authorization != null) {
                        withContext(Dispatchers.IO) {
                            val consulta = RetrofitClient.localizacionesApi
                                .obtenerPuntos(authorization)
                            if (!consulta.isSuccessful) {
                                error("No se pudieron consultar puntos (HTTP ${consulta.code()})")
                            }

                            val candidatos = consulta.body().orEmpty().filter { remoto ->
                                // Room y MySQL NO comparten obligatoriamente ID.
                                // Buscar el lugar original, nunca el nuevo nombre editado.
                                val mismoNombre = remoto.nombre.trim().equals(
                                    puntoExistente.nombre.trim(), ignoreCase = true
                                )
                                val mismaDireccion = remoto.direccion?.trim()?.equals(
                                    puntoExistente.direccion.trim(), ignoreCase = true
                                ) == true
                                val mismasCoordenadas =
                                    remoto.latitud != null && remoto.longitud != null &&
                                            kotlin.math.abs(remoto.latitud - puntoExistente.latitud) < 0.0001 &&
                                            kotlin.math.abs(remoto.longitud - puntoExistente.longitud) < 0.0001
                                mismoNombre && (mismaDireccion || mismasCoordenadas)
                            }

                            if (candidatos.size != 1) {
                                error("No se identificó un único punto en MySQL. Coincidencias: ${candidatos.size}. No se modificó nada.")
                            }

                            val remoto = candidatos.single()
                            val solicitud = PuntoInteresBackendDto(
                                id = remoto.id,
                                codigo = remoto.codigo,
                                nombre = puntoParaGuardar.nombre,
                                descripcion = puntoParaGuardar.descripcion,
                                direccion = puntoParaGuardar.direccion,
                                latitud = puntoParaGuardar.latitud,
                                longitud = puntoParaGuardar.longitud,
                                altitud = puntoParaGuardar.altitud,
                                radioToleranciaMetros = puntoParaGuardar.radioToleranciaMetros,
                                imagenUri = puntoParaGuardar.imagenUri?.takeIf {
                                    it.startsWith("/api/localizaciones/multimedia/") ||
                                            it.startsWith("http://10.0.2.2:8083/api/localizaciones/multimedia/")
                                } ?: remoto.imagenUri,
                                completado = remoto.completado,
                                puntosRecompensa = remoto.puntosRecompensa,
                                qrEsperado = remoto.qrEsperado
                            )
                            val actualizacion = RetrofitClient.localizacionesApi
                                .actualizarPunto(authorization, remoto.id, solicitud)
                            if (!actualizacion.isSuccessful) {
                                error("No se actualizó el punto en MySQL (HTTP ${actualizacion.code()})")
                            }
                        }
                    } else {
                        // Las cuentas exclusivamente locales no tienen JWT.
                        // No se debe informar que se sincronizó con MySQL.
                        error("Inicia sesión con un administrador del backend para actualizar MySQL.")
                    }
                    puntoDao.actualizar(puntoParaGuardar)
                } else {
                    // MySQL es la fuente principal. No registrar únicamente en Room.
                    val authorization = sesion.obtenerAuthorizationHeader()
                        ?: error("Inicia sesión con un administrador del backend para crear el punto.")
                    withContext(Dispatchers.IO) {
                        val consulta = RetrofitClient.localizacionesApi.obtenerPuntos(authorization)
                        if (!consulta.isSuccessful) {
                            error("No se pudieron consultar puntos en MySQL (HTTP ${consulta.code()})")
                        }
                        val yaExiste = consulta.body().orEmpty().any { remoto ->
                            remoto.nombre.trim().equals(puntoParaGuardar.nombre, ignoreCase = true) &&
                                    remoto.latitud != null && remoto.longitud != null &&
                                    kotlin.math.abs(remoto.latitud - puntoParaGuardar.latitud) < 0.0001 &&
                                    kotlin.math.abs(remoto.longitud - puntoParaGuardar.longitud) < 0.0001
                        }
                        if (yaExiste) error("Este punto ya existe en MySQL. No se creó un duplicado.")

                        val solicitud = PuntoInteresBackendDto(
                            id = 0L,
                            codigo = "",
                            nombre = puntoParaGuardar.nombre,
                            descripcion = puntoParaGuardar.descripcion,
                            direccion = puntoParaGuardar.direccion,
                            latitud = puntoParaGuardar.latitud,
                            longitud = puntoParaGuardar.longitud,
                            altitud = puntoParaGuardar.altitud,
                            radioToleranciaMetros = puntoParaGuardar.radioToleranciaMetros,
                            imagenUri = puntoParaGuardar.imagenUri?.takeIf {
                                it.startsWith("/api/localizaciones/multimedia/") ||
                                        it.startsWith("http://10.0.2.2:8083/api/localizaciones/multimedia/")
                            },
                            completado = false,
                            puntosRecompensa = 100,
                            qrEsperado = ""
                        )
                        val creacion = RetrofitClient.localizacionesApi.crearPunto(authorization, solicitud)
                        if (!creacion.isSuccessful) {
                            error("No se creó el punto en MySQL (HTTP ${creacion.code()})")
                        }
                        creacion.body() ?: error("MySQL no confirmó los datos del punto.")
                    }
                    // Caché local, únicamente después de confirmar el registro remoto.
                    puntoDao.crearPunto(puntoParaGuardar)
                }
                sincronizarPuntos()

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
                Log.e("PUNTOS_BACKEND", "Fallo al guardar el punto", e)
                informar(
                    e.message ?: "No se pudo guardar el punto turístico.",
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

