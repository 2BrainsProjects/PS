package commands

import domain.UserStorage
import http.HttpRequests

class Register(private val httpRequests: HttpRequests, private val userStorage: UserStorage) : Command {
    override fun execute(args: List<String>) { // name, email, password, clientCSR, ip

        val id = httpRequests.registerClient(args[0], args[1], args[2], args[3])
        val token = httpRequests.loginClient(args[0], args[4], args[2])
        userStorage.id = id
        userStorage.name = args[0]
        userStorage.token = token
    }
}