const express = require('express');
const router = express.Router();
const { query } = require('../config/database');

/**
 * GET /api/tramites
 * Obtener todos los trámites activos
 */
router.get('/', async (req, res) => {
  try {
    const resultado = await query(
      'SELECT codigo, nombre, descripcion, requisitos, precio, activo FROM tramites WHERE activo = true ORDER BY nombre'
    );

    res.json({
      success: true,
      mensaje: 'Trámites obtenidos',
      data: resultado.rows
    });

  } catch (error) {
    console.error('Error al obtener trámites:', error);
    res.status(500).json({
      success: false,
      mensaje: 'Error al obtener trámites'
    });
  }
});

/**
 * GET /api/tramites/buscar?q=texto
 * Buscar trámites por nombre o descripción
 */
router.get('/buscar', async (req, res) => {
  try {
    const { q } = req.query;

    if (!q || q.trim() === '') {
      return res.status(400).json({
        success: false,
        mensaje: 'El parámetro de búsqueda es obligatorio'
      });
    }

    const resultado = await query(
      `SELECT codigo, nombre, descripcion, requisitos, precio, activo
       FROM tramites
       WHERE activo = true
       AND (LOWER(nombre) LIKE LOWER($1) OR LOWER(descripcion) LIKE LOWER($1))
       ORDER BY nombre`,
      [`%${q}%`]
    );

    res.json({
      success: true,
      mensaje: resultado.rows.length > 0 ? 'Resultados encontrados' : 'No se encontraron resultados',
      data: resultado.rows
    });

  } catch (error) {
    console.error('Error en búsqueda de trámites:', error);
    res.status(500).json({
      success: false,
      mensaje: 'Error en la búsqueda'
    });
  }
});

/**
 * GET /api/tramites/:codigo
 * Obtener detalle de un trámite específico
 */
router.get('/:codigo', async (req, res) => {
  try {
    const { codigo } = req.params;

    const resultado = await query(
      'SELECT codigo, nombre, descripcion, requisitos, precio, activo FROM tramites WHERE codigo = $1',
      [codigo]
    );

    if (resultado.rows.length === 0) {
      return res.status(404).json({
        success: false,
        mensaje: 'Trámite no encontrado'
      });
    }

    res.json({
      success: true,
      mensaje: 'Trámite obtenido',
      data: resultado.rows[0]
    });

  } catch (error) {
    console.error('Error al obtener trámite:', error);
    res.status(500).json({
      success: false,
      mensaje: 'Error al obtener trámite'
    });
  }
});

/**
 * GET /api/horarios?fecha=YYYY-MM-DD
 * Obtener horarios disponibles para una fecha
 */
router.get('/horarios', async (req, res) => {
  try {
    const { fecha } = req.query;

    if (!fecha) {
      return res.status(400).json({
        success: false,
        mensaje: 'La fecha es obligatoria'
      });
    }

    const resultado = await query(
      `SELECT id, fecha, hora, capacidad, disponible, created_at
       FROM horarios_disponibles
       WHERE fecha = $1 AND disponible = true
       ORDER BY hora`,
      [fecha]
    );

    res.json({
      success: true,
      mensaje: resultado.rows.length > 0 ? 'Horarios disponibles' : 'No hay horarios disponibles',
      data: resultado.rows
    });

  } catch (error) {
    console.error('Error al obtener horarios:', error);
    res.status(500).json({
      success: false,
      mensaje: 'Error al obtener horarios'
    });
  }
});

module.exports = router;
