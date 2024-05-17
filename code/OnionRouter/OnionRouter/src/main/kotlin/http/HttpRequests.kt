package http

import Crypto
import com.google.gson.Gson
import domain.*
import http.siren.*

fun main() {
    val crypto = Crypto()
    val name = "Joao"
    val pass = "Joao1234?"
    val csr = crypto.generateClientCSR(1, "cn", pass).joinToString("\n")
    val h = HttpRequests(crypto)
    val (token, _) = h.loginClient(name, "123", pass)
    println(token)
    val client = h.getClient(token.token) // h.registerClient(name, email, pass, csr)
    println(client)
}

class HttpRequests(private val crypto: Crypto = Crypto()) {
    private val httpUtils = HttpUtils()
    private val json = "application/json"
    private val apiUri = "http://localhost:8080/api"
    private val routerUrl = "$apiUri/routers"
    private val userUrl = "$apiUri/users"
    private val gson = Gson()

    /**
     * This function makes a request to the API to register a new client
     * @param name the name of the client
     * @param email the email of the client
     * @param password the password of the client
     * @param clientCSR the client certificate signing request
     * @return the id of the client created
     * @throws Exception if the request fails
     */
    fun registerClient(
        name: String,
        email: String,
        password: String,
        clientCSR: String,
    ): Int {
        val registerResponse =
            httpUtils.postRequest(
                hashMapOf("Content-Type" to json),
                userUrl,
                hashMapOf("name" to name, "email" to email, "password" to password, "clientCSR" to clientCSR),
                "Error registering user",
            )

        if (registerResponse.code != 201) throw Exception("Error registering user")
        val responseBody = registerResponse.body?.string()
        requireNotNull(responseBody)

        val siren = transformBodyToSiren(responseBody)
        val clientId = siren.extractProperty<Double>("userId").toInt()

        return clientId
    }

    /**
     * This function makes a request to the API to login a client
     * @param nameOrEmail the name or email of the client
     * @param ip the ip of the client
     * @param password the password of the client
     * @return the token of the client
     */
    fun loginClient(
        nameOrEmail: String,
        ip: String,
        password: String,
    ): Pair<Token, UserStorage?> {
        val body =
            if (nameOrEmail.contains("@")) {
                hashMapOf("email" to nameOrEmail, "ip" to ip, "password" to password)
            } else {
                hashMapOf("name" to nameOrEmail, "ip" to ip, "password" to password)
            }

        val loginResponse =
            httpUtils.postRequest(
                hashMapOf("Content-Type" to json),
                "$apiUri/login",
                body,
                "Error logging in",
            )

        val responseBody = loginResponse.body?.string()
        requireNotNull(responseBody)

        val siren = transformBodyToSiren(responseBody)
        val token = siren.extractProperty<String>("token")
        val expiresIn = siren.extractProperty<Double>("expiresIn").toLong()
        val sessionInfo = siren.extractProperty<String>("sessionInfo")

        val decryptedSessionInfo = crypto.decryptWithPwd(sessionInfo, password.hashCode().toString())
        val storage = gson.fromJson(decryptedSessionInfo, UserStorage::class.java)

        return Pair(Token(token, expiresIn), storage)
    }

    fun logoutClient(pwd: String, token: String, storage: UserStorage): Boolean {
        val gson = Gson().toJson(storage)
        val encryptedStorage = Crypto().encryptWithPwd(gson, pwd)
        val headers = hashMapOf("Content-Type" to json, "Authorization" to "Bearer $token")
        val body = hashMapOf("sessionInfo" to encryptedStorage)
        val logoutResponse = httpUtils.postRequest(headers, "$apiUri/logout", body, "Error logout in")

        return logoutResponse.isSuccessful
    }

    fun getClient(token: String): Client {
        val headers = hashMapOf("Content-Type" to json, "Authorization" to "Bearer $token")
        val response = httpUtils.getRequest(headers, "$apiUri/user", hashMapOf(), "Error getting user")
        val responseBody = response.body?.string()
        requireNotNull(responseBody)

        val client = transformBodyToSiren(responseBody).extractClient()
        return client
    }

    fun getMessages(token: String, cid: String, msgDate: String? = null): List<Message> {
        val headers = hashMapOf("Content-Type" to json, "Authorization" to "Bearer $token")
        val query = hashMapOf("cid" to cid, "msgDate" to msgDate.toString())
        val response = httpUtils.getRequest(headers, "$apiUri/messages", query,"Error getting messages")
        val body = response.body?.string()
        requireNotNull(body)

        val messages = transformBodyToSiren(body).extractMessages()
        return messages
    }

    fun saveMessage(
        token: String,
        cid: String,
        message: String,
        msgDate: String
    ): Boolean {
        val headers = hashMapOf("Content-Type" to json, "Authorization" to "Bearer $token")
        val body = hashMapOf("cid" to cid, "message" to message, "msgDate" to msgDate)
        val response = httpUtils.postRequest(headers, "$apiUri/messages", body, "Error saving message")

        return response.isSuccessful
    }

    /**
     * This function makes a request to the API to get clients information
     * @param ids the ids of the clients to get
     * @return a list of clients
     */
    fun getClients(ids: List<Int>): List<ClientInformation> {
        val response =
            httpUtils.getRequest(
                hashMapOf("Content-Type" to json),
                userUrl,
                hashMapOf("ids" to ids.joinToString(",")),
                "Users not found",
            )

        val body = response.body?.string()
        requireNotNull(body)

        val clients = transformBodyToSiren(body).extractClients(crypto)
        return clients
    }

    /**
     * This function makes a request to the API to create a new onion router
     * @param csr the certificate signing request
     * @param pwd the password of the router
     * @param ip the ip of the router
     * @return the id of the router created
     * @throws Exception if the request fails
     */
    fun registerOnionRouter(
        csr: String,
        ip: String,
        pwd: String,
    ): Int {
        val registerResponse =
            httpUtils.postRequest(
                hashMapOf("Content-Type" to json),
                routerUrl,
                hashMapOf("routerCSR" to csr, "ip" to ip, "pwd" to pwd),
                "Error creating router",
            )

        if (registerResponse.code != 201) throw Exception("Error creating router")

        val responseBody = registerResponse.body?.string()
        requireNotNull(responseBody)
        val siren = transformBodyToSiren(responseBody)
        val routerId = siren.extractProperty<Double>("routerId").toInt()

        return routerId
    }

    /**
     * This function makes a request to the API to get routers information
     * @param ids the ids of the routers to get
     * @return a list of routers
     */
    fun getRouters(ids: List<Int>): List<Router> {
        val response =
            httpUtils.getRequest(
                hashMapOf("Content-Type" to json),
                routerUrl,
                hashMapOf("ids" to ids.joinToString(",")),
                "Error getting routers",
            )

        val body = response.body?.string()
        requireNotNull(body)

        val routers = transformBodyToSiren(body).extractRouters(crypto)

        return routers
    }

    /**
     * This function makes a request to the API to get the number of routers
     * @return the number of routers
     */
    fun getRouterCount(): Int = getCount("$routerUrl/count")

    /**
     * This function makes a request to the API to get the number of clients
     * @return the number of clients
     */
    fun getClientCount(): Int = getCount("$userUrl/count")

    /**
     * This function sends a DELETE request to the API to remove the router from the list of routers
     * and then closes the server socket
     * @param routerId the id of the router to be removed
     * @param pwd the password of the router to be removed
     */
    fun deleteRouter(
        routerId: Int,
        pwd: String,
    ) {
        httpUtils.deleteRequest(
            hashMapOf("Content-Type" to json),
            "$routerUrl/$routerId",
            hashMapOf("pwd" to pwd),
            "Error deleting Router",
        )
    }

    /**
     * This function makes a request to the API to get the max id.
     * @param uri the uri to get the max id
     * @return the max id
     */
    private fun getCount(uri: String): Int {
        val response =
            httpUtils.getRequest(
                hashMapOf("Content-Type" to json),
                uri,
                null,
                "Error getting routers max id",
            )

        val responseBody = response.body?.string()
        requireNotNull(responseBody)
        val routerCount = transformBodyToSiren(responseBody).extractProperty<Double>("maxId").toInt()

        return routerCount
    }

    /**
     * This function transforms the body of the response to a SirenEntity
     * @param body the body of the response
     * @return the SirenEntity
     */
    private fun transformBodyToSiren(body: String): SirenEntity<*> = gson.fromJson(body, SirenEntity::class.java)
}
