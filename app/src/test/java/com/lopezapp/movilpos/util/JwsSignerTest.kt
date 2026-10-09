package com.lopezapp.movilpos.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

class JwsSignerTest {

    private val samplePassword = "SecretPassword123!"
    private val sampleAlias = "dte_test_cert"
    private val sampleDteJson = """
        {
            "identificacion": {
                "version": 1,
                "ambiente": "00",
                "tipoDte": "01",
                "numeroControl": "DTE-01-12345678-000000000000001",
                "codigoGeneracion": "01234567-89AB-CDEF-0123-456789ABCDEF"
            },
            "emisor": {
                "nit": "0614-010190-101-5",
                "nombre": "EMPRESA DE PRUEBA S.A. DE C.V."
            },
            "resumen": {
                "totalPagar": 113.00
            }
        }
    """.trimIndent()

    @Test
    fun testSignDteJsonWithValidPkcs12() {
        val pkcs12Bytes = generateMockPkcs12Stream(sampleAlias, samplePassword)

        val result = JwsSigner.signDteJson(
            jsonPayload = sampleDteJson,
            certificateInputStream = ByteArrayInputStream(pkcs12Bytes),
            password = samplePassword,
        )

        assertTrue("El resultado de firma debe ser exitoso", result.isSuccess)
        val jwsCompact = result.getOrThrow()
        val parts = jwsCompact.split(".")
        assertEquals("El JWS Compact debe tener exactamente 3 partes", 3, parts.size)

        val decodedPayloadResult = JwsSigner.decodeJwsPayload(jwsCompact)
        assertTrue("Debe decodificar el payload JWS", decodedPayloadResult.isSuccess)
        assertEquals("El payload decodificado debe ser idéntico al JSON original", sampleDteJson, decodedPayloadResult.getOrThrow())
    }

    @Test
    fun testSignDteJsonWithInvalidPassword() {
        val pkcs12Bytes = generateMockPkcs12Stream(sampleAlias, samplePassword)

        val result = JwsSigner.signDteJson(
            jsonPayload = sampleDteJson,
            certificateInputStream = ByteArrayInputStream(pkcs12Bytes),
            password = "wrong_password_456",
        )

        assertTrue("El resultado debe ser de falla al usar contraseña incorrecta", result.isFailure)
        val exceptionMessage = result.exceptionOrNull()?.message ?: ""
        assertTrue(
            "El mensaje de excepción debe indicar error de contraseña o certificado",
            exceptionMessage.contains("Contraseña", ignoreCase = true) ||
                    exceptionMessage.contains("incorrecta", ignoreCase = true) ||
                    exceptionMessage.contains("inválido", ignoreCase = true),
        )
    }

    @Test
    fun testPayloadSignatureVerification() {
        val pkcs12Bytes = generateMockPkcs12Stream(sampleAlias, samplePassword)

        val certResult = JwsSigner.validateCertificate(
            certificateInputStream = ByteArrayInputStream(pkcs12Bytes),
            password = samplePassword,
        )
        assertTrue("La validación del certificado debe ser exitosa", certResult.isSuccess)
        val cert = certResult.getOrThrow()

        val signResult = JwsSigner.signDteJson(
            jsonPayload = sampleDteJson,
            certificateInputStream = ByteArrayInputStream(pkcs12Bytes),
            password = samplePassword,
        )
        assertTrue("La firma debe ser exitosa", signResult.isSuccess)
        val jwsCompact = signResult.getOrThrow()

        val verifyResult = JwsSigner.verifySignature(jwsCompact, cert)
        assertTrue("La verificación de firma debe ser exitosa para el JWS original", verifyResult.isSuccess)
        assertTrue("La firma debe ser válida", verifyResult.getOrThrow())

        val parts = jwsCompact.split(".").toMutableList()
        parts[1] = parts[1] + "tampered"
        val tamperedJws = parts.joinToString(".")

        val tamperedVerifyResult = JwsSigner.verifySignature(tamperedJws, cert)
        val isTamperedValid = tamperedVerifyResult.getOrDefault(defaultValue = false)
        assertFalse("Un JWS alterado debe fallar la verificación de firma", isTamperedValid)
    }

    @Test
    fun testValidateCertificateSuccessAndFailure() {
        val pkcs12Bytes = generateMockPkcs12Stream(sampleAlias, samplePassword)

        val successResult = JwsSigner.validateCertificate(
            certificateInputStream = ByteArrayInputStream(pkcs12Bytes),
            password = samplePassword,
        )
        assertTrue(successResult.isSuccess)
        val cert = successResult.getOrThrow()
        assertNotNull(cert)

        val failureResult = JwsSigner.validateCertificate(
            certificateInputStream = ByteArrayInputStream(pkcs12Bytes),
            password = "invalid_password",
        )
        assertTrue(failureResult.isFailure)
    }

    @Test
    fun testSignDteJsonWithEmptyPayload() {
        val pkcs12Bytes = generateMockPkcs12Stream(sampleAlias, samplePassword)

        val result = JwsSigner.signDteJson(
            jsonPayload = "",
            certificateInputStream = ByteArrayInputStream(pkcs12Bytes),
            password = samplePassword,
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun testCorruptedCertificateInputStream() {
        val corruptedBytes = "Este no es un archivo PKCS12".toByteArray(Charsets.UTF_8)

        val result = JwsSigner.signDteJson(
            jsonPayload = sampleDteJson,
            certificateInputStream = ByteArrayInputStream(corruptedBytes),
            password = samplePassword,
        )

        assertTrue(result.isFailure)
    }

    private fun generateMockPkcs12Stream(
        alias: String = "testAlias",
        password: String = "testPassword",
    ): ByteArray {
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
