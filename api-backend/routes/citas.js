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
      `SELECT c.id, t.codigo as tramite_codigo, c.tramite_usuario_id, c.estado, hd.fecha, hd.hora, t.nombre as tramite_nombre, t.descripcion as tramite_descripcion, t.requisitos as tramite_requisitos, t.precio, c.observaciones, c.creada_en FROM citas c JOIN horarios_disponibles hd ON hd.id = c.horario_id JOIN tramites_usuarios tu ON tu.id = c.tramite_usuario_id JOIN tramites t ON t.codigo = tu.tramite_codigo WHERE c.id = $1`,
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
    const client = await require('../config/database').pool.connect();
    const { id } = req.params;
    const { fecha, hora } = req.body;

    if (!fecha || !hora) {
        return res.status(400).json({ success: false, mensaje: 'Se requiere nueva fecha y hora.' });
    }

    try {
        await client.query('BEGIN');

        // 1. Obtener el horario_id ANTERIOR de la cita
        const citaAnterior = await client.query('SELECT horario_id FROM citas WHERE id = $1', [id]);
        if (citaAnterior.rows.length === 0) {
            throw new Error('La cita a reprogramar no existe.');
        }
        const horarioIdAnterior = citaAnterior.rows[0].horario_id;

        // 2. Liberar el horario ANTERIOR
        await client.query('UPDATE horarios_disponibles SET disponible = TRUE WHERE id = $1', [horarioIdAnterior]);

        // 3. Buscar o crear el NUEVO horario y ocuparlo
        let nuevoHorario = await client.query('SELECT id, disponible FROM horarios_disponibles WHERE fecha = $1 AND hora = $2', [fecha, hora]);
        let nuevoHorarioId;

        if (nuevoHorario.rows.length === 0) {
            // Si no existe, lo creamos
            const horarioInsertado = await client.query('INSERT INTO horarios_disponibles (fecha, hora, disponible) VALUES ($1, $2, FALSE) RETURNING id', [fecha, hora]);
            nuevoHorarioId = horarioInsertado.rows[0].id;
        } else {
            // Si ya existe, verificamos que esté disponible
            if (!nuevoHorario.rows[0].disponible) {
                throw new Error('El nuevo horario seleccionado ya no está disponible.');
            }
            nuevoHorarioId = nuevoHorario.rows[0].id;
            await client.query('UPDATE horarios_disponibles SET disponible = FALSE WHERE id = $1', [nuevoHorarioId]);
        }

        // 4. Actualizar la cita con el nuevo horario_id
        const citaActualizada = await client.query(
            'UPDATE citas SET horario_id = $1, estado = \'AGENDADO\' WHERE id = $2 RETURNING *',
            [nuevoHorarioId, id]
        );

        await client.query('COMMIT');

        res.json({ success: true, mensaje: 'Cita reprogramada exitosamente.', data: citaActualizada.rows[0] });

    } catch (error) {
        await client.query('ROLLBACK');
        res.status(500).json({ success: false, mensaje: 'Error al reprogramar la cita.', error: error.message });
    } finally {
        client.release();
    }
});

/**
 * PATCH /api/citas/:id/cancelar
 */
router.patch('/:id/cancelar', async (req, res) => {
    const client = await require('../config/database').pool.connect();
    const { id } = req.params;
    const { motivo } = req.body;

    try {
        await client.query('BEGIN');

        // 1. Obtener el horario_id de la cita a cancelar
        const cita = await client.query('SELECT horario_id FROM citas WHERE id = $1', [id]);
        if (cita.rows.length === 0) {
            throw new Error('La cita a cancelar no existe.');
        }
        const horarioId = cita.rows[0].horario_id;

        // 2. Liberar el horario
        await client.query('UPDATE horarios_disponibles SET disponible = TRUE WHERE id = $1', [horarioId]);

        // 3. Actualizar el estado de la cita a CANCELADO
        const citaCancelada = await client.query(
            'UPDATE citas SET estado = \'CANCELADO\', motivo_cancelacion = $1 WHERE id = $2 RETURNING *',
            [motivo, id]
        );
        
        await client.query('COMMIT');

        res.json({ success: true, mensaje: 'Cita cancelada exitosamente.', data: citaCancelada.rows[0] });

    } catch (error) {
        await client.query('ROLLBACK');
        res.status(500).json({ success: false, mensaje: 'Error al cancelar la cita.', error: error.message });
    } finally {
        client.release();
    }
});

/**
 * DELETE /api/citas/:id
 */
router.delete('/:id', async (req, res) => {
    // Código existente para eliminar
});

module.exports = router;