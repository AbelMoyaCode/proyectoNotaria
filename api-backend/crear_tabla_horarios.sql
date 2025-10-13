-- ============================================
-- AGREGAR TABLA HORARIOS_DISPONIBLES
-- Ejecutar este script en pgAdmin en la base de datos traminotar
-- ============================================

-- Crear tabla horarios_disponibles si no existe
CREATE TABLE IF NOT EXISTS horarios_disponibles (
    id SERIAL PRIMARY KEY,
    fecha DATE NOT NULL,
    hora TIME NOT NULL,
    capacidad INTEGER NOT NULL DEFAULT 1 CHECK (capacidad >= 1),
    disponible BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT unique_fecha_hora UNIQUE (fecha, hora)
);

-- Comentarios
COMMENT ON TABLE horarios_disponibles IS 'Horarios disponibles para agendar citas';
COMMENT ON COLUMN horarios_disponibles.fecha IS 'Fecha del horario disponible';
COMMENT ON COLUMN horarios_disponibles.hora IS 'Hora del horario (formato 24h)';
COMMENT ON COLUMN horarios_disponibles.capacidad IS 'Número máximo de citas para este horario';
COMMENT ON COLUMN horarios_disponibles.disponible IS 'Si el horario está disponible para reservar';

-- Índices
CREATE INDEX IF NOT EXISTS idx_horarios_fecha ON horarios_disponibles(fecha);
CREATE INDEX IF NOT EXISTS idx_horarios_disponible ON horarios_disponibles(disponible);
CREATE INDEX IF NOT EXISTS idx_horarios_fecha_hora ON horarios_disponibles(fecha, hora);

-- Insertar algunos horarios de ejemplo (opcional)
-- Horarios para los próximos 7 días, de 8:00 AM a 6:00 PM
DO $$
DECLARE
    fecha_inicio DATE := CURRENT_DATE + 1;
    dia INT;
    hora_str TEXT;
BEGIN
    FOR dia IN 0..6 LOOP
        FOREACH hora_str IN ARRAY ARRAY['08:00', '08:30', '09:00', '09:30', '10:00', '10:30',
                                         '11:00', '11:30', '12:00', '12:30', '13:00', '13:30',
                                         '14:00', '14:30', '15:00', '15:30', '16:00', '16:30',
                                         '17:00', '17:30', '18:00'] LOOP
            INSERT INTO horarios_disponibles (fecha, hora, capacidad, disponible)
            VALUES (fecha_inicio + dia, hora_str::TIME, 1, TRUE)
            ON CONFLICT (fecha, hora) DO NOTHING;
        END LOOP;
    END LOOP;
END $$;

-- Verificar que se crearon los horarios
SELECT COUNT(*) as total_horarios FROM horarios_disponibles;

NOTICE 'Tabla horarios_disponibles creada exitosamente';

