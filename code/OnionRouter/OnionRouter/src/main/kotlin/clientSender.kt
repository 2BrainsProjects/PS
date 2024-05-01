import domain.Router
import http.HttpRequests
import okhttp3.Response
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

fun main() {
    clientSender(1, "hello", listOf(20, 21))
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
    val crypto = Crypto(certificatePath)
    val httpRequest = HttpRequests(crypto)

    val nodesToConnect = httpRequest.getRouters(nodes).toMutableList()
    val serverIp = getServerIp(nodesToConnect, nodes)
    val socketChannel = SocketChannel.open(serverIp)

    val client = httpRequest.getClients(listOf(clientId)).first()

    // extrair o clientAddr e clientPort do client do ip
    val clientIp = client.ip

    try {
        // possuir clientIp: InetSocketAddress

        // basic version of sender does not have CSR
        // generate the CSR to register in the API as a client
        // val csrOutput = crypto.generateClientCSR(hostIp.port, hostIp.toString(), pwd)
        println("message: $msg")

        var finalMsg = msg // nickname: msg

        val port = clientIp.split(":").last().toInt()
        val address = clientIp.split(":").first()
        finalMsg = crypto.encipher(finalMsg, port)
        finalMsg += "||${address}:${port}"
        println(finalMsg)
        // reverse the nodes list to facilitate the user, so he just have to build the message path in order
        for (i in nodes.size - 1 downTo 1) {
            val node = nodesToConnect.firstOrNull { it.id == nodes[i] }
            requireNotNull(node)
            val ip = node.ip
            // construir certificado com o que vem da api e com o port send o id da api
            finalMsg = crypto.encipher(finalMsg, ip.split(":").last().toInt())
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
    nodesToConnect: MutableList<Router>,
    nodes: List<Int>,
): InetSocketAddress {
    val firstNode = nodesToConnect.first { it.id == nodes.first() }.ip
    val serverAddr = firstNode.dropLastWhile { it != ':' }.dropLast(1)
    val serverPort = firstNode.takeLastWhile { it != ':' }.toIntOrNull() ?: -1

    nodesToConnect.removeIf { it.id == nodes.first() }

    return InetSocketAddress(serverAddr, serverPort)
}