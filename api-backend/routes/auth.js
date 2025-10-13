const express = require('express');
const router = express.Router();
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const { query } = require('../config/database');

/**
 * POST /api/auth/register
 * Registro de usuario
 */
router.post('/register', async (req, res) => {
    try {
        const { tipo_doc, nro_doc, nombres, apellidos, correo, password, direccion, telefono } = req.body;

        // Validaciones básicas
        if (!tipo_doc || !nro_doc || !nombres || !apellidos || !correo || !password) {
            return res.status(400).json({
                success: false,
                mensaje: 'Todos los campos obligatorios deben estar completos'
            });
        }

        // Verificar si el correo ya existe
        const correoExiste = await query(
            'SELECT id FROM usuarios WHERE correo = $1',
            [correo]
        );

        if (correoExiste.rows.length > 0) {
            return res.status(409).json({
                success: false,
                mensaje: 'El correo ya está registrado'
            });
        }

        // Verificar si el documento ya existe
        const docExiste = await query(
            'SELECT id FROM usuarios WHERE tipo_doc = $1 AND nro_doc = $2',
            [tipo_doc, nro_doc]
        );

        if (docExiste.rows.length > 0) {
            return res.status(409).json({
                success: false,
                mensaje: 'El número de documento ya está registrado'
            });
        }

        // Hash de la contraseña
        const passwordHash = await bcrypt.hash(password, 10);

        // Insertar usuario
        const resultado = await query(
            `INSERT INTO usuarios (tipo_doc, nro_doc, nombres, apellidos, correo, direccion, telefono, password_hash)
             VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
             RETURNING id, tipo_doc, nro_doc, nombres, apellidos, correo, direccion, telefono, estado, created_at`,
            [tipo_doc, nro_doc, nombres, apellidos, correo, direccion, telefono, passwordHash]
        );

        res.status(201).json({
            success: true,
            mensaje: 'Usuario registrado exitosamente',
            data: resultado.rows[0]
        });

    } catch (error) {
        console.error('Error en registro:', error);
        res.status(500).json({
            success: false,
            mensaje: 'Error al registrar usuario'
        });
    }
});

/**
 * POST /api/auth/login
 * Login de usuario
 */
router.post('/login', async (req, res) => {
    try {
        const { correo, password } = req.body;

        // Validaciones básicas
        if (!correo || !password) {
            return res.status(400).json({
                success: false,
                mensaje: 'Correo y contraseña son obligatorios'
            });
        }

        // Buscar usuario
        const resultado = await query(
            'SELECT * FROM usuarios WHERE correo = $1',
            [correo]
        );

        if (resultado.rows.length === 0) {
            return res.status(401).json({
                success: false,
                mensaje: 'Credenciales incorrectas'
            });
        }

        const usuario = resultado.rows[0];

        // Verificar contraseña
        const passwordValida = await bcrypt.compare(password, usuario.password_hash);

        if (!passwordValida) {
            return res.status(401).json({
                success: false,
                mensaje: 'Credenciales incorrectas'
            });
        }

        // Generar token JWT
        const token = jwt.sign(
            { id: usuario.id, correo: usuario.correo },
            process.env.JWT_SECRET,
            { expiresIn: '7d' }
        );

        // No enviar el hash de contraseña
        delete usuario.password_hash;

        res.json({
            success: true,
            mensaje: 'Login exitoso',
            token,
            usuario
        });

    } catch (error) {
        console.error('Error en login:', error);
        res.status(500).json({
            success: false,
            mensaje: 'Error al iniciar sesión'
        });
    }
});

module.exports = router;
