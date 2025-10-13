# Integración API REST - TramiNotar

## 📋 Resumen

Este proyecto Android ahora está configurado para consumir una API REST que se conecta a PostgreSQL.

## 🏗️ Arquitectura

```
App Android (Kotlin) → Retrofit → API REST (Backend) → PostgreSQL
```

**Importante:** La app Android NO se conecta directamente a PostgreSQL. Siempre usa la API REST como intermediario.

## 📦 Estructura de Paquetes Creada

```
com.ampn.proyecto_notaria/
├── api/
│   ├── modelos/
│   │   ├── Usuario.kt              // Modelos de datos de usuario
│   │   ├── Tramite.kt              // Modelo de trámite
│   │   ├── HorarioDisponible.kt    // Modelo de horarios
│   │   ├── Cita.kt                 // Modelo de citas
│   │   ├── TramiteUsuario.kt       // Modelo de trámites del usuario
│   │   └── Notificacion.kt         // Modelo de notificaciones
│   │
│   ├── servicios/
│   │   ├── AutenticacionService.kt // Endpoints de autenticación
│   │   ├── TramitesService.kt      // Endpoints de trámites
│   │   ├── CitasService.kt         // Endpoints de citas
│   │   └── NotificacionesService.kt// Endpoints de notificaciones
│   │
│   ├── repositorios/
│   │   ├── AutenticacionRepositorio.kt
│   │   ├── TramitesRepositorio.kt
│   │   ├── CitasRepositorio.kt
│   │   └── NotificacionesRepositorio.kt
│   │
│   ├── utils/
│   │   └── GestorSesion.kt         // Manejo de token y sesión
│   │
│   └── RetrofitClient.kt           // Cliente Retrofit configurado
```

## 🔧 Configuración Necesaria

### 1. Cambiar la URL del Backend

En `RetrofitClient.kt`, actualiza la URL según tu entorno:

```kotlin
// Para emulador Android (localhost)
private const val BASE_URL = "http://10.0.2.2:3000/api/"

// Para dispositivo físico (reemplaza con tu IP local)
private const val BASE_URL = "http://192.168.1.100:3000/api/"

// Para servidor desplegado
private const val BASE_URL = "https://tu-dominio.com/api/"
```

### 2. Permisos en AndroidManifest.xml

Agrega estos permisos si aún no los tienes:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

Para desarrollo con HTTP (no HTTPS), agrega también:

```xml
<application
    android:usesCleartextTraffic="true"
    ...>
```

## 💻 Ejemplos de Uso

### Registro de Usuario

```kotlin
import com.ampn.proyecto_notaria.api.modelos.RegistroUsuarioRequest
import com.ampn.proyecto_notaria.api.repositorios.AutenticacionRepositorio
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

// En tu Activity o Fragment
val repositorio = AutenticacionRepositorio()

lifecycleScope.launch {
    val request = RegistroUsuarioRequest(
        tipoDocumento = "DNI",
        numeroDocumento = "74223311",
        nombres = "Abel",
        apellidos = "Moya",
        correo = "abel@correo.com",
        password = "password123",
        direccion = "Av. Principal 123",
        telefono = "987654321"
    )
    
    val resultado = repositorio.registrarUsuario(request)
    
    resultado.onSuccess { usuario ->
        // Registro exitoso
        Toast.makeText(this@MainActivity, "Usuario registrado", Toast.LENGTH_SHORT).show()
    }
    
    resultado.onFailure { error ->
        // Error en el registro
        Toast.makeText(this@MainActivity, error.message, Toast.LENGTH_SHORT).show()
    }
}
```

### Login

```kotlin
import com.ampn.proyecto_notaria.api.repositorios.AutenticacionRepositorio
import com.ampn.proyecto_notaria.api.utils.GestorSesion

val repositorio = AutenticacionRepositorio()
val gestorSesion = GestorSesion(this)

lifecycleScope.launch {
    val resultado = repositorio.login("abel@correo.com", "password123")
    
    resultado.onSuccess { loginResponse ->
        // Guardar token y usuario
        loginResponse.token?.let { gestorSesion.guardarToken(it) }
        gestorSesion.guardarUsuario(loginResponse.usuario)
        
        // Ir a la pantalla principal
        Toast.makeText(this@LoginActivity, "Bienvenido", Toast.LENGTH_SHORT).show()
        // startActivity(Intent(this@LoginActivity, MainActivity::class.java))
    }
    
    resultado.onFailure { error ->
        Toast.makeText(this@LoginActivity, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
    }
}
```

### Obtener Trámites

```kotlin
import com.ampn.proyecto_notaria.api.repositorios.TramitesRepositorio

val repositorio = TramitesRepositorio()

lifecycleScope.launch {
    val resultado = repositorio.obtenerTramites()
    
    resultado.onSuccess { tramites ->
        // Mostrar trámites en RecyclerView
        tramites.forEach { tramite ->
            Log.d("Tramite", "${tramite.nombre} - S/.${tramite.precio}")
        }
    }
    
    resultado.onFailure { error ->
        Toast.makeText(this@TramitesActivity, "Error al cargar", Toast.LENGTH_SHORT).show()
    }
}
```

### Crear una Cita

```kotlin
import com.ampn.proyecto_notaria.api.repositorios.CitasRepositorio
import com.ampn.proyecto_notaria.api.utils.GestorSesion

val repositorio = CitasRepositorio()
val gestorSesion = GestorSesion(this)

lifecycleScope.launch {
    val token = gestorSesion.obtenerToken() ?: return@launch
    val usuario = gestorSesion.obtenerUsuario() ?: return@launch
    
    val resultado = repositorio.crearCita(
        token = token,
        usuarioId = usuario.id!!,
        tramiteCodigo = "TR-LF",
        horarioId = "uuid-del-horario"
    )
    
    resultado.onSuccess { cita ->
        Toast.makeText(this@CitaActivity, "Cita reservada exitosamente", Toast.LENGTH_SHORT).show()
    }
    
    resultado.onFailure { error ->
        Toast.makeText(this@CitaActivity, error.message, Toast.LENGTH_SHORT).show()
    }
}
```

### Obtener Mis Trámites

```kotlin
import com.ampn.proyecto_notaria.api.repositorios.CitasRepositorio

val repositorio = CitasRepositorio()
val gestorSesion = GestorSesion(this)

lifecycleScope.launch {
    val token = gestorSesion.obtenerToken() ?: return@launch
    
    val resultado = repositorio.obtenerMisTramites(token)
    
    resultado.onSuccess { misTramites ->
        // Mostrar en RecyclerView
        misTramites.forEach { tramite ->
            Log.d("MiTramite", "${tramite.tramiteNombre} - ${tramite.estado}")
        }
    }
}
```

## 🔐 Manejo de Sesión

### Verificar si está autenticado

```kotlin
val gestorSesion = GestorSesion(this)

if (gestorSesion.estaAutenticado()) {
    // Usuario autenticado, ir a pantalla principal
} else {
    // Mostrar pantalla de login
}
```

### Cerrar Sesión

```kotlin
val gestorSesion = GestorSesion(this)
gestorSesion.cerrarSesion()

// Redirigir al login
startActivity(Intent(this, LoginActivity::class.java))
finish()
```

## 📡 Endpoints Disponibles

### Autenticación
- `POST /auth/register` - Registrar usuario
- `POST /auth/login` - Iniciar sesión
- `GET /auth/perfil` - Obtener perfil
- `PUT /auth/perfil` - Actualizar perfil
- `POST /auth/logout` - Cerrar sesión

### Trámites
- `GET /tramites` - Listar todos los trámites
- `GET /tramites/buscar?q={query}` - Buscar trámites
- `GET /tramites/{codigo}` - Detalle de trámite
- `GET /horarios?fecha={YYYY-MM-DD}` - Horarios disponibles

### Citas
- `POST /citas` - Crear/Reservar cita
- `PATCH /citas/{id}/reprogramar` - Reprogramar cita
- `PATCH /citas/{id}/cancelar` - Cancelar cita
- `GET /mis-tramites` - Ver mis trámites
- `GET /mis-tramites?estado={estado}` - Filtrar por estado
- `GET /mis-tramites/{id}` - Detalle de mi trámite

### Notificaciones
- `GET /notificaciones` - Todas las notificaciones
- `GET /notificaciones?leido=false` - No leídas
- `PATCH /notificaciones/{id}/marcar-leida` - Marcar como leída
- `PATCH /notificaciones/marcar-todas-leidas` - Marcar todas

## 🚀 Próximos Pasos

1. **Desarrollar el Backend (API REST)**
   - Puedes usar Spring Boot, Node.js/Express, o tu framework preferido
   - Conectar el backend a PostgreSQL (ya tienes la base configurada en pgAdmin)
   - Implementar los endpoints listados arriba

2. **Sincronizar con el proyecto**
   - Una vez que el backend esté funcionando, actualiza la `BASE_URL` en `RetrofitClient.kt`
   - Prueba los endpoints con Postman primero
   - Luego integra desde la app Android

3. **Actualizar las Activities existentes**
   - Reemplaza las llamadas directas a base de datos por llamadas a los repositorios
   - Usa `lifecycleScope.launch` para llamadas asíncronas
   - Maneja los estados de carga y error apropiadamente

## ⚠️ Notas Importantes

- **NO uses PostgreSQL directamente desde Android**. Siempre usa la API REST.
- La dependencia `postgresql:42.2.5` en build.gradle puede eliminarse si no la usas.
- Todas las llamadas a repositorios son `suspend fun`, úsalas con coroutines.
- El `GestorSesion` guarda el token automáticamente en SharedPreferences.
- El logging de red está activado para debug (ver `RetrofitClient.kt`).

## 🛠️ Troubleshooting

### Error de conexión
- Verifica que el backend esté corriendo
- Confirma la URL correcta (10.0.2.2 para emulador, IP local para dispositivo físico)
- Asegúrate de tener `usesCleartextTraffic="true"` si usas HTTP

### Token expirado
- Implementa lógica de refresh token en el backend
- Maneja el código 401 (Unauthorized) y redirige al login

### Errores de serialización JSON
- Verifica que los nombres de los campos en los modelos coincidan con la respuesta del backend
- Usa `@SerializedName` para mapear correctamente

---

**Creado para el proyecto TramiNotar - Sprint 1**  
**Autor: Abel Moya**

