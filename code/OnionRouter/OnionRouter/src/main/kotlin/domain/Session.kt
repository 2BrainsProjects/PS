package domain

data class Session(
    var id: Int? = null, // api
    var pwd: String? = null, // nada
    var name: String? = null, // api
    var token: Token? = null, // nada
    var timestamp: String? = null, // nada
    var contacts: MutableList<Contact> = mutableListOf(), // api
)
