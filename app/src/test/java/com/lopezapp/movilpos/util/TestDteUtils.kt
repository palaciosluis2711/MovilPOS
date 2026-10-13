package com.lopezapp.movilpos.util

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

object TestDteUtils {
    fun createMockOkHttpClient(handler: (okhttp3.Request) -> Response): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                handler(chain.request())
            })
            .build()
    }

    fun generateMockPkcs12Stream(alias: String, password: String): ByteArray {
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
