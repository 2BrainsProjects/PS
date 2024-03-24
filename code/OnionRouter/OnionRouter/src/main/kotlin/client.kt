import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

fun main() {
    val lastClient = ServerSocketChannel.open()
    lastClient.socket().bind(InetSocketAddress(8083))
    var socket : SocketChannel? = null

    val finalClientBuffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)
    finalClientBuffer.clear()

    try {
        while (true) {
            println("waiting to accept")
            socket = lastClient.accept()
            println("reading")
            socket.read(finalClientBuffer)
            finalClientBuffer.flip()
            println(Charsets.UTF_8.decode(finalClientBuffer).toString())
        }
    }catch (e: Exception){
        println(e.message)
    }finally {
        lastClient.close()
        socket?.close()
    }
}