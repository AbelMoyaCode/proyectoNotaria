-- ============================================
-- SCRIPT PARA ACTUALIZAR TABLA CITAS
-- Agrega soporte para horarios_disponibles
-- ============================================

-- PASO 1: Eliminar constraint de fecha futura si existe
ALTER TABLE citas DROP CONSTRAINT IF EXISTS chk_fecha_futura;
ALTER TABLE citas DROP CONSTRAINT IF EXISTS unique_usuario_fecha;

-- PASO 2: Agregar columna horario_id si no existe
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'citas' AND column_name = 'horario_id'
    ) THEN
        ALTER TABLE citas ADD COLUMN horario_id INTEGER;
        ALTER TABLE citas ADD CONSTRAINT fk_citas_horario
            FOREIGN KEY (horario_id) REFERENCES horarios_disponibles(id) ON DELETE CASCADE;
        RAISE NOTICE 'Columna horario_id agregada exitosamente';
    ELSE
        RAISE NOTICE 'La columna horario_id ya existe';
    END IF;
END $$;

-- PASO 3: Agregar columnas adicionales necesarias
ALTER TABLE citas ADD COLUMN IF NOT EXISTS tramite_usuario_id INTEGER;
ALTER TABLE citas ADD COLUMN IF NOT EXISTS creada_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE citas ADD COLUMN IF NOT EXISTS reprogramada_en TIMESTAMP;

-- PASO 4: Crear constraint único para horario (un horario solo puede tener una cita)
CREATE UNIQUE INDEX IF NOT EXISTS idx_citas_horario_unico
    ON citas(horario_id)
    WHERE estado IN ('AGENDADO', 'EN_PROCESO');

-- PASO 5: Verificar estructura
SELECT
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_name = 'citas'
ORDER BY ordinal_position;

-- ============================================
-- VERIFICACIÓN
-- ============================================
DO $$
BEGIN
    RAISE NOTICE '✅ Actualización completada';
    RAISE NOTICE 'La tabla citas ahora tiene soporte para horarios_disponibles';
    RAISE NOTICE 'Columnas agregadas: horario_id, tramite_usuario_id, creada_en, reprogramada_en';
END $$;

