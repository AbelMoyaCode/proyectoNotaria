const express = require('express');
const router = express.Router();
const { query } = require('../config/database');
const { verificarToken } = require('../middleware/auth');

/**
 * POST /api/citas
 * Crear/Reservar una nueva cita
 */
router.post('/', async (req, res) => {
  const client = await require('../config/database').pool.connect();
  try {
    const { usuario_id, tramite_codigo, fecha, hora } = req.body;

    if (!usuario_id || !tramite_codigo || !fecha || !hora) {
      return res.status(400).json({
        success: false,
        mensaje: 'Por favor, complete todos los datos: fecha, hora y trámite'
      });
    }

    const fechaCita = new Date(fecha);
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);

    if (fechaCita < hoy) {
      return res.status(400).json({
        success: false,
        mensaje: 'No se pueden agendar citas en fechas pasadas'
      });
    }

    await client.query('BEGIN');

    let horario = await client.query(
      'SELECT id FROM horarios_disponibles WHERE fecha = $1 AND hora = $2 AND disponible = TRUE',
      [fecha, hora]
    );

    let horarioId;
    if (horario.rows.length === 0) {
        const nuevoHorario = await client.query(
            `INSERT INTO horarios_disponibles (fecha, hora, disponible) VALUES ($1, $2, TRUE) RETURNING id`,
            [fecha, hora]
        );
        horarioId = nuevoHorario.rows[0].id;
    } else {
        horarioId = horario.rows[0].id;
    }

    const citaActivaUsuario = await client.query(
      `SELECT c.id FROM citas c WHERE c.usuario_id = $1 AND c.horario_id = $2 AND c.estado IN ('AGENDADO', 'EN_PROCESO')`,
      [usuario_id, horarioId]
    );

    if (citaActivaUsuario.rows.length > 0) {
      await client.query('ROLLBACK');
      return res.status(400).json({
        success: false,
        mensaje: 'Ya tiene una cita agendada para este horario.'
      });
    }

    const tramiteUsuario = await client.query(
      `INSERT INTO tramites_usuarios (usuario_id, tramite_codigo, estado_general) VALUES ($1, $2, 'AGENDADO') RETURNING id`,
      [usuario_id, tramite_codigo]
    );
    const tramiteUsuarioId = tramiteUsuario.rows[0].id;

    const cita = await client.query(
      `INSERT INTO citas (usuario_id, tramite_id, fecha_cita, hora_cita, tramite_usuario_id, horario_id, estado) VALUES ($1, (SELECT id FROM tramites WHERE codigo = $2), $3, $4, $5, $6, 'AGENDADO') RETURNING *`,
      [usuario_id, tramite_codigo, fecha, hora, tramiteUsuarioId, horarioId]
    );

    await client.query('UPDATE horarios_disponibles SET disponible = FALSE WHERE id = $1', [horarioId]);
    
    const citaCompleta = await client.query(
      `SELECT c.id, c.estado, hd.fecha, hd.hora, t.nombre as tramite_nombre, t.descripcion as tramite_descripcion, t.requisitos as tramite_requisitos, c.observaciones FROM citas c JOIN horarios_disponibles hd ON hd.id = c.horario_id JOIN tramites_usuarios tu ON tu.id = c.tramite_usuario_id JOIN tramites t ON t.codigo = tu.tramite_codigo WHERE c.id = $1`,
      [cita.rows[0].id]
    );

    await client.query('COMMIT');
    res.status(201).json({ success: true, mensaje: 'Cita creada exitosamente', data: citaCompleta.rows[0] });

  } catch (error) {
    await client.query('ROLLBACK');
    res.status(400).json({ success: false, mensaje: 'Error al crear la cita', error: error.message });
  } finally {
    client.release();
  }
});

/**
 * GET /api/citas/usuario/:usuarioId
 */
router.get('/usuario/:usuarioId', async (req, res) => {
  try {
    const { usuarioId } = req.params;
    const resultado = await query(
      `SELECT c.id, c.tramite_usuario_id, c.estado, hd.fecha, hd.hora, t.nombre as tramite_nombre, t.descripcion as tramite_descripcion, t.precio, c.creada_en FROM citas c JOIN horarios_disponibles hd ON hd.id = c.horario_id JOIN tramites_usuarios tu ON tu.id = c.tramite_usuario_id JOIN tramites t ON t.codigo = tu.tramite_codigo WHERE tu.usuario_id = $1 ORDER BY hd.fecha DESC, hd.hora DESC`,
      [usuarioId]
    );
    res.json({ success: true, mensaje: 'Citas obtenidas', data: resultado.rows });
  } catch (error) {
    res.status(500).json({ success: false, mensaje: 'Error al obtener citas' });
  }
});

/**
 * GET /api/citas/:id
 */
router.get('/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const resultado = await query(
      `SELECT c.id, c.tramite_usuario_id, c.estado, hd.fecha, hd.hora, t.nombre as tramite_nombre, t.descripcion as tramite_descripcion, t.requisitos as tramite_requisitos, t.precio, c.observaciones, c.creada_en FROM citas c JOIN horarios_disponibles hd ON hd.id = c.horario_id JOIN tramites_usuarios tu ON tu.id = c.tramite_usuario_id JOIN tramites t ON t.codigo = tu.tramite_codigo WHERE c.id = $1`,
      [id]
    );
    if (resultado.rows.length === 0) {
      return res.status(404).json({ success: false, mensaje: 'Cita no encontrada' });
    }
    res.json({ success: true, mensaje: 'Detalle de la cita obtenido', data: resultado.rows[0] });
  } catch (error) {
    console.error('Error al obtener detalle de la cita:', error);
    res.status(500).json({ success: false, mensaje: 'Error al obtener el detalle de la cita' });
  }
});

/**
 * PATCH /api/citas/:id/reprogramar
 */
router.patch('/:id/reprogramar', async (req, res) => {
    // Código existente para reprogramar
});

/**
 * PATCH /api/citas/:id/cancelar
 */
router.patch('/:id/cancelar', async (req, res) => {
    // Código existente para cancelar
});

/**
 * DELETE /api/citas/:id
 */
router.delete('/:id', async (req, res) => {
    // Código existente para eliminar
});

module.exports = router;