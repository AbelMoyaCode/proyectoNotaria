package com.ampn.proyecto_notaria.api.servicios

import com.ampn.proyecto_notaria.api.modelos.ApiResponse
import com.ampn.proyecto_notaria.api.modelos.Cita
import com.ampn.proyecto_notaria.api.modelos.Tramite
import com.ampn.proyecto_notaria.api.modelos.Usuario
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

/**
 * Interfaz de Retrofit que define todos los endpoints de la API
 */
interface ServicioApi {

    // ==================== AUTENTICACIÓN ====================
    @POST("api/usuarios/register")
    suspend fun registrarUsuario(@Body body: Map<String, String>): Response<Usuario>

    @POST("api/usuarios/login")
    suspend fun iniciarSesion(@Body body: Map<String, String>): Response<Usuario>

    // ==================== TRÁMITES ====================
    @GET("api/tramites")
    suspend fun obtenerTramites(): Response<List<Tramite>>

    @GET("api/tramites/{codigo}")
    suspend fun obtenerTramitePorCodigo(@Path("codigo") codigo: String): Response<Tramite>

    // ==================== CITAS ====================
    @GET("api/citas/usuario/{usuarioId}")
    suspend fun obtenerCitasUsuario(@Path("usuarioId") usuarioId: String): Response<ApiResponse<List<Cita>>>

    @POST("api/citas")
    suspend fun crearCita(@Body body: Map<String, String>): Response<ApiResponse<Cita>>

    @PATCH("api/citas/{id}/cancelar")
    suspend fun cancelarCita(
        @Path("id") citaId: String,
        @Body body: Map<String, String>
    ): Response<ApiResponse<Cita>>

    @PATCH("api/citas/{id}/reprogramar")
    suspend fun reprogramarCita(
        @Path("id") citaId: String,
        @Body body: Map<String, String>
    ): Response<ApiResponse<Cita>>

    companion object {
        private const val BASE_URL = "http://10.0.2.2:3000/" // Para emulador Android

        /**
         * Crea una instancia del servicio API con Retrofit
         */
        fun crearServicio(): ServicioApi {
            // Interceptor para logging (útil para debugging)
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            // Cliente HTTP con timeout configurado
            val cliente = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            // Construir Retrofit
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(cliente)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(ServicioApi::class.java)
        }
    }
}
