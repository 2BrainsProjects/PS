package commands

import LocalMemory
import domain.Contact
import domain.Session
import http.HttpRequests
import java.io.File

class Login(private val httpRequests: HttpRequests, private val clientStorage: Session, private val localMemory: LocalMemory) : Command {

    override fun execute(args: List<String>) { // nameOrEmail, ip, password
        val (token, storage) = httpRequests.loginClient(args[0], args[1], args[2])
        val pwdHash = args[2].hashCode().toString()
        if (storage == null){
            val c = httpRequests.getClient(token.token)
            clientStorage.id = c.id
            clientStorage.name = c.name
            clientStorage.contacts = emptyList<Contact>().toMutableList()
        } else{
            clientStorage.contacts = storage.contacts.toMutableList()
            clientStorage.id = storage.id
            clientStorage.name = storage.name
            
        }
        
        clientStorage.loginTimestamp = System.currentTimeMillis().toString()
        clientStorage.pwd = pwdHash
        clientStorage.token = token
        
        localMemory.contactsFilesSetup(clientStorage.id!!, pwdHash, token.token, clientStorage.contacts)
        //criar as pastas
        // Verificar se existe memoria local
        // pedir as conversas:
        // Verificar se existe memoria local
        // construir o cid (formula)
        // getMessages(cid, logoutTimestamp)
        // guardar nos ficheiros
    }
}