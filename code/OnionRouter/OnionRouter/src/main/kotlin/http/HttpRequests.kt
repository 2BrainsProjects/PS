package http

import Crypto
import com.google.gson.Gson
import domain.Client
import domain.Router
import domain.Token
import http.siren.SirenEntity

fun main() {
    val crypto = Crypto()
    val name = "jnchuco"
    val pass = "password"
    val email = "jnchuco@gmail.com"
    val csr = crypto.generateClientCSR(1, "cn", pass).joinToString("\n")
    val h = HttpRequests(crypto)
    val token = h.loginClient(email, "123", pass).token
    val client = h.logoutClient(token) // h.registerClient(name, email, pass, csr)
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

    fun loginClient(
        nameOrEmail: String,
        ip: String,
        password: String,
    ): Token {
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
        println(siren)
        val token = siren.extractProperty<String>("token")
        val expiresIn = siren.extractProperty<Double>("expiresIn").toLong()

        return Token(token, expiresIn)
    }

    fun logoutClient(token: String): Boolean {
        val headers = hashMapOf("Content-Type" to json, "Authorization" to "Bearer $token")
        val logoutResponse = httpUtils.postRequest(headers, "$apiUri/logout", hashMapOf(), "Error logout in")

        return logoutResponse.isSuccessful
    }

    /**
     * This function makes a request to the API to get clients information
     * @param ids the ids of the clients to get
     * @return a list of clients
     */
    fun getClients(ids: List<Int>): List<Client> {
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
