import java.nio.ByteBuffer
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.charset.StandardCharsets

/**
 * This function reads the message from the client
 * @param client the socket to read the message from
 * @return the message read from the client
 */
fun readFromClient(
    client: SocketChannel,
    socketsList: MutableList<SocketChannel>,
): String? {
    val buffer = ByteBuffer.allocate(1024)
    buffer.clear()
    var msg = ""
    var size: Int = client.read(buffer)
    if (size == -1) {
        client.close()
        socketsList.removeIf { !it.isOpen }
        return null
    }
    while (size > 0) {
        val bbOutput = String(buffer.array(), 0, buffer.position(), StandardCharsets.UTF_8)
        msg += bbOutput
        buffer.flip()
        buffer.clear()
        size = client.read(buffer)
    }
    return msg
}

/**
 * This function writes the message to the client
 * @param msgBytes the message to be sent
 * @param socket the socket to send the message
 */
fun writeToClient(
    msgBytes: ByteArray,
    socket: SocketChannel,
) {
    var newMsgBytes = msgBytes
    val byteBuffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)
    var msgSize = newMsgBytes.size
    while (msgSize > 0) {
        // minimum size between msgSize and DEFAULT_BUFFER_SIZE
        val min = minOf(msgSize, DEFAULT_BUFFER_SIZE)
        val chunk = newMsgBytes.copyOf(min)
        byteBuffer.put(chunk)
        msgSize -= chunk.size
        newMsgBytes = newMsgBytes.drop(min).toByteArray()
        byteBuffer.flip()

        // enviar onion2
        socket.write(byteBuffer)
        byteBuffer.clear()
    }
}

/**
 * This function closes all the sockets, the server socket and selector
 * @param sSocket the server socket to be closed
 */
fun finalizeOnionRouter(
    sSocket: ServerSocketChannel,
    selector: Selector,
) {
    val keys = selector.keys()
    val iterator = keys.iterator()
    while (iterator.hasNext()) {
        val key = iterator.next()
        val client = key.channel() as SocketChannel
        client.close()
    }
    selector.close()
    sSocket.close()
}
