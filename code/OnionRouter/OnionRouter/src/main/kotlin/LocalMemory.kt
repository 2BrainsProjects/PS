import com.google.gson.Gson
import domain.Contact
import domain.Conversation
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
            val file = File(System.getProperty("user.dir") + "client/conversations/${it.name}.txt")
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
                val content = file.readLines().joinToString("\n")
                val conversation = gson.fromJson(content, Conversation::class.java)
                val messages = conversation.messages.filter { m -> m.timestamp.toLong() > timestamp.toLong() }
                messages.forEach{ msg ->
                    val t = Timestamp(msg.timestamp.toLong()).toString()
                    httpRequests.saveMessage(token, msg.conversationId, msg.content, t)
                }
            }
        }
    }

    fun saveSession(timestamp: String) {
        val file = File("$basePath/session.txt")
        file.writeText("timestamp: $timestamp")
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
            decryptContent.split("\n").firstOrNull{ it.contains("timestamp") }?.replace("timestamp: ", "")
        }else{
            file.createNewFile()
            null
        }
    }

    private fun createFolders(path: String) = File(path).mkdirs()

    private fun buildCid(id1: Int, id2: Int, pwdHash: String): String {
        val cid = "$id1$id2${id1*id2+id1}"
        return crypto.encryptWithPwd(cid, pwdHash)
    }
}