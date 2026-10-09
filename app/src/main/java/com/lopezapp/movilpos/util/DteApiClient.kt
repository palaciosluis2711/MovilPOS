package com.lopezapp.movilpos.util

import com.lopezapp.movilpos.data.model.DteEnvironment
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class AuthBody(
    val token: String? = null,
    val user: String? = null,
    val roles: List<String> = emptyList(),
    val tokenType: String? = null
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    val status: String = "OK",
    val token: String? = null,
    val body: AuthBody? = null
)

@JsonClass(generateAdapter = true)
data class ReceptionResponse(
    val estado: String = "",
    val selloRecibido: String? = null,
    val codigoGeneracion: String? = null,
    val observaciones: List<String> = emptyList()
)

private fun createDefaultOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
}

open class DteApiClient(
    private val okHttpClient: OkHttpClient = createDefaultOkHttpClient(),
    private val baseUrlOverride: String? = null
) {
    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val authResponseAdapter by lazy { moshi.adapter(AuthResponse::class.java) }
    private val receptionResponseAdapter by lazy { moshi.adapter(ReceptionResponse::class.java) }

    open fun authenticate(
        environment: DteEnvironment,
        nit: String,
        apiKey: String
    ): Result<String> {
        return runCatching {
            require(nit.isNotBlank()) { "El NIT no puede estar vacío" }
            require(apiKey.isNotBlank()) { "La clave de API (apiKey) no puede estar vacía" }

            val baseUrl = baseUrlOverride ?: getBaseUrl(environment)
            val url = "$baseUrl/gettoken"

            val jsonBody = """
                {
                    "user": "$nit",
                    "pwd": "$apiKey",
                    "nit": "$nit",
                    "apiKey": "$apiKey"
                }
            """.trimIndent()

            val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .header("Content-Type", "application/json")
                .header("User-Agent", "MovilPOS/1.0")
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBodyStr = response.body?.string()

            if (!response.isSuccessful || responseBodyStr.isNullOrBlank()) {
                throw Exception("Error de respuesta del servidor MH (${response.code}): ${responseBodyStr ?: "Respuesta vacía"}")
            }

            val authResponse = authResponseAdapter.fromJson(responseBodyStr)
                ?: throw Exception("No se pudo interpretar la respuesta de autenticación del MH")

            val token = authResponse.token?.takeIf { it.isNotBlank() }
                ?: authResponse.body?.token?.takeIf { it.isNotBlank() }
                ?: throw Exception("La respuesta del MH no contiene un token válido: $responseBodyStr")

            token
        }
    }

    open fun transmitDte(
        environment: DteEnvironment,
        token: String,
        signedJwsPayload: String,
        dteType: String,
        generationCode: String
    ): Result<ReceptionResponse> {
        return runCatching {
            require(token.isNotBlank()) { "El token de autenticación no puede estar vacío" }
            require(signedJwsPayload.isNotBlank()) { "El payload JWS firmado no puede estar vacío" }

            val baseUrl = baseUrlOverride ?: getBaseUrl(environment)
            val url = "$baseUrl/fesv/recepciondte"

            val ambienteCode = if (environment == DteEnvironment.PRODUCTION) "01" else "00"

            val jsonBody = """
                {
                    "ambiente": "$ambienteCode",
                    "idEnvio": 1,
                    "version": 1,
                    "tipoDte": "$dteType",
                    "documento": "$signedJwsPayload",
                    "codigoGeneracion": "$generationCode"
                }
            """.trimIndent()

            val formattedToken = if (token.startsWith("Bearer ", ignoreCase = true)) token else "Bearer $token"

            val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .header("Authorization", formattedToken)
                .header("Content-Type", "application/json")
                .header("User-Agent", "MovilPOS/1.0")
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBodyStr = response.body?.string()

            if (!response.isSuccessful || responseBodyStr.isNullOrBlank()) {
                throw Exception("Error al transmitir DTE al MH (${response.code}): ${responseBodyStr ?: "Respuesta vacía"}")
            }

            val receptionResponse = receptionResponseAdapter.fromJson(responseBodyStr)
                ?: throw Exception("No se pudo interpretar la respuesta de recepción del MH")

            receptionResponse
        }
    }

    open fun voidDte(
        environment: DteEnvironment,
        token: String,
        signedJwsPayload: String,
        generationCode: String
    ): Result<ReceptionResponse> {
        return runCatching {
            require(token.isNotBlank()) { "El token de autenticación no puede estar vacío" }
            require(signedJwsPayload.isNotBlank()) { "El payload JWS firmado no puede estar vacío" }

            val baseUrl = baseUrlOverride ?: getBaseUrl(environment)
            val url = "$baseUrl/fesv/anulardte"

            val ambienteCode = if (environment == DteEnvironment.PRODUCTION) "01" else "00"

            val jsonBody = """
                {
                    "ambiente": "$ambienteCode",
                    "idEnvio": 1,
                    "version": 2,
                    "documento": "$signedJwsPayload",
                    "codigoGeneracion": "$generationCode"
                }
            """.trimIndent()

            val formattedToken = if (token.startsWith("Bearer ", ignoreCase = true)) token else "Bearer $token"

            val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .header("Authorization", formattedToken)
                .header("Content-Type", "application/json")
                .header("User-Agent", "MovilPOS/1.0")
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBodyStr = response.body?.string()

            if (!response.isSuccessful || responseBodyStr.isNullOrBlank()) {
                throw Exception("Error al anular DTE en MH (${response.code}): ${responseBodyStr ?: "Respuesta vacía"}")
            }

            val receptionResponse = receptionResponseAdapter.fromJson(responseBodyStr)
                ?: throw Exception("No se pudo interpretar la respuesta de anulación del MH")

            receptionResponse
        }
    }

    companion object {
        const val SANDBOX_URL = "https://test.dte.gob.sv"
        const val PRODUCTION_URL = "https://factura.gob.sv"

        val defaultInstance: DteApiClient by lazy { DteApiClient() }

        fun authenticate(
            environment: DteEnvironment,
            nit: String,
            apiKey: String
        ): Result<String> = defaultInstance.authenticate(environment, nit, apiKey)

        fun transmitDte(
            environment: DteEnvironment,
            token: String,
            signedJwsPayload: String,
            dteType: String,
            generationCode: String
        ): Result<ReceptionResponse> = defaultInstance.transmitDte(environment, token, signedJwsPayload, dteType, generationCode)

        fun voidDte(
            environment: DteEnvironment,
            token: String,
            signedJwsPayload: String,
            generationCode: String
        ): Result<ReceptionResponse> = defaultInstance.voidDte(environment, token, signedJwsPayload, generationCode)

        fun getBaseUrl(environment: DteEnvironment): String {
            return when (environment) {
                DteEnvironment.SANDBOX -> SANDBOX_URL
                DteEnvironment.PRODUCTION -> PRODUCTION_URL
            }
        }
    }
}
