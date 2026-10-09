package com.lopezapp.movilpos.util

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.UnrecoverableKeyException
import java.security.cert.X509Certificate

object JwsSigner {

    /**
     * Signs a DTE JSON payload using a PKCS12 certificate (.p12 / .pfx) and returns
     * a Compact JWS string (Header.Payload.Signature) signed with SHA256withRSA (RS256).
     */
    fun signDteJson(
        jsonPayload: String,
        certificateInputStream: InputStream,
        password: String,
    ): Result<String> {
        return runCatching {
            require(jsonPayload.isNotBlank()) { "El payload JSON no puede estar vacío" }

            val (privateKey, _) = extractKeyAndCertificate(certificateInputStream, password).getOrThrow()

            val headerJson = """{"alg":"RS256","typ":"JWT"}"""
            val encodedHeader = Base64Url.encode(headerJson.toByteArray(Charsets.UTF_8))
            val encodedPayload = Base64Url.encode(jsonPayload.toByteArray(Charsets.UTF_8))

            val signingInput = "$encodedHeader.$encodedPayload"

            val signature = Signature.getInstance("SHA256withRSA")
            signature.initSign(privateKey)
            signature.update(signingInput.toByteArray(Charsets.UTF_8))
            val rawSignatureBytes = signature.sign()

            val encodedSignature = Base64Url.encode(rawSignatureBytes)

            "$encodedHeader.$encodedPayload.$encodedSignature"
        }.recoverCatching { throwable ->
            val message = when {
                (throwable is IllegalArgumentException) || (throwable is IllegalStateException) ->
                    throwable.message ?: "Error al firmar el payload JSON"
                (throwable.cause is UnrecoverableKeyException) ||
                        (throwable is UnrecoverableKeyException) ||
                        (throwable.message?.contains("password", ignoreCase = true) == true) ||
                        (throwable.message?.contains("keystore", ignoreCase = true) == true) ->
                    "Contraseña de certificado PKCS12 incorrecta o formato de archivo inválido"
                else -> throwable.message ?: "Error al procesar el certificado digital PKCS12"
            }
            throw Exception(message, throwable)
        }
    }

    /**
     * Validates a PKCS12 certificate input stream with the provided password and returns
     * the X509Certificate if successful.
     */
    fun validateCertificate(
        certificateInputStream: InputStream,
        password: String,
    ): Result<X509Certificate> {
        return runCatching {
            val (_, certificate) = extractKeyAndCertificate(certificateInputStream, password).getOrThrow()
            certificate
        }.recoverCatching { throwable ->
            val message = when {
                (throwable is IllegalArgumentException) || (throwable is IllegalStateException) ->
                    throwable.message ?: "Certificado PKCS12 inválido"
                (throwable.cause is UnrecoverableKeyException) ||
                        (throwable is UnrecoverableKeyException) ||
                        (throwable.message?.contains("password", ignoreCase = true) == true) ||
                        (throwable.message?.contains("keystore", ignoreCase = true) == true) ->
                    "Contraseña de certificado PKCS12 incorrecta o archivo corrupto"
                else -> throwable.message ?: "Error al validar el certificado PKCS12"
            }
            throw Exception(message, throwable)
        }
    }

    /**
     * Verifies a Compact JWS signature against a given X509Certificate.
     */
    fun verifySignature(
        jwsCompact: String,
        certificate: X509Certificate,
    ): Result<Boolean> {
        return verifySignature(jwsCompact, certificate.publicKey)
    }

    /**
     * Verifies a Compact JWS signature against a given PublicKey.
     */
    fun verifySignature(
        jwsCompact: String,
        publicKey: PublicKey,
    ): Result<Boolean> {
        return runCatching {
            val parts = jwsCompact.split(".")
            require(parts.size == 3) { "Formato Compact JWS inválido: debe contener 3 partes separadas por punto" }

            val encodedHeader = parts[0]
            val encodedPayload = parts[1]
            val encodedSignature = parts[2]

            val signingInput = "$encodedHeader.$encodedPayload"
            val signatureBytes = Base64Url.decode(encodedSignature)

            val signature = Signature.getInstance("SHA256withRSA")
            signature.initVerify(publicKey)
            signature.update(signingInput.toByteArray(Charsets.UTF_8))
            signature.verify(signatureBytes)
        }
    }

    /**
     * Helper to decode payload from a Compact JWS string.
     */
    fun decodeJwsPayload(jwsCompact: String): Result<String> {
        return runCatching {
            val parts = jwsCompact.split(".")
            require(parts.size == 3) { "Formato Compact JWS inválido" }
            val payloadBytes = Base64Url.decode(parts[1])
            String(payloadBytes, Charsets.UTF_8)
        }
    }

    private fun extractKeyAndCertificate(
        certificateInputStream: InputStream,
        password: String,
    ): Result<Pair<PrivateKey, X509Certificate>> {
        return runCatching {
            val keyStore = KeyStore.getInstance("PKCS12")
            certificateInputStream.use { stream ->
                keyStore.load(stream, password.toCharArray())
            }

            var privateKey: PrivateKey? = null
            var x509Cert: X509Certificate? = null

            val aliases = keyStore.aliases()
            while (aliases.hasMoreElements()) {
                val alias = aliases.nextElement()
                val key = runCatching { keyStore.getKey(alias, password.toCharArray()) }.getOrNull()
                val cert = keyStore.getCertificate(alias)
                if ((key is PrivateKey) && (cert is X509Certificate)) {
                    privateKey = key
                    x509Cert = cert
                    break
                }
            }

            if (privateKey == null) {
                val fallbackAliases = keyStore.aliases()
                while (fallbackAliases.hasMoreElements()) {
                    val alias = fallbackAliases.nextElement()
                    val key = runCatching { keyStore.getKey(alias, password.toCharArray()) }.getOrNull()
                    if (key is PrivateKey) {
                        privateKey = key
                        break
                    }
                }
            }

            if (x509Cert == null) {
                val fallbackAliases = keyStore.aliases()
                while (fallbackAliases.hasMoreElements()) {
                    val alias = fallbackAliases.nextElement()
                    val cert = keyStore.getCertificate(alias)
                    if (cert is X509Certificate) {
                        x509Cert = cert
                        break
                    }
                    val chain = keyStore.getCertificateChain(alias)
                    if (!chain.isNullOrEmpty() && chain[0] is X509Certificate) {
                        x509Cert = chain[0] as X509Certificate
                        break
                    }
                }
            }

            val key = privateKey ?: throw IllegalStateException("No se encontró una clave privada en el certificado PKCS12")
            val cert = x509Cert ?: throw IllegalStateException("No se encontró un certificado X509 válido en el archivo PKCS12")

            Pair(key, cert)
        }
    }

    private object Base64Url {
        private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"

        fun encode(bytes: ByteArray): String {
            val sb = StringBuilder()
            var i = 0
            while (i < bytes.size) {
                val b0 = bytes[i++].toInt() and 0xFF
                val b1 = if (i < bytes.size) bytes[i++].toInt() and 0xFF else -1
                val b2 = if (i < bytes.size) bytes[i++].toInt() and 0xFF else -1

                val c0 = b0 shr 2
                val c1 = ((b0 and 0x03) shl 4) or (if (b1 >= 0) b1 shr 4 else 0)
                val c2 = if (b1 >= 0) ((b1 and 0x0F) shl 2) or (if (b2 >= 0) b2 shr 6 else 0) else -1
                val c3 = if (b2 >= 0) b2 and 0x3F else -1

                sb.append(ALPHABET[c0])
                sb.append(ALPHABET[c1])
                if (c2 >= 0) sb.append(ALPHABET[c2])
                if (c3 >= 0) sb.append(ALPHABET[c3])
            }
            return sb.toString()
        }

        fun decode(str: String): ByteArray {
            val decodeTable = IntArray(256) { -1 }
            for (idx in ALPHABET.indices) {
                decodeTable[ALPHABET[idx].code] = idx
            }
            decodeTable['+'.code] = 62
            decodeTable['/'.code] = 63

            val cleanStr = str.trimEnd('=')
            val baos = ByteArrayOutputStream()
            var i = 0
            while (i < cleanStr.length) {
                val c0 = decodeTable[cleanStr[i++].code]
                val c1 = if (i < cleanStr.length) decodeTable[cleanStr[i++].code] else 0
                val c2 = if (i < cleanStr.length) decodeTable[cleanStr[i++].code] else -1
                val c3 = if (i < cleanStr.length) decodeTable[cleanStr[i++].code] else -1

                if (c0 < 0 || c1 < 0) break

                val b0 = (c0 shl 2) or (c1 shr 4)
                baos.write(b0)

                if (c2 >= 0) {
                    val b1 = ((c1 and 0x0F) shl 4) or (c2 shr 2)
                    baos.write(b1)
                }
                if (c3 >= 0) {
                    val b2 = ((c2 and 0x03) shl 6) or c3
                    baos.write(b2)
                }
            }
            return baos.toByteArray()
        }
    }
}
