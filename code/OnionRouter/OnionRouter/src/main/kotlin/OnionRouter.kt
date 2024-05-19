import domain.ClientInformation
import http.HttpRequests
import sun.misc.Signal
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.ClosedByInterruptException
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import kotlin.system.exitProcess
/**
 * This class represents an Onion Router
 */
class OnionRouter(private val ip: InetSocketAddress, path: String = System.getProperty("user.dir") + "\\crypto") {
    private val timeout = 5000L
    private val selector = Selector.open()
    private val socketsList = emptyList<SocketChannel>().toMutableList()
    private val crypto = Crypto(path)
    private var status = 0
    private var command = ""
    private val httpRequests = HttpRequests(crypto)
    private val client = Client(crypto, httpRequests, ::sendMessage)

    init {
        println("onion router running on port $ip")
    }

    /**
     * This function starts the onion router
     * It creates a server socket, generates a certificate signing request and creates the onion router
     * It also creates a thread to handle the connection with the client
     * and a thread to accept new clients
     * Has support to interruption of the program
     */
    fun start() {
        val sSocket = ServerSocketChannel.open().bind(ip)
        val addrString = sSocket.localAddress.toString().drop(1)

        val th =
            Thread {
                try {
                    while (true) {
                        val clientSocket = sSocket.accept()
                        clientSocket.configureBlocking(false)
                        clientSocket.register(selector, SelectionKey.OP_READ)
                        socketsList.add(clientSocket)
                        selector.wakeup()
                    }
                } catch (_: ClosedByInterruptException) {
                    // ignore, this exception is thrown when the program ends
                    // so it kills both working threads, this one being the master
                } catch (e: Exception) {
                    status = 1
                }
            }
        th.start()

        try {
            Thread {
                handleConnection()
            }.start()

            Signal.handle(Signal("INT")) {
                th.interrupt()
                status = 1
                client.deleteNode()
                finalizeOnionRouter(sSocket, selector)
                exitProcess(0)
            }

            client.initializationMenu(addrString)
            if(client.getInfo().second != null)
                getInput()
        } catch (e: IOException) {
            println(e.message)
        } finally {
            th.interrupt()
            status = 1
            client.deleteNode()
            finalizeOnionRouter(sSocket, selector)
        }
    }

    private fun sendMessage(
        clientInformation: ClientInformation,
        msg: String,
        msgDate: String
    ): String {

        val path = client.buildMessagePath().map { Pair(it.ip, it.certificate) } + Pair(clientInformation.ip, clientInformation.certificate)
        val firstNodeIp = path.first().first

        val sender = this.client.getInfo().first
        val msgToSend = "final:${sender?.id}:${sender?.name}:$msg:${msgDate}"
        val encipherMsg = client.encipherMessage(msgToSend, path.reversed())

        putConnectionIfAbsent(firstNodeIp)
        val socket =
            socketsList.firstOrNull { // /127.0.0.1:8083
                it.remoteAddress.toString().contains(firstNodeIp)
            }

        if (socket != null) {
            val newMsgBytes = encipherMsg.toByteArray(Charsets.UTF_8)
            writeToClient(newMsgBytes, socket)
        }

        return msgToSend.replace("final:", "")
    }

    /**
     * Method to handle the input command
     * Possible future maintenance interface
     */
    private fun getInput() {
        while (true) {
            //println("Command:")
            //print(">")
            command = readln()
            when (command) {
                "exit" -> {
                    break
                }
                else -> {
                    println("Invalid command")
                }
            }
        }
    }

    /**
     * This function handles the connection with the client
     * The selector wake up when a new client connects and
     * reads the message from the client and processes it
     */
    private fun handleConnection() {
        while (status == 0) {
            var readyToRead = selector.select(timeout)

            if (readyToRead == 0) continue
            val keys = selector.selectedKeys()

            val iterator = keys.iterator()

            while (iterator.hasNext()) {
                val key = iterator.next()
                iterator.remove()

                if (key.isReadable) {
                    val client = key.channel() as SocketChannel

                    val msg = readFromClient(client, socketsList) ?: continue

                    println("Received message: $msg")
                    processMessage(msg)

                    if (--readyToRead == 0) break
                }
            }
        }
    }

    /**
     * This function processes the message received from the client.
     * It deciphers the message, extracts the address of the next node and sends the message to the next node
     * @param msg the message to be processed
     */
    private fun processMessage(msg: String) {
        println("processing...")
        if (msg.isBlank() || msg.isEmpty()) return
        println(msg)
        println(ip.port)
        val plainText = crypto.decipher(msg, ip.port)
        println("deciphered message: $plainText")

        //final:id:name:msg
        if(plainText.startsWith("final:")){
            readMsg(plainText)
            return
        }

        val addr = plainText.split("||").last() // onion || 234 325 345 234:4363
        val newMsg = plainText.dropLastWhile { it != '|' }.dropLast(2)
        val newMsgBytes = newMsg.toByteArray(Charsets.UTF_8)

        socketsList.forEach {
            if (!it.isOpen) socketsList.remove(it)
        }

        // verfificar se existe/estabelecer ligação ao nextNode
        putConnectionIfAbsent(addr)

        println("-------------------------------------------------------")

        // socket com o próximo node                           removing prefix '/'  e.g. /127.0.0.1
        val socket =
            socketsList.firstOrNull { // /127.0.0.1:8083
                it.remoteAddress.toString().contains(addr)
            }
        if (socket != null) {
            writeToClient(newMsgBytes, socket)
        }
    }

    private fun readMsg(msg:String){
        //final:id:name:msg
        val info = msg.split(":").drop(1)
        val id = info.first()
        val name = info[1]
        val message = info.drop(2).joinToString(":")
        println("$name,$id:$message")
        // se user tiver na conversa com $name então ele vê a msg
        // guardamos sempre essa mensagem no ficheiro $name.txt
    }

    /**
     * This function adds a connection to the list of sockets if it doesn't exist
     * @param addr the address of the connection to be added
     */
    private fun putConnectionIfAbsent(addr: String) {
        if (!addr.contains(":")) return

        if (!socketsList.any { it.remoteAddress.toString().contains(addr) }) {
            println("sending to: $addr")
            val splitAddr = addr.split(':')
            if (splitAddr.size != 2 && splitAddr.size != 9) return
            val newAddr = InetSocketAddress(addr.dropLastWhile { it != ':' }.dropLast(1), splitAddr.last().toInt())
            val nextNode = SocketChannel.open(newAddr)
            socketsList.add(nextNode)
        }
    }
}
