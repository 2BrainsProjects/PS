package domain

import java.security.cert.X509Certificate

data class Router(val id: Int, val ip: String, val certificate: X509Certificate)
