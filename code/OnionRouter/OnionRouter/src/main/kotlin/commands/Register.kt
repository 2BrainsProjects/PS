package commands

import http.HttpRequests

class Register(private val httpRequests: HttpRequests) : Command {
    override val id = 1

    override fun execute(args: List<String>) { // name, email, password, clientCSR
        httpRequests.registerClient(args[0], args[1], args[2], args[3])
    }
}
