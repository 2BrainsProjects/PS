import java.net.InetSocketAddress

fun main() {
    OnionRouter(InetSocketAddress("127.0.0.1", 8085)).start()
}
