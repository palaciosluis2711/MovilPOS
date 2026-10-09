package com.lopezapp.movilpos.util

import com.lopezapp.movilpos.data.model.DteEnvironment
import com.lopezapp.movilpos.data.model.ElectronicBillingConfig
import com.lopezapp.movilpos.data.model.InvoiceType
import com.lopezapp.movilpos.data.repository.AppRepository
import com.lopezapp.movilpos.ui.viewmodel.POSViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

@OptIn(ExperimentalCoroutinesApi::class)
class DteApiClientTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun authenticate_success_returnsBearerToken() {
        var capturedUrl = ""
        var capturedBodyStr = ""

        val mockClient = createMockOkHttpClient { request ->
            capturedUrl = request.url.toString()
            val buffer = Buffer()
            request.body?.writeTo(buffer)
            capturedBodyStr = buffer.readUtf8()

            val responseJson = """
                {
                    "status": "OK",
                    "token": "token_bearer_12345",
                    "body": {
                        "user": "06140101901015",
                        "roles": ["ROLE_USER"]
                    }
                }
            """.trimIndent()

            Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(responseJson.toResponseBody("application/json".toMediaType()))
                .build()
        }

        val apiClient = DteApiClient(okHttpClient = mockClient)

        val result = apiClient.authenticate(
            environment = DteEnvironment.SANDBOX,
            nit = "06140101901015",
            apiKey = "secret_api_key"
        )

        assertTrue("Autenticación debe ser exitosa", result.isSuccess)
        assertEquals("token_bearer_12345", result.getOrNull())
        assertTrue("La URL debe apuntar al endpoint /gettoken", capturedUrl.endsWith("/gettoken"))
        assertTrue("El cuerpo JSON debe incluir user y pwd", capturedBodyStr.contains("06140101901015") && capturedBodyStr.contains("secret_api_key"))
    }

    @Test
    fun authenticate_success_extractsTokenFromNestedBody() {
        val mockClient = createMockOkHttpClient { request ->
            val responseJson = """
                {
                    "status": "200",
                    "body": {
                        "token": "nested_body_token_777"
                    }
                }
            """.trimIndent()

            Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(responseJson.toResponseBody("application/json".toMediaType()))
                .build()
        }

        val apiClient = DteApiClient(okHttpClient = mockClient)

        val result = apiClient.authenticate(
            environment = DteEnvironment.PRODUCTION,
            nit = "06140101901015",
            apiKey = "prod_api_key"
        )

        assertTrue(result.isSuccess)
        assertEquals("nested_body_token_777", result.getOrNull())
    }

    @Test
    fun authenticate_failure_httpError() {
        val mockClient = createMockOkHttpClient { request ->
            Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(401)
                .message("Unauthorized")
                .body("""{"error": "Credenciales inválidas"}""".toResponseBody("application/json".toMediaType()))
                .build()
        }

        val apiClient = DteApiClient(okHttpClient = mockClient)

        val result = apiClient.authenticate(
            environment = DteEnvironment.SANDBOX,
            nit = "06140101901015",
            apiKey = "wrong_key"
        )

        assertTrue("Debe retornar failure ante HTTP 401", result.isFailure)
    }

    @Test
    fun authenticate_failure_blankArguments() {
        val apiClient = DteApiClient()

        val blankNitResult = apiClient.authenticate(DteEnvironment.SANDBOX, "", "api_key")
        assertTrue(blankNitResult.isFailure)

        val blankKeyResult = apiClient.authenticate(DteEnvironment.SANDBOX, "06140101901015", "")
        assertTrue(blankKeyResult.isFailure)
    }

    @Test
    fun transmitDte_success_returnsReceptionResponse() {
        var capturedAuthHeader = ""
        var capturedUrl = ""

        val mockClient = createMockOkHttpClient { request ->
            capturedUrl = request.url.toString()
            capturedAuthHeader = request.header("Authorization") ?: ""

            val responseJson = """
                {
                    "estado": "PROCESADO",
                    "selloRecibido": "MH-2025-SELLO-9999",
                    "codigoGeneracion": "GEN-12345",
                    "observaciones": ["Ninguna"]
                }
            """.trimIndent()

            Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(responseJson.toResponseBody("application/json".toMediaType()))
                .build()
        }

        val apiClient = DteApiClient(okHttpClient = mockClient)

        val result = apiClient.transmitDte(
            environment = DteEnvironment.SANDBOX,
            token = "my_bearer_token",
            signedJwsPayload = "HEADER.PAYLOAD.SIGNATURE",
            dteType = "01",
            generationCode = "GEN-12345"
        )

        assertTrue("Transmisión DTE debe ser exitosa", result.isSuccess)
        val response = result.getOrThrow()
        assertEquals("PROCESADO", response.estado)
        assertEquals("MH-2025-SELLO-9999", response.selloRecibido)
        assertEquals("GEN-12345", response.codigoGeneracion)
        assertEquals(1, response.observaciones.size)
        assertTrue("El endpoint debe ser /fesv/recepciondte", capturedUrl.endsWith("/fesv/recepciondte"))
        assertEquals("Bearer my_bearer_token", capturedAuthHeader)
    }

    @Test
    fun transmitDte_failure_httpError() {
        val mockClient = createMockOkHttpClient { request ->
            Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(500)
                .message("Internal Server Error")
                .body("""{"error": "Error interno del MH"}""".toResponseBody("application/json".toMediaType()))
                .build()
        }

        val apiClient = DteApiClient(okHttpClient = mockClient)

        val result = apiClient.transmitDte(
            environment = DteEnvironment.SANDBOX,
            token = "token_123",
            signedJwsPayload = "signed_jws",
            dteType = "01",
            generationCode = "GEN-123"
        )

        assertTrue("Debe retornar failure ante error 500", result.isFailure)
    }

    @Test
    fun transmitDte_failure_blankArguments() {
        val apiClient = DteApiClient()

        val blankTokenResult = apiClient.transmitDte(
            environment = DteEnvironment.SANDBOX,
            token = "",
            signedJwsPayload = "signed_jws",
            dteType = "01",
            generationCode = "GEN-123"
        )
        assertTrue(blankTokenResult.isFailure)

        val blankJwsResult = apiClient.transmitDte(
            environment = DteEnvironment.SANDBOX,
            token = "token",
            signedJwsPayload = "",
            dteType = "01",
            generationCode = "GEN-123"
        )
        assertTrue(blankJwsResult.isFailure)
    }

    @Test
    fun getBaseUrl_sandboxAndProduction() {
        assertEquals("https://test.dte.gob.sv", DteApiClient.getBaseUrl(DteEnvironment.SANDBOX))
        assertEquals("https://factura.gob.sv", DteApiClient.getBaseUrl(DteEnvironment.PRODUCTION))
    }

    @Test
    fun posViewModel_integration_realDteTransmission_success() = runTest {
        val mockClient = createMockOkHttpClient { request ->
            val urlStr = request.url.toString()
            val responseJson = if (urlStr.contains("/gettoken")) {
                """{"status": "OK", "token": "real_auth_token_123"}"""
            } else {
                """{"estado": "PROCESADO", "selloRecibido": "MH-REAL-SELLO-123", "codigoGeneracion": "GEN-REAL-123"}"""
            }

            Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(responseJson.toResponseBody("application/json".toMediaType()))
                .build()
        }

        val apiClient = DteApiClient(okHttpClient = mockClient)
        val repository = AppRepository()
        val viewModel = POSViewModel(repository = repository, dteApiClient = apiClient)

        val certAlias = "test_alias"
        val certPassword = "CertPassword123!"
        val pkcs12Bytes = generateMockPkcs12Stream(certAlias, certPassword)

        repository.updateElectronicBillingConfig(
            ElectronicBillingConfig(
                isEnabled = true,
                environment = DteEnvironment.SANDBOX,
                nit = "06140101901015",
                apiToken = "real_api_key_valid",
                certificatePassword = certPassword,
                isSimulationMode = false
            )
        )

        viewModel.selectInvoiceType(InvoiceType.CONSUMIDOR_FINAL)
        val product = repository.products.value.first { !it.isService }
        viewModel.addToCart(product)
        testDispatcher.scheduler.advanceUntilIdle()

        var navigatedSaleId: String? = null
        viewModel.processSale(certificateInputStream = ByteArrayInputStream(pkcs12Bytes)) { saleId ->
            navigatedSaleId = saleId
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull("Debe navegar al recibo de venta", navigatedSaleId)
        val savedSale = repository.sales.value.find { it.id == navigatedSaleId }
        assertNotNull("La venta debe guardarse en el repositorio", savedSale)
        assertTrue(savedSale!!.isDteIssued)
        assertEquals("MH-REAL-SELLO-123", savedSale.dteReceptionSeal)
        assertEquals("01", savedSale.dteType)
    }

    @Test
    fun posViewModel_integration_simulatesDte_whenSimulationModeIsTrue() = runTest {
        val repository = AppRepository()
        val viewModel = POSViewModel(repository = repository)

        repository.updateElectronicBillingConfig(
            ElectronicBillingConfig(
                isEnabled = true,
                environment = DteEnvironment.SANDBOX,
                nit = "06140101901015",
                apiToken = "test" // test credentials -> trigger fallback simulation
            )
        )

        viewModel.selectInvoiceType(InvoiceType.CONSUMIDOR_FINAL)
        val product = repository.products.value.first { !it.isService }
        viewModel.addToCart(product)
        testDispatcher.scheduler.advanceUntilIdle()

        var navigatedSaleId: String? = null
        viewModel.processSale { saleId ->
            navigatedSaleId = saleId
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(navigatedSaleId)
        val savedSale = repository.sales.value.find { it.id == navigatedSaleId }
        assertNotNull(savedSale)
        assertTrue(savedSale!!.isDteIssued)
        assertTrue("El sello de simulación debe comenzar con MH-DTE-", savedSale.dteReceptionSeal?.startsWith("MH-DTE-") == true)
    }

    @Test
    fun posViewModel_integration_abortsRealDte_whenCredentialsAreTest() = runTest {
        val repository = AppRepository()
        val viewModel = POSViewModel(repository = repository)

        repository.updateElectronicBillingConfig(
            ElectronicBillingConfig(
                isEnabled = true,
                environment = DteEnvironment.SANDBOX,
                nit = "06140101901015",
                apiToken = "test", // test credentials -> should abort
                isSimulationMode = false
            )
        )

        viewModel.selectInvoiceType(InvoiceType.CONSUMIDOR_FINAL)
        val product = repository.products.value.first { !it.isService }
        viewModel.addToCart(product)
        testDispatcher.scheduler.advanceUntilIdle()

        var navigatedSaleId: String? = null
        viewModel.processSale { saleId ->
            navigatedSaleId = saleId
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull("La venta debe ser abortada", navigatedSaleId)
        val sales = repository.sales.value
        assertEquals("No debe agregarse una nueva venta, solo debe estar la inicial de prueba", 1, sales.size)
    }

    private fun createMockOkHttpClient(handler: (Request) -> Response): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                handler(chain.request())
            })
            .build()
    }

    private fun generateMockPkcs12Stream(alias: String, password: String): ByteArray {
        val keyPairGen = KeyPairGenerator.getInstance("RSA")
        keyPairGen.initialize(2048)
        val keyPair = keyPairGen.generateKeyPair()

        val cert = generateSelfSignedCertificate(keyPair)

        val keyStore = KeyStore.getInstance("PKCS12")
        keyStore.load(null, null)
        keyStore.setKeyEntry(
            alias,
            keyPair.private,
            password.toCharArray(),
            arrayOf(cert)
        )

        val baos = ByteArrayOutputStream()
        keyStore.store(baos, password.toCharArray())
        return baos.toByteArray()
    }

    private fun generateSelfSignedCertificate(keyPair: KeyPair): X509Certificate {
        fun derLen(len: Int): ByteArray {
            return when {
                len < 128 -> byteArrayOf(len.toByte())
                len < 256 -> byteArrayOf(0x81.toByte(), len.toByte())
                len < 65536 -> byteArrayOf(0x82.toByte(), (len shr 8).toByte(), len.toByte())
                else -> byteArrayOf(0x83.toByte(), (len shr 16).toByte(), (len shr 8).toByte(), len.toByte())
            }
        }

        fun derSeq(vararg elements: ByteArray): ByteArray {
            val totalLen = elements.sumOf { it.size }
            val baos = ByteArrayOutputStream()
            baos.write(0x30)
            baos.write(derLen(totalLen))
            for (el in elements) baos.write(el)
            return baos.toByteArray()
        }

        val version = byteArrayOf(0xA0.toByte(), 0x03, 0x02, 0x01, 0x02)
        val serial = byteArrayOf(0x02, 0x01, 0x01)
        val sigAlg = byteArrayOf(
            0x30, 0x0D,
            0x06, 0x09, 0x2A.toByte(), 0x86.toByte(), 0x48, 0x86.toByte(), 0xF7.toByte(), 0x0D, 0x01, 0x01, 0x0B,
            0x05, 0x00
        )
        val name = byteArrayOf(
            0x30, 0x0F,
            0x31, 0x0D,
            0x30, 0x0B,
            0x06, 0x03, 0x55, 0x04, 0x03,
            0x0C, 0x04, 'T'.code.toByte(), 'e'.code.toByte(), 's'.code.toByte(), 't'.code.toByte()
        )
        val notBefore = byteArrayOf(0x17, 0x0D) + "200101000000Z".toByteArray(Charsets.US_ASCII)
        val notAfter = byteArrayOf(0x17, 0x0D) + "300101000000Z".toByteArray(Charsets.US_ASCII)
        val validity = derSeq(notBefore, notAfter)

        val pubKeyInfo = keyPair.public.encoded

        val tbsCertificate = derSeq(version, serial, sigAlg, name, validity, name, pubKeyInfo)

        val signer = Signature.getInstance("SHA256withRSA")
        signer.initSign(keyPair.private)
        signer.update(tbsCertificate)
        val sigBytes = signer.sign()

        val sigBitStringBaos = ByteArrayOutputStream()
        sigBitStringBaos.write(0x03)
        sigBitStringBaos.write(derLen(sigBytes.size + 1))
        sigBitStringBaos.write(0x00)
        sigBitStringBaos.write(sigBytes)
        val sigBitString = sigBitStringBaos.toByteArray()

        val certDer = derSeq(tbsCertificate, sigAlg, sigBitString)

        val certFactory = CertificateFactory.getInstance("X.509")
        return certFactory.generateCertificate(ByteArrayInputStream(certDer)) as X509Certificate
    }
}
