package commands

import domain.Session
import http.HttpRequests
import java.io.File

class Login(private val httpRequests: HttpRequests, private val clientStorage: Session) : Command {

    override fun execute(args: List<String>) { // nameOrEmail, ip, password
        val (token, storage) = httpRequests.loginClient(args[0], args[1], args[2])
        clientStorage.loginTimestamp = System.currentTimeMillis().toString()
        clientStorage.id = storage.id
        val pwdHash = args[2].hashCode().toString()
        clientStorage.pwd = pwdHash
        clientStorage.name = storage.name
        clientStorage.token = token
        val contacts = storage.contacts
        clientStorage.contacts = contacts.toMutableList()

        //criar as pastas
        File(System.getProperty("user.dir") + "client/conversations").mkdirs()

        // Verificar se existe memoria local
        // criar class que trate de guardar e ler ficheiros (?)
        val file = File(System.getProperty("user.dir") + "client/session.txt")
        val msgDate = if (file.exists()) {
            val contentFile = file.readLines().joinToString("\n")
            val decryptContent = crypto.decryptWithPwd(contentFile, args[2].hashCode().toString())
            decryptContent.split("\n").firstOrNull{ it.contains("timestamp") }?.replace("timestamp: ", "")
        }else{
            file.createNewFile()
            null
        }

        contacts.forEach {
            val cid = buildCid(storage.id, it.id, pwdHash)
            val messages = httpRequests.getMessages(token.token, cid, msgDate)
            val file = File(System.getProperty("user.dir") + "client/conversations/${it.name}.txt")
            file.createNewFile()
            file.writeText(messages.joinToString("\n"))
        }

        // pedir as conversas:
        // Verificar se existe memoria local
        // construir o cid (formula)
        // getMessages(cid, logoutTimestamp)
        // guardar nos ficheiros
    }

    private fun buildCid(id1: Int, id2: Int, pwdHash: String): String {
        val cid = "$id1$id2${id1*id2+id1}"
        return crypto.encryptWithPwd(cid, pwdHash)
    }
}