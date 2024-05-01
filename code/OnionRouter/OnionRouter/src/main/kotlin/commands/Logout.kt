package commands

import http.HttpRequests

class Logout(private val httpRequests: HttpRequests) : Command {
    override fun execute(args: List<String>) {
        httpRequests.logoutClient(args[0]/*token*/)
        // Apagar o token do ficheiro
        // Enviar as msgs do User para a API
    }
}
