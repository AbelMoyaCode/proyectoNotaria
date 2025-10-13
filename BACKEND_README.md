# Backend API REST - TramiNotar

## 📋 Descripción

API REST para el sistema de gestión de citas notariales TramiNotar.  
Esta API se conecta a PostgreSQL y expone endpoints para ser consumidos por la app Android.

## 🗄️ Base de Datos

**PostgreSQL** ya está configurada con el script en `database_setup.sql`

### Tablas principales:
- `usuarios` - Datos de los usuarios
- `tramites` - Catálogo de trámites notariales
- `horarios_disponibles` - Slots de horarios
- `tramites_usuarios` - Relación usuario-trámite
- `citas` - Reservas de citas
- `notificaciones` - Notificaciones del sistema

### Reglas de negocio implementadas en triggers:
- ✅ Solo fechas futuras para reservas
- ✅ Una cita por usuario por día
- ✅ Anticipación mínima de 1 día para cancelar/reprogramar

## 🚀 Tecnologías Recomendadas

Puedes usar cualquiera de estas opciones:

### Opción 1: Spring Boot (Java/Kotlin)
```
Spring Boot 3.x
Spring Data JPA
PostgreSQL Driver
Spring Security (JWT)
```

### Opción 2: Node.js/Express
```
Node.js + Express
pg (PostgreSQL driver)
jsonwebtoken (JWT)
bcrypt (hash passwords)
```

## 📡 Endpoints Requeridos

### Autenticación
```
POST   /api/auth/register          - Registrar usuario
POST   /api/auth/login             - Login (retorna token JWT)
GET    /api/auth/perfil            - Obtener perfil (requiere token)
PUT    /api/auth/perfil            - Actualizar perfil
POST   /api/auth/logout            - Cerrar sesión
```

### Trámites
```
GET    /api/tramites               - Listar todos
GET    /api/tramites/buscar?q=...  - Buscar por nombre/descripción
GET    /api/tramites/:codigo       - Detalle de trámite
GET    /api/horarios?fecha=...     - Horarios disponibles por fecha
```

### Citas
```
POST   /api/citas                  - Crear/Reservar cita
PATCH  /api/citas/:id/reprogramar  - Reprogramar cita
PATCH  /api/citas/:id/cancelar     - Cancelar cita
GET    /api/mis-tramites           - Mis trámites (requiere token)
GET    /api/mis-tramites?estado=.. - Filtrar por estado
GET    /api/mis-tramites/:id       - Detalle de mi trámite
```

### Notificaciones
```
GET    /api/notificaciones         - Todas las notificaciones
GET    /api/notificaciones?leido=false - Solo no leídas
PATCH  /api/notificaciones/:id/marcar-leida
PATCH  /api/notificaciones/marcar-todas-leidas
```

## 🔐 Autenticación

Usar **JWT (JSON Web Tokens)**:

1. El usuario hace login con correo y password
2. El backend valida contra la BD (comparar hash)
3. Si es válido, genera un token JWT
4. El token se envía en el header: `Authorization: Bearer {token}`
5. El backend valida el token en cada request protegido

## 📦 Estructura de Respuestas JSON

### Respuesta exitosa
```json
{
  "success": true,
  "mensaje": "Operación exitosa",
  "data": { /* objeto o array */ }
}
```

### Respuesta de error
```json
{
  "success": false,
  "mensaje": "Descripción del error",
  "data": null
}
```

### Login exitoso
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "usuario": {
    "id": "uuid",
    "nombres": "Abel",
    "apellidos": "Moya",
    "correo": "abel@correo.com",
    ...
  },
  "mensaje": "Login exitoso"
}
```

## 🛠️ Configuración PostgreSQL

### Variables de entorno (.env)
```
DB_HOST=localhost
DB_PORT=5432
DB_NAME=traminotar
DB_USER=postgres
DB_PASSWORD=notaria1234
JWT_SECRET=tu_secreto_super_seguro_aqui
PORT=3000
```

### Conexión en Node.js
```javascript
const { Pool } = require('pg');

const pool = new Pool({
  host: process.env.DB_HOST,
  port: process.env.DB_PORT,
  database: process.env.DB_NAME,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD
});
```

### Conexión en Spring Boot (application.properties)
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/traminotar
spring.datasource.username=postgres
spring.datasource.password=notaria1234
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
```

## 📝 Validaciones Importantes

### Registro de Usuario
- ✅ Correo único
- ✅ Formato de correo válido
- ✅ Contraseña mínimo 6 caracteres
- ✅ Hash de contraseña (bcrypt)
- ✅ Campos obligatorios: nombres, apellidos, correo, password

### Crear Cita
- ✅ Usuario autenticado
- ✅ Fecha futura (no hoy ni pasado)
- ✅ Horario disponible
- ✅ Usuario no tiene otra cita ese día
- ✅ Trámite existe y está activo

### Reprogramar Cita
- ✅ Cita existe y pertenece al usuario
- ✅ Anticipación mínima de 1 día
- ✅ Nuevo horario disponible
- ✅ No duplicar citas del usuario

### Cancelar Cita
- ✅ Anticipación mínima de 1 día
- ✅ Cita en estado AGENDADO o EN_PROCESO
- ✅ Liberar el horario

## 🚦 Códigos de Estado HTTP

- `200 OK` - Operación exitosa
- `201 Created` - Recurso creado
- `400 Bad Request` - Datos inválidos
- `401 Unauthorized` - No autenticado o token inválido
- `403 Forbidden` - No autorizado
- `404 Not Found` - Recurso no encontrado
- `409 Conflict` - Conflicto (ej: correo ya registrado)
- `500 Internal Server Error` - Error del servidor

## 🧪 Pruebas con Postman

### Ejemplo: Registro
```
POST http://localhost:3000/api/auth/register
Content-Type: application/json

{
  "tipo_doc": "DNI",
  "nro_doc": "74223311",
  "nombres": "Abel",
  "apellidos": "Moya",
  "correo": "abel@correo.com",
  "password": "password123",
  "direccion": "Av. Principal 123",
  "telefono": "987654321"
}
```

### Ejemplo: Login
```
POST http://localhost:3000/api/auth/login
Content-Type: application/json

{
  "correo": "abel@correo.com",
  "password": "password123"
}
```

### Ejemplo: Obtener Trámites
```
GET http://localhost:3000/api/tramites
```

### Ejemplo: Crear Cita (requiere token)
```
POST http://localhost:3000/api/citas
Authorization: Bearer {tu_token_aqui}
Content-Type: application/json

{
  "usuario_id": "uuid-del-usuario",
  "tramite_codigo": "TR-LF",
  "horario_id": "uuid-del-horario"
}
```

## 📂 Estructura Sugerida del Proyecto Backend

### Node.js/Express
```
backend/
├── config/
│   └── database.js         # Configuración de PostgreSQL
├── controllers/
│   ├── authController.js
│   ├── tramitesController.js
│   ├── citasController.js
│   └── notificacionesController.js
├── middleware/
│   └── authMiddleware.js   # Verificar JWT
├── routes/
│   ├── auth.js
│   ├── tramites.js
│   ├── citas.js
│   └── notificaciones.js
├── utils/
│   └── helpers.js
├── .env
├── package.json
└── server.js               # Punto de entrada
```

### Spring Boot
```
backend/
├── src/main/java/com/ampn/traminotar/
│   ├── config/
│   ├── controllers/
│   ├── models/
│   ├── repositories/
│   ├── services/
│   ├── security/
│   └── TramiNotarApplication.java
└── src/main/resources/
    └── application.properties
```

## 🔄 Próximos Pasos

1. ✅ Base de datos PostgreSQL configurada
2. ⏳ Desarrollar el backend (Spring Boot o Node.js)
3. ⏳ Implementar endpoints y validaciones
4. ⏳ Probar con Postman
5. ⏳ Desplegar el backend
6. ⏳ Actualizar `BASE_URL` en la app Android
7. ⏳ Integrar y probar desde la app

## 📚 Recursos Útiles

- [Spring Boot REST API Tutorial](https://spring.io/guides/tutorials/rest/)
- [Node.js + PostgreSQL](https://node-postgres.com/)
- [JWT Introduction](https://jwt.io/introduction)
- [Express.js Guide](https://expressjs.com/es/guide/routing.html)

---

**Proyecto: TramiNotar - Sprint 1**  
**Base de datos: PostgreSQL (traminotar)**  
**Puerto sugerido: 3000**

