const express = require('express');
const router = express.Router();
const { query } = require('../config/database');
const { verificarToken } = require('../middleware/auth');

/**
 * POST /api/citas
 * Crear/Reservar una nueva cita
 *
 * VALIDACIONES IMPLEMENTADAS:
 * 1. Validar disponibilidad de horarios
 * 2. Verificar que el usuario NO tenga ya una cita en el MISMO HORARIO
 * 3. Crear horarios automáticamente si no existen
 *
 * PRUEBAS DE RESERVA:
 * - Integridad de datos (fecha, hora, usuario, trámite)
 * - Transacciones atómicas (COMMIT/ROLLBACK)
 * - Validación de conflictos
 */
router.post('/', async (req, res) => {
  const client = await require('../config/database').pool.connect();

  try {
    const { usuario_id, tramite_codigo, fecha, hora } = req.body;

    console.log('🔍 VALIDACIÓN: Iniciando creación de cita...');
    console.log('   Usuario:', usuario_id);
    console.log('   Trámite:', tramite_codigo);
    console.log('   Fecha:', fecha);
    console.log('   Hora:', hora);

    if (!usuario_id || !tramite_codigo || !fecha || !hora) {
      console.log('❌ VALIDACIÓN FALLIDA: Faltan campos obligatorios');
      return res.status(400).json({
        success: false,
        mensaje: 'Por favor, complete todos los datos: fecha, hora y trámite'
      });
    }

    // VALIDACIÓN: Permitir agendar para HOY o FUTURO (no fechas pasadas)
    const fechaCita = new Date(fecha);
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);

    if (fechaCita < hoy) {
      console.log('❌ VALIDACIÓN FALLIDA: Fecha pasada');
      return res.status(400).json({
        success: false,
        mensaje: 'No se pueden agendar citas en fechas pasadas'
      });
    }

    await client.query('BEGIN');
    console.log('🔄 Transacción iniciada');

    // Buscar o crear horario disponible automáticamente
    console.log('🔎 Buscando horario disponible...');
    let horario = await client.query(
      'SELECT id, disponible FROM horarios_disponibles WHERE fecha = $1 AND hora = $2',
      [fecha, hora]
    );

    let horarioId;

    if (horario.rows.length === 0) {
      console.log('📝 Horario no existe, creando automáticamente...');
      const nuevoHorario = await client.query(
        `INSERT INTO horarios_disponibles (fecha, hora, disponible)
         VALUES ($1, $2, TRUE)
         RETURNING id`,
        [fecha, hora]
      );
      horarioId = nuevoHorario.rows[0].id;
      console.log('✅ Horario creado con ID:', horarioId);
    } else {
      horarioId = horario.rows[0].id;
      console.log('✅ Horario encontrado con ID:', horarioId);
    }

    // VERIFICACIÓN PRINCIPAL: Verificar si el usuario ya tiene una cita ACTIVA en este horario
    console.log('🔎 Verificando si el usuario ya tiene cita activa en este horario específico...');
    const citaActivaUsuario = await client.query(
      `SELECT c.id, c.estado FROM citas c
       WHERE c.usuario_id = $1
       AND c.horario_id = $2
       AND c.estado IN ('AGENDADO', 'EN_PROCESO')`,
      [usuario_id, horarioId]
    );

    if (citaActivaUsuario.rows.length > 0) {
      await client.query('ROLLBACK');
      console.log('❌ VALIDACIÓN FALLIDA: Usuario ya tiene cita activa para este horario');
      return res.status(400).json({
        success: false,
        mensaje: 'Ya tiene una cita agendada para este horario. Seleccione un horario diferente.'
      });
    }

    // Verificar si el horario está ocupado por OTRO usuario
    console.log('🔎 Verificando si el horario está ocupado por otro usuario...');
    const citasActivas = await client.query(
      `SELECT c.id FROM citas c
       WHERE c.horario_id = $1
       AND c.estado IN ('AGENDADO', 'EN_PROCESO')`,
      [horarioId]
    );

    if (citasActivas.rows.length > 0) {
      await client.query('ROLLBACK');
      console.log('❌ VALIDACIÓN FALLIDA: Horario ocupado por otro usuario');
      return res.status(400).json({
        success: false,
        mensaje: 'Este horario ya está ocupado. Por favor, seleccione otro horario disponible.'
      });
    }

    console.log('✅ Horario disponible para agendar');

    // Crear tramite_usuario
    console.log('💾 Creando tramite_usuario...');
    const tramiteUsuario = await client.query(
      `INSERT INTO tramites_usuarios (usuario_id, tramite_codigo, estado_general)
       VALUES ($1, $2, 'AGENDADO')
       RETURNING id`,
      [usuario_id, tramite_codigo]
    );

    const tramiteUsuarioId = tramiteUsuario.rows[0].id;
    console.log('✅ Tramite_usuario creado con ID:', tramiteUsuarioId);

    // Crear cita
    console.log('💾 Creando cita...');
    const cita = await client.query(
      `INSERT INTO citas (usuario_id, tramite_id, fecha_cita, hora_cita, tramite_usuario_id, horario_id, estado)
       VALUES ($1, (SELECT id FROM tramites WHERE codigo = $2), $3, $4, $5, $6, 'AGENDADO')
       RETURNING *`,
      [usuario_id, tramite_codigo, fecha, hora, tramiteUsuarioId, horarioId]
    );
    console.log('✅ Cita creada con ID:', cita.rows[0].id);

    // Marcar horario como NO disponible
    console.log('🔒 Marcando horario como no disponible...');
    await client.query(
      'UPDATE horarios_disponibles SET disponible = FALSE WHERE id = $1',
      [horarioId]
    );
    console.log('✅ Horario bloqueado');

    // Obtener datos completos de la cita
    const citaCompleta = await client.query(
      `SELECT c.id, c.estado, hd.fecha, hd.hora, t.nombre as tramite_nombre, t.descripcion as tramite_descripcion
       FROM citas c
       JOIN horarios_disponibles hd ON hd.id = c.horario_id
       JOIN tramites_usuarios tu ON tu.id = c.tramite_usuario_id
       JOIN tramites t ON t.codigo = tu.tramite_codigo
       WHERE c.id = $1`,
      [cita.rows[0].id]
    );

    await client.query('COMMIT');
    console.log('✅ TRANSACCIÓN COMPLETADA EXITOSAMENTE');

    res.status(201).json({
      success: true,
      mensaje: 'Cita creada exitosamente',
      data: citaCompleta.rows[0]
    });

  } catch (error) {
    await client.query('ROLLBACK');
    console.error('❌ ERROR al crear cita:', error);

    res.status(400).json({
      success: false,
      mensaje: 'Error al crear la cita',
      error: error.message
    });
  } finally {
    client.release();
  }
});

/**
 * GET /api/citas/usuario/:usuarioId
 * Obtener todas las citas de un usuario (INCLUYENDO canceladas)
 * Las citas canceladas aparecerán en "Pasadas" para que el usuario las elimine manualmente
 */
router.get('/usuario/:usuarioId', async (req, res) => {
  try {
    const { usuarioId } = req.params;

    const resultado = await query(
      `SELECT
        c.id,
        c.tramite_usuario_id,
        c.estado,
        hd.fecha,
        hd.hora,
        t.nombre as tramite_nombre,
        t.descripcion as tramite_descripcion,
        t.precio,
        c.creada_en
      FROM citas c
      JOIN horarios_disponibles hd ON hd.id = c.horario_id
      JOIN tramites_usuarios tu ON tu.id = c.tramite_usuario_id
      JOIN tramites t ON t.codigo = tu.tramite_codigo
      WHERE tu.usuario_id = $1
      ORDER BY hd.fecha DESC, hd.hora DESC`,
      [usuarioId]
    );

    res.json({
      success: true,
      mensaje: 'Citas obtenidas',
      data: resultado.rows
    });

  } catch (error) {
    console.error('Error al obtener citas:', error);
    res.status(500).json({
      success: false,
      mensaje: 'Error al obtener citas'
    });
  }
});

/**
 * PATCH /api/citas/:id/reprogramar
 * Reprogramar una cita
 */
router.patch('/:id/reprogramar', async (req, res) => {
  const client = await require('../config/database').pool.connect();

  try {
    const { id } = req.params;
    const { fecha, hora } = req.body;

    if (!fecha || !hora) {
      return res.status(400).json({
        success: false,
        mensaje: 'La fecha y hora son obligatorias'
      });
    }

    await client.query('BEGIN');

    // Buscar o crear nuevo horario
    let nuevoHorario = await client.query(
      'SELECT id FROM horarios_disponibles WHERE fecha = $1 AND hora = $2',
      [fecha, hora]
    );

    let nuevoHorarioId;
    if (nuevoHorario.rows.length === 0) {
      const horarioCreado = await client.query(
        `INSERT INTO horarios_disponibles (fecha, hora, capacidad, disponible)
         VALUES ($1, $2, 1, true)
         RETURNING id`,
        [fecha, hora]
      );
      nuevoHorarioId = horarioCreado.rows[0].id;
    } else {
      nuevoHorarioId = nuevoHorario.rows[0].id;
    }

    // Actualizar cita
    const cita = await client.query(
      `UPDATE citas
       SET horario_id = $1, reprogramada_en = NOW()
       WHERE id = $2
       RETURNING *`,
      [nuevoHorarioId, id]
    );

    await client.query('COMMIT');

    res.json({
      success: true,
      mensaje: 'Cita reprogramada exitosamente',
      data: cita.rows[0]
    });

  } catch (error) {
    await client.query('ROLLBACK');
    console.error('Error al reprogramar:', error);

    let mensaje = 'Error al reprogramar la cita';
    if (error.message.includes('anticipación')) {
      mensaje = 'Se requiere al menos 1 día de anticipación para reprogramar';
    }

    res.status(400).json({
      success: false,
      mensaje,
      error: error.message
    });
  } finally {
    client.release();
  }
});

/**
 * PATCH /api/citas/:id/cancelar
 * Cancelar una cita y LIBERAR el horario
 */
router.patch('/:id/cancelar', async (req, res) => {
  const client = await require('../config/database').pool.connect();

  try {
    const { id } = req.params;
    const { motivo } = req.body;

    await client.query('BEGIN');

    // Obtener horario_id antes de cancelar
    const citaActual = await client.query(
      'SELECT horario_id, tramite_usuario_id FROM citas WHERE id = $1',
      [id]
    );

    if (citaActual.rows.length === 0) {
      await client.query('ROLLBACK');
      return res.status(404).json({
        success: false,
        mensaje: 'Cita no encontrada'
      });
    }

    const horarioId = citaActual.rows[0].horario_id;

    // Cancelar la cita
    const cita = await client.query(
      `UPDATE citas
       SET estado = 'CANCELADO', motivo_cancelacion = $1, fecha_cancelacion = NOW()
       WHERE id = $2
       RETURNING *`,
      [motivo || 'Cancelado por el usuario', id]
    );

    // Actualizar estado del trámite_usuario
    await client.query(
      `UPDATE tramites_usuarios
       SET estado_general = 'CANCELADO'
       WHERE id = $1`,
      [citaActual.rows[0].tramite_usuario_id]
    );

    // ✅ LIBERAR el horario (marcarlo como disponible nuevamente)
    if (horarioId) {
      await client.query(
        `UPDATE horarios_disponibles
         SET disponible = TRUE
         WHERE id = $1`,
        [horarioId]
      );
      console.log('✅ Horario liberado:', horarioId);
    }

    await client.query('COMMIT');

    res.json({
      success: true,
      mensaje: 'Cita cancelada exitosamente y horario liberado',
      data: cita.rows[0]
    });

  } catch (error) {
    await client.query('ROLLBACK');
    console.error('Error al cancelar:', error);

    res.status(400).json({
      success: false,
      mensaje: 'Error al cancelar la cita',
      error: error.message
    });
  } finally {
    client.release();
  }
});

/**
 * DELETE /api/citas/:id
 * Eliminar físicamente una cita y LIBERAR el horario
 * Solo para citas CANCELADAS o FINALIZADAS
 */
router.delete('/:id', async (req, res) => {
  const client = await require('../config/database').pool.connect();

  try {
    const { id } = req.params;

    await client.query('BEGIN');

    // Obtener datos de la cita antes de eliminar
    const citaActual = await client.query(
      'SELECT horario_id, tramite_usuario_id, estado FROM citas WHERE id = $1',
      [id]
    );

    if (citaActual.rows.length === 0) {
      await client.query('ROLLBACK');
      return res.status(404).json({
        success: false,
        mensaje: 'Cita no encontrada'
      });
    }

    const { horario_id, tramite_usuario_id, estado } = citaActual.rows[0];

    // Solo permitir eliminar citas canceladas o finalizadas
    if (!['CANCELADO', 'FINALIZADO'].includes(estado)) {
      await client.query('ROLLBACK');
      return res.status(400).json({
        success: false,
        mensaje: 'Solo se pueden eliminar citas canceladas o finalizadas'
      });
    }

    console.log('🗑️ ELIMINANDO CITA:', id);

    // ✅ LIBERAR el horario ANTES de eliminar
    if (horario_id) {
      await client.query(
        `UPDATE horarios_disponibles
         SET disponible = TRUE
         WHERE id = $1`,
        [horario_id]
      );
      console.log('✅ Horario liberado:', horario_id);
    }

    // Eliminar la cita físicamente
    await client.query('DELETE FROM citas WHERE id = $1', [id]);
    console.log('✅ Cita eliminada de la base de datos');

    // Eliminar tramite_usuario si ya no tiene citas asociadas
    const citasRestantes = await client.query(
      'SELECT COUNT(*) as total FROM citas WHERE tramite_usuario_id = $1',
      [tramite_usuario_id]
    );

    if (parseInt(citasRestantes.rows[0].total) === 0) {
      await client.query('DELETE FROM tramites_usuarios WHERE id = $1', [tramite_usuario_id]);
      console.log('✅ Tramite_usuario eliminado (sin citas asociadas)');
    }

    await client.query('COMMIT');

    res.json({
      success: true,
      mensaje: 'Cita eliminada exitosamente y horario liberado'
    });

  } catch (error) {
    await client.query('ROLLBACK');
    console.error('❌ Error al eliminar cita:', error);

    res.status(500).json({
      success: false,
      mensaje: 'Error al eliminar la cita',
      error: error.message
    });
  } finally {
    client.release();
  }
});

module.exports = router;
