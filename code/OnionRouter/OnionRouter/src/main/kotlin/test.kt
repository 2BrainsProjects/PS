import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

fun main(){
    println("started")
    val socketChannel = SocketChannel.open()
    socketChannel.connect(InetSocketAddress(8080))
    val lastClient = ServerSocketChannel.open()
    lastClient.socket().bind(InetSocketAddress(8082))
    val socket = lastClient.accept()

    val b = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)
    socketChannel.write(b.put("hello||127.0.0.1:8082".toByteArray(Charsets.UTF_8)))
    println("sent msg")
    socketChannel.close()

    val finalClientBuffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)

    println("reading")
    socket.read(finalClientBuffer)
    finalClientBuffer.flip()
    println(Charsets.UTF_8.decode(finalClientBuffer).toString())
    socket.close()
}