-- ================================================================
-- SCRIPT DE ACTUALIZACIÓN PARA BASE DE DATOS TRAMINOTAR
-- Ejecuta este script completo en pgAdmin para actualizar tu BD
-- ================================================================

-- Conectar a la base de datos traminotar
\c traminotar;

-- PASO 1: Crear tabla tramites_usuarios (si no existe)
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

-- PASO 2: Eliminar constraints problemáticas de la tabla citas
ALTER TABLE citas DROP CONSTRAINT IF EXISTS chk_fecha_futura;
ALTER TABLE citas DROP CONSTRAINT IF EXISTS unique_usuario_fecha;

-- PASO 3: Agregar columnas necesarias a la tabla citas
ALTER TABLE citas ADD COLUMN IF NOT EXISTS horario_id INTEGER;
ALTER TABLE citas ADD COLUMN IF NOT EXISTS tramite_usuario_id INTEGER;
ALTER TABLE citas ADD COLUMN IF NOT EXISTS creada_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE citas ADD COLUMN IF NOT EXISTS reprogramada_en TIMESTAMP;

-- PASO 4: Crear Foreign Keys
DO $$
BEGIN
    -- FK para horario_id
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_citas_horario'
    ) THEN
        ALTER TABLE citas ADD CONSTRAINT fk_citas_horario
            FOREIGN KEY (horario_id) REFERENCES horarios_disponibles(id) ON DELETE CASCADE;
    END IF;

    -- FK para tramite_usuario_id
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_citas_tramite_usuario'
    ) THEN
        ALTER TABLE citas ADD CONSTRAINT fk_citas_tramite_usuario
            FOREIGN KEY (tramite_usuario_id) REFERENCES tramites_usuarios(id) ON DELETE CASCADE;
    END IF;
END $$;

-- PASO 5: Crear índices para optimización
CREATE INDEX IF NOT EXISTS idx_citas_horario ON citas(horario_id);
CREATE INDEX IF NOT EXISTS idx_citas_tramite_usuario ON citas(tramite_usuario_id);
CREATE INDEX IF NOT EXISTS idx_tramites_usuarios_usuario ON tramites_usuarios(usuario_id);
CREATE INDEX IF NOT EXISTS idx_tramites_usuarios_tramite ON tramites_usuarios(tramite_codigo);

-- VERIFICACIÓN FINAL
DO $$
BEGIN
    RAISE NOTICE '========================================';
    RAISE NOTICE '✅ ACTUALIZACIÓN COMPLETADA EXITOSAMENTE';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Cambios aplicados:';
    RAISE NOTICE '- Tabla tramites_usuarios creada';
    RAISE NOTICE '- Columna horario_id agregada a citas';
    RAISE NOTICE '- Columna tramite_usuario_id agregada a citas';
    RAISE NOTICE '- Foreign Keys configuradas';
    RAISE NOTICE '- Índices creados';
    RAISE NOTICE '========================================';
END $$;

-- Mostrar estructura actualizada de la tabla citas
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'citas'
ORDER BY ordinal_position;

