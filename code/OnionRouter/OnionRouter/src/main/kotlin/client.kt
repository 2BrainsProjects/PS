import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.charset.StandardCharsets

fun main() {
    val lastClient = ServerSocketChannel.open()
    lastClient.socket().bind(InetSocketAddress(8082))
    var socket : SocketChannel? = null

    val finalClientBuffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)
    finalClientBuffer.clear()

    try {
        while (true) {
            println("waiting to accept")
            socket = lastClient.accept()
            println("reading")
            socket.read(finalClientBuffer)
            val bbOutput = String(finalClientBuffer.array(), 0, finalClientBuffer.position(), StandardCharsets.UTF_8)
            finalClientBuffer.flip()
            println(bbOutput)
        }
    }catch (e: Exception){
        println(e.message)
    }finally {
        lastClient.close()
        socket?.close()
    }
}