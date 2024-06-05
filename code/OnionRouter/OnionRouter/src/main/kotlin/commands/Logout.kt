package commands

import LocalMemory
import domain.Contact
import domain.Session
import domain.UserStorage
import http.HttpRequests
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Logout(
    private val httpRequests: HttpRequests,
    private val clientStorage: Session,
    private val localMemory: LocalMemory,
) : Command {
    override fun execute(args: List<String>) { // pwd, token, privateKey
        // Temos de ir buscar as mensagens
        val token = args[1]
        val pwdHash = args[0]
        val privateKey = args[2]
        val loginTimestamp = clientStorage.timestamp
        requireNotNull(loginTimestamp)
        localMemory.saveMessages(token, loginTimestamp, clientStorage.contacts)

        val id = clientStorage.id
        val name = clientStorage.name
        requireNotNull(id)
        requireNotNull(name)
        val storage = UserStorage(id, name, privateKey, clientStorage.contacts.map { Contact(it.id, it.name) })
        httpRequests.logoutClient(pwdHash, token, storage)
        clientStorage.token = null

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val current = LocalDateTime.now().format(formatter)
        localMemory.saveSession(id, current, pwdHash)
    }
}
