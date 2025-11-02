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
        const {
            nro_documento,
            nombre,
            apellido_paterno,
            apellido_materno,
            fecha_nacimiento,
            correo,
            contrasena,
            direccion,
            telefono
        } = req.body;

        // Validaciones básicas
        if (!nro_documento || !nombre || !apellido_paterno || !apellido_materno || !correo || !contrasena) {
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
            'SELECT id FROM usuarios WHERE nro_documento = $1',
            [nro_documento]
        );

        if (docExiste.rows.length > 0) {
            return res.status(409).json({
                success: false,
                mensaje: 'El número de documento ya está registrado'
            });
        }

        // Hash de la contraseña
        const passwordHash = await bcrypt.hash(contrasena, 10);

        // Insertar usuario con los nombres de columnas correctos
        const resultado = await query(
            `INSERT INTO usuarios (nro_documento, nombre, apellido_paterno, apellido_materno, fecha_nacimiento, correo, direccion, telefono, contrasena)
             VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
             RETURNING id, nro_documento, nombre, apellido_paterno, apellido_materno, correo, direccion, telefono, estado, fecha_registro`,
            [nro_documento, nombre, apellido_paterno, apellido_materno, fecha_nacimiento || '1990-01-01', correo, direccion, telefono, passwordHash]
        );

        // Formatear respuesta para el frontend
        const usuario = resultado.rows[0];
        const respuesta = {
            id: usuario.id,
            nroDocumento: usuario.nro_documento,
            nombres: usuario.nombre,
            apellidoPaterno: usuario.apellido_paterno,
            apellidoMaterno: usuario.apellido_materno,
            correo: usuario.correo,
            direccion: usuario.direccion,
            telefono: usuario.telefono,
            estado: usuario.estado,
            fechaRegistro: usuario.fecha_registro
        };

        res.status(201).json({
            success: true,
            mensaje: 'Usuario registrado exitosamente',
            data: respuesta
        });

    } catch (error) {
        console.error('Error en registro:', error);
        res.status(500).json({
            success: false,
            mensaje: 'Error al registrar usuario',
            error: error.message
        });
    }
});

/**
 * POST /api/auth/login
 * Login de usuario
 */
router.post('/login', async (req, res) => {
    try {
        const { correo, password, contrasena } = req.body;

        // Aceptar tanto "password" como "contrasena"
        const passwordFinal = password || contrasena;

        // Validaciones básicas
        if (!correo || !passwordFinal) {
            return res.status(400).json({
                success: false,
                mensaje: 'Correo y contraseña son obligatorios'
            });
        }

        // Buscar usuario con los nombres de columnas correctos
        const resultado = await query(
            'SELECT id, nro_documento, nombre, apellido_paterno, apellido_materno, correo, direccion, telefono, contrasena, estado, fecha_registro FROM usuarios WHERE correo = $1',
            [correo]
        );

        if (resultado.rows.length === 0) {
            return res.status(401).json({
                success: false,
                mensaje: 'Credenciales incorrectas'
            });
        }

        const usuario = resultado.rows[0];

        // Verificar contraseña (la columna se llama "contrasena" no "password_hash")
        const passwordValida = await bcrypt.compare(passwordFinal, usuario.contrasena);

        if (!passwordValida) {
            return res.status(401).json({
                success: false,
                mensaje: 'Credenciales incorrectas'
            });
        }

        // Generar token JWT
        const token = jwt.sign(
            { id: usuario.id, correo: usuario.correo },
            process.env.JWT_SECRET || 'secret_key_default',
            { expiresIn: '7d' }
        );

        // Mapear respuesta al formato esperado por el frontend
        const respuesta = {
            token: token,
            usuario: {
                id: usuario.id,
                nroDocumento: usuario.nro_documento,
                nombres: usuario.nombre,
                apellidoPaterno: usuario.apellido_paterno,
                apellidoMaterno: usuario.apellido_materno,
                correo: usuario.correo,
                direccion: usuario.direccion,
                telefono: usuario.telefono,
                estado: usuario.estado,
                fechaRegistro: usuario.fecha_registro
            }
        };

        res.json({
            success: true,
            mensaje: 'Login exitoso',
            ...respuesta
        });

    } catch (error) {
        console.error('Error en login:', error);
        res.status(500).json({
            success: false,
            mensaje: 'Error al iniciar sesión',
            error: error.message
        });
    }
});

module.exports = router;
