-- ============================================
-- SCRIPT DE CONFIGURACIÓN DE BASE DE DATOS
-- Proyecto: Aplicativo para Notaría
-- Base de datos: notariaBD (PostgreSQL)
-- ============================================

-- ============================================
-- 1. TABLA DE USUARIOS
-- ============================================
CREATE TABLE IF NOT EXISTS usuarios (
    id SERIAL PRIMARY KEY,
    nro_documento VARCHAR(20) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellido_paterno VARCHAR(100) NOT NULL,
    apellido_materno VARCHAR(100) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    correo VARCHAR(150) UNIQUE NOT NULL,
    direccion VARCHAR(250),
    contrasena VARCHAR(255) NOT NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(20) DEFAULT 'ACTIVO'
);

-- ============================================
-- 2. TABLA DE TRÁMITES
-- ============================================
CREATE TABLE IF NOT EXISTS tramites (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT NOT NULL,
    requisitos TEXT NOT NULL,
    precio DECIMAL(10, 2) NOT NULL,
    duracion_estimada VARCHAR(50),
    categoria VARCHAR(100),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE
);

-- ============================================
-- 3. TABLA DE CITAS
-- ============================================
CREATE TABLE IF NOT EXISTS citas (
    id SERIAL PRIMARY KEY,
    usuario_id INTEGER NOT NULL,
    tramite_id INTEGER NOT NULL,
    fecha_cita DATE NOT NULL,
    hora_cita TIME NOT NULL,
    estado VARCHAR(20) DEFAULT 'AGENDADO',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (tramite_id) REFERENCES tramites(id) ON DELETE CASCADE,
    CONSTRAINT unique_usuario_fecha UNIQUE (usuario_id, fecha_cita)
);

-- ============================================
-- 4. TABLA DE NOTIFICACIONES
-- ============================================
CREATE TABLE IF NOT EXISTS notificaciones (
    id SERIAL PRIMARY KEY,
    usuario_id INTEGER NOT NULL,
    cita_id INTEGER,
    tipo VARCHAR(50) NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    mensaje TEXT NOT NULL,
    leido BOOLEAN DEFAULT FALSE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (cita_id) REFERENCES citas(id) ON DELETE CASCADE
);

-- ============================================
-- 5. INSERTAR DATOS DE EJEMPLO - TRÁMITES
-- ============================================
INSERT INTO tramites (nombre, descripcion, requisitos, precio, duracion_estimada, categoria) VALUES
('Poder Simple',
 'Otorgamiento de poder para realizar trámites específicos en representación de otra persona.',
 'DNI vigente del otorgante,Datos completos del apoderado,Descripción clara de las facultades otorgadas',
 50.00, '1 día', 'Poderes'),

('Poder Amplio y General',
 'Poder con amplias facultades para representación legal en diversos actos jurídicos.',
 'DNI vigente del otorgante,Datos completos del apoderado,Lista detallada de facultades,Dos testigos con DNI',
 80.00, '1 día', 'Poderes'),

('Compraventa de Inmueble',
 'Formalización legal de la transferencia de propiedad de un bien inmueble.',
 'DNI vigente de ambas partes,Partida registral actualizada,Certificado de búsqueda catastral,Comprobante de pago de impuestos,Certificado de gravámenes',
 250.00, '3-5 días', 'Escrituras'),

('Donación',
 'Acto de liberalidad mediante el cual una persona transfiere gratuitamente un bien a otra.',
 'DNI vigente del donante,DNI vigente del donatario,Partida de nacimiento (si es familiar),Documento de propiedad del bien',
 150.00, '2-3 días', 'Escrituras'),

('Constitución de Empresa',
 'Formalización de la constitución de una persona jurídica (SAC, SRL, SA).',
 'DNI de todos los socios,Reserva de nombre en SUNARP,Estatutos de la empresa,Capital social mínimo,Minuta de constitución',
 300.00, '5-7 días', 'Empresarial'),

('Aumento de Capital',
 'Incremento del capital social de una empresa ya constituida.',
 'Vigencia de poder del representante legal,Acuerdo de junta de socios,Estados financieros actualizados,RUC de la empresa',
 200.00, '3-5 días', 'Empresarial'),

('Declaratoria de Herederos',
 'Reconocimiento legal de los herederos de una persona fallecida.',
 'Partida de defunción original,Partidas de nacimiento de herederos,DNI vigente de todos los herederos,Testamento (si existe),Partidas de matrimonio (si aplica)',
 400.00, '7-10 días', 'Sucesiones'),

('Testamento',
 'Documento legal mediante el cual una persona dispone de sus bienes para después de su muerte.',
 'DNI vigente del testador,Lista de bienes y propiedades,Datos de los beneficiarios,Dos testigos con DNI',
 180.00, '2-3 días', 'Sucesiones'),

('Testimonio de Escritura Pública',
 'Copia certificada y legalizada de una escritura pública registrada.',
 'Solicitud escrita,Número de partida registral,Pago de derechos registrales',
 30.00, '1-2 días', 'Certificaciones'),

('Legalización de Firma',
 'Certificación de la autenticidad de una firma en un documento.',
 'DNI vigente,Documento original a legalizar,Presencia del firmante',
 20.00, '30 minutos', 'Certificaciones'),

('Legalización de Contrato',
 'Certificación notarial de un contrato privado entre partes.',
 'DNI de todas las partes,Contrato impreso (3 copias),Presencia de todos los firmantes',
 60.00, '1 día', 'Certificaciones'),

('Divorcio por Mutuo Acuerdo',
 'Disolución del vínculo matrimonial por acuerdo de ambas partes sin hijos menores.',
 'Partida de matrimonio,DNI de ambos cónyuges,Acuerdo de separación de bienes,Declaración jurada de no tener hijos menores',
 350.00, '15-20 días', 'Familia'),

('Reconocimiento de Unión de Hecho',
 'Reconocimiento legal de convivencia como pareja sin vínculo matrimonial.',
 'DNI de ambos convivientes,Declaración jurada de convivencia (mínimo 2 años),Testigos que acrediten la convivencia,Certificado de soltería',
 120.00, '5-7 días', 'Familia');

-- ============================================
-- 6. INSERTAR USUARIO DE PRUEBA
-- ============================================
-- Contraseña: 12345678
INSERT INTO usuarios (nro_documento, nombre, apellido_paterno, apellido_materno, fecha_nacimiento, correo, direccion, contrasena)
VALUES ('74223311', 'Abel', 'Moya', 'Acosta', '2000-05-15', 'abel@correo.com', 'Av. Ejemplo 123', '12345678')
ON CONFLICT (nro_documento) DO NOTHING;

-- ============================================
-- 7. VERIFICAR DATOS
-- ============================================
SELECT 'Usuarios registrados:' as info, COUNT(*) as total FROM usuarios
UNION ALL
SELECT 'Trámites disponibles:', COUNT(*) FROM tramites
UNION ALL
SELECT 'Citas agendadas:', COUNT(*) FROM citas
UNION ALL
SELECT 'Notificaciones:', COUNT(*) FROM notificaciones;

-- ============================================
-- 8. ÍNDICES PARA OPTIMIZACIÓN
-- ============================================
CREATE INDEX IF NOT EXISTS idx_usuarios_correo ON usuarios(correo);
CREATE INDEX IF NOT EXISTS idx_usuarios_documento ON usuarios(nro_documento);
CREATE INDEX IF NOT EXISTS idx_citas_usuario ON citas(usuario_id);
CREATE INDEX IF NOT EXISTS idx_citas_fecha ON citas(fecha_cita);
CREATE INDEX IF NOT EXISTS idx_tramites_categoria ON tramites(categoria);

-- ============================================
-- FIN DEL SCRIPT
-- ============================================
