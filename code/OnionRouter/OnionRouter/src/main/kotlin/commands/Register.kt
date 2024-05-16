package commands

import domain.Session
import http.HttpRequests

class Register(private val httpRequests: HttpRequests, private val userStorage: Session) : Command {
    override fun execute(args: List<String>) { // name, email, password, clientCSR, ip

        val id = httpRequests.registerClient(args[0], args[1], args[2], args[3])
        Login(httpRequests, userStorage).execute(listOf(args[0], args[4], args[2]))
    }
}