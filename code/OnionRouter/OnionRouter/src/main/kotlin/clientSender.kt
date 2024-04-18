import okhttp3.Response
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

fun main() {
    clientSender(InetSocketAddress("127.0.0.1" ,8083), "password", "hello", listOf(53,54))
}

/**
 * Method to create a client sender where it creates a socket to send a message.
 * @param clientIp - InetSocketAddress of the client sender
 * @param pwd - password of the client sender CSR
 * @param msg - message to send
 * @param nodes - list of nodes the message needs to pass through
 * @param certificatePath - path where the certificate will be saved
 * @return how many bytes have sent
 */
fun clientSender(
    clientIp: InetSocketAddress,
    pwd: String,
    msg: String,
    nodes: List<Int>,
    certificatePath: String = System.getProperty("user.dir") + "\\crypto"
): Int{
    val httpUtils = HttpUtils()
    val nodesToConnect = mutableMapOf<Int, Pair<String, String>>()

    val routersRequest = httpUtils
            .createGetRequest(
                httpUtils.JSON,
                httpUtils.apiUri + "/routers",
                hashMapOf("ids" to nodes.joinToString(","))
            )

    val routersResponse: Response

    try {
        routersResponse = httpUtils.client.newCall(routersRequest).execute()
    } catch (e: Exception) {
        println(e.message)
        throw Exception("Error creating router")
    }
    val body = routersResponse.body?.string()
    requireNotNull(body)

    val formattedBody = body.split("properties").filter { it.contains("id") }

    formattedBody.forEach { se ->
        val id = se.split(',').first { it.contains("id") }.split(":").last()
        val ip = se.split(',').first { it.contains("ip") }.dropWhile { !it.isDigit() && it != '[' }.dropLast(1)
        val certificate = se.split(',').first { it.contains("certificate") }.dropWhile { it != '-' }.dropLastWhile { it != '-' }
        nodesToConnect[id.toInt()] = Pair(ip, certificate)
    }

    val firstNode = nodesToConnect[nodes[0]]?.first
    val serverAddr = firstNode?.dropLastWhile { it != ':' }?.dropLast(1)
    val serverPort = firstNode?.takeLastWhile { it != ':' }?.toIntOrNull() ?: -1

    nodesToConnect.remove(nodes[0])

    val serverIp = InetSocketAddress(serverAddr, serverPort)

    val socketChannel = SocketChannel.open(serverIp)

    try {
        val crypto = Crypto(certificatePath)

        // generate the CSR to register in the API as a client
        val csrOutput = crypto.generateClientCSR(serverIp.port, serverIp.toString(), pwd)
        println("message: $msg")

        var finalMsg = msg

        finalMsg = crypto.encipher(finalMsg, clientIp.port)
        finalMsg += "||${clientIp.address.toString().drop(1)}:${clientIp.port}"
        println(finalMsg)
        // reverse the nodes list to facilitate the user, so he just have to build the message path in order
        for(i in nodes.size - 1 downTo 1){
            val node = nodesToConnect[nodes[i]]
            val ip = node?.first
            // construir certificado com o que vem da api e com o port send o id da api
            val port = ip?.split(":")?.last()?.toInt() ?: -1
            finalMsg = crypto.encipher(finalMsg, port)
            finalMsg += "||$ip"
            println(finalMsg)
        }

        finalMsg = crypto.encipher(finalMsg, serverPort)
        println(finalMsg)

        var bytesWritten: Int
        // writing to socket
        socketChannel.use {
            val output = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)
            output.clear()
            output.put(finalMsg.toByteArray(Charsets.UTF_8))
            output.flip()    // reset the buffer position to forward data
            bytesWritten = socketChannel.write(output)
            output.clear()
        }
        return bytesWritten
    } finally {
        socketChannel.close()
    }
}
