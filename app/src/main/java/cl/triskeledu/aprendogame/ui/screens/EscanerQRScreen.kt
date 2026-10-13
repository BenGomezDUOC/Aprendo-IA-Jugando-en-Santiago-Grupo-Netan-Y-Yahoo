package cl.triskeledu.aprendogame.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun EscanerQRScreen() {

    var permisoConcedido by remember {
        mutableStateOf(false)
    }

    val solicitarPermiso = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        permisoConcedido = concedido
    }

    Column {
        Text("Escáner de código QR")

        Button(
            onClick = {
                solicitarPermiso.launch(Manifest.permission.CAMERA)
            }
        ) {
            Text("Solicitar permiso de cámara")
        }

        if (permisoConcedido) {
            Text("Permiso concedido")
        } else {
            Text("Permiso no concedido")
        }
    }
}


/*
 * GUARDAR EL ESTADO DEL PERMISO DE CÁMARA
 *
 * SINTAXIS GENERAL:
 *
 * var nombreVariable by remember { mutableStateOf(valorInicial) }
 *
 * NUESTRO CÓDIGO:
 *
 * var permisoConcedido by remember { mutableStateOf(false) }
 *
 * 1. mutableStateOf(false): OBLIGA A LA PANTALLA A CAMBIAR LO QUE MUESTRA
 *    - Sin esto: Cambias la variable a true, pero en el teléfono el botón se queda congelado.
 *    - Con esto: Al pasar a true, activa una señal para que la pantalla cambie el botón
 *      por la cámara o el juego, si así lo programamos.
 *    - "Redibujar": Quitar un elemento visual de la pantalla (botón)
 *      y poner otro (cámara/juego).
 *
 * 2. remember: EL CANDADO DE MEMORIA
 *    - Para actualizar lo que vemos, Compose puede volver a leer el código.
 *    - Sin esto: Al volver a crear el estado, lee 'false' otra vez,
 *      olvida el cambio y puede volver a mostrar el botón.
 *    - Con esto: Le pone un candado al 'true' para mantener el cambio
 *      mientras Compose vuelve a leer esa parte del código.
 *
 * 3. var: LA CAJITA QUE PUEDE CAMBIAR
 *    - Imagina una cajita donde guardamos una respuesta.
 *    - Primero guardamos false.
 *    - Cuando el usuario acepta el permiso, podemos guardar true.
 *    - Usamos var porque necesitamos cambiar ese valor.
 *
 * 4. permisoConcedido: EL NOMBRE DE LA CAJITA
 *    - Es el nombre que elegimos para guardar la respuesta.
 *    - Podemos poner otro nombre, pero tenemos que utilizarlo
 *      igual en el resto del código.
 *
 * 5. by: NOS AHORRA ESCRIBIR .value
 *    - Con by: permisoConcedido = true
 *    - Sin by, declarando el estado directamente:
 *      permisoConcedido.value = true
 *    - Hace que cambiar el valor sea más sencillo.
 *
 * 6. { }: LAS LLAVES
 *    - En remember { mutableStateOf(false) }, guardan
 *      la instrucción que crea nuestra cajita.
 *
 * 7. ( ): LOS PARÉNTESIS
 *    - En mutableStateOf(false), indican qué valor
 *      queremos guardar al principio.
 *
 * 8. false: COMENZAR EN FALSO
 *    - false significa falso.
 *    - true significa verdadero.
 *    - Boolean es el tipo de dato que admite estos dos valores.
 *    - Aquí false es solamente el valor inicial;
 *      todavía no hemos preguntado a Android si ya dio permiso.
 *
 * 9. =: GUARDAR UN VALOR
 *    - permisoConcedido = true significa guardar true.
 *    - = sirve para asignar un valor.
 *    - == sirve para comparar si dos valores son iguales.
 *
 * Hay dos trabajos diferentes:
- Guardar la respuesta: necesitamos recordar que el usuario aceptó (true).
- Cambiar la pantalla: necesitamos que el teléfono deje de mostrar «Permiso no concedido» y muestre «Permiso concedido».
Por eso utilizamos las tres cosas juntas:
var permisoConcedido by remember {
    mutableStateOf(false)
}
- var permisoConcedido — LA CAJITA: guarda true o false.
- remember — EL CANDADO: evita que Compose olvide el valor cuando vuelve a ejecutar esa parte de la pantalla.
- mutableStateOf — LA ALARMA: cuando cambia el valor, avisa a Compose para que actualice lo que depende de él.
 */

/*
 * PREPARAR LA SOLICITUD DE PERMISO (LA VENTANA FLOTANTE DE ANDROID)
 *
 * SINTAXIS GENERAL:
 *
 * val nombreLauncher = rememberLauncherForActivityResult(
 *     contract = tipoDeContrato
 * ) { nombreRespuesta ->
 *     instrucciones
 * }
 *
 * NUESTRO CÓDIGO:
 *
 * val solicitarPermiso = rememberLauncherForActivityResult(
 *     contract = ActivityResultContracts.RequestPermission()
 * ) { concedido ->
 *     permisoConcedido = concedido
 * }
 *
 * 1. val solicitarPermiso (EL CONTROL REMOTO / DISPARADOR):
 *    - Es la variable donde guardas el control para activar la ventana cuando quieras.
 *    - Cuando el usuario toca el botón de tu pantalla, ejecutas:
 *
 *      solicitarPermiso.launch(...)
 *
 *    - Eso inicia la solicitud y Android puede mostrar
 *      la ventana flotante para aceptar o rechazar el permiso.
 *
 *    - val: Significa que no podemos reemplazar ese control
 *      por otro después.
 *
 *    - solicitarPermiso: Es el nombre que nosotros elegimos
 *      para nuestro control remoto.
 *
 *
 * 2. rememberLauncherForActivityResult (EL ENCARGADO / EL PUENTE):
 *
 *   - Es la función de Android que abre la ventana flotante del sistema y recuerda la respuesta.
*    - "remember": Guarda la conexión en memoria para que la app no pierda la respuesta ni se confunda
*      cuando la pantalla se redibuje.
*
 *    - Es la función que prepara la solicitud del permiso
 *      y se encarga de recibir la respuesta de Android.
 *
 *    - Imagina que tenemos un encargado que hace dos trabajos:
 *      primero prepara la solicitud y después nos trae la respuesta.
 *
 *    - Sin esto: No tendríamos preparado ese control
 *      para pedir el permiso y recibir su resultado.
 *
 *    - Con esto: Dejamos preparado el control remoto
 *      que utilizaremos cuando el usuario toque el botón.
 *
 *    - "remember": Mantiene registrado ese control
 *      aunque Compose vuelva a leer el código de la pantalla.
 *
 *
 * 3. DENTRO DEL PARÉNTESIS (...) -> CONFIGURACIÓN Y TIPO DE VENTANA:
 *
 *    contract = ActivityResultContracts.RequestPermission()
 *
 *    - Los paréntesis llevan la CONFIGURACIÓN
 *      (los datos que la función necesita para prepararse).
 *
 *    - contract: Le dice al encargado qué trabajo queremos realizar.
 *
 *    - ActivityResultContracts: Es como una caja que contiene
 *      diferentes tipos de trabajos preparados por Android.
 *
 *    - RequestPermission(): Elegimos el trabajo de pedir UN permiso.
 *
 *    - El punto (.) permite entrar desde ActivityResultContracts
 *      hasta RequestPermission().
 *
 *    - El signo (=) indica qué tipo de trabajo
 *      estamos entregando a contract.
 *
 *      RESUMEN: 1. rememberLauncherForActivityResult: EL ENCARGADO
        Imagina que contratamos a un encargado para realizar un trabajo.
        Ese encargado necesita saber dos cosas:
        - ¿Qué trabajo tiene que hacer? Solicitar un permiso.
        - ¿Qué debe hacer cuando llegue la respuesta? Entregarnos true o false para guardarlo.

        2. Dentro del paréntesis: LE DECIMOS QUÉ TRABAJO TIENE QUE PREPARAR
            contract = ActivityResultContracts.RequestPermission()


        Aquí le estamos diciendo:
        «Encargado, el trabajo que vas a realizar es solicitar UN permiso de Android y recibir la respuesta de esa solicitud».
        - contract: Indica qué tipo de trabajo debe preparar.
        - ActivityResultContracts: Es como una caja con diferentes tipos de trabajos que Android ya tiene preparados.
        - RequestPermission(): Elegimos el trabajo de solicitar un permiso./ Le dice que su trabajo es solicitar un permiso.
        Y aquí viene un detalle importante: todavía no estamos indicando que el permiso es de cámara. Solamente estamos diciendo que queremos solicitar un permiso.
 *
 *
 * 4. DENTRO DE LAS LLAVES {...} -> LA ACCIÓN Y RESPUESTA DEL USUARIO:
 *
 *    { concedido ->
 *        permisoConcedido = concedido
 *    }
 *
 *    - Las llaves encierran el código que se ejecutará
 *      cuando Android entregue la respuesta.
 *
 *    - concedido: Es el mensajero que trae la respuesta.
 *
 *      * Si tocó "Permitir" -> concedido vale true.
 *      * Si tocó "Denegar"  -> concedido vale false.
 *
 *
 * 5. LA FLECHA (->): SEPARA LA RESPUESTA DE LO QUE HACEMOS CON ELLA
 *
 *    SINTAXIS GENERAL:
 *
 *    { nombreRespuesta ->
 *        instrucciones
 *    }
 *
 *    NUESTRO CÓDIGO:
 *
 *    { concedido ->
 *        permisoConcedido = concedido
 *    }
 *
 *    - Antes de la flecha: El dato que recibimos de Android.
 *    - Después de la flecha: Lo que hacemos con ese dato.
 *
 *
 * 6. permisoConcedido = concedido: GUARDAR LA RESPUESTA EN NUESTRA CAJITA
 *
 *    - ¿Recuerdas la variable que creamos en la primera parte?
 *
 *      var permisoConcedido by remember { mutableStateOf(false) }
 *
 *    - Aquí es donde finalmente la utilizamos.
 *
 *    - concedido: Es la respuesta que acaba de llegar de Android.
 *    - permisoConcedido: Es nuestra cajita donde guardamos la respuesta.
 *    - =: Guarda el valor recibido dentro de nuestra cajita.
 *
 *    EJEMPLO:
 *
 *    - Android devuelve true.
 *    - concedido recibe true.
 *    - permisoConcedido = concedido.
 *    - Nuestra cajita ahora guarda true.
 *    - mutableStateOf avisa a Compose que el valor cambió.
 *    - La pantalla puede mostrar "Permiso concedido".
 *
 * IMPORTANTE:
 * - Aquí todavía NO estamos abriendo la ventana de permiso.
 * - Solamente estamos preparando el control remoto.
 * - La ventana se solicitará después, cuando ejecutemos launch().
 */

/*
 * TERCERA PARTE: MOSTRAR LA PANTALLA Y SOLICITAR EL PERMISO
 *
 * CÓDIGO COMPLETO:
 *
 * Column {
 *     Text("Escáner de código QR")
 *
 *     Button(
 *         onClick = {
 *             solicitarPermiso.launch(Manifest.permission.CAMERA)
 *         }
 *     ) {
 *         Text("Solicitar permiso de cámara")
 *     }
 *
 *     if (permisoConcedido) {
 *         Text("Permiso concedido")
 *     } else {
 *         Text("Permiso no concedido")
 *     }
 * }
 *
 *
 * 1. Column: COLOCAR UNA COSA DEBAJO DE OTRA
 *
 *    SINTAXIS GENERAL:
 *
 *    Column {
 *        primerElemento
 *        segundoElemento
 *        tercerElemento
 *    }
 *
 *    - Imagina que estás organizando tres objetos en una columna.
 *    - Primero colocas el título.
 *    - Debajo colocas el botón.
 *    - Y debajo colocas el mensaje del permiso.
 *
 *    - Sin esto: No tendríamos esta organización vertical.
 *    - Con esto: Los elementos aparecen uno debajo del otro.
 *
 *    LAS LLAVES { }:
 *    - La primera llave { abre la columna.
 *    - Dentro colocamos los elementos que queremos mostrar.
 *    - La última llave } cierra la columna.
 *
 *
 * 2. Text("Escáner de código QR"): MOSTRAR UN TEXTO EN EL TELÉFONO
 *
 *    SINTAXIS GENERAL:
 *
 *    Text("mensaje")
 *
 *    NUESTRO CÓDIGO:
 *
 *    Text("Escáner de código QR")
 *
 *    - Text es la herramienta que escribe algo en la pantalla.
 *    - Sin esto: No aparecería nuestro título.
 *    - Con esto: El usuario verá "Escáner de código QR".
 *
 *    LOS PARÉNTESIS ( ):
 *    - Dentro colocamos el texto que queremos mostrar.
 *
 *    LAS COMILLAS " ":
 *    - Indican que estamos escribiendo un texto.
 *    - Sin comillas: Kotlin intentaría interpretar
 *      esas palabras como instrucciones.
 *
 *
 * 3. Button: CREAR UN BOTÓN QUE EL USUARIO PUEDA PRESIONAR
 *
 *    SINTAXIS GENERAL:
 *
 *    Button(
 *        onClick = {
 *            instrucciones
 *        }
 *    ) {
 *        contenidoDelBoton
 *    }
 *
 *    NUESTRO CÓDIGO:
 *
 *    Button(
 *        onClick = {
 *            solicitarPermiso.launch(Manifest.permission.CAMERA)
 *        }
 *    ) {
 *        Text("Solicitar permiso de cámara")
 *    }
 *
 *    - Button crea un botón en nuestra pantalla.
 *    - Sin esto: No tendríamos ese botón para solicitar el permiso.
 *    - Con esto: El usuario tendrá un botón que puede tocar.
 *
 *    ¿POR QUÉ TENEMOS DOS PARES DE LLAVES?
 *
 *    PRIMERAS LLAVES:
 *
 *    onClick = {
 *        solicitarPermiso.launch(Manifest.permission.CAMERA)
 *    }
 *
 *    - Indican LO QUE HACE el botón cuando lo presionan.
 *
 *    SEGUNDAS LLAVES:
 *
 *    {
 *        Text("Solicitar permiso de cámara")
 *    }
 *
 *    - Indican LO QUE MUESTRA el botón.
 *
 *
 * 4. onClick: EL ENCARGADO DE ESCUCHAR CUANDO TOCAN EL BOTÓN
 *
 *    SINTAXIS GENERAL:
 *
 *    onClick = {
 *        instrucciones
 *    }
 *
 *    NUESTRO CÓDIGO:
 *
 *    onClick = {
 *        solicitarPermiso.launch(Manifest.permission.CAMERA)
 *    }
 *
 *    - Imagina que el botón tiene un timbre.
 *    - Cuando el usuario lo toca, onClick escucha ese timbre.
 *    - Entonces ejecuta las instrucciones que tiene dentro.
 *
 *    - Sin esto: No estaríamos indicando qué debe hacer el botón.
 *    - Con esto: Cuando el usuario toca el botón,
 *      se activa nuestro control remoto.
 *
 *    EL SIGNO =:
 *    - Le entrega a onClick las instrucciones que ejecutará.
 *
 *    LAS LLAVES { }:
 *    - Guardan las instrucciones que se ejecutarán
 *      cuando el usuario toque el botón.
 *
 *
 * 5. solicitarPermiso.launch(...): ACTIVAR NUESTRO CONTROL REMOTO
 *
 *    SINTAXIS GENERAL:
 *
 *    nombreLauncher.launch(datoNecesario)
 *
 *    NUESTRO CÓDIGO:
 *
 *    solicitarPermiso.launch(Manifest.permission.CAMERA)
 *
 *    - ¿Recuerdas el control remoto que preparamos
 *      en la segunda parte?
 *
 *      val solicitarPermiso = rememberLauncherForActivityResult(...)
 *
 *    - Aquí finalmente lo utilizamos.
 *
 *    solicitarPermiso: EL CONTROL REMOTO
 *    - Es el control que preparamos anteriormente.
 *
 *    launch: EL BOTÓN QUE ACTIVA EL CONTROL REMOTO
 *    - Imagina que tienes el control remoto de un televisor.
 *    - Tener el control en la mano no enciende la televisión.
 *    - Necesitas presionar el botón de encendido.
 *    - launch hace algo parecido: inicia la solicitud.
 *
 *    - Sin launch: La solicitud queda preparada,
 *      pero no la activamos.
 *    - Con launch: Android comienza a solicitar el permiso.
 *
 *    EL PUNTO . :
 *    - Permite utilizar launch desde solicitarPermiso.
 *
 *    LOS PARÉNTESIS ( ):
 *    - Dentro indicamos qué permiso queremos solicitar.
 *
 *
 * 6. Manifest.permission.CAMERA: PEDIR EL PERMISO DE LA CÁMARA
 *
 *    - En la segunda parte dijimos que el encargado
 *      tenía que preparar una solicitud de permiso.
 *
 *    - Pero todavía no habíamos dicho QUÉ permiso.
 *
 *    - Aquí finalmente le decimos que queremos
 *      el permiso para utilizar la cámara.
 *
 *    Manifest: CONTIENE INFORMACIÓN DE ANDROID
 *    - En este caso utilizamos su clase de permisos.
 *
 *    permission: LOS PERMISOS DISPONIBLES
 *    - Permite acceder a los nombres de permisos.
 *
 *    CAMERA: EL PERMISO DE CÁMARA
 *    - Identifica exactamente el permiso que necesitamos.
 *
 *    - Sin indicar CAMERA: No le estaríamos entregando
 *      el permiso específico que queremos solicitar.
 *
 *    - Con esto: Android sabe que queremos
 *      solicitar permiso para utilizar la cámara.
 *
 *
 * 7. if: PREGUNTAR SI EL PERMISO FUE CONCEDIDO
 *
 *    SINTAXIS GENERAL:
 *
 *    if (condicion) {
 *        instruccionesSiEsVerdadero
 *    } else {
 *        instruccionesSiEsFalso
 *    }
 *
 *    NUESTRO CÓDIGO:
 *
 *    if (permisoConcedido) {
 *        Text("Permiso concedido")
 *    } else {
 *        Text("Permiso no concedido")
 *    }
 *
 *    - if significa SI.
 *    - Imagina que le hacemos una pregunta a nuestra cajita:
 *
 *      ¿permisoConcedido guarda true?
 *
 *    - Si la respuesta es SÍ, mostramos "Permiso concedido".
 *    - Si la respuesta es NO, mostramos "Permiso no concedido".
 *
 *    LOS PARÉNTESIS ( ):
 *    - Dentro colocamos la condición que queremos comprobar.
 *
 *    LAS PRIMERAS LLAVES { }:
 *    - Contienen lo que hacemos si la condición es verdadera.
 *
 *
 * 8. else: SI NO SE CUMPLE LA CONDICIÓN
 *
 *    - else significa SI NO.
 *
 *    - Si permisoConcedido no vale true,
 *      ejecutamos las instrucciones de else.
 *
 *    - En nuestro caso mostramos:
 *
 *      Text("Permiso no concedido")
 *
 *    - Sin else: Si la condición fuera false,
 *      no mostraríamos este mensaje alternativo.
 *
 *    - Con else: Podemos mostrar un mensaje diferente
 *      cuando el permiso no está concedido.
 *
 *
 * 9. ¿CÓMO FUNCIONA TODA ESTA PARTE?
 *
 *    PASO 1:
 *    - Column organiza los elementos de la pantalla.
 *
 *    PASO 2:
 *    - Text muestra "Escáner de código QR".
 *
 *    PASO 3:
 *    - Button muestra "Solicitar permiso de cámara".
 *
 *    PASO 4:
 *    - El usuario toca el botón.
 *
 *    PASO 5:
 *    - onClick ejecuta las instrucciones.
 *
 *    PASO 6:
 *    - solicitarPermiso.launch(...) activa
 *      el control remoto de la segunda parte.
 *
 *    PASO 7:
 *    - Manifest.permission.CAMERA indica que
 *      queremos solicitar el permiso de cámara.
 *
 *    PASO 8:
 *    - Android procesa la solicitud.
 *
 *    PASO 9:
 *    - La respuesta llega a concedido,
 *      que declaramos en la segunda parte.
 *
 *    PASO 10:
 *    - Guardamos esa respuesta en permisoConcedido,
 *      nuestra variable de la primera parte.
 *
 *    PASO 11:
 *    - mutableStateOf avisa que cambió el valor.
 *
 *    PASO 12:
 *    - if revisa si permisoConcedido vale true o false.
 *
 *    PASO 13:
 *    - La pantalla muestra el mensaje correspondiente.
 *
 * IMPORTANTE:
 * - Al comenzar, permisoConcedido vale false.
 * - Por eso inicialmente aparecerá "Permiso no concedido".
 * - Ese mensaje todavía no comprueba si Android ya tenía
 *   concedido el permiso antes de abrir la pantalla.
 * - Además, este código todavía NO abre la cámara:
 *   solamente solicita el permiso.
 */


