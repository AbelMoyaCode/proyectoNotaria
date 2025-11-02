-- ============================================
-- ACTUALIZACIÓN CRÍTICA PARA TABLA CITAS
-- Ejecuta SOLO esta parte en tu base de datos traminotar
-- ============================================

-- PASO 1: Eliminar constraints que causan problemas
ALTER TABLE citas DROP CONSTRAINT IF EXISTS chk_fecha_futura;
ALTER TABLE citas DROP CONSTRAINT IF EXISTS unique_usuario_fecha;

-- PASO 2: Agregar columnas necesarias para el sistema de horarios
ALTER TABLE citas ADD COLUMN IF NOT EXISTS horario_id INTEGER;
ALTER TABLE citas ADD COLUMN IF NOT EXISTS tramite_usuario_id INTEGER;
ALTER TABLE citas ADD COLUMN IF NOT EXISTS creada_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE citas ADD COLUMN IF NOT EXISTS reprogramada_en TIMESTAMP;
ALTER TABLE citas ADD COLUMN IF NOT EXISTS motivo_cancelacion TEXT;

-- PASO 3: Crear relaciones con otras tablas
ALTER TABLE citas DROP CONSTRAINT IF EXISTS fk_citas_horario;
ALTER TABLE citas ADD CONSTRAINT fk_citas_horario
    FOREIGN KEY (horario_id) REFERENCES horarios_disponibles(id) ON DELETE CASCADE;

-- PASO 4: Crear tabla tramites_usuarios si no existe
CREATE TABLE IF NOT EXISTS tramites_usuarios (
    id SERIAL PRIMARY KEY,
    usuario_id INTEGER NOT NULL,
    tramite_codigo VARCHAR(20) NOT NULL,
    estado_general VARCHAR(20) DEFAULT 'AGENDADO',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (tramite_codigo) REFERENCES tramites(codigo) ON DELETE CASCADE,

    CONSTRAINT chk_estado_tramite_usuario CHECK (estado_general IN ('AGENDADO', 'EN_PROCESO', 'FINALIZADO', 'CANCELADO'))
);

-- PASO 5: Crear relación con tramites_usuarios
ALTER TABLE citas DROP CONSTRAINT IF EXISTS fk_citas_tramite_usuario;
ALTER TABLE citas ADD CONSTRAINT fk_citas_tramite_usuario
    FOREIGN KEY (tramite_usuario_id) REFERENCES tramites_usuarios(id) ON DELETE CASCADE;

-- PASO 6: Crear índices para optimización
CREATE INDEX IF NOT EXISTS idx_citas_horario ON citas(horario_id);
CREATE INDEX IF NOT EXISTS idx_citas_tramite_usuario ON citas(tramite_usuario_id);
CREATE INDEX IF NOT EXISTS idx_tramites_usuarios_usuario ON tramites_usuarios(usuario_id);
CREATE INDEX IF NOT EXISTS idx_tramites_usuarios_tramite ON tramites_usuarios(tramite_codigo);

-- VERIFICACIÓN
SELECT 'Columnas de la tabla citas:' as mensaje;
SELECT column_name, data_type
FROM information_schema.columns
WHERE table_name = 'citas'
ORDER BY ordinal_position;

SELECT 'Actualización completada exitosamente ✅' as resultado;

