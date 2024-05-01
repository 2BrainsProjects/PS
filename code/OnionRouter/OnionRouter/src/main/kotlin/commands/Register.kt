package commands

import http.HttpRequests

class Register(private val httpRequests: HttpRequests) : Command {
    override fun execute(args: List<String>) { // name, email, password, clientCSR

        val id = httpRequests.registerClient(args[0], args[1], args[2], args[3])
        println(id)
    }
}
