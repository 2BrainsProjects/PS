import java.net.InetSocketAddress

fun main(args: Array<String>) {
    require(args.size == 1) { "Usage: main <port>" }
    val ip = args[0]
    val port = args[1].toIntOrNull()
    requireNotNull(port) { "Port must be a number" }
    require(port != 8080) { "Port 8080 is reserved" }
   //val port = 80811
    OnionRouter(InetSocketAddress(ip, port)).start()
}
