import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

fun main(){
    println("started")
    val socketChannel = SocketChannel.open(InetSocketAddress(8080))
    val lastClient = ServerSocketChannel.open()
    lastClient.socket().bind(InetSocketAddress(8082))

    val output = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)
    output.put ("hello||127.0.0.1:8082".toByteArray())
    output.flip()    // reset the buffer position to forward data
    socketChannel.write(output)

    val finalClientBuffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)

    println("reading")
    val socket = lastClient.accept()
    socket.read(finalClientBuffer)
    finalClientBuffer.flip()
    println(Charsets.UTF_8.decode(finalClientBuffer).toString())

    lastClient.close()
    socket.close()
    socketChannel.close()
}