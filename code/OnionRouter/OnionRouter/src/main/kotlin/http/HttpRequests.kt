package http

import Crypto
import domain.Client
import domain.Router

class HttpRequests(private val crypto: Crypto = Crypto()) {
    private val httpUtils = HttpUtils()
    private val json = "application/json"
    private val apiUri = "http://localhost:8080/api"
    private val routerUrl = "$apiUri/routers"
    private val userUrl = "$apiUri/users"

    fun registerClient(
        name: String,
        email: String,
        password: String,
        clientCSR: String,
    ): Int {
        val registerResponse =
            httpUtils.postRequest(
                json,
                userUrl,
                hashMapOf("name" to name, "email" to email, "password" to password, "clientCSR" to clientCSR),
                "Error registering user",
            )

        if (registerResponse.code != 201) throw Exception("Error registering user")

        val responseBody = registerResponse.body?.string()
        requireNotNull(responseBody)

        val formattedBody = getFirstPropertiesStringFromId(responseBody)

        val clientId = getId(formattedBody)

        require(clientId != null) { "Error registering user" }

        return clientId
    }

    fun getClients(ids: List<Int>): List<Client> {
        val list = mutableListOf<Client>()
        val response =
            httpUtils.getRequest(
                json,
                "http://localhost:8080/api/users",
                hashMapOf("ids" to ids.joinToString(",")),
                "Users not found",
            )

        val body = response.body?.string()
        requireNotNull(body)
        val formattedBody = getPropertiesStringsFromId(body)

        formattedBody.forEach { se ->
            val id = getId(se)
            // Ip ta mal tenmos de corrigir
            val ip = se.split(',').first { it.contains("ip") }.dropWhile { !it.isDigit() && it != '[' }.dropLast(1)
            val name = getName(se)
            val certificateContent = getCertificate(se)
            val certificate = crypto.buildCertificate(certificateContent)
            if (id != null) list.add(Client(id, ip, name, certificate))
        }
        return list
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
                json,
                routerUrl,
                hashMapOf("routerCSR" to csr, "ip" to ip, "pwd" to pwd),
                "Error creating router",
            )

        if (registerResponse.code != 201) throw Exception("Error creating router")

        val responseBody = registerResponse.body?.string()
        requireNotNull(responseBody)
        val routerId = getId(responseBody)
        // val routerId = responseBody?.split(',')?.get(1)?.dropWhile { !it.isDigit() }?.takeWhile { it.isDigit() }?.toIntOrNull()

        requireNotNull(routerId) { "Error creating router" }

        return routerId
    }

    /**
     * This function makes a request to the API to get routers information
     * @param ids the ids of the routers to get
     * @return a list of routers
     */
    fun getRouters(ids: List<Int>): List<Router> {
        val nodesToConnect: MutableList<Router> = mutableListOf()
        val response = httpUtils.getRequest(json, routerUrl, hashMapOf("ids" to ids.joinToString(",")), "Error getting routers")

        val body = response.body?.string()
        requireNotNull(body)
        val formattedBody = getPropertiesStringsFromId(body)

        formattedBody.forEach { se ->
            val id = getId(se)
            val ip = se.split(',').first { it.contains("ip") }.dropWhile { !it.isDigit() && it != '[' }.dropLast(1)
            val certificateContent = getCertificate(se)
            val certificate = crypto.buildCertificate(certificateContent)
            if (id != null) nodesToConnect.add(Router(id, ip, certificate))
        }
        return nodesToConnect
    }

    fun getRouterCount(): Int {
        return getCount("$routerUrl/count")
    }

    fun getClientCount(): Int {
        return getCount("$userUrl/count")
    }

    private fun getCount(uri: String): Int {
        val response = httpUtils.getRequest(json, uri, null, "Error getting routers max id")

        val responseBody = response.body?.string()

        // Nao sei so depois de testar
        val routerCount = responseBody?.split(',')?.get(1)?.dropWhile { !it.isDigit() }?.takeWhile { it.isDigit() }?.toIntOrNull()
        println("countRouter: $routerCount")

        require(routerCount != null) { "Error getting router count" }

        return routerCount
    }

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
            json,
            "$routerUrl/$routerId",
            hashMapOf("pwd" to pwd),
            "Error deleting Router",
        )
    }
}
