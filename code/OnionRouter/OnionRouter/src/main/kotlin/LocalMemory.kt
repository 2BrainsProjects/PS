import com.google.gson.Gson
import domain.Contact
import domain.Message
import http.HttpRequests
import java.io.File
import java.util.*

class LocalMemory(private val httpRequests: HttpRequests, private val crypto: Crypto) {
    private val basePath = System.getProperty("user.dir") + "/client"
    private val pathConversation = "$basePath/conversations"
    private val gson = Gson()

    init {
        File(basePath).mkdirs()
        File(pathConversation).mkdirs()
    }

    /**
     * Creates the files if they don't exist and saves the messages in the files
     * criar as pastas
     * Verificar se existe memoria local
     * pedir as conversas:
     * Verificar se existe memoria local
     * construir o cid (formula)
     * getMessages(cid, logoutTimestamp)
     * guardar nos ficheiros
     * @param storageId the storage id
     * @param pwdHash the password hash
     * @param token the token
     * @param contacts the contacts
     */
    fun contactsFilesSetup(
        storageId: Int,
        pwdHash: String,
        token: String,
        contacts: List<Contact>,
    ) {
        val path = "$basePath/session$storageId.txt"
        val msgDate = getMsgDate(path, pwdHash)
        contacts.forEach {
            val cid = buildCid(storageId, it.id, pwdHash)
            val messages = httpRequests.getMessages(token, cid, msgDate)
            val file = File("$pathConversation/${it.name}.txt")
            file.createNewFile()
            messages.forEach { msg ->
                file.appendText(gson.toJson(msg) + "\n")
            }
        }
    }

    /**
     * Saves the messages from the files in the server
     * @param token the token
     * @param timestamp the timestamp
     * @param contacts the contacts
     */
    fun saveMessages(
        token: String,
        timestamp: String,
        contacts: List<Contact>,
    ) {
        val gson = Gson()
        contacts.forEach {
            val path = pathConversation + "/${it.name}.txt"
            val file = File(path)
            if (file.exists()) {
                val conversation = file.readLines().map { msg -> gson.fromJson(msg, Message::class.java) }
                val messages = conversation.filter { m -> m.timestamp > timestamp }
                messages.forEach { msg ->
                    httpRequests.saveMessage(token, msg.conversationId, msg.content, msg.timestamp)
                }
            }
        }
    }

    /**
     * Saves the session in the file
     * @param storageId the storage id
     * @param timestamp the timestamp
     * @param pwdHash the password hash
     */
    fun saveSession(
        storageId: Int,
        timestamp: String,
        pwdHash: String,
    ) {
        val file = File("$basePath/session$storageId.txt")
        file.delete()
        file.createNewFile()
        val text = "timestamp:$timestamp"
        val encryptText = crypto.encryptWithPwd(text, pwdHash)
        file.writeText(encryptText)
    }

    /**
     * Gets the session from the file if it exists otherwise, return null
     * @param storageId the storage id
     * @param pwdHash the password hash
     * @return the session or null
     */
    fun getSession(
        storageId: Int,
        pwdHash: String,
    ): String? {
        val path = "$basePath/session$storageId.txt"
        return getMsgDate(path, pwdHash)
    }

    /**
     * Gets the messages from the file if it exists otherwise, return an empty list
     * @param name the name of the contact
     * @param pwdHash the password hash
     * @return the list of messages
     */
    fun getMessages(
        name: String,
        pwdHash: String,
    ): List<Message> {
        val path = "$pathConversation/$name.txt"
        val file = File(path)
        return if (file.exists()) {
            file.readLines()
                .map {
                    val msg = gson.fromJson(it, Message::class.java)
                    val decryptContent = crypto.decryptWithPwd(String(Base64.getDecoder().decode(msg.content)), pwdHash)
                    msg.copy(content = decryptContent)
                }
        } else {
            file.createNewFile()
            emptyList()
        }
    }

    /**
     * Saves the message in the file
     * @param msg the message
     * @param pwdHash the password hash
     * @param name the name of the contact
     */
    fun saveMessageInFile(
        msg: Message,
        pwdHash: String,
        name: String,
    ) {
        val path = "$pathConversation/$name.txt"
        val file = File(path)
        file.createNewFile()
        val gson = Gson()
        val contentEncrypt = crypto.encryptWithPwd(msg.content, pwdHash)
        val content = Base64.getEncoder().encodeToString(contentEncrypt.toByteArray())
        val newMsg = Message(msg.conversationId, content, msg.timestamp)
        file.appendText(gson.toJson(newMsg) + "\n")
    }

    /**
     * Builds the conversation id
     * @param id1 the id of the user
     * @param id2 the id of the contact
     * @param pwdHash the password hash
     * @return the conversation id
     */
    fun buildCid(
        id1: Int,
        id2: Int,
        pwdHash: String,
    ): String {
        val cid = "$id1$id2${id1 * id2 + id1}"
        val encrypt = crypto.encryptWithPwd(cid, pwdHash)
        return Base64.getEncoder().encodeToString(encrypt.toByteArray())
    }

    /**
     * Deletes the conversation file
     * @param name the name of the contact
     */
    fun deleteConversation(name: String) {
        val file = File("$pathConversation/$name.txt")
        file.delete()
    }

    /**
     * Get the timestamp from the file if it exists otherwise, return null
     * @param path the path to the file
     * @param pwdHash the password hash
     * @return the timestamp or null
     */
    private fun getMsgDate(
        path: String,
        pwdHash: String,
    ): String? {
        val file = File(path)
        return if (file.exists()) {
            val contentFile = file.readText()
            val decryptContent = crypto.decryptWithPwd(contentFile, pwdHash)
            val timestamp =
                decryptContent.split("\n").firstOrNull { it.contains("timestamp") }
                    ?.replace("timestamp:", "")
            timestamp?.format()
        } else {
            file.createNewFile()
            null
        }
    }
}
