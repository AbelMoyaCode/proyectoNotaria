package com.ampn.proyecto_notaria.api.repositorios

import com.ampn.proyecto_notaria.api.RetrofitClient
import com.ampn.proyecto_notaria.api.modelos.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio para manejar operaciones de autenticación
 */
class AutenticacionRepositorio {

    private val service = RetrofitClient.autenticacionService

    /**
     * Registrar un nuevo usuario
     */
    suspend fun registrarUsuario(request: RegistroUsuarioRequest): Result<Usuario> {
        return withContext(Dispatchers.IO) {
            try {
                val response = service.registrarUsuario(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val usuario = response.body()?.data
                    if (usuario != null) {
                        Result.success(usuario)
                    } else {
                        Result.failure(Exception("Usuario vacío en la respuesta"))
                    }
                } else {
                    // Intentar obtener el mensaje de error de diferentes campos posibles
                    val errorBody = response.errorBody()?.string()
                    val mensaje = response.body()?.mensaje
                        ?: response.body()?.message
                        ?: response.body()?.error
                        ?: errorBody
                        ?: "Error desconocido (código ${response.code()})"

                    Result.failure(Exception(mensaje))
                }
            } catch (e: java.net.UnknownHostException) {
                Result.failure(Exception("No se puede conectar al servidor. Verifica tu conexión a internet."))
            } catch (e: java.net.SocketTimeoutException) {
                Result.failure(Exception("Tiempo de espera agotado. Intenta de nuevo."))
            } catch (e: java.net.ConnectException) {
                Result.failure(Exception("No se puede conectar al servidor. Verifica que el backend esté ejecutándose."))
            } catch (e: Exception) {
                Result.failure(Exception("Error: ${e.message ?: "Error desconocido"}"))
            }
        }
    }

    /**
     * Iniciar sesión
     */
    suspend fun login(correo: String, password: String): Result<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = LoginRequest(correo, password)
                val response = service.login(request)
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        Result.success(loginResponse)
                    } else {
                        Result.failure(Exception("Respuesta vacía"))
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        401 -> "Credenciales incorrectas"
                        400 -> "Datos incompletos"
                        else -> "Error en el servidor: ${response.code()}"
                    }
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Error de conexión: ${e.message}"))
            }
        }
    }

    /**
     * Obtener perfil del usuario autenticado
     */
    suspend fun obtenerPerfil(token: String): Result<Usuario> {
        return withContext(Dispatchers.IO) {
            try {
                val response = service.obtenerPerfil("Bearer $token")
                if (response.isSuccessful && response.body()?.success == true) {
                    val usuario = response.body()?.data
                    if (usuario != null) {
                        Result.success(usuario)
                    } else {
                        Result.failure(Exception("Usuario no encontrado"))
                    }
                } else {
                    Result.failure(Exception("Error al obtener perfil"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Actualizar perfil del usuario
     */
    suspend fun actualizarPerfil(token: String, usuario: Usuario): Result<Usuario> {
        return withContext(Dispatchers.IO) {
            try {
                val response = service.actualizarPerfil("Bearer $token", usuario)
                if (response.isSuccessful && response.body()?.success == true) {
                    val usuarioActualizado = response.body()?.data
                    if (usuarioActualizado != null) {
                        Result.success(usuarioActualizado)
                    } else {
                        Result.failure(Exception("Error al actualizar"))
                    }
                } else {
                    val mensaje = response.body()?.mensaje ?: response.body()?.message ?: response.body()?.error ?: "Error desconocido"
                    Result.failure(Exception(mensaje))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Cerrar sesión
     */
    suspend fun logout(token: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = service.logout("Bearer $token")
                if (response.isSuccessful && response.body()?.success == true) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("Error al cerrar sesión"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
