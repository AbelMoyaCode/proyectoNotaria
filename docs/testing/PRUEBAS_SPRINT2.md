# 📋 PRUEBAS DEL SPRINT 2 - PROYECTO NOTARÍA
**Fecha:** 01/11/2025  
**Versión:** 1.0  
**Estado:** En Pruebas

---

## 🎯 OBJETIVOS DEL SPRINT 2

Implementar funcionalidades de gestión de perfil, navegación entre pantallas, y sistema de notificaciones dinámicas.

---

## ✅ HISTORIAS DE USUARIO IMPLEMENTADAS

### **HU-03: Implementar Cierre de Sesión**
- **Estado:** ✅ COMPLETADA
- **Pantalla:** PerfilActivity
- **Funcionalidad:** Botón "Cerrar Sesión" que limpia la sesión y redirige al login

### **HU-04: Gestión de Perfil del Cliente**
- **Estado:** ✅ COMPLETADA
- **Pantalla:** PerfilActivity
- **Funcionalidades:**
  - Ver nombre completo del usuario
  - Ver correo electrónico
  - Editar DNI
  - Editar teléfono
  - Guardar cambios
  - Cerrar sesión

### **HU-08: Implementar Lógica de Agendamiento de Cita**
- **Estado:** ✅ COMPLETADA
- **Pantalla:** HomeFragment
- **Funcionalidades:**
  - Mostrar próxima cita agendada
  - Botón "Ver detalles" que navega a MisCitasActivity
  - Sincronización con base de datos PostgreSQL

### **Sistema de Notificaciones Dinámicas**
- **Estado:** ✅ COMPLETADA
- **Pantalla:** HomeFragment
- **Funcionalidades:**
  - Mostrar notificaciones recientes (últimas 3)
  - Tipos de notificaciones: Confirmación, Reprogramación, Recordatorio, Cancelación
  - Al hacer clic, navega a la cita correspondiente
  - Se oculta automáticamente si no hay notificaciones

---

## 🧪 PLAN DE PRUEBAS

### **1. PRUEBAS DE NAVEGACIÓN**

#### **Prueba 1.1: Navegación desde Home a Perfil**
- **Precondición:** Usuario autenticado en Home
- **Pasos:**
  1. Abrir la app y hacer login
  2. En la pantalla Home, tocar el botón "Perfil"
- **Resultado esperado:** Se abre PerfilActivity mostrando los datos del usuario
- **Estado:** ⬜ Pendiente

#### **Prueba 1.2: Navegación desde Home a Mis Citas**
- **Precondición:** Usuario autenticado en Home
- **Pasos:**
  1. En la pantalla Home, tocar el botón "Mis Citas"
- **Resultado esperado:** Se abre MisCitasActivity con el listado de citas
- **Estado:** ⬜ Pendiente

#### **Prueba 1.3: Navegación desde Home a Ver Trámites**
- **Precondición:** Usuario autenticado en Home
- **Pasos:**
  1. En la pantalla Home, tocar el botón "Ver Trámites"
- **Resultado esperado:** Se abre ListadoTramitesActivity con trámites disponibles
- **Estado:** ⬜ Pendiente

---

### **2. PRUEBAS DE PERFIL DE USUARIO**

#### **Prueba 2.1: Visualización de datos del perfil**
- **Precondición:** Usuario autenticado
- **Pasos:**
  1. Navegar a PerfilActivity
  2. Verificar que se muestran:
     - Nombre completo
     - Correo electrónico
     - DNI
     - Teléfono
- **Resultado esperado:** Todos los datos se muestran correctamente
- **Estado:** ⬜ Pendiente

#### **Prueba 2.2: Editar DNI y guardar cambios**
- **Precondición:** Usuario en PerfilActivity
- **Pasos:**
  1. Modificar el campo DNI (ej: "12345678")
  2. Tocar "Guardar cambios"
- **Resultado esperado:** 
  - Mensaje "✅ Datos actualizados correctamente"
  - El DNI se guarda en la sesión
- **Estado:** ⬜ Pendiente

#### **Prueba 2.3: Validación de campos obligatorios**
- **Precondición:** Usuario en PerfilActivity
- **Pasos:**
  1. Borrar el campo DNI
  2. Tocar "Guardar cambios"
- **Resultado esperado:** Mensaje de error "El DNI es obligatorio"
- **Estado:** ⬜ Pendiente

#### **Prueba 2.4: Cerrar sesión**
- **Precondición:** Usuario en PerfilActivity
- **Pasos:**
  1. Tocar el botón "Cerrar Sesión"
- **Resultado esperado:**
  - Mensaje "✅ Sesión cerrada correctamente"
  - Redirige a MainActivity (login)
  - No se puede volver atrás con el botón back
- **Estado:** ⬜ Pendiente

---

### **3. PRUEBAS DE PRÓXIMA CITA**

#### **Prueba 3.1: Mostrar próxima cita agendada**
- **Precondición:** Usuario con al menos 1 cita en estado "AGENDADO" en BD
- **Pasos:**
  1. Iniciar sesión
  2. Ver la sección "Mi próxima cita" en Home
- **Resultado esperado:**
  - Se muestra la cita más próxima
  - Formato: "📅 DD/MM/YYYY a las HH:MM"
  - Botón "Ver detalles" visible
- **Estado:** ⬜ Pendiente

#### **Prueba 3.2: Ocultar sección si no hay citas**
- **Precondición:** Usuario sin citas agendadas en BD
- **Pasos:**
  1. Iniciar sesión
  2. Ver la pantalla Home
- **Resultado esperado:** La sección "Mi próxima cita" NO se muestra
- **Estado:** ⬜ Pendiente

#### **Prueba 3.3: Navegación al detalle de cita**
- **Precondición:** Sección "Mi próxima cita" visible
- **Pasos:**
  1. Tocar el botón "Ver detalles"
- **Resultado esperado:** Navega a MisCitasActivity con la cita correspondiente
- **Estado:** ⬜ Pendiente

---

### **4. PRUEBAS DE NOTIFICACIONES DINÁMICAS**

#### **Prueba 4.1: Mostrar notificaciones de confirmación**
- **Precondición:** Usuario con cita en estado "AGENDADO"
- **Pasos:**
  1. Iniciar sesión
  2. Ver la sección "Notificaciones recientes"
- **Resultado esperado:**
  - Se muestra notificación con icono ✅
  - Título: "Confirmación de cita"
  - Mensaje: "Tu cita para [nombre trámite] ha sido confirmada"
  - Fecha formateada
- **Estado:** ⬜ Pendiente

#### **Prueba 4.2: Mostrar notificaciones de reprogramación**
- **Precondición:** Usuario con cita en estado "REPROGRAMADO"
- **Pasos:**
  1. Iniciar sesión
  2. Ver las notificaciones
- **Resultado esperado:**
  - Notificación con icono 🔄
  - Título: "Cita reprogramada"
- **Estado:** ⬜ Pendiente

#### **Prueba 4.3: Mostrar notificaciones de cancelación**
- **Precondición:** Usuario con cita en estado "CANCELADO"
- **Pasos:**
  1. Iniciar sesión
  2. Ver las notificaciones
- **Resultado esperado:**
  - Notificación con icono ❌
  - Título: "Cita cancelada"
  - Apariencia atenuada (opacidad 0.6) porque ya fue leída
- **Estado:** ⬜ Pendiente

#### **Prueba 4.4: Limitar a 3 notificaciones máximo**
- **Precondición:** Usuario con más de 3 citas
- **Pasos:**
  1. Iniciar sesión
  2. Contar notificaciones mostradas
- **Resultado esperado:** Solo se muestran las 3 más recientes
- **Estado:** ⬜ Pendiente

#### **Prueba 4.5: Ocultar sección si no hay notificaciones**
- **Precondición:** Usuario sin citas en la BD
- **Pasos:**
  1. Iniciar sesión
  2. Ver la pantalla Home
- **Resultado esperado:** La sección "Notificaciones recientes" NO se muestra
- **Estado:** ⬜ Pendiente

#### **Prueba 4.6: Navegación desde notificación a cita**
- **Precondición:** Notificaciones visibles
- **Pasos:**
  1. Tocar una notificación
- **Resultado esperado:** Navega a MisCitasActivity con la cita correspondiente
- **Estado:** ⬜ Pendiente

---

### **5. PRUEBAS DE SINCRONIZACIÓN CON BASE DE DATOS**

#### **Prueba 5.1: Sincronización de datos de usuario**
- **Precondición:** Usuario registrado en PostgreSQL
- **Pasos:**
  1. Hacer login
  2. Verificar datos en Home y Perfil
- **Resultado esperado:** Los datos coinciden con los de la BD
- **Estado:** ⬜ Pendiente

#### **Prueba 5.2: Sincronización de citas**
- **Precondición:** Citas registradas en tabla `citas` de PostgreSQL
- **Pasos:**
  1. Hacer login
  2. Ver "Mi próxima cita"
- **Resultado esperado:** Se muestra la cita correcta según la BD
- **Estado:** ⬜ Pendiente

#### **Prueba 5.3: Actualización en tiempo real**
- **Precondición:** App abierta
- **Pasos:**
  1. Desde otro cliente (ej: Postman), agregar una nueva cita
  2. Cerrar y volver a abrir la app
- **Resultado esperado:** La nueva cita se refleja en Home
- **Estado:** ⬜ Pendiente

---

### **6. PRUEBAS DE INTERFAZ Y UX**

#### **Prueba 6.1: Colores y tema consistente**
- **Pasos:**
  1. Revisar todas las pantallas del Sprint 2
- **Resultado esperado:** 
  - Color principal verde (#1adb8e) aplicado correctamente
  - Textos legibles
  - Contraste adecuado
- **Estado:** ⬜ Pendiente

#### **Prueba 6.2: Responsividad en diferentes tamaños**
- **Pasos:**
  1. Probar en emulador Pixel 5
  2. Probar en emulador de tablet
- **Resultado esperado:** La interfaz se adapta correctamente
- **Estado:** ⬜ Pendiente

#### **Prueba 6.3: Animaciones y transiciones**
- **Pasos:**
  1. Navegar entre pantallas
  2. Observar transiciones
- **Resultado esperado:** Transiciones fluidas sin lag
- **Estado:** ⬜ Pendiente

---

### **7. PRUEBAS DE ERRORES Y EXCEPCIONES**

#### **Prueba 7.1: Sin conexión a internet**
- **Precondición:** Desactivar WiFi y datos móviles
- **Pasos:**
  1. Intentar cargar Home
- **Resultado esperado:** 
  - No se crashea la app
  - Mensaje de error apropiado
  - Secciones dinámicas se ocultan
- **Estado:** ⬜ Pendiente

#### **Prueba 7.2: Token de sesión expirado**
- **Precondición:** Token inválido o expirado
- **Pasos:**
  1. Intentar acceder a Home
- **Resultado esperado:** Redirige automáticamente al login
- **Estado:** ⬜ Pendiente

#### **Prueba 7.3: Datos corruptos en sesión**
- **Precondición:** Datos de usuario corruptos en SharedPreferences
- **Pasos:**
  1. Abrir la app
- **Resultado esperado:** Limpia sesión y redirige al login sin crash
- **Estado:** ⬜ Pendiente

---

## 📊 RESUMEN DE PRUEBAS

| Categoría | Total | Pendientes | Pasadas | Fallidas |
|-----------|-------|------------|---------|----------|
| Navegación | 3 | 3 | 0 | 0 |
| Perfil | 4 | 4 | 0 | 0 |
| Próxima Cita | 3 | 3 | 0 | 0 |
| Notificaciones | 6 | 6 | 0 | 0 |
| Sincronización BD | 3 | 3 | 0 | 0 |
| Interfaz/UX | 3 | 3 | 0 | 0 |
| Errores/Excepciones | 3 | 3 | 0 | 0 |
| **TOTAL** | **25** | **25** | **0** | **0** |

---

## 🔧 INSTRUCCIONES PARA EJECUTAR PRUEBAS

### **Preparación del Entorno:**

1. **Limpiar proyecto:**
   ```cmd
   cd C:\Users\Abel\AndroidStudioProjects\proyectoNotaria
   LIMPIAR_PROYECTO.bat
   ```

2. **Compilar app:**
   - Build → Rebuild Project en Android Studio

3. **Iniciar emulador:**
   - Pixel 5 API 33

4. **Verificar servidor backend:**
   ```cmd
   cd api-backend
   npm start
   ```
   - Debe mostrar: ✅ Conectado a PostgreSQL

5. **Verificar datos en PostgreSQL:**
   - Abrir pgAdmin
   - Verificar que existan usuarios y citas de prueba

---

## 📝 PLANTILLA PARA REGISTRAR RESULTADOS

```
PRUEBA: [Número y nombre]
FECHA: [DD/MM/YYYY]
EJECUTADA POR: [Nombre]
RESULTADO: [✅ Pasada / ❌ Fallida]
OBSERVACIONES: [Descripción de lo observado]
EVIDENCIA: [Captura de pantalla adjunta]
```

---

## 🐛 REGISTRO DE BUGS ENCONTRADOS

| ID | Descripción | Severidad | Estado | Solucionado en |
|----|-------------|-----------|--------|----------------|
| - | - | - | - | - |

**Severidad:**
- 🔴 Crítica: Bloquea funcionalidad principal
- 🟡 Alta: Afecta funcionalidad importante
- 🟢 Media: Problema menor
- ⚪ Baja: Cosmético

---

## ✅ CRITERIOS DE ACEPTACIÓN DEL SPRINT 2

- [x] Todas las HU están implementadas
- [ ] Al menos el 80% de las pruebas pasan exitosamente
- [ ] No hay bugs críticos sin resolver
- [ ] La app compila sin errores
- [ ] El código está documentado
- [ ] La interfaz es consistente con el diseño

---

**Notas adicionales:**
- Ejecutar pruebas en orden secuencial
- Documentar cada prueba con capturas de pantalla
- Reportar inmediatamente cualquier bug crítico
- Actualizar este documento con los resultados

