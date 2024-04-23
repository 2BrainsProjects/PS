import java.net.InetSocketAddress

fun main() {
    OnionRouter(InetSocketAddress(8082)).start()
}
