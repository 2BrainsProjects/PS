import testing.CertificateDomain

fun main() {
    val crypto = Crypto()
    val certificateDomain = CertificateDomain()

    val routerId = 8
    val port = 8081
    val msg = "hello"

    val csr = crypto.generateClientCSR(port, "router", "password")

    val certPath = certificateDomain.createCertCommand(csr.joinToString("\n"), routerId, System.getProperty("user.dir") + "\\crypto")

    val certContent = certificateDomain.readFile(certPath)

    val cert = crypto.buildCertificate(certContent)

    val encrypher = crypto.encipher(msg, cert)

    println(encrypher)

    val decrypher = crypto.decipher(encrypher, port)

    println(decrypher)

    // OnionRouter(InetSocketAddress("127.0.0.1", 8081)).start()
}
