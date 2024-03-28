import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.charset.StandardCharsets

fun main() {
    val lastClient = ServerSocketChannel.open()
    lastClient.socket().bind(InetSocketAddress(8082))
    var socket : SocketChannel? = null

    val selector = Selector.open()

    Thread{
        handleConnection(selector)
    }.start()
    try {
        while (true) {
            println("waiting to accept")
            socket = lastClient.accept()
            println("accepted")
            socket.configureBlocking(false)
            socket.register(selector, SelectionKey.OP_READ)
            selector.wakeup()
        }
    }catch (e: Exception){
        println(e.message)
    }finally {
        lastClient.close()
        socket?.close()
    }
}

private fun handleConnection(selector: Selector) {
    while (true) {
        var readyToRead = selector.select()

        if (readyToRead == 0) continue
        val keys = selector.selectedKeys()

        val iterator = keys.iterator()

        while (iterator.hasNext()) {
            val key = iterator.next()
            iterator.remove()

            if (key.isReadable) {
                val client = key.channel() as SocketChannel
                val buffer = ByteBuffer.allocate(1024)
                buffer.clear()
                var msg = ""
                var size:Int = client.read(buffer)
                if(size == -1) {
                    client.close()
                    continue
                }
                println("reading")
                while (size > 0) {
                    val bbOutput = String(buffer.array(), 0, buffer.position(), StandardCharsets.UTF_8)
                    buffer.flip()
                    msg += bbOutput
                    buffer.clear()
                    size = client.read(buffer)
                }

                println(msg)
                println("____________________")

                if (--readyToRead == 0) break
            }
        }
    }
}