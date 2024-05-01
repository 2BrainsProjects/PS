package commands

import http.HttpRequests

class Login(private val httpRequests: HttpRequests) : Command {

    override fun execute(args: List<String>) { // nameOrEmail, ip, password
        val token = httpRequests.loginClient(args[0], args[1], args[2])
        // TODO: Save token
    }
}
