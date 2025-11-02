const express = require('express');
const cors = require('cors');
const { testConnection, corregirConstraintDuplicado } = require('./config/database');
const { seedTramites } = require('./config/seed-tramites');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;

// Middlewares
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Importar rutas
const authRoutes = require('./routes/auth');
const tramitesRoutes = require('./routes/tramites');
const citasRoutes = require('./routes/citas');

// Usar rutas
app.use('/api/auth', authRoutes);
app.use('/api/tramites', tramitesRoutes);
app.use('/api/citas', citasRoutes);

// Ruta principal /api
app.get('/api', (req, res) => {
    res.json({
        success: true,
        mensaje: 'Bienvenido a la API de TramiNotar',
        version: '1.0.0',
        endpoints: {
            auth: {
                registro: 'POST /api/auth/register',
                login: 'POST /api/auth/login',
                perfil: 'GET /api/auth/perfil'
            },
            tramites: {
                listar: 'GET /api/tramites',
                buscar: 'GET /api/tramites/buscar?q=texto',
                detalle: 'GET /api/tramites/:codigo'
            },
            citas: {
                crear: 'POST /api/citas',
                misTramites: 'GET /api/citas/mis-tramites',
                reprogramar: 'PATCH /api/citas/:id/reprogramar',
                cancelar: 'PATCH /api/citas/:id/cancelar'
            }
        }
    });
});

// Ruta de estado
app.get('/api/health', (req, res) => {
    res.json({
        success: true,
        mensaje: 'API TramiNotar funcionando correctamente',
        timestamp: new Date().toISOString()
    });
});

// Manejo de rutas no encontradas
app.use((req, res) => {
    res.status(404).json({
        success: false,
        mensaje: 'Endpoint no encontrado'
    });
});

// Manejo de errores
app.use((err, req, res, next) => {
    console.error(err.stack);
    res.status(500).json({
        success: false,
        mensaje: 'Error interno del servidor',
        error: process.env.NODE_ENV === 'development' ? err.message : undefined
    });
});

// Iniciar servidor
app.listen(PORT, async () => {
    console.log('🚀 Servidor corriendo en http://localhost:' + PORT);
    console.log('📡 API disponible en http://localhost:' + PORT + '/api');
    console.log('✅ Health check: http://localhost:' + PORT + '/api/health');

    try {
        // Probar conexión a la base de datos
        await testConnection();

        // 🔧 CORREGIR AUTOMÁTICAMENTE EL CONSTRAINT DUPLICADO
        await corregirConstraintDuplicado();

        // Seed de trámites
        await seedTramites();
    } catch (error) {
        console.error('❌ Error al inicializar:', error);
    }
});

module.exports = app;
