package com.ampn.proyecto_notaria

import com.ampn.proyecto_notaria.modelos.Usuario
import com.ampn.proyecto_notaria.modelos.Tramite
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class DatabaseHelper {

    private val dbUrl = "jdbc:postgresql://localhost:5432/notariaBD"
    private val dbUser = "postgres"
    private val dbPassword = "notaria1234"

    init {
        Class.forName("org.postgresql.Driver")
    }

    @Throws(SQLException::class)
    private fun getConnection(): Connection {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword)
    }

    /**
     * Inserta un nuevo usuario en la base de datos.
     * @param usuario El objeto Usuario con los datos a registrar.
     */
    @Throws(SQLException::class)
    fun registrarUsuario(usuario: Usuario) {
        val sql = "INSERT INTO usuarios (nro_documento, nombre, apellido_paterno, apellido_materno, fecha_nacimiento, correo, direccion, contrasena) VALUES (?, ?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'), ?, ?, ?)"
        getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, usuario.nro_documento)
                stmt.setString(2, usuario.nombre)
                stmt.setString(3, usuario.apellido_paterno)
                stmt.setString(4, usuario.apellido_materno)
                stmt.setString(5, usuario.fecha_nacimiento)
                stmt.setString(6, usuario.correo)
                stmt.setString(7, usuario.direccion)
                stmt.setString(8, usuario.contrasena) // Idealmente, hashear antes de guardar
                stmt.executeUpdate()
            }
        }
    }

    /**
     * Verifica las credenciales de un usuario.
     * @param nroDni El DNI del usuario.
     * @param contrasena La contraseña del usuario.
     * @return Un objeto Usuario si las credenciales son correctas, de lo contrario null.
     */
    @Throws(SQLException::class)
    fun verificarUsuario(nroDni: String, contrasena: String): Usuario? {
        val sql = "SELECT * FROM usuarios WHERE nro_documento = ? AND contrasena = ?"
        getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, nroDni)
                stmt.setString(2, contrasena)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) {
                        return Usuario(
                            id = rs.getInt("id"),
                            nro_documento = rs.getString("nro_documento"),
                            nombre = rs.getString("nombre"),
                            apellido_paterno = rs.getString("apellido_paterno"),
                            apellido_materno = rs.getString("apellido_materno"),
                            fecha_nacimiento = rs.getDate("fecha_nacimiento").toString(),
                            correo = rs.getString("correo"),
                            direccion = rs.getString("direccion")
                        )
                    }
                }
            }
        }
        return null // Retorna null si no se encontró el usuario
    }

    /**
     * Obtiene todos los trámites disponibles de la base de datos.
     * @return Lista de trámites disponibles.
     */
    @Throws(SQLException::class)
    fun obtenerTramites(): List<Tramite> {
        val listaTramites = mutableListOf<Tramite>()
        val sql = "SELECT * FROM tramites ORDER BY nombre"

        getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.executeQuery().use { rs ->
                    while (rs.next()) {
                        val tramite = Tramite(
                            codigo = rs.getString("codigo"),
                            nombre = rs.getString("nombre"),
                            descripcion = rs.getString("descripcion"),
                            requisitos = rs.getString("requisitos") ?: "",
                            precio = rs.getDouble("precio"),
                            duracion_estimada = rs.getString("duracion_estimada"),
                            categoria = rs.getString("categoria")
                        )
                        listaTramites.add(tramite)
                    }
                }
            }
        }
        return listaTramites
    }

    /**
     * Obtiene un trámite específico por su código.
     * @param codigoTramite El código del trámite a buscar.
     * @return El objeto Tramite si se encuentra, de lo contrario null.
     */
    @Throws(SQLException::class)
    fun obtenerTramitePorCodigo(codigoTramite: String): Tramite? {
        val sql = "SELECT * FROM tramites WHERE codigo = ?"

        getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, codigoTramite)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) {
                        return Tramite(
                            codigo = rs.getString("codigo"),
                            nombre = rs.getString("nombre"),
                            descripcion = rs.getString("descripcion"),
                            requisitos = rs.getString("requisitos") ?: "",
                            precio = rs.getDouble("precio"),
                            duracion_estimada = rs.getString("duracion_estimada"),
                            categoria = rs.getString("categoria")
                        )
                    }
                }
            }
        }
        return null
    }

    /**
     * Busca trámites por nombre o descripción.
     * @param termino El término de búsqueda.
     * @return Lista de trámites que coinciden con la búsqueda.
     */
    @Throws(SQLException::class)
    fun buscarTramites(termino: String): List<Tramite> {
        val listaTramites = mutableListOf<Tramite>()
        val sql = "SELECT * FROM tramites WHERE LOWER(nombre) LIKE ? OR LOWER(descripcion) LIKE ? ORDER BY nombre"

        getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                val busqueda = "%${termino.lowercase()}%"
                stmt.setString(1, busqueda)
                stmt.setString(2, busqueda)
                stmt.executeQuery().use { rs ->
                    while (rs.next()) {
                        val tramite = Tramite(
                            codigo = rs.getString("codigo"),
                            nombre = rs.getString("nombre"),
                            descripcion = rs.getString("descripcion"),
                            requisitos = rs.getString("requisitos") ?: "",
                            precio = rs.getDouble("precio"),
                            duracion_estimada = rs.getString("duracion_estimada"),
                            categoria = rs.getString("categoria")
                        )
                        listaTramites.add(tramite)
                    }
                }
            }
        }
        return listaTramites
    }
}