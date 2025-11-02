-- ========================================
-- SOLUCIÓN AL PROBLEMA DE CITAS DUPLICADAS
-- ========================================
-- PROBLEMA: El constraint 'idx_usuario_fecha_hora' impide crear citas
-- con la misma fecha/hora/usuario, INCLUSO si la anterior fue CANCELADA.
--
-- SOLUCIÓN: Eliminar el constraint y permitir que el sistema valide
-- solo citas ACTIVAS (estado = 'AGENDADO' o 'EN_PROCESO')
-- ========================================

-- 1. Eliminar el constraint problemático
DROP INDEX IF EXISTS idx_usuario_fecha_hora;

COMMIT;

-- ========================================
-- EXPLICACIÓN:
-- ========================================
-- Antes: No podías agendar una cita si ya habías agendado (y cancelado)
--        una cita con la misma fecha/hora
--
-- Ahora: El sistema valida en el código del servidor (citas.js) que solo
--        verifica citas ACTIVAS, ignorando las canceladas
--
-- Esto permite:
-- ✅ Cancelar una cita y volver a agendar en el mismo horario
-- ✅ Eliminar citas canceladas sin problemas
-- ✅ Reutilizar horarios liberados
-- ========================================

