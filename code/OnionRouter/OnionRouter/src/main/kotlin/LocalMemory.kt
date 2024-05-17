import com.google.gson.Gson
import domain.Contact
import domain.Message
import http.HttpRequests
import java.io.File
import java.sql.Timestamp

class LocalMemory(private val httpRequests: HttpRequests, private val crypto: Crypto) {

    private val basePath = System.getProperty("user.dir") + "/client"
    private val pathConversation = "$basePath/conversations"

    /**
     * Creates the files if they don't exist and saves the messages in the files
     * @param storageId the storage id
     * @param pwdHash the password hash
     * @param token the token
     * @param contacts the contacts
     */
    fun contactsFilesSetup(storageId: Int, pwdHash: String, token: String, contacts: List<Contact>) {
        createFolders(pathConversation)
        // Verificar se existe memoria local
        // criar class que trate de guardar e ler ficheiros
        val path = "$basePath/session.txt"
        val msgDate = getMsgDate(path, pwdHash)
        contacts.forEach {
            val cid = buildCid(storageId, it.id, pwdHash)
            val messages = httpRequests.getMessages(token, cid, msgDate)
            val file = File("$pathConversation/${it.name}.txt")
            file.createNewFile()
            file.appendText(messages.joinToString("\n"))
        }
    }

    fun saveMessages(token: String, timestamp: String, contacts: List<Contact>) {
        val gson = Gson()
        contacts.forEach {
            val path = pathConversation +"/${it.name}.txt"
            val file = File(path)
            if(file.exists()){
                val conversation = file.readLines().map { msg-> gson.fromJson(msg, Message::class.java) }
                val messages = conversation.filter { m -> m.timestamp.toLong() > timestamp.toLong() }
                messages.forEach{ msg ->
                    val t = Timestamp(msg.timestamp.toLong()).toString()
                    httpRequests.saveMessage(token, msg.conversationId, msg.content, t)
                }
            }
        }
    }

    fun saveSession(timestamp: String, pwdHash: String) {
        val file = File("$basePath/session.txt")
        file.delete()
        file.createNewFile()
        val text = "timestamp: $timestamp"
        val encryptText = crypto.encryptWithPwd(text, pwdHash)
        file.writeText(encryptText)
    }

    fun getMessages(name: String, pwdHash: String): List<Message> {
        val path = "$pathConversation/$name.txt"
        val file = File(path)
        return if (file.exists()) {
            file.readLines()
                .map {
                    val msg = Gson().fromJson(it, Message::class.java)
                    val decryptContent = crypto.decryptWithPwd(msg.content, pwdHash)
                    msg.copy(content = decryptContent)
                }

        }else{
            file.createNewFile()
            emptyList()
        }
    }

    fun saveMessageInFile(msg: Message, pwdHash: String){
        val path = pathConversation +"/${msg.conversationId}.txt"
        val file = File(path)
        val gson = Gson()
        val contentEncrypt = crypto.encryptWithPwd(msg.content, pwdHash)
        val newMsg = Message(msg.conversationId, contentEncrypt, msg.timestamp)
        file.appendText(gson.toJson(newMsg) + "\n")
    }

    /**
     * Get the timestamp from the file if it exists otherwise, return null
     * @param path the path to the file
     * @param pwdHash the password hash
     * @return the timestamp or null
     */
    private fun getMsgDate(path: String, pwdHash: String): String? {
        val file = File(path)
        return if (file.exists()) {
            val contentFile = file.readLines().joinToString("\n")
            val decryptContent = crypto.decryptWithPwd(contentFile, pwdHash)
            val timestamp = decryptContent.split("\n").firstOrNull{ it.contains("timestamp") }?.replace("timestamp: ", "")
            timestamp
        }else{
            file.createNewFile()
            null
        }
    }

    private fun createFolders(path: String) = File(path).mkdirs()

    fun buildCid(id1: Int, id2: Int, pwdHash: String): String {
        val cid = "$id1$id2${id1*id2+id1}"
        return crypto.encryptWithPwd(cid, pwdHash)
    }
}