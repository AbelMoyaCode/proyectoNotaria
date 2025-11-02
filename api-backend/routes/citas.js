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
 * 2. Verificar que no haya más de 1 cita por día por usuario
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
        mensaje: 'Todos los campos son obligatorios (usuario_id, tramite_codigo, fecha, hora)'
      });
    }

    await client.query('BEGIN');
    console.log('🔄 Transacción iniciada');

    // ========================================
    // VALIDACIÓN 1: DISPONIBILIDAD DE HORARIOS
    // Buscar o crear horario disponible automáticamente
    // ========================================
    console.log('🔎 Buscando horario disponible...');
    let horario = await client.query(
      'SELECT id, disponible FROM horarios_disponibles WHERE fecha = $1 AND hora = $2',
      [fecha, hora]
    );

    let horarioId;

    if (horario.rows.length === 0) {
      // Auto-crear el horario si no existe
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
      // Verificar que el horario esté disponible
      console.log('📋 Horario encontrado, verificando disponibilidad...');
      if (!horario.rows[0].disponible) {
        await client.query('ROLLBACK');
        console.log('❌ VALIDACIÓN FALLIDA: Horario ocupado');
        return res.status(400).json({
          success: false,
          mensaje: 'Este horario ya está ocupado. Por favor, seleccione otro.'
        });
      }
      horarioId = horario.rows[0].id;
      console.log('✅ Horario disponible con ID:', horarioId);
    }

    // ========================================
    // VALIDACIÓN 2: 1 CITA POR DÍA MÁXIMO
    // Verificar que el usuario no tenga otra cita el mismo día
    // ========================================
    console.log('🔎 Verificando citas existentes del usuario en la fecha...');
    const citaExistente = await client.query(
      `SELECT c.id FROM citas c
       JOIN horarios_disponibles hd ON hd.id = c.horario_id
       JOIN tramites_usuarios tu ON tu.id = c.tramite_usuario_id
       WHERE tu.usuario_id = $1 AND hd.fecha = $2 AND c.estado IN ('AGENDADO', 'EN_PROCESO')`,
      [usuario_id, fecha]
    );

    if (citaExistente.rows.length > 0) {
      await client.query('ROLLBACK');
      console.log('❌ VALIDACIÓN FALLIDA: Usuario ya tiene cita para esta fecha');
      return res.status(400).json({
        success: false,
        mensaje: 'Ya tiene una cita agendada para esta fecha. Solo se permite una cita por día.'
      });
    }
    console.log('✅ Usuario no tiene citas en conflicto');

    // ========================================
    // PRUEBA DE RESERVA: CREAR CITA
    // Crear tramite_usuario y cita en transacción atómica
    // ========================================
    console.log('💾 Creando tramite_usuario...');
    const tramiteUsuario = await client.query(
      `INSERT INTO tramites_usuarios (usuario_id, tramite_codigo, estado_general)
       VALUES ($1, $2, 'AGENDADO')
       RETURNING id`,
      [usuario_id, tramite_codigo]
    );

    const tramiteUsuarioId = tramiteUsuario.rows[0].id;
    console.log('✅ Tramite_usuario creado con ID:', tramiteUsuarioId);

    // Crear cita con todos los campos necesarios
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
 * Obtener todas las citas de un usuario
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
 * Cancelar una cita
 */
router.patch('/:id/cancelar', async (req, res) => {
  const client = await require('../config/database').pool.connect();

  try {
    const { id } = req.params;
    const { motivo } = req.body;

    await client.query('BEGIN');

    const cita = await client.query(
      `UPDATE citas
       SET estado = 'CANCELADO', motivo_cancelacion = $1
       WHERE id = $2
       RETURNING *`,
      [motivo || 'Cancelado por el usuario', id]
    );

    // Actualizar estado del trámite_usuario
    await client.query(
      `UPDATE tramites_usuarios
       SET estado_general = 'CANCELADO'
       WHERE id = (SELECT tramite_usuario_id FROM citas WHERE id = $1)`,
      [id]
    );

    await client.query('COMMIT');

    res.json({
      success: true,
      mensaje: 'Cita cancelada exitosamente',
      data: cita.rows[0]
    });

  } catch (error) {
    await client.query('ROLLBACK');
    console.error('Error al cancelar:', error);

    let mensaje = 'Error al cancelar la cita';
    if (error.message.includes('anticipación')) {
      mensaje = 'Se requiere al menos 1 día de anticipación para cancelar';
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

module.exports = router;
