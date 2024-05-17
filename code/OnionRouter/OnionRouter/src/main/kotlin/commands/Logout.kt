package commands

import LocalMemory
import domain.Contact
import domain.Session
import domain.UserStorage
import http.HttpRequests

class Logout(
    private val httpRequests: HttpRequests,
    private val clientStorage: Session,
    private val localMemory: LocalMemory
) : Command {
    override fun execute(args: List<String>) {  // pwd, token
        //Temos de ir buscar as mensagens
        val token = args[1]
        val pwd = args[0]
        val loginTimestamp = clientStorage.loginTimestamp
        requireNotNull(loginTimestamp)
        localMemory.saveMessages(token, loginTimestamp, clientStorage.contacts)

        val id = clientStorage.id
        val name = clientStorage.name
        requireNotNull(id)
        requireNotNull(name)
        val storage = UserStorage(id, name, clientStorage.contacts.map { Contact(it.id, it.name) })
        httpRequests.logoutClient(pwd, token, storage)
        clientStorage.token = null
        clientStorage.logoutTimestamp = System.currentTimeMillis().toString()

        localMemory.saveSession(clientStorage.logoutTimestamp!!)
    }
}
