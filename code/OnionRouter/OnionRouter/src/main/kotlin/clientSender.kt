import http.HttpUtils
import okhttp3.Request
import okhttp3.Response
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

fun main() {
    clientSender(74, "hello", listOf(140, 141))
}

/**
 * Method to create a client sender where it creates a socket to send a message.
 * @param clientId - InetSocketAddress of the client sender
 * @param msg - message to send
 * @param nodes - list of nodes the message needs to pass through
 * @param certificatePath - path where the certificate will be saved
 * @return how many bytes have sent
 */
fun clientSender(
    clientId: Int,
    msg: String,
    nodes: List<Int>,
    certificatePath: String = System.getProperty("user.dir") + "\\crypto",
): Int {
    val nodesToConnect = mutableMapOf<Int, Pair<String, String>>()

    val routersResponse = getRoutersResponseBody(nodes)
    buildNodesToConnect(routersResponse, nodesToConnect)
    val serverIp = getServerIp(nodesToConnect, nodes)

    val socketChannel = SocketChannel.open(serverIp)

    val clientResponse = getClientResponseBody(listOf(clientId))
    val body = clientResponse.body?.string()
    requireNotNull(body)
    val formattedBody = body.split("properties").filter { it.contains("id") }[0]

    val id = formattedBody.split(',').first { it.contains("id") }.split(":").last()
    val ip = formattedBody.split(',').first { it.contains("ip") }.dropWhile { !it.isDigit() && it != '[' }.dropLast(1)
    val certificate = formattedBody.split(',').first { it.contains("certificate") }.dropWhile { it != '-' }.dropLastWhile { it != '-' }

    println(id)
    println(ip) // client ip written in login
    println(certificate)

    // extrair o clientAddr e clientPort do client do ip
    // val clientIp = InetSocketAddress(clientAddr, clientPort)
    val clientIp = InetSocketAddress("127.0.0.1", 8083)

    println(clientIp.address)

    try {
        val crypto = Crypto(certificatePath)
        // possuir clientIp: InetSocketAddress

        // basic version of sender does not have CSR
        // generate the CSR to register in the API as a client
        // val csrOutput = crypto.generateClientCSR(hostIp.port, hostIp.toString(), pwd)
        println("message: $msg")

        var finalMsg = msg // nickname: msg

        finalMsg = crypto.encipher(finalMsg, clientIp.port)
        finalMsg += "||${clientIp.address.toString().drop(1)}:${clientIp.port}"
        println(finalMsg)
        // reverse the nodes list to facilitate the user, so he just have to build the message path in order
        for (i in nodes.size - 1 downTo 1) {
            val node = nodesToConnect[nodes[i]]
            val ip = node?.first
            // construir certificado com o que vem da api e com o port send o id da api
            val port = ip?.split(":")?.last()?.toInt() ?: -1
            finalMsg = crypto.encipher(finalMsg, port)
            finalMsg += "||$ip"
            println(finalMsg)
        }

        finalMsg = crypto.encipher(finalMsg, serverIp.port)
        println(finalMsg)

        var bytesWritten: Int
        // writing to socket
        socketChannel.use {
            val output = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)
            output.clear()
            output.put(finalMsg.toByteArray(Charsets.UTF_8))
            output.flip() // reset the buffer position to forward data
            bytesWritten = socketChannel.write(output)
            output.clear()
        }
        return bytesWritten
    } finally {
        socketChannel.close()
    }
}

private fun buildNodesToConnect(
    routersResponse: Response,
    nodesToConnect: MutableMap<Int, Pair<String, String>>,
) {
    val body = routersResponse.body?.string()
    requireNotNull(body)
    val formattedBody = body.split("properties").filter { it.contains("id") }

    formattedBody.forEach { se ->
        val id = se.split(',').first { it.contains("id") }.split(":").last()
        val ip = se.split(',').first { it.contains("ip") }.dropWhile { !it.isDigit() && it != '[' }.dropLast(1)
        val certificate = se.split(',').first { it.contains("certificate") }.dropWhile { it != '-' }.dropLastWhile { it != '-' }
        nodesToConnect[id.toInt()] = Pair(ip, certificate)
    }
}

private fun getServerIp(
    nodesToConnect: MutableMap<Int, Pair<String, String>>,
    nodes: List<Int>,
): InetSocketAddress {
    val firstNode = nodesToConnect[nodes[0]]?.first
    val serverAddr = firstNode?.dropLastWhile { it != ':' }?.dropLast(1)
    val serverPort = firstNode?.takeLastWhile { it != ':' }?.toIntOrNull() ?: -1

    nodesToConnect.remove(nodes[0])

    return InetSocketAddress(serverAddr, serverPort)
}

private fun getRequest(
    uri: String,
    query: HashMap<String, String>,
): Response {
    val httpUtils = HttpUtils()

    val request =
        createGetRequest(
            httpUtils.json,
            uri,
            query,
        )

    try {
        return httpUtils.client.newCall(request).execute()
    } catch (e: Exception) {
        println(e.message)
        throw Exception("Error creating router")
    }
}

private fun createGetRequest(
    mediaType: String,
    url: String,
    query: HashMap<String, String>? = null,
): Request {
    val finalUrl =
        if (query != null) {
            url + "?" + query.map { (k, v) -> "$k=$v" }.joinToString("&")
        } else {
            url
        }
    return Request.Builder()
        .header("Content-Type", mediaType)
        .url(finalUrl)
        .get()
        .build()
}

private fun getRoutersResponseBody(ids: List<Int>) =
    getRequest("http://localhost:8080/api/routers", hashMapOf("ids" to ids.joinToString(",")))

private fun getClientResponseBody(ids: List<Int>) = getRequest("http://localhost:8080/api/users", hashMapOf("ids" to ids.joinToString(",")))
