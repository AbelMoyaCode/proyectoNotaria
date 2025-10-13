package com.ampn.proyecto_notaria.api

import com.ampn.proyecto_notaria.api.servicios.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit para consumir la API REST
 * Singleton para mantener una única instancia
 */
object RetrofitClient {

    // IMPORTANTE: Cambiar esta URL por la URL real de tu backend cuando esté desplegado
    // Para desarrollo local con emulador Android: usa "http://10.0.2.2:puerto"
    // Para desarrollo local con dispositivo físico: usa "http://TU_IP_LOCAL:puerto"
    private const val BASE_URL = "http://10.0.2.2:3000/api/" // Cambiar según tu configuración

    /**
     * Configuración de logging para debug
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * Cliente HTTP con configuración de timeouts y logging
     */
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Instancia de Retrofit configurada
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Servicio de Autenticación
     */
    val autenticacionService: AutenticacionService by lazy {
        retrofit.create(AutenticacionService::class.java)
    }

    /**
     * Servicio de Trámites
     */
    val tramitesService: TramitesService by lazy {
        retrofit.create(TramitesService::class.java)
    }

    /**
     * Servicio de Citas
     */
    val citasService: CitasService by lazy {
        retrofit.create(CitasService::class.java)
    }

    /**
     * Servicio de Notificaciones
     */
    val notificacionesService: NotificacionesService by lazy {
        retrofit.create(NotificacionesService::class.java)
    }
}

