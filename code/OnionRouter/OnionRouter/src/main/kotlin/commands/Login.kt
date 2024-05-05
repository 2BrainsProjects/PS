package commands

import domain.UserStorage
import http.HttpRequests

class Login(private val httpRequests: HttpRequests, private val clientStorage: UserStorage) : Command {

    override fun execute(args: List<String>) { // nameOrEmail, ip, password
        val token = httpRequests.loginClient(args[0], args[1], args[2])
        val client = httpRequests.getClient(token.token)
        clientStorage.id = client.id
        clientStorage.name = client.name
        clientStorage.token = token
    }
}
