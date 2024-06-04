import java.net.InetSocketAddress

fun main(args: Array<String>) {
    /*require(args.size == 1) { "Usage: main <port>" }
    val port = args[0].toIntOrNull()
    requireNotNull(port) { "Port must be a number" }
    require(port != 8080) { "Port 8080 is reserved" }*/
    val port = 8082
    OnionRouter(InetSocketAddress("127.0.0.1", port)).start()
}
