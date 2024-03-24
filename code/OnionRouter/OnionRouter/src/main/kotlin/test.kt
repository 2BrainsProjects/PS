import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

fun main(){
    println("started")
    val socketChannel = SocketChannel.open(InetSocketAddress(8080))

    val output = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)
    output.clear()
    output.put ("hello||127.0.0.1:8083||127.0.0.1:8082".toByteArray(Charsets.UTF_8))
    output.flip()    // reset the buffer position to forward data
    socketChannel.write(output)

    socketChannel.close()
}

// u1 -> on1 -> u2
// u1 -> on1 -> on2
