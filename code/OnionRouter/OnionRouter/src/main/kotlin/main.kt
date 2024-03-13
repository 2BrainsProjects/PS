import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

fun main(args: Array<String>) {
    require(args.size == 1) { "Missing port number" }
    val port = args.first().toIntOrNull()
    require(port != null) { "Invalid port" }
    require(port >= 0) { "Port must not be negative" }

    val sSocket = ServerSocketChannel.open()
    val selector = Selector.open()

    val sockets = emptyList<SocketChannel>().toMutableList()
    try {
        sSocket.socket().bind(InetSocketAddress(port))

        Thread{ handleConnection(sockets, selector) }.start()
        while(true){

            val clientSocket = sSocket.accept()

            clientSocket.configureBlocking(false)
            clientSocket.register(selector, SelectionKey.OP_READ)

            if(!sockets.contains(clientSocket)){
                sockets.add(clientSocket)
            }
        }

    } catch(e: IOException) {
        println(e.message)
    } finally {
        sockets.forEach { it.close() }
        sSocket.close()
        selector.close()
    }
}

private fun handleConnection(sockets: MutableList<SocketChannel>, selector: Selector) {
    while (true){
        selector.select()

    }
}