package commands

import domain.UserStorage
import http.HttpRequests

class Logout(private val httpRequests: HttpRequests, private val clientStorage: UserStorage) : Command {
    override fun execute(args: List<String>) {
        httpRequests.logoutClient(args[0])  // token
        clientStorage.token = null
        //TODO("Enviar as msgs do User para a API")
    }
}
