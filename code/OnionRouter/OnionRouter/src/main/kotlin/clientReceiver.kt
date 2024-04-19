import okhttp3.Response
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.charset.StandardCharsets

fun main() {
    // setup of a ServerSocket simulating the last client
    val path: String = System.getProperty("user.dir") + "\\crypto"
    val crypto = Crypto(path)
    val ip = InetSocketAddress(8083)
    println("running on port ${ip.port}")
    val lastClient = ServerSocketChannel.open().bind(ip)
    var socket : SocketChannel? = null

    val name = "testUser"
    val email = "testUser@gmail.com"
    val pwd = "password"

    // generating the CSR so the API use it to generate the user certificate
    val csr = crypto.generateClientCSR(ip.port, lastClient.localAddress.toString(), "password")
    val clientId = createClient(name, email, pwd, csr.joinToString ( "\n" ))

    // so recebe o ip no login

    println(clientId)

    val selector = Selector.open()

    // In this thread we start the process of handling accepted connections
    Thread{
        handleConnection(selector, ip.port)
    }.start()

    // And in this one, we keep accepting connections
    try {
        while(true) {
            socket = lastClient.accept()

            // register this socket on selector so we can select the sockets ready to read
            socket.configureBlocking(false)
            socket.register(selector, SelectionKey.OP_READ)

            // wakes up the selector from select() in handleConnection
            selector.wakeup()
        }
    } catch (e: Exception){
        println(e.message)
    } finally {
        lastClient.close()
        socket?.close()
    }
}

/**
 * Method to check if the selector has any Readable sockets
 * to iterate and read them.
 * @param selector - selector used to mark the sockets
 * @param port - port of the host to decipher the message
 */
private fun handleConnection(selector: Selector, port: Int) {
    while (true) {
        // check if any socket is ready to read
        var readyToRead = selector.select()

        if (readyToRead == 0) continue

        // select the sockets ready to read
        val keys = selector.selectedKeys()

        val iterator = keys.iterator()

        // iterate through all the selected sockets
        while (iterator.hasNext()) {
            val key = iterator.next()
            // remove the socket from the iterator so the socket can be marked again with Readable
            iterator.remove()

            // confirm if the socket is indeed Readable so we can cast to SocketChannel without Exception
            if (key.isReadable) {
                val client = key.channel() as SocketChannel

                // handle socket read
                readFromClient(client, port)

                // if all the marked sockets have been read, leave this cicle to wait for for
                if (--readyToRead == 0) break
            }
        }
    }
}

/**
 * Method to read from the client socket.
 * @param client - client socket to read from
 * @param port - port of the host to decypher the message
 * @return the message read from the client socket
 */
private fun readFromClient(client: SocketChannel, port: Int): String{
    val path: String = System.getProperty("user.dir") + "\\crypto"

    // create auxiliar buffer to read in chunks
    val buffer = ByteBuffer.allocate(1024)

    // clear the buffer from any possible trash it contains
    buffer.clear()
    var msg = ""
    var size: Int = client.read(buffer)
    if(size == -1) {
        client.close()
        return msg
    }
    while (size > 0) {
        val bbOutput = String(buffer.array(), 0, buffer.position(), StandardCharsets.UTF_8)

        // reset the buffer pointer to the starting position
        buffer.flip()
        msg += bbOutput

        // clears the last chunk from the buffer
        buffer.clear()
        size = client.read(buffer)
    }
    println("received message: $msg")
    println("deciphering message...")
    val decipherMsg = Crypto(path).decipher(msg, port)
    println("deciphered message: $decipherMsg")
    return decipherMsg
}

private fun createClient(name: String, email: String, pwd: String, csr: String): Int{
    val httpUtils = HttpUtils()
    val JSON = "application/json"
    val apiUri = "http://localhost:8080/api"
    val url = "$apiUri/users"

    val registerBody = httpUtils.createBody(hashMapOf("name" to name, "email" to email, "password" to pwd, "clientCSR" to csr))

    val registerRequest = httpUtils.createPostRequest(JSON, url, registerBody)
    val registerResponse: Response

    try {
        registerResponse = httpUtils.client.newCall(registerRequest).execute()
    } catch (e: Exception) {
        println(e.message)
        throw Exception("Error creating client")
    }

    if(registerResponse.code != 201) throw Exception("Error creating client")

    val responseBody = registerResponse.body?.string()

    val clientId = responseBody?.split(',')?.get(1)?.dropWhile { !it.isDigit() }?.takeWhile { it.isDigit() }?.toIntOrNull()

    require(clientId != null){ "Error creating router" }

    return clientId
}