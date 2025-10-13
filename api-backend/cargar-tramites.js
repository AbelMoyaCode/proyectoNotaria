/**
 * Script para cargar todos los trámites en la base de datos
 * Ejecutar con: node cargar-tramites.js
 */

const { Pool } = require('pg');
require('dotenv').config();

// Configuración de la base de datos
const pool = new Pool({
    host: process.env.DB_HOST || 'localhost',
    port: process.env.DB_PORT || 5432,
    user: process.env.DB_USER || 'postgres',
    password: process.env.DB_PASSWORD || 'postgres',
    database: process.env.DB_NAME || 'notariaBD'
});

// Datos de trámites
const tramites = [
    // PODERES
    { codigo: 'POD-001', nombre: 'Poder Simple', descripcion: 'Otorgamiento de poder para realizar trámites específicos en representación de otra persona.', requisitos: 'DNI vigente del otorgante,Datos completos del apoderado,Descripción clara de las facultades otorgadas', precio: 50.00, duracion_estimada: '1 día', categoria: 'Poderes' },
    { codigo: 'POD-002', nombre: 'Poder Amplio y General', descripcion: 'Poder con amplias facultades para representación legal en diversos actos jurídicos.', requisitos: 'DNI vigente del otorgante,Datos completos del apoderado,Lista detallada de facultades,Dos testigos con DNI', precio: 80.00, duracion_estimada: '1 día', categoria: 'Poderes' },
    { codigo: 'POD-003', nombre: 'Poder Especial', descripcion: 'Poder otorgado para actos específicos como venta de inmuebles o trámites bancarios.', requisitos: 'DNI vigente del otorgante,DNI del apoderado,Descripción detallada del acto específico', precio: 70.00, duracion_estimada: '1 día', categoria: 'Poderes' },

    // ESCRITURAS
    { codigo: 'ESC-001', nombre: 'Compraventa de Inmueble', descripcion: 'Formalización legal de la transferencia de propiedad de un bien inmueble.', requisitos: 'DNI vigente de ambas partes,Partida registral actualizada,Certificado de búsqueda catastral,Comprobante de pago de impuestos,Certificado de gravámenes', precio: 250.00, duracion_estimada: '3-5 días', categoria: 'Escrituras' },
    { codigo: 'ESC-002', nombre: 'Donación', descripcion: 'Acto de liberalidad mediante el cual una persona transfiere gratuitamente un bien a otra.', requisitos: 'DNI vigente del donante,DNI vigente del donatario,Partida de nacimiento (si es familiar),Documento de propiedad del bien', precio: 150.00, duracion_estimada: '2-3 días', categoria: 'Escrituras' },
    { codigo: 'ESC-003', nombre: 'Anticipo de Legítima', descripcion: 'Adelanto de herencia que realiza el padre a favor de sus hijos.', requisitos: 'DNI del donante y donatarios,Partida de nacimiento de hijos,Título de propiedad del bien,Certificado de gravámenes', precio: 200.00, duracion_estimada: '3-5 días', categoria: 'Escrituras' },

    // EMPRESARIAL
    { codigo: 'EMP-001', nombre: 'Constitución de Empresa', descripcion: 'Formalización de la constitución de una persona jurídica (SAC, SRL, SA).', requisitos: 'DNI de todos los socios,Reserva de nombre en SUNARP,Estatutos de la empresa,Capital social mínimo,Minuta de constitución', precio: 300.00, duracion_estimada: '5-7 días', categoria: 'Empresarial' },
    { codigo: 'EMP-002', nombre: 'Aumento de Capital', descripcion: 'Incremento del capital social de una empresa ya constituida.', requisitos: 'Vigencia de poder del representante legal,Acuerdo de junta de socios,Estados financieros actualizados,RUC de la empresa', precio: 200.00, duracion_estimada: '3-5 días', categoria: 'Empresarial' },
    { codigo: 'EMP-003', nombre: 'Modificación de Estatutos', descripcion: 'Cambio en el estatuto de la empresa (razón social, objeto social, etc.).', requisitos: 'Vigencia de poder,Acta de junta de socios,RUC de la empresa,Estatutos actuales', precio: 180.00, duracion_estimada: '3-5 días', categoria: 'Empresarial' },
    { codigo: 'EMP-004', nombre: 'Disolución y Liquidación', descripcion: 'Proceso de cierre definitivo de una empresa.', requisitos: 'Vigencia de poder,Acuerdo de disolución,Balance final,Constancia de no adeudo SUNAT', precio: 350.00, duracion_estimada: '7-10 días', categoria: 'Empresarial' },

    // TESTAMENTOS Y SUCESIONES
    { codigo: 'TEST-001', nombre: 'Testamento', descripcion: 'Documento legal mediante el cual una persona dispone de sus bienes para después de su muerte.', requisitos: 'DNI vigente del testador,Lista de bienes y propiedades,Datos de los beneficiarios,Dos testigos con DNI', precio: 180.00, duracion_estimada: '2-3 días', categoria: 'Testamentos' },
    { codigo: 'TEST-002', nombre: 'Apertura de Testamento', descripcion: 'Procedimiento para dar a conocer el contenido de un testamento cerrado.', requisitos: 'Partida de defunción del testador,Testamento cerrado,DNI de herederos', precio: 250.00, duracion_estimada: '5-7 días', categoria: 'Testamentos' },
    { codigo: 'SUC-001', nombre: 'Declaratoria de Herederos', descripcion: 'Reconocimiento legal de los herederos de una persona fallecida sin testamento.', requisitos: 'Partida de defunción original,Partidas de nacimiento de herederos,DNI vigente de todos los herederos,Testamento (si existe),Partidas de matrimonio (si aplica)', precio: 400.00, duracion_estimada: '7-10 días', categoria: 'Sucesiones' },
    { codigo: 'SUC-002', nombre: 'Sucesión Intestada', descripcion: 'Trámite de herencia cuando no existe testamento válido.', requisitos: 'Partida de defunción,Partidas de nacimiento de herederos,DNI de herederos,Certificado de no testamento', precio: 450.00, duracion_estimada: '10-15 días', categoria: 'Sucesiones' },

    // CERTIFICACIONES
    { codigo: 'CERT-001', nombre: 'Legalización de Firmas', descripcion: 'Certificación de la autenticidad de una firma en un documento.', requisitos: 'DNI vigente,Documento original a legalizar,Presencia del firmante', precio: 35.00, duracion_estimada: '30 minutos', categoria: 'Certificación' },
    { codigo: 'CERT-002', nombre: 'Legalización de Contratos', descripcion: 'Certificación notarial de un contrato privado entre partes.', requisitos: 'DNI de todas las partes,Contrato impreso (3 copias),Presencia de todos los firmantes', precio: 60.00, duracion_estimada: '1 día', categoria: 'Certificación' },
    { codigo: 'CERT-003', nombre: 'Testimonio de Escritura', descripcion: 'Copia certificada y legalizada de una escritura pública registrada.', requisitos: 'Solicitud escrita,Número de partida registral,Pago de derechos registrales', precio: 30.00, duracion_estimada: '1-2 días', categoria: 'Certificación' },
    { codigo: 'CERT-004', nombre: 'Certificación de Documentos', descripcion: 'Autenticación de fotocopias de documentos originales.', requisitos: 'Documento original,Fotocopia a certificar,DNI del solicitante', precio: 15.00, duracion_estimada: '30 minutos', categoria: 'Certificación' },
    { codigo: 'CERT-005', nombre: 'Protocolización de Partidas', descripcion: 'Incorporación de partidas de registros civiles al protocolo notarial.', requisitos: 'Partida original del registro civil,Solicitud del interesado,DNI vigente', precio: 40.00, duracion_estimada: '1-2 días', categoria: 'Certificación' },

    // DOCUMENTACIÓN
    { codigo: 'DOC-001', nombre: 'Declaración Jurada', descripcion: 'Manifestación escrita de hechos bajo juramento ante notario.', requisitos: 'DNI vigente del declarante,Redacción del texto a declarar', precio: 25.00, duracion_estimada: '1 día', categoria: 'Documentación' },
    { codigo: 'DOC-002', nombre: 'Cartas Notariales', descripcion: 'Comunicación formal certificada por notario con validez legal.', requisitos: 'DNI del remitente,Texto de la carta,Datos del destinatario', precio: 50.00, duracion_estimada: '1-2 días', categoria: 'Documentación' },
    { codigo: 'DOC-003', nombre: 'Constancia de Sobrevivencia', descripcion: 'Certificación notarial de que una persona se encuentra con vida.', requisitos: 'DNI vigente,Presencia física del interesado', precio: 20.00, duracion_estimada: '30 minutos', categoria: 'Documentación' },
    { codigo: 'DOC-004', nombre: 'Certificado de Convivencia', descripcion: 'Documento que acredita la convivencia en un domicilio determinado.', requisitos: 'DNI de convivientes,Recibos de servicios,Dos testigos con DNI', precio: 45.00, duracion_estimada: '1 día', categoria: 'Documentación' },

    // TRANSFERENCIA VEHICULAR
    { codigo: 'VEH-001', nombre: 'Transferencia Vehicular', descripcion: 'Cambio de titularidad de un vehículo automotor.', requisitos: 'DNI de vendedor y comprador,Tarjeta de propiedad original,Certificado de no gravamen,Pago de impuestos,Revisión técnica vigente', precio: 120.00, duracion_estimada: '2-3 días', categoria: 'Transferencias' },
    { codigo: 'VEH-002', nombre: 'Donación de Vehículo', descripcion: 'Transferencia gratuita de propiedad de un vehículo.', requisitos: 'DNI del donante y donatario,Tarjeta de propiedad,Certificado de no gravamen,Partida de nacimiento (si es familiar)', precio: 100.00, duracion_estimada: '2-3 días', categoria: 'Transferencias' },

    // DIVISIÓN Y PARTICIÓN
    { codigo: 'DIV-001', nombre: 'División y Partición de Bienes', descripcion: 'Distribución de bienes entre copropietarios o herederos.', requisitos: 'Partida registral del bien,DNI de todos los copropietarios,Plano de distribución,Tasación comercial', precio: 280.00, duracion_estimada: '5-7 días', categoria: 'División' },
    { codigo: 'DIV-002', nombre: 'Deslinde y Rectificación', descripcion: 'Corrección de medidas y linderos de un predio.', requisitos: 'Partida registral,Plano perimétrico,Certificado catastral,DNI del propietario', precio: 220.00, duracion_estimada: '5-7 días', categoria: 'División' },

    // HIPOTECA Y GARANTÍAS
    { codigo: 'HIP-001', nombre: 'Constitución de Hipoteca', descripcion: 'Garantía real sobre un bien inmueble para respaldar un crédito.', requisitos: 'DNI de acreedor y deudor,Partida registral del inmueble,Contrato de préstamo,Tasación del bien', precio: 200.00, duracion_estimada: '3-5 días', categoria: 'Garantías' },
    { codigo: 'HIP-002', nombre: 'Levantamiento de Hipoteca', descripcion: 'Cancelación de gravamen hipotecario por pago total de deuda.', requisitos: 'Constancia de cancelación del banco,Partida registral,DNI del propietario', precio: 150.00, duracion_estimada: '2-3 días', categoria: 'Garantías' },

    // FAMILIA
    { codigo: 'ADOP-001', nombre: 'Trámite de Adopción', descripcion: 'Proceso notarial para formalizar la adopción de un menor.', requisitos: 'Resolución judicial de adopción,DNI de adoptantes,Partida de nacimiento del menor,Evaluación psicológica', precio: 500.00, duracion_estimada: '10-15 días', categoria: 'Familia' },
    { codigo: 'MAT-001', nombre: 'Matrimonio Civil', descripcion: 'Celebración de matrimonio civil ante notario público.', requisitos: 'DNI vigente de ambos contrayentes,Certificado médico prenupcial,Dos testigos mayores de edad con DNI,Certificado de soltería', precio: 250.00, duracion_estimada: '15 días', categoria: 'Familia' },
    { codigo: 'MAT-002', nombre: 'Separación de Bienes', descripcion: 'Régimen patrimonial que separa los bienes de los cónyuges.', requisitos: 'Partida de matrimonio,DNI de ambos cónyuges,Inventario de bienes,Acuerdo firmado', precio: 150.00, duracion_estimada: '3-5 días', categoria: 'Familia' },

    // RECTIFICACIÓN
    { codigo: 'RECT-001', nombre: 'Rectificación de Partida', descripcion: 'Corrección de errores en partidas de nacimiento, matrimonio o defunción.', requisitos: 'Partida con error,Documentos que acrediten el error,DNI del solicitante,Sentencia judicial (si aplica)', precio: 180.00, duracion_estimada: '5-7 días', categoria: 'Rectificación' },

    // AUTORIZACIONES
    { codigo: 'VIAJE-001', nombre: 'Autorización de Viaje de Menor', descripcion: 'Permiso notarial para que un menor viaje al extranjero.', requisitos: 'DNI del menor,DNI de ambos padres,Partida de nacimiento del menor,Datos del viaje (destino, fechas)', precio: 60.00, duracion_estimada: '1 día', categoria: 'Autorizaciones' }
];

async function cargarTramites() {
    console.log('========================================');
    console.log('  CARGANDO TRÁMITES EN LA BASE DE DATOS');
    console.log('========================================\n');

    try {
        // Probar conexión
        console.log('📡 Conectando a PostgreSQL...');
        await pool.query('SELECT NOW()');
        console.log('✅ Conexión exitosa\n');

        console.log(`🌱 Insertando ${tramites.length} trámites...\n`);

        let insertados = 0;
        let actualizados = 0;

        for (const tramite of tramites) {
            try {
                const verificar = await pool.query(
                    'SELECT codigo FROM tramites WHERE codigo = $1',
                    [tramite.codigo]
                );

                if (verificar.rows.length > 0) {
                    // Actualizar
                    await pool.query(
                        `UPDATE tramites SET
                            nombre = $2,
                            descripcion = $3,
                            requisitos = $4,
                            precio = $5,
                            duracion_estimada = $6,
                            categoria = $7
                        WHERE codigo = $1`,
                        [
                            tramite.codigo,
                            tramite.nombre,
                            tramite.descripcion,
                            tramite.requisitos,
                            tramite.precio,
                            tramite.duracion_estimada,
                            tramite.categoria
                        ]
                    );
                    actualizados++;
                    console.log(`   🔄 Actualizado: ${tramite.codigo} - ${tramite.nombre}`);
                } else {
                    // Insertar
                    await pool.query(
                        `INSERT INTO tramites (codigo, nombre, descripcion, requisitos, precio, duracion_estimada, categoria, activo)
                        VALUES ($1, $2, $3, $4, $5, $6, $7, TRUE)`,
                        [
                            tramite.codigo,
                            tramite.nombre,
                            tramite.descripcion,
                            tramite.requisitos,
                            tramite.precio,
                            tramite.duracion_estimada,
                            tramite.categoria
                        ]
                    );
                    insertados++;
                    console.log(`   ✅ Insertado: ${tramite.codigo} - ${tramite.nombre}`);
                }
            } catch (err) {
                console.error(`   ❌ Error con ${tramite.codigo}:`, err.message);
            }
        }

        console.log('\n========================================');
        console.log(`✅ COMPLETADO`);
        console.log(`   Nuevos: ${insertados}`);
        console.log(`   Actualizados: ${actualizados}`);
        console.log('========================================\n');

        // Verificar total
        const result = await pool.query('SELECT COUNT(*) as total FROM tramites');
        console.log(`📊 Total de trámites en BD: ${result.rows[0].total}\n`);

    } catch (error) {
        console.error('\n❌ ERROR:', error.message);
        console.error('Stack:', error.stack);
    } finally {
        await pool.end();
        console.log('👋 Conexión cerrada');
    }
}

// Ejecutar
cargarTramites();
