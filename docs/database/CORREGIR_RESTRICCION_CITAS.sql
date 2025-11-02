-- ============================================
-- CORREGIR RESTRICCIÓN DE CITAS
-- Permite múltiples citas por usuario en diferentes horarios del mismo día
-- Fecha: 2025-11-02
-- ============================================

-- PASO 1: Eliminar restricción incorrecta que impide múltiples citas por día
DO $$
BEGIN
    -- Eliminar constraint unique_usuario_fecha si existe
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'unique_usuario_fecha'
        AND table_name = 'citas'
    ) THEN
        ALTER TABLE citas DROP CONSTRAINT unique_usuario_fecha;
        RAISE NOTICE '✅ Restricción unique_usuario_fecha eliminada';
    ELSE
        RAISE NOTICE 'ℹ️  La restricción unique_usuario_fecha no existe';
    END IF;

    -- Eliminar índice si existe
    IF EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE indexname = 'unique_usuario_fecha'
    ) THEN
        DROP INDEX unique_usuario_fecha;
        RAISE NOTICE '✅ Índice unique_usuario_fecha eliminado';
    END IF;
END $$;

-- PASO 2: Crear restricción correcta (un usuario NO puede tener dos citas en el MISMO HORARIO)
-- Esto permite múltiples citas por día, pero en horarios diferentes
DO $$
BEGIN
    -- Crear índice único para evitar que un usuario reserve el mismo horario dos veces
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE indexname = 'idx_usuario_horario_unico'
    ) THEN
        CREATE UNIQUE INDEX idx_usuario_horario_unico
            ON citas(usuario_id, horario_id)
            WHERE estado IN ('AGENDADO', 'EN_PROCESO');
        RAISE NOTICE '✅ Índice idx_usuario_horario_unico creado';
    ELSE
        RAISE NOTICE 'ℹ️  El índice idx_usuario_horario_unico ya existe';
    END IF;
END $$;

-- VERIFICAR QUE LOS CAMBIOS SE APLICARON CORRECTAMENTE
SELECT
    'Restricciones actuales en tabla citas:' as mensaje,
    constraint_name,
    constraint_type
FROM information_schema.table_constraints
WHERE table_name = 'citas'
ORDER BY constraint_type, constraint_name;

-- ============================================
-- RESULTADO ESPERADO
-- ============================================
-- ✅ Un usuario PUEDE agendar múltiples citas en el mismo día (diferentes horarios)
-- ✅ Un usuario NO PUEDE agendar dos veces el mismo horario
-- ✅ Un horario solo puede ser reservado una vez (por la restricción en horarios_disponibles)
-- ============================================
