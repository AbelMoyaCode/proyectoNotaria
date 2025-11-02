const { Pool } = require('pg');
require('dotenv').config();

// Configuración de la conexión a PostgreSQL
const pool = new Pool({
    host: process.env.DB_HOST,
    port: process.env.DB_PORT,
    database: process.env.DB_NAME,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
});

// Verificar conexión al iniciar
pool.on('connect', () => {
    console.log('✅ Conectado a PostgreSQL');
});

pool.on('error', (err) => {
    console.error('❌ Error en PostgreSQL:', err);
});

// Función para probar la conexión
async function testConnection() {
    try {
        const client = await pool.connect();
        const result = await client.query('SELECT NOW()');
        client.release();
        console.log('✅ Conexión a PostgreSQL exitosa:', result.rows[0].now);
        return true;
    } catch (error) {
        console.error('❌ Error al conectar con PostgreSQL:', error.message);
        throw error;
    }
}

// Función para corregir automáticamente el constraint duplicado
async function corregirConstraintDuplicado() {
    try {
        const client = await pool.connect();

        // Eliminar el constraint problemático si existe
        await client.query('DROP INDEX IF EXISTS idx_usuario_fecha_hora');

        console.log('✅ Constraint duplicado corregido automáticamente');
        client.release();
        return true;
    } catch (error) {
        console.error('⚠️ Error al corregir constraint (puede que ya esté corregido):', error.message);
        return false;
    }
}

// Función helper para ejecutar queries
const query = (text, params) => pool.query(text, params);

module.exports = {
    pool,
    query,
    testConnection,
    corregirConstraintDuplicado
};
