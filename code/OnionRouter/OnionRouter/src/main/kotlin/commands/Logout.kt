package commands

import domain.Contact
import domain.Session
import domain.UserStorage
import http.HttpRequests

class Logout(private val httpRequests: HttpRequests, private val clientStorage: Session) : Command {
    override fun execute(args: List<String>) {  // pwd, token
        //Todo: guardar as mensagens na api antes do logout
        val id = clientStorage.id
        val name = clientStorage.name
        requireNotNull(id){ "" }
        requireNotNull(name){ "" }
        val storage = UserStorage(id, name, clientStorage.contacts.map { Contact(it.id, it.name) })
        httpRequests.logoutClient(args[0], args[1], storage)
        clientStorage.token = null
        clientStorage.logoutTimestamp = System.currentTimeMillis().toString()

        TODO("guardar localmente o clientStorage")
    }
}

/*
{
    id:10,
    name:jose,
    contacts:[
        {
            id:32,
            name:joana
        },
        {
            id:35,
            name:diogo
        },
    ]
}
*/