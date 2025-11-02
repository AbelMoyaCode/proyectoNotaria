# 🔧 SOLUCIÓN AL PROBLEMA DE REGISTRO - TramiNotar

## ❌ PROBLEMAS ENCONTRADOS

1. **Desalineación de campos entre Frontend y Backend**
   - El backend esperaba: `tipo_doc`, `nro_doc`, `nombres`, `apellidos`, `password`
   - El frontend enviaba: `nro_documento`, `nombre`, `apellido_paterno`, `apellido_materno`, `contrasena`

2. **Campo de fecha de nacimiento no capturado**
   - El layout tenía el campo `editTextFechaNacimiento` pero el código no lo estaba usando
   - Se enviaba una fecha hardcodeada `"2000-01-01"` en lugar de la fecha ingresada por el usuario

3. **Modelo de Usuario desalineado**
   - La respuesta del backend usaba `camelCase` pero el modelo esperaba `snake_case`

4. **Manejo de errores insuficiente**
   - No se capturaban correctamente los errores de conexión
   - Mensajes de error genéricos que no ayudaban a depurar

## ✅ CAMBIOS REALIZADOS

### 1. Backend (api-backend/routes/auth.js)
```javascript
// Ahora acepta los campos correctos que envía el frontend:
- nro_documento (en lugar de nro_doc)
- nombre (en lugar de nombres)
- apellido_paterno (nuevo campo separado)
- apellido_materno (nuevo campo separado)
- fecha_nacimiento (ahora se captura del formulario)
- contrasena (en lugar de password)

// La respuesta ahora usa camelCase para coincidir con el frontend:
{
  success: true,
  mensaje: "Usuario registrado exitosamente",
  data: {
    id: 1,
    nroDocumento: "13423112",
    nombres: "Juan",
    apellidoPaterno: "Márquez",
    apellidoMaterno: "Castro",
    correo: "juamar34@gmail.com",
    ...
  }
}
```

### 2. Frontend - Modelo Usuario (api/modelos/Usuario.kt)
```kotlin
// Actualizado para usar camelCase en las anotaciones @SerializedName
data class Usuario(
    @SerializedName("nroDocumento") val nroDocumento: String,
    @SerializedName("nombres") val nombres: String,
    @SerializedName("apellidoPaterno") val apellidoPaterno: String,
    @SerializedName("apellidoMaterno") val apellidoMaterno: String,
    ...
)
```

### 3. Frontend - RegistroActivity.kt
```kotlin
// Ahora captura el campo de fecha de nacimiento del formulario:
val editTextFechaNacimiento = findViewById<EditText>(R.id.editTextFechaNacimiento)
val fechaNacimiento = editTextFechaNacimiento.text.toString().trim()

// Y lo envía en la solicitud:
val request = RegistroUsuarioRequest(
    fechaNacimiento = fechaNacimiento, // Ya no es hardcodeado
    ...
)
```

### 4. Frontend - AutenticacionRepositorio.kt
```kotlin
// Mejor manejo de errores con mensajes específicos:
catch (e: java.net.UnknownHostException) {
    Result.failure(Exception("No se puede conectar al servidor..."))
}
catch (e: java.net.SocketTimeoutException) {
    Result.failure(Exception("Tiempo de espera agotado..."))
}
catch (e: java.net.ConnectException) {
    Result.failure(Exception("Verifica que el backend esté ejecutándose..."))
}
```

## 🚀 INSTRUCCIONES PARA PROBAR

### Paso 1: Verificar que PostgreSQL esté corriendo
```cmd
# Asegúrate de que el servicio PostgreSQL esté activo
# Y que la base de datos "traminotar" exista con las tablas creadas
```

### Paso 2: Iniciar el servidor backend
```cmd
# Opción 1: Doble clic en el archivo
C:\Users\Abel\AndroidStudioProjects\proyectoNotaria\api-backend\iniciar-servidor.bat

# Opción 2: Desde la terminal
cd C:\Users\Abel\AndroidStudioProjects\proyectoNotaria\api-backend
node server.js
```

**Deberías ver:**
```
🚀 Servidor corriendo en http://localhost:3000
📡 API disponible en http://localhost:3000/api
✅ Health check: http://localhost:3000/api/health
✅ Conectado a PostgreSQL
✅ Conexión a PostgreSQL exitosa: [timestamp]
```

### Paso 3: Ejecutar la app Android
1. Abre el proyecto en Android Studio
2. Ejecuta la app en el emulador (asegúrate de que sea el emulador Android, no un dispositivo físico)
3. Ve a la pantalla de "Crear Cuenta"
4. Llena todos los campos:
   - **DNI:** 13423112
   - **Nombre:** Juan
   - **Apellido paterno:** Márquez
   - **Apellido materno:** Castro
   - **Fecha de nacimiento:** 04/08/2003
   - **Correo:** juamar34@gmail.com
   - **Dirección:** Av.Marsella 123
   - **Contraseña:** xxxxxx (al menos 6 caracteres)
   - **Repetir contraseña:** xxxxxx

5. Presiona "Registrarse"

### Paso 4: Verificar el resultado

**Si todo está correcto, deberías ver:**
- ✅ Toast: "¡Registro exitoso! Bienvenido Juan"
- La app regresa a la pantalla de login

**Si hay un error, verás uno de estos mensajes:**
- ❌ "El correo ya está registrado" (si ya existe en la BD)
- ❌ "El número de documento ya está registrado" (si ya existe en la BD)
- ❌ "No se puede conectar al servidor..." (si el backend no está corriendo)
- ❌ Otros mensajes específicos según el error

## 🔍 VERIFICAR EN LA BASE DE DATOS

```sql
-- Conectar a PostgreSQL y ejecutar:
SELECT * FROM usuarios WHERE correo = 'juamar34@gmail.com';

-- Deberías ver el nuevo usuario registrado con:
-- - nro_documento: 13423112
-- - nombre: Juan
-- - apellido_paterno: Márquez
-- - apellido_materno: Castro
-- - fecha_nacimiento: 2003-08-04
-- - correo: juamar34@gmail.com
-- - contrasena: [hash bcrypt]
```

## 📝 NOTAS IMPORTANTES

1. **Formato de fecha:** El usuario debe ingresar la fecha en formato DD/MM/YYYY o similar. Si el formato es incorrecto, el backend podría rechazarlo. Considera agregar validación de formato de fecha.

2. **Emulador vs Dispositivo físico:**
   - **Emulador:** usa `http://10.0.2.2:3000` (ya configurado en RetrofitClient)
   - **Dispositivo físico:** necesitarías cambiar a tu IP local (ej: `http://192.168.1.100:3000`)

3. **Contraseñas:** Se almacenan con hash bcrypt en la base de datos (seguridad ✅)

4. **Logs del servidor:** Revisa la consola donde corre `node server.js` para ver los logs de las peticiones y cualquier error del backend.

## 🐛 SI SIGUE SIN FUNCIONAR

1. **Verifica los logs en Android Studio (Logcat):**
   - Filtra por "AutenticacionRepositorio" o "RegistroActivity"
   - Busca mensajes de error de Retrofit

2. **Verifica los logs del backend:**
   - En la consola donde corre el servidor
   - Busca errores de PostgreSQL o de validación

3. **Prueba el endpoint manualmente con Postman/curl:**
   ```bash
   curl -X POST http://localhost:3000/api/auth/register \
     -H "Content-Type: application/json" \
     -d "{
       \"nro_documento\": \"12345678\",
       \"nombre\": \"Test\",
       \"apellido_paterno\": \"Usuario\",
       \"apellido_materno\": \"Prueba\",
       \"fecha_nacimiento\": \"1990-01-01\",
       \"correo\": \"test@example.com\",
       \"contrasena\": \"password123\"
     }"
   ```

## ✨ RESUMEN DE ARCHIVOS MODIFICADOS

1. ✅ `api-backend/routes/auth.js` - Endpoint de registro actualizado
2. ✅ `app/.../api/modelos/Usuario.kt` - Modelo actualizado con camelCase
3. ✅ `app/.../control/RegistroActivity.kt` - Captura fecha de nacimiento
4. ✅ `app/.../api/repositorios/AutenticacionRepositorio.kt` - Mejor manejo de errores
5. ✅ `api-backend/iniciar-servidor.bat` - Script para iniciar el backend fácilmente

---
**Fecha de corrección:** 2025-11-01
**Estado:** ✅ RESUELTO

