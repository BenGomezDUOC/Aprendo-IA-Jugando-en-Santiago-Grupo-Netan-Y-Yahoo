# Aprendo IA Jugando en Santiago 🏙️🤖

Plataforma móvil gamificada y georreferenciada para el aprendizaje de Inteligencia Artificial urbana, desarrollada para **Triskeledu SpA** en el marco de la asignatura **DSY1105 Desarrollo de Aplicaciones Móviles** (Duoc UC).

---

## 👥 Integrantes del Equipo

* **Benjamín Gómez** — *Arquitectura de Persistencia, Base de Datos Room SQLite, Patrón Repositorio, Notificaciones Nativas y Validación Desacoplada.*
* **Fernando Zapata** — *Módulo de Geolocalización, Integración OpenStreetMap (OSMDroid), GPS en Vivo, Tarjetas de Proximidad y UI Outdoor.*
* **Magdiel Esther Reynoso Ramírez** — *Módulo de Hardware Cámara, Escáner QR, Validación de Selfie Turística, Flujos de Roles y Navegación.*
* **Juan Pablo Taulis** — *Módulo Multimedia, Reproducción de Cápsulas de Video sobre IA, Sistema de Gamificación y Puntuación.*

---

## 📖 Descripción del Proyecto

Actualmente, los levantamientos de información en terreno suelen gestionarse de forma manual y desorganizada. **Aprendo IA Jugando en Santiago** resuelve esta problemática transformando el aprendizaje de la Inteligencia Artificial en una experiencia interactiva de gamificación urbana:
- Permite que la ciudadanía recorra puntos emblemáticos de la capital (Plaza de Armas, Cerro Santa Lucía, Cerro San Cristóbal, La Moneda, Parque Forestal, Barrio Bellavista).
- Detecta la proximidad mediante GPS en tiempo real.
- Permite escanear códigos QR físicos en terreno y capturar una selfie frontal como evidencia de visita.
- Desbloquea y reproduce cápsulas audiovisuales educativas sobre IA y acumula puntajes dinámicos en un entorno **100% offline y autónomo**.

---

## 🏛️ Arquitectura y Tecnologías

La aplicación implementa una arquitectura moderna, modular y desacoplada basada en las directrices oficiales de Android:

* **Arquitectura:** MVVM (Model-View-ViewModel) + Clean Repository Pattern.
* **UI Toolkit:** Jetpack Compose con Material Design 3 y Navigation Compose.
* **Persistencia Local:** Room Database (SQLite) con Coroutines (`Flow` reactivo) y precarga inicial de datos (*Data Seeding*).
* **Seguridad:** Encriptación y hashing de contraseñas con algoritmo PBKDF2 (`PasswordUtils`).

### 📱 Recursos Nativos Integrados (IE 2.4.1)
1. **Cámara del Dispositivo:** Implementada con **CameraX** para la lectura nativa de códigos QR y captura de selfies turísticas almacenadas internamente con `FileProvider`.
2. **GPS y Geolocalización:** Integrada con **Google Play Services Location** (`FusedLocationProviderClient`) para obtener coordenadas precisas (latitud, longitud, altitud) y calcular distancias en metros.
3. **Notificaciones del Sistema:** Implementada con `NotificationManager` y `NotificationCompat`, registrando un canal oficial (`NotificationChannel`) para emitir alertas emergentes en la barra de estado de Android ante logros y visitas validadas.

### ✨ Animaciones Visuales Funcionales (IE 2.2.2)
* `AnimatedVisibility` con `expandVertically() + fadeIn()` en mensajes de validación y errores de formularios.
* `animateFloatAsState` con curvas `FastOutSlowInEasing` en las barras de progreso general y exploración.
* `animateFloatAsState` (resorte *spring*) y `animateDpAsState` para retroalimentación táctil en la selección interactiva de avatares.
* `Modifier.animateContentSize()` en tarjetas expandibles de la colección de cápsulas.
* `slideInVertically()` y `slideOutVertically()` en la tarjeta emergente inferior de puntos en el mapa.

### 🛡️ Lógica de Validación Centralizada y Desacoplada (IE 2.2.1)
* **`FormularioValidator`:** Clase pura e independiente de la interfaz gráfica que centraliza las reglas de validación de campos (nombre, alias, correo con regex estándar, teléfono de 8-15 dígitos, contraseñas y confirmación). Acompañada de su suite de pruebas unitarias con JUnit.
* **`SelfieValidator`:** Validador dedicado a la verificación de rostros y códigos QR en imágenes capturadas.

---

## 🎭 Roles y Perfiles de Usuario (Sección 3.1)

1. **Cliente / Jugador:**
   - Explora el mapa de Santiago con marcadores interactivos.
   - Escanea códigos QR físicos y toma selfies turísticas para validar visitas.
   - Visualiza cápsulas audiovisuales sobre IA y consulta su progreso, historial y ranking.
2. **Generador de Contenido:**
   - Panel dedicado para crear, redactar y clasificar cápsulas educativas digitales sobre IA.
3. **Administrador:**
   - Gestión integral de puntos georreferenciados, alta de videos y generación de códigos QR asignados.

---

## 🧪 Cuentas de Acceso Rápido para Pruebas

Para facilitar la evaluación y testeo de todos los roles, se encuentran preconfiguradas las siguientes cuentas:

| Rol | Usuario / Alias | Correo | Contraseña |
| :--- | :--- | :--- | :--- |
| **Administrador** | Administrador | `admin@triskeledu.cl` | `admin1234` |
| **Generador de Contenido** | Generador | `generador@triskeledu.cl` | `generador1234` |
| **Cliente / Jugador** | *(Cualquier jugador registrado o creado desde la pantalla de Registro)* |

---

## 🚀 Pasos para Compilar y Ejecutar

### Requisitos Previos
* **Android Studio:** Ladybug (2024.2.1+) o posterior.
* **JDK:** Versión 17 o 21 (incluida en Android Studio).
* **Dispositivo de prueba:** Teléfono físico Android (recomendado para probar cámara y GPS en vivo) o Emulador con Android 10.0+ (API 29+).

### Instrucciones
1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/BenGomezDUOC/Aprendo-IA-Jugando-en-Santiago-Grupo-Netan-Y-Yahoo.git
   cd Aprendo-IA-Jugando-en-Santiago-Grupo-Netan-Y-Yahoo
   ```
2. **Abrir en Android Studio:**
   - Inicia Android Studio, haz clic en **Open** y selecciona la carpeta clonada.
   - Espera que finalice la sincronización inicial de Gradle (*Gradle Sync*).
3. **Ejecutar Pruebas Unitarias:**
   - En la pestaña *Project*, haz clic derecho sobre `app/src/test` y selecciona **Run 'Tests in 'test''**.
4. **Ejecutar la Aplicación:**
   - Selecciona tu dispositivo físico o emulador en la barra superior.
   - Presiona el botón verde de **Play ▶ (Run 'app')** o pulsa `Shift + F10`.
