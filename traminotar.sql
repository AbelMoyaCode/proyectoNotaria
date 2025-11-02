-- ===========================================
--  BASE DE DATOS: TRAMINOTAR
--  Sistema de Gestión de Trámites y Citas Notariales
--  Compatible con Backend Node.js
--  Autor: Abel Moya
--  Fecha: 2025-10-12
-- ===========================================

-- ===========================================
-- PASO 1: ELIMINAR BASE DE DATOS SI EXISTE
-- ===========================================
-- Desconectar usuarios activos de la base de datos
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = 'traminotar' AND pid <> pg_backend_pid();

-- Eliminar base de datos si existe
DROP DATABASE IF EXISTS traminotar;

-- ===========================================
-- PASO 2: CREAR BASE DE DATOS
-- ===========================================
CREATE DATABASE traminotar
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'Spanish_Spain.1252'
    LC_CTYPE = 'Spanish_Spain.1252'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- Comentario de la base de datos
COMMENT ON DATABASE traminotar IS 'Sistema de gestión de trámites notariales';

-- ===========================================
-- PASO 3: CONECTAR A LA BASE DE DATOS
-- ===========================================
-- Nota: En pgAdmin, selecciona la base de datos 'traminotar' en el panel izquierdo
-- antes de ejecutar el resto del script
-- OJO SI EL PASO 1 Y 2 O SI YA HA SIDO CREADO YA HA SIDO CREADO DESDE ANTES POR EL POSTGRESQL, OBVIAR EL PASO 1 Y 2 , EMPIEZA POR EL PASO 4

-- ===========================================
-- PASO 4: CREAR EXTENSIONES NECESARIAS
-- ===========================================
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ===========================================
-- PASO 5: CREAR TABLAS PRINCIPALES
-- ===========================================

-- ============================================
-- TABLA: usuarios
-- Almacena información de los usuarios del sistema
-- ============================================
CREATE TABLE usuarios (
    id SERIAL PRIMARY KEY,
    nro_documento VARCHAR(20) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellido_paterno VARCHAR(100) NOT NULL,
    apellido_materno VARCHAR(100) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    correo VARCHAR(150) UNIQUE NOT NULL,
    direccion VARCHAR(250),
    telefono VARCHAR(40),
    contrasena VARCHAR(255) NOT NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(20) DEFAULT 'ACTIVO',
    
    CONSTRAINT chk_estado_usuario CHECK (estado IN ('ACTIVO', 'INACTIVO', 'SUSPENDIDO')),
    CONSTRAINT chk_email_formato CHECK (correo ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

COMMENT ON TABLE usuarios IS 'Usuarios registrados en el sistema';
COMMENT ON COLUMN usuarios.id IS 'Identificador único del usuario';
COMMENT ON COLUMN usuarios.nro_documento IS 'Número de documento (DNI, CE, Pasaporte)';
COMMENT ON COLUMN usuarios.contrasena IS 'Contraseña hasheada del usuario';

-- ============================================
-- TABLA: tramites
-- Catálogo de trámites disponibles
-- ============================================
CREATE TABLE tramites (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(20) UNIQUE NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT NOT NULL,
    requisitos TEXT NOT NULL,
    precio NUMERIC(10, 2) NOT NULL DEFAULT 0,
    duracion_estimada VARCHAR(50),
    categoria VARCHAR(100),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE,
    
    CONSTRAINT chk_precio_positivo CHECK (precio >= 0),
    CONSTRAINT chk_codigo_formato CHECK (codigo ~ '^[A-Z0-9-]+$')
);

COMMENT ON TABLE tramites IS 'Catálogo de trámites notariales disponibles';
COMMENT ON COLUMN tramites.codigo IS 'Código único del trámite (POD-001, ESC-001, etc.)';
COMMENT ON COLUMN tramites.categoria IS 'Categoría: Poderes, Escrituras, Empresarial, Certificación, etc.';

-- ============================================
-- TABLA: citas
-- Registro de citas agendadas
-- ============================================
CREATE TABLE citas (
    id SERIAL PRIMARY KEY,
    usuario_id INTEGER NOT NULL,
    tramite_id INTEGER NOT NULL,
    fecha_cita DATE NOT NULL,
    hora_cita TIME NOT NULL,
    estado VARCHAR(20) DEFAULT 'AGENDADO',
    motivo_cancelacion TEXT,
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_cancelacion TIMESTAMP,
    
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (tramite_id) REFERENCES tramites(id) ON DELETE CASCADE,
    
    CONSTRAINT chk_estado_cita CHECK (estado IN ('AGENDADO', 'EN_PROCESO', 'FINALIZADO', 'CANCELADO'))
);

COMMENT ON TABLE citas IS 'Registro de citas agendadas por usuarios';
COMMENT ON COLUMN citas.estado IS 'AGENDADO, EN_PROCESO, FINALIZADO, CANCELADO';


-- ============================================
-- TABLA: horarios_disponibles
-- Reserva de cita agendada
-- ============================================
CREATE TABLE horarios_disponibles (
    id SERIAL PRIMARY KEY,
    fecha DATE NOT NULL,
    hora TIME NOT NULL,
    disponible BOOLEAN DEFAULT TRUE
);


-- SOLO LO NECESARIO - Actualización mínima

-- 1. Crear tabla tramites_usuarios
CREATE TABLE IF NOT EXISTS tramites_usuarios (
    id SERIAL PRIMARY KEY,
    usuario_id INTEGER NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    tramite_codigo VARCHAR(20) NOT NULL REFERENCES tramites(codigo) ON DELETE CASCADE,
    estado_general VARCHAR(20) DEFAULT 'AGENDADO',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Agregar columnas a citas
ALTER TABLE citas ADD COLUMN IF NOT EXISTS horario_id INTEGER REFERENCES horarios_disponibles(id);
ALTER TABLE citas ADD COLUMN IF NOT EXISTS tramite_usuario_id INTEGER REFERENCES tramites_usuarios(id);
ALTER TABLE citas ADD COLUMN IF NOT EXISTS creada_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- 3. ✅ ELIMINAR RESTRICCIONES PROBLEMÁTICAS
ALTER TABLE citas DROP CONSTRAINT IF EXISTS chk_fecha_futura;
ALTER TABLE citas DROP CONSTRAINT IF EXISTS unique_usuario_fecha;
DROP INDEX IF EXISTS unique_usuario_fecha;
DROP INDEX IF EXISTS idx_usuario_fecha_hora;

-- 4. ✅ CREAR ÍNDICE PARCIAL: Solo bloquea citas ACTIVAS (no canceladas)
CREATE UNIQUE INDEX IF NOT EXISTS idx_cita_activa_usuario_fecha_hora
ON citas (usuario_id, fecha_cita, hora_cita)
WHERE estado IN ('AGENDADO', 'EN_PROCESO');

-- Mensaje de confirmación
DO $$
BEGIN
    RAISE NOTICE '========================================';
    RAISE NOTICE '✅ Base de datos TRAMINOTAR actualizada';
    RAISE NOTICE '✅ Restricciones problemáticas eliminadas';
    RAISE NOTICE '✅ Ahora se permite reutilizar horarios de citas canceladas';
    RAISE NOTICE '✅ Restricción: Solo 1 cita ACTIVA por usuario/fecha/hora';
    RAISE NOTICE '========================================';
END $$;

-- ============================================
-- TABLA: notificaciones
-- Sistema de notificaciones
-- ============================================
CREATE TABLE notificaciones (
    id SERIAL PRIMARY KEY,
    usuario_id INTEGER NOT NULL,
    cita_id INTEGER,
    tipo VARCHAR(50) NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    mensaje TEXT NOT NULL,
    leido BOOLEAN DEFAULT FALSE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_lectura TIMESTAMP,
    
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (cita_id) REFERENCES citas(id) ON DELETE CASCADE,
    
    CONSTRAINT chk_tipo_notificacion CHECK (tipo IN ('CITA_CREADA', 'CITA_RECORDATORIO', 'CITA_CANCELADA', 
                                                      'CITA_REPROGRAMADA', 'SISTEMA', 'PROMOCION'))
);

COMMENT ON TABLE notificaciones IS 'Sistema de notificaciones para usuarios';

-- ============================================
-- TABLA: historial_cambios
-- Auditoría de cambios
-- ============================================
CREATE TABLE historial_cambios (
    id SERIAL PRIMARY KEY,
    tabla_afectada VARCHAR(50) NOT NULL,
    registro_id INTEGER NOT NULL,
    accion VARCHAR(20) NOT NULL,
    usuario_id INTEGER,
    datos_anteriores JSONB,
    datos_nuevos JSONB,
    fecha_cambio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_origen VARCHAR(45),
    
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL,
    
    CONSTRAINT chk_accion CHECK (accion IN ('INSERT', 'UPDATE', 'DELETE'))
);

COMMENT ON TABLE historial_cambios IS 'Auditoría de cambios en el sistema';

-- ===========================================
-- PASO 6: CREAR ÍNDICES PARA OPTIMIZACIÓN
-- ===========================================

-- Índices para usuarios
CREATE INDEX idx_usuarios_correo ON usuarios(correo);
CREATE INDEX idx_usuarios_documento ON usuarios(nro_documento);
CREATE INDEX idx_usuarios_estado ON usuarios(estado);
CREATE INDEX idx_usuarios_fecha_registro ON usuarios(fecha_registro DESC);

-- Índices para tramites
CREATE INDEX idx_tramites_codigo ON tramites(codigo);
CREATE INDEX idx_tramites_categoria ON tramites(categoria);
CREATE INDEX idx_tramites_activo ON tramites(activo);
CREATE INDEX idx_tramites_precio ON tramites(precio);

-- Índices para citas
CREATE INDEX idx_citas_usuario ON citas(usuario_id);
CREATE INDEX idx_citas_tramite ON citas(tramite_id);
CREATE INDEX idx_citas_fecha ON citas(fecha_cita);
CREATE INDEX idx_citas_estado ON citas(estado);
CREATE INDEX idx_citas_fecha_hora ON citas(fecha_cita, hora_cita);

-- Índices para notificaciones
CREATE INDEX idx_notificaciones_usuario ON notificaciones(usuario_id);
CREATE INDEX idx_notificaciones_leido ON notificaciones(leido);
CREATE INDEX idx_notificaciones_tipo ON notificaciones(tipo);
CREATE INDEX idx_notificaciones_fecha ON notificaciones(fecha_creacion DESC);

-- Índices para historial
CREATE INDEX idx_historial_tabla ON historial_cambios(tabla_afectada);
CREATE INDEX idx_historial_usuario ON historial_cambios(usuario_id);
CREATE INDEX idx_historial_fecha ON historial_cambios(fecha_cambio DESC);

-- ===========================================
-- PASO 7: CREAR FUNCIONES Y TRIGGERS (CORREGIDO)
-- ===========================================

-- Función para actualizar fecha de modificación (para la tabla "citas")
CREATE OR REPLACE FUNCTION actualizar_fecha_modificacion()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fecha_modificacion = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger para "citas"
DROP TRIGGER IF EXISTS trigger_actualizar_fecha_citas ON citas;
CREATE TRIGGER trigger_actualizar_fecha_citas
    BEFORE UPDATE ON citas
    FOR EACH ROW
    EXECUTE FUNCTION actualizar_fecha_modificacion();

-- CORRECCIÓN: función específica para "tramites"
-- Actualiza la columna correcta: fecha_actualizacion
CREATE OR REPLACE FUNCTION actualizar_fecha_actualizacion_tramites()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fecha_actualizacion = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- CORRECCIÓN: trigger de "tramites" apuntando a la función correcta
DROP TRIGGER IF EXISTS trigger_actualizar_fecha_tramites ON tramites;
CREATE TRIGGER trigger_actualizar_fecha_tramites
    BEFORE UPDATE ON tramites
    FOR EACH ROW
    EXECUTE FUNCTION actualizar_fecha_actualizacion_tramites();

-- Función para crear notificación automática
CREATE OR REPLACE FUNCTION crear_notificacion_cita()
RETURNS TRIGGER AS $$
DECLARE
    v_tramite_nombre VARCHAR(200);
BEGIN
    SELECT nombre INTO v_tramite_nombre FROM tramites WHERE id = NEW.tramite_id;
    
    INSERT INTO notificaciones (usuario_id, cita_id, tipo, titulo, mensaje)
    VALUES (
        NEW.usuario_id,
        NEW.id,
        'CITA_CREADA',
        'Cita Agendada',
        'Su cita para "' || v_tramite_nombre || '" ha sido agendada para el ' || 
        TO_CHAR(NEW.fecha_cita, 'DD/MM/YYYY') || ' a las ' || TO_CHAR(NEW.hora_cita, 'HH24:MI')
    );
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_notificacion_cita
    AFTER INSERT ON citas
    FOR EACH ROW
    EXECUTE FUNCTION crear_notificacion_cita();

-- Función para registrar cancelación
CREATE OR REPLACE FUNCTION registrar_cancelacion_cita()
RETURNS TRIGGER AS $$
DECLARE
    v_tramite_nombre VARCHAR(200);
BEGIN
    IF NEW.estado = 'CANCELADO' AND OLD.estado <> 'CANCELADO' THEN
        NEW.fecha_cancelacion = CURRENT_TIMESTAMP;
        
        SELECT nombre INTO v_tramite_nombre FROM tramites WHERE id = NEW.tramite_id;
        
        INSERT INTO notificaciones (usuario_id, cita_id, tipo, titulo, mensaje)
        VALUES (
            NEW.usuario_id,
            NEW.id,
            'CITA_CANCELADA',
            'Cita Cancelada',
            'Su cita para "' || v_tramite_nombre || '" ha sido cancelada.'
        );
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_cancelacion_cita
    BEFORE UPDATE OF estado ON citas
    FOR EACH ROW
    EXECUTE FUNCTION registrar_cancelacion_cita();

-- ===========================================
-- PASO 8: CREAR VISTAS ÚTILES
-- ===========================================

-- Vista de citas activas
CREATE OR REPLACE VIEW vista_citas_activas AS
SELECT 
    c.id AS cita_id,
    c.fecha_cita,
    c.hora_cita,
    c.estado,
    u.id AS usuario_id,
    u.nombre || ' ' || u.apellido_paterno || ' ' || u.apellido_materno AS nombre_completo,
    u.correo,
    u.telefono,
    t.id AS tramite_id,
    t.codigo AS tramite_codigo,
    t.nombre AS tramite_nombre,
    t.categoria,
    t.precio,
    t.duracion_estimada,
    c.fecha_creacion,
    c.observaciones
FROM citas c
INNER JOIN usuarios u ON c.usuario_id = u.id
INNER JOIN tramites t ON c.tramite_id = t.id
WHERE c.estado IN ('AGENDADO', 'EN_PROCESO')
    AND c.fecha_cita >= CURRENT_DATE
ORDER BY c.fecha_cita, c.hora_cita;

-- Vista de trámites disponibles
CREATE OR REPLACE VIEW vista_tramites_disponibles AS
SELECT 
    id,
    codigo,
    nombre,
    descripcion,
    requisitos,
    precio,
    duracion_estimada,
    categoria,
    fecha_creacion
FROM tramites
WHERE activo = TRUE
ORDER BY categoria, nombre;

-- Vista de estadísticas por usuario
CREATE OR REPLACE VIEW vista_estadisticas_usuario AS
SELECT 
    u.id AS usuario_id,
    u.nombre || ' ' || u.apellido_paterno AS nombre_completo,
    u.correo,
    COUNT(CASE WHEN c.estado = 'AGENDADO' THEN 1 END) AS citas_agendadas,
    COUNT(CASE WHEN c.estado = 'FINALIZADO' THEN 1 END) AS citas_finalizadas,
    COUNT(CASE WHEN c.estado = 'CANCELADO' THEN 1 END) AS citas_canceladas,
    COUNT(*) AS total_citas,
    MAX(c.fecha_creacion) AS ultima_cita_creada
FROM usuarios u
LEFT JOIN citas c ON u.id = c.usuario_id
GROUP BY u.id, u.nombre, u.apellido_paterno, u.correo;

-- ===========================================
-- PASO 9: CREAR FUNCIONES DE UTILIDAD
-- ===========================================

-- Función para obtener citas de un usuario
CREATE OR REPLACE FUNCTION obtener_citas_usuario(p_usuario_id INTEGER)
RETURNS TABLE (
    cita_id INTEGER,
    tramite_nombre VARCHAR,
    fecha_cita DATE,
    hora_cita TIME,
    estado VARCHAR,
    precio NUMERIC
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        c.id,
        t.nombre,
        c.fecha_cita,
        c.hora_cita,
        c.estado,
        t.precio
    FROM citas c
    INNER JOIN tramites t ON c.tramite_id = t.id
    WHERE c.usuario_id = p_usuario_id
    ORDER BY c.fecha_cita DESC, c.hora_cita DESC;
END;
$$ LANGUAGE plpgsql;

-- Función para estadísticas del sistema
CREATE OR REPLACE FUNCTION estadisticas_sistema()
RETURNS TABLE (
    total_usuarios BIGINT,
    total_tramites BIGINT,
    total_citas BIGINT,
    citas_hoy BIGINT,
    citas_pendientes BIGINT,
    notificaciones_no_leidas BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        (SELECT COUNT(*) FROM usuarios WHERE estado = 'ACTIVO'),
        (SELECT COUNT(*) FROM tramites WHERE activo = TRUE),
        (SELECT COUNT(*) FROM citas),
        (SELECT COUNT(*) FROM citas WHERE fecha_cita = CURRENT_DATE),
        (SELECT COUNT(*) FROM citas WHERE estado = 'AGENDADO' AND fecha_cita >= CURRENT_DATE),
        (SELECT COUNT(*) FROM notificaciones WHERE leido = FALSE);
END;
$$ LANGUAGE plpgsql;

-- ===========================================
-- PASO 10: INSERTAR DATOS AMPLIADOS - SOLO TRAMITES
-- ===========================================

BEGIN;

TRUNCATE TABLE tramites RESTART IDENTITY CASCADE;

INSERT INTO tramites (codigo, nombre, descripcion, requisitos, precio, duracion_estimada, categoria) VALUES
-- PODERES
('POD-001', 'Poder General', 'Otorgamiento de poder general para realizar diversos actos jurídicos', 'DNI vigente del otorgante y apoderado, datos completos de ambas partes', 80.00, '30 minutos', 'Poderes'),
('POD-002', 'Poder Especial', 'Otorgamiento de poder para actos específicos', 'DNI vigente del otorgante y apoderado, especificación del acto a realizar', 60.00, '25 minutos', 'Poderes'),
('POD-003', 'Poder para Venta de Inmueble', 'Poder específico para venta de bienes inmuebles', 'DNI vigente, título de propiedad, datos del inmueble', 120.00, '45 minutos', 'Poderes'),
('POD-004', 'Poder para Trámites Bancarios', 'Poder para realizar operaciones bancarias', 'DNI vigente, datos de las cuentas bancarias', 70.00, '20 minutos', 'Poderes'),
('POD-005', 'Revocatoria de Poder', 'Anulación de poder previamente otorgado', 'DNI vigente, copia del poder a revocar', 50.00, '15 minutos', 'Poderes'),
('POD-006', 'Poder para Cobranza', 'Poder específico para cobro de deudas', 'DNI vigente, documentos que acrediten la deuda', 85.00, '30 minutos', 'Poderes'),
('POD-007', 'Poder para Representación Legal', 'Poder para representación en procesos judiciales', 'DNI vigente, datos del proceso legal', 110.00, '40 minutos', 'Poderes'),
('POD-008', 'Poder para Administración', 'Poder para administración de bienes', 'DNI vigente, inventario de bienes a administrar', 95.00, '35 minutos', 'Poderes'),
('POD-009', 'Poder para Trámites Tributarios', 'Poder para gestiones ante SUNAT', 'DNI vigente, RUC, datos tributarios', 75.00, '25 minutos', 'Poderes'),
('POD-010', 'Poder para Compra de Inmueble', 'Poder específico para adquisición de bienes inmuebles', 'DNI vigente, datos del inmueble a adquirir', 130.00, '45 minutos', 'Poderes'),
('POD-011', 'Poder para Trámites de Herencia', 'Poder para gestiones sucesorias', 'DNI vigente, partida de defunción, documentos hereditarios', 140.00, '50 minutos', 'Poderes'),
('POD-012', 'Poder para Negocios', 'Poder para operaciones comerciales', 'DNI vigente, RUC o documentos comerciales', 90.00, '30 minutos', 'Poderes'),
('POD-013', 'Poder para Trámites Municipales', 'Poder para gestiones ante municipalidades', 'DNI vigente, datos específicos del trámite municipal', 65.00, '20 minutos', 'Poderes'),
('POD-014', 'Poder para Vehículos', 'Poder para trámites vehiculares', 'DNI vigente, tarjeta de propiedad del vehículo', 80.00, '25 minutos', 'Poderes'),
('POD-015', 'Poder Irrevocable', 'Poder que no puede ser revocado', 'DNI vigente, justificación de irrevocabilidad', 150.00, '60 minutos', 'Poderes'),

-- ESCRITURAS
('ESC-001', 'Compraventa de Inmueble', 'Escritura pública de compraventa de bien inmueble', 'DNI de comprador y vendedor, título de propiedad, certificado de gravámenes, tasación', 350.00, '2 horas', 'Escrituras'),
('ESC-002', 'Donación de Inmueble', 'Escritura pública de donación de bien inmueble', 'DNI de donante y donatario, título de propiedad, certificado de gravámenes', 280.00, '90 minutos', 'Escrituras'),
('ESC-003', 'Hipoteca', 'Constitución de hipoteca sobre inmueble', 'DNI de las partes, título de propiedad, evaluación del inmueble', 300.00, '2 horas', 'Escrituras'),
('ESC-004', 'Cancelación de Hipoteca', 'Cancelación de gravamen hipotecario', 'DNI, constancia de pago total del préstamo, escritura de hipoteca', 180.00, '60 minutos', 'Escrituras'),
('ESC-005', 'Permuta de Inmuebles', 'Intercambio de bienes inmuebles', 'DNI de ambas partes, títulos de propiedad, tasaciones', 400.00, '2.5 horas', 'Escrituras'),
('ESC-006', 'Anticresis', 'Constitución de anticresis sobre inmueble', 'DNI de las partes, título de propiedad, condiciones del uso', 250.00, '90 minutos', 'Escrituras'),
('ESC-007', 'Usufructo', 'Constitución de derecho de usufructo', 'DNI de propietario y usufructuario, título de propiedad', 220.00, '75 minutos', 'Escrituras'),
('ESC-008', 'Servidumbre', 'Constitución de servidumbre predial', 'DNI de propietarios, planos, especificaciones técnicas', 200.00, '90 minutos', 'Escrituras'),
('ESC-009', 'Dación en Pago', 'Entrega de bien en pago de deuda', 'DNI de las partes, documentos de la deuda, tasación', 320.00, '2 horas', 'Escrituras'),
('ESC-010', 'Compraventa con Reserva', 'Venta con reserva de dominio', 'DNI de las partes, condiciones de la reserva, garantías', 380.00, '2.5 horas', 'Escrituras'),
('ESC-011', 'Superficie', 'Constitución de derecho de superficie', 'DNI de las partes, proyecto de construcción, planos', 290.00, '2 horas', 'Escrituras'),
('ESC-012', 'Rectificación de Área', 'Rectificación de medidas y linderos', 'DNI, título de propiedad, plano actualizado, informe técnico', 250.00, '90 minutos', 'Escrituras'),

-- EMPRESARIAL
('EMP-001', 'Constitución de SAC', 'Constitución de Sociedad Anónima Cerrada', 'DNI de socios, reserva de nombre, capital social, estatutos', 450.00, '3 horas', 'Empresarial'),
('EMP-002', 'Constitución de SRL', 'Constitución de Sociedad de Responsabilidad Limitada', 'DNI de socios, reserva de nombre, capital social, estatutos', 420.00, '2.5 horas', 'Empresarial'),
('EMP-003', 'Aumento de Capital', 'Aumento de capital social de empresa', 'DNI de socios, acuerdo de junta, balance auditado', 280.00, '90 minutos', 'Empresarial'),
('EMP-004', 'Reducción de Capital', 'Reducción del capital social', 'Acuerdo de junta, balance, publicación del acuerdo', 300.00, '2 horas', 'Empresarial'),
('EMP-005', 'Modificación de Estatutos', 'Modificación de estatutos sociales', 'DNI de representantes, acuerdo de junta, nuevos estatutos', 220.00, '75 minutos', 'Empresarial'),
('EMP-006', 'Nombramiento de Gerente', 'Nombramiento de gerente general', 'DNI del gerente, acuerdo de junta, carta de aceptación', 150.00, '45 minutos', 'Empresarial'),
('EMP-007', 'Renuncia de Gerente', 'Renuncia de gerente general', 'Carta de renuncia, acta de junta de aceptación', 120.00, '30 minutos', 'Empresarial'),
('EMP-008', 'Disolución de Empresa', 'Disolución y liquidación de empresa', 'Acuerdo de junta, balance final, declaraciones tributarias', 380.00, '2.5 horas', 'Empresarial'),
('EMP-009', 'Fusión de Empresas', 'Fusión de dos o más sociedades', 'Balances, acuerdos de juntas, proyecto de fusión', 800.00, '4 horas', 'Empresarial'),
('EMP-010', 'Escisión de Empresa', 'División de patrimonio empresarial', 'Balance, proyecto de escisión, acuerdos societarios', 750.00, '4 horas', 'Empresarial'),
('EMP-011', 'Transformación Societaria', 'Cambio de tipo de sociedad', 'Balance, acuerdo unánime, nuevos estatutos', 500.00, '3 horas', 'Empresarial'),
('EMP-012', 'Transferencia de Participaciones', 'Cesión de participaciones en SRL', 'DNI de cedente y cesionario, valorización', 200.00, '60 minutos', 'Empresarial'),
('EMP-013', 'Transferencia de Acciones', 'Cesión de acciones en SAC', 'DNI de las partes, certificados de acciones', 180.00, '60 minutos', 'Empresarial'),
('EMP-014', 'Constitución de Sucursal', 'Establecimiento de sucursal de empresa extranjera', 'Documentos legalizados del extranjero, capital asignado', 600.00, '3.5 horas', 'Empresarial'),
('EMP-015', 'Cierre de Sucursal', 'Cierre de sucursal de empresa', 'Acuerdo de cierre, balance de cierre, liquidación', 350.00, '2 horas', 'Empresarial'),
('EMP-016', 'Junta General Extraordinaria', 'Protocolización de acuerdo de junta', 'Acta de junta, lista de asistentes', 180.00, '45 minutos', 'Empresarial'),
('EMP-017', 'Otorgamiento de Garantías', 'Otorgamiento de garantías empresariales', 'Acuerdo de junta, documentos de la garantía', 250.00, '90 minutos', 'Empresarial'),
('EMP-018', 'Liquidación de Empresa', 'Proceso de liquidación societaria', 'Acuerdo de disolución, inventario, balance de liquidación', 450.00, '3 horas', 'Empresarial'),

-- CERTIFICACIONES
('CER-001', 'Certificación de Firmas', 'Certificación de autenticidad de firmas', 'DNI vigente, documento a certificar', 25.00, '10 minutos', 'Certificación'),
('CER-002', 'Legalización de Documentos', 'Legalización de documentos para uso en el extranjero', 'Documento original, DNI vigente', 35.00, '15 minutos', 'Certificación'),
('CER-003', 'Copia Certificada', 'Copia certificada de documento', 'Documento original, DNI vigente', 20.00, '10 minutos', 'Certificación'),
('CER-004', 'Certificación de Fecha Cierta', 'Certificación de fecha de documento', 'Documento original, DNI vigente', 30.00, '10 minutos', 'Certificación'),
('CER-005', 'Certificación de Apertura de Libros', 'Certificación de inicio de libros contables', 'Libros en blanco, DNI del representante legal', 40.00, '20 minutos', 'Certificación'),
('CER-006', 'Certificación de Cierre de Libros', 'Certificación de cierre de libros contables', 'Libros completos, balance final', 45.00, '25 minutos', 'Certificación'),
('CER-007', 'Certificación de Supervivencia', 'Certificación de que una persona vive', 'DNI vigente, presencia física del interesado', 30.00, '10 minutos', 'Certificación'),
('CER-008', 'Certificación de Contenido', 'Certificación del contenido de documento', 'Documento original completo', 35.00, '15 minutos', 'Certificación'),
('CER-009', 'Certificación de Traducción', 'Certificación de traducción oficial', 'Documento original, traducción oficial', 50.00, '20 minutos', 'Certificación'),
('CER-010', 'Certificación de Huella Digital', 'Certificación de huella dactilar', 'DNI vigente, presencia del interesado', 25.00, '10 minutos', 'Certificación'),
('CER-011', 'Certificación de Entrega', 'Certificación de entrega de documentos', 'Documentos a entregar, datos del destinatario', 40.00, '15 minutos', 'Certificación'),
('CER-012', 'Certificación de Destrucción', 'Certificación de destrucción de documentos', 'Documentos a destruir, solicitud escrita', 60.00, '30 minutos', 'Certificación'),

-- MATRIMONIAL
('MAT-001', 'Capitulaciones Matrimoniales', 'Régimen patrimonial del matrimonio', 'DNI de ambos cónyuges, certificado de matrimonio', 180.00, '60 minutos', 'Matrimonial'),
('MAT-002', 'Separación de Patrimonios', 'Separación de patrimonios matrimoniales', 'DNI de cónyuges, inventario de bienes, certificado de matrimonio', 220.00, '90 minutos', 'Matrimonial'),
('MAT-003', 'Divorcio por Mutuo Acuerdo', 'Escritura de divorcio por mutuo acuerdo', 'DNI de cónyuges, certificado de matrimonio, acuerdo de separación', 300.00, '2 horas', 'Matrimonial'),
('MAT-004', 'Liquidación de Sociedad Conyugal', 'División de bienes matrimoniales', 'Inventario de bienes, tasaciones, acuerdo de división', 350.00, '2.5 horas', 'Matrimonial'),
('MAT-005', 'Reconciliación Matrimonial', 'Protocolización de reconciliación', 'DNI de ambos cónyuges, declaración de reconciliación', 150.00, '45 minutos', 'Matrimonial'),
('MAT-006', 'Modificación de Capitulaciones', 'Cambio en régimen patrimonial', 'Capitulación original, nuevas condiciones acordadas', 200.00, '75 minutos', 'Matrimonial'),
('MAT-007', 'Donación entre Cónyuges', 'Donación de bienes entre esposos', 'DNI de ambos, título del bien, tasación', 180.00, '60 minutos', 'Matrimonial'),
('MAT-008', 'Adopción de Régimen', 'Adopción de régimen patrimonial específico', 'DNI de cónyuges, certificado de matrimonio', 160.00, '50 minutos', 'Matrimonial'),

-- SUCESIONES
('SUC-001', 'Declaratoria de Herederos', 'Declaración de herederos legales', 'Partida de defunción, DNI de herederos, partidas de nacimiento', 400.00, '2.5 horas', 'Sucesiones'),
('SUC-002', 'Partición de Herencia', 'División de bienes hereditarios', 'Declaratoria de herederos, inventario de bienes, tasaciones', 500.00, '3 horas', 'Sucesiones'),
('SUC-003', 'Testamento Cerrado', 'Otorgamiento de testamento cerrado', 'DNI vigente, testamento escrito y firmado', 200.00, '60 minutos', 'Sucesiones'),
('SUC-004', 'Testamento Abierto', 'Otorgamiento de testamento abierto', 'DNI vigente, relación de bienes, datos de beneficiarios', 180.00, '60 minutos', 'Sucesiones'),
('SUC-005', 'Apertura de Testamento', 'Apertura y protocolización de testamento', 'Testamento cerrado, partida de defunción', 250.00, '90 minutos', 'Sucesiones'),
('SUC-006', 'Renuncia de Herencia', 'Renuncia a derechos hereditarios', 'DNI del renunciante, declaratoria de herederos', 150.00, '45 minutos', 'Sucesiones'),
('SUC-007', 'Aceptación de Herencia', 'Aceptación expresa de herencia', 'DNI del heredero, inventario de bienes', 180.00, '60 minutos', 'Sucesiones'),
('SUC-008', 'Inventario de Bienes', 'Inventario detallado de masa hereditaria', 'Relación de bienes, tasaciones, documentos', 300.00, '2 horas', 'Sucesiones'),
('SUC-009', 'Administración de Herencia', 'Nombramiento de administrador', 'Acuerdo de herederos, perfil del administrador', 220.00, '75 minutos', 'Sucesiones'),
('SUC-010', 'Compraventa de Herencia', 'Venta de derechos hereditarios', 'DNI de las partes, declaratoria de herederos, tasación', 350.00, '2 horas', 'Sucesiones'),

-- LABORAL
('LAB-001', 'Contrato de Trabajo', 'Formalización de relación laboral', 'DNI de empleador y trabajador, condiciones laborales', 80.00, '30 minutos', 'Laboral'),
('LAB-002', 'Finiquito Laboral', 'Liquidación de beneficios sociales', 'Contrato de trabajo, boletas de pago, cálculo de beneficios', 100.00, '45 minutos', 'Laboral'),
('LAB-003', 'Convenio de Prácticas', 'Convenio de prácticas profesionales', 'DNI, certificado de estudios, plan de prácticas', 60.00, '25 minutos', 'Laboral'),
('LAB-004', 'Acuerdo de Confidencialidad', 'Acuerdo de no divulgación laboral', 'DNI de las partes, especificación de información confidencial', 70.00, '20 minutos', 'Laboral'),
('LAB-005', 'Contrato de Consultoría', 'Contrato de servicios profesionales', 'DNI/RUC, especificaciones del servicio, honorarios', 120.00, '45 minutos', 'Laboral'),
('LAB-006', 'Pacto de No Competencia', 'Acuerdo de no competencia post laboral', 'Contrato laboral, condiciones específicas, compensación', 150.00, '60 minutos', 'Laboral'),
('LAB-007', 'Transacción Laboral', 'Acuerdo transaccional laboral', 'Documentos del conflicto, propuesta de solución', 200.00, '90 minutos', 'Laboral'),
('LAB-008', 'Convenio Colectivo', 'Protocolización de convenio colectivo', 'Representantes sindicales, acuerdo firmado', 300.00, '2 horas', 'Laboral'),

-- INMOBILIARIO
('INM-001', 'Contrato de Arrendamiento', 'Formalización de contrato de alquiler', 'DNI de arrendador y arrendatario, datos del inmueble', 100.00, '40 minutos', 'Inmobiliario'),
('INM-002', 'Contrato de Subarrendamiento', 'Autorización de subarriendo', 'Contrato original, autorización del propietario, datos del subarrendatario', 120.00, '45 minutos', 'Inmobiliario'),
('INM-003', 'Resolución de Contrato', 'Terminación anticipada de contrato', 'Contrato original, causales de resolución, acuerdo de partes', 90.00, '35 minutos', 'Inmobiliario'),
('INM-004', 'Renovación de Contrato', 'Renovación de contrato de arrendamiento', 'Contrato original, nuevas condiciones, acuerdo de partes', 80.00, '30 minutos', 'Inmobiliario'),
('INM-005', 'Cesión de Arrendamiento', 'Cesión de derechos de arrendamiento', 'Contrato original, datos del cesionario, autorización', 110.00, '40 minutos', 'Inmobiliario'),
('INM-006', 'Depósito en Garantía', 'Constitución de depósito de garantía', 'Contrato de arrendamiento, monto del depósito', 60.00, '20 minutos', 'Inmobiliario'),
('INM-007', 'Inventario de Inmueble', 'Inventario detallado del inmueble', 'Acceso al inmueble, descripción detallada', 150.00, '60 minutos', 'Inmobiliario'),
('INM-008', 'Acta de Entrega', 'Acta de entrega de inmueble', 'Llaves, inventario, estado del inmueble', 70.00, '25 minutos', 'Inmobiliario'),
('INM-009', 'Prórroga de Contrato', 'Extensión de plazo contractual', 'Contrato original, acuerdo de prórroga', 75.00, '25 minutos', 'Inmobiliario'),
('INM-010', 'Modificación de Renta', 'Cambio en el monto de alquiler', 'Contrato original, justificación del aumento', 85.00, '30 minutos', 'Inmobiliario'),

-- FINANCIERO
('FIN-001', 'Préstamo Personal', 'Contrato de préstamo entre particulares', 'DNI de prestamista y deudor, garantías, cronograma de pagos', 100.00, '45 minutos', 'Financiero'),
('FIN-002', 'Préstamo Hipotecario', 'Préstamo con garantía hipotecaria', 'DNI de las partes, título de propiedad, evaluación', 250.00, '2 horas', 'Financiero'),
('FIN-003', 'Reconocimiento de Deuda', 'Formalización de deuda existente', 'DNI de deudor y acreedor, detalle de la deuda', 60.00, '25 minutos', 'Financiero'),
('FIN-004', 'Novación de Deuda', 'Modificación de condiciones de deuda', 'Contrato original, nuevas condiciones acordadas', 80.00, '35 minutos', 'Financiero'),
('FIN-005', 'Constitución de Prenda', 'Garantía prendaria sobre bienes muebles', 'DNI de las partes, descripción del bien, tasación', 120.00, '50 minutos', 'Financiero'),
('FIN-006', 'Cesión de Créditos', 'Transferencia de derechos crediticios', 'DNI del cedente y cesionario, documentos del crédito', 150.00, '60 minutos', 'Financiero'),
('FIN-007', 'Convenio de Pagos', 'Acuerdo de facilidades de pago', 'DNI de las partes, cronograma de pagos propuesto', 90.00, '40 minutos', 'Financiero'),
('FIN-008', 'Extinción de Obligación', 'Cancelación total de deuda', 'Documento de deuda, constancia de pago total', 70.00, '25 minutos', 'Financiero'),

-- COMERCIAL
('COM-001', 'Contrato de Compraventa', 'Contrato comercial de compraventa', 'DNI de las partes, descripción de mercancías, condiciones', 80.00, '35 minutos', 'Comercial'),
('COM-002', 'Contrato de Suministro', 'Acuerdo de provisión continua', 'DNI/RUC de las partes, especificaciones técnicas, cronograma', 120.00, '50 minutos', 'Comercial'),
('COM-003', 'Contrato de Distribución', 'Acuerdo de distribución comercial', 'RUC de las empresas, territorio, productos, exclusividad', 200.00, '90 minutos', 'Comercial'),
('COM-004', 'Contrato de Franquicia', 'Otorgamiento de franquicia comercial', 'RUC de franquiciante, manual de operaciones, territorio', 350.00, '2.5 horas', 'Comercial'),
('COM-005', 'Contrato de Representación', 'Representación comercial exclusiva', 'RUC de las partes, productos, territorio, comisiones', 180.00, '75 minutos', 'Comercial'),
('COM-006', 'Joint Venture', 'Acuerdo de empresa conjunta', 'RUC de las empresas, proyecto específico, aportes', 300.00, '2 horas', 'Comercial'),
('COM-007', 'Contrato de Maquila', 'Acuerdo de producción por encargo', 'RUC de las partes, especificaciones técnicas, calidad', 150.00, '60 minutos', 'Comercial'),
('COM-008', 'Licencia de Marca', 'Otorgamiento de licencia de uso de marca', 'Registro de marca, condiciones de uso, regalías', 250.00, '90 minutos', 'Comercial'),
('COM-009', 'Contrato de Agencia', 'Nombramiento de agente comercial', 'RUC del principal y agente, territorio, productos', 160.00, '70 minutos', 'Comercial'),
('COM-010', 'Terminación de Contrato', 'Terminación amigable de contrato comercial', 'Contrato original, acuerdo de terminación, liquidación', 100.00, '45 minutos', 'Comercial'),

-- OTROS
('OTR-001', 'Reconocimiento de Hijo', 'Reconocimiento de paternidad o maternidad', 'DNI de reconociente, partida de nacimiento del menor', 120.00, '45 minutos', 'Otros'),
('OTR-002', 'Autorización de Viaje de Menor', 'Autorización notarial para viaje de menor', 'DNI de padres y menor, datos del viaje, acompañante', 80.00, '30 minutos', 'Otros'),
('OTR-003', 'Rectificación de Partida', 'Rectificación de datos en partida registral', 'Partida a rectificar, documentos sustentatorios, DNI', 150.00, '60 minutos', 'Otros'),
('OTR-004', 'Autorización para Menor', 'Autorización general para menor de edad', 'DNI de padres y menor, especificación de actos autorizados', 60.00, '20 minutos', 'Otros'),
('OTR-005', 'Declaración Jurada', 'Protocolización de declaración jurada', 'DNI del declarante, contenido de la declaración', 40.00, '15 minutos', 'Otros'),
('OTR-006', 'Protesta de Documentos', 'Protesta notarial de documentos', 'Documentos a protestar, datos del deudor', 80.00, '30 minutos', 'Otros'),
('OTR-007', 'Requerimiento Notarial', 'Requerimiento formal extrajudicial', 'DNI del requirente, contenido del requerimiento', 100.00, '40 minutos', 'Otros'),
('OTR-008', 'Constatación de Hechos', 'Verificación notarial de hechos', 'Solicitud específica, lugar de constatación', 120.00, '60 minutos', 'Otros'),
('OTR-009', 'Entrega de Bien', 'Protocolización de entrega de bien', 'Descripción del bien, datos del receptor', 90.00, '35 minutos', 'Otros'),
('OTR-010', 'Sorteo Notarial', 'Realización de sorteo con fe notarial', 'Lista de participantes, premios, metodología', 150.00, '45 minutos', 'Otros'),
('OTR-011', 'Levantamiento de Cadáver', 'Acta de levantamiento de cadáver', 'Solicitud de autoridad, presencia en el lugar', 200.00, '90 minutos', 'Otros'),
('OTR-012', 'Información Testimonial', 'Información testimonial notarial', 'DNI de testigos, materia de testimonio', 100.00, '45 minutos', 'Otros'),
('OTR-013', 'Constitución de Fundación', 'Constitución de entidad sin fines de lucro', 'DNI de fundadores, estatutos, patrimonio inicial', 300.00, '2 horas', 'Otros'),
('OTR-014', 'Disolución de Fundación', 'Disolución de fundación', 'Acuerdo de disolución, destino del patrimonio', 250.00, '90 minutos', 'Otros'),
('OTR-015', 'Constitución de Asociación', 'Constitución de asociación civil', 'DNI de fundadores, estatutos, acta constitutiva', 250.00, '90 minutos', 'Otros');

COMMIT;

-- ===========================================
-- VERIFICACIÓN FINAL
-- ===========================================
DO $$
DECLARE
    v_tramites_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO v_tramites_count FROM tramites;
    RAISE NOTICE 'DATOS INSERTADOS CORRECTAMENTE:';
    RAISE NOTICE '- Trámites: % registros', v_tramites_count;
    RAISE NOTICE 'Base de datos TRAMINOTAR lista para usar.';
END $$;
-- =========================================================================================================
-- ESTO DE ABAJO YA NO ES NECESARIO SOLO ES PARA CONSULTAR CUANDO YA FUNCIONE EL PROGRAM Y CAMBIOS A LA BD
-- =========================================================================================================
SELECT * FROM usuarios;
SELECT * FROM tramites;
SELECT * FROM citas ORDER BY id DESC LIMIT 5;

INSERT INTO citas (usuario_id, tramite_id, fecha_cita, hora_cita, estado)
VALUES (1, 1, '2025-11-02', '10:00', 'AGENDADO');
