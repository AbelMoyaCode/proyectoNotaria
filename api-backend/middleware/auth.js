const jwt = require('jsonwebtoken');

/**
 * Middleware para verificar token JWT
 */
const verificarToken = (req, res, next) => {
  const authHeader = req.headers['authorization'];

  if (!authHeader) {
    return res.status(401).json({
      success: false,
      mensaje: 'Token no proporcionado'
    });
  }

  const token = authHeader.split(' ')[1]; // Bearer TOKEN

  if (!token) {
    return res.status(401).json({
      success: false,
      mensaje: 'Token no válido'
    });
  }

  try {
    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    req.usuario = decoded; // Agregar datos del usuario al request
    next();
  } catch (error) {
    return res.status(401).json({
      success: false,
      mensaje: 'Token expirado o inválido'
    });
  }
};

module.exports = { verificarToken };

