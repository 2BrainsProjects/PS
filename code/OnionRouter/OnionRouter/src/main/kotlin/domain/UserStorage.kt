package domain

class UserStorage {
    var id: Int? = null
    var name: String? = null
    var token: Token? = null
    val client : MutableList<Client> = mutableListOf()
}