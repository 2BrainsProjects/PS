package domain

import java.security.cert.X509Certificate

data class Client(val id: Int, val ip: String, val name: String, val certificate: X509Certificate)

data class Token(val token: String, val expiresIn: Long)
