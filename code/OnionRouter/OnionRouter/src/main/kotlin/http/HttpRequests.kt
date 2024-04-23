package http

import Crypto
import domain.Client
import domain.Router
import okhttp3.Request
import okhttp3.Response

class HttpRequests(private val crypto: Crypto = Crypto()) {
    private val httpUtils = HttpUtils()
    private val json = "application/json"
    private val apiUri = "http://localhost:8080/api"
    private val routerUrl = "$apiUri/routers"
    private val userUrl = "$apiUri/users"

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
        val formattedBody = body.split("properties").filter { it.contains("id") }

        formattedBody.forEach { se ->
            val id = se.split(',').first { it.contains("id") }.split(":").last().toInt()
            val ip = se.split(',').first { it.contains("ip") }.dropWhile { !it.isDigit() && it != '[' }.dropLast(1)
            val name = se.split(',').first { it.contains("name") }
            val certificateContent = se.split(',').first { it.contains("certificate") }.dropWhile { it != '-' }.dropLastWhile { it != '-' }
            val certificate = crypto.buildCertificate(certificateContent)
            list.add(Client(id, ip, name, certificate))
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
    fun createOnionRouter(
        csr: String,
        ip: String,
        pwd: String,
    ): Int {
        val registerBody = httpUtils.createBody(hashMapOf("routerCSR" to csr, "ip" to ip, "pwd" to pwd))

        val registerRequest = httpUtils.createPostRequest(json, routerUrl, registerBody)
        val registerResponse: Response

        try {
            registerResponse = httpUtils.client.newCall(registerRequest).execute()
        } catch (e: Exception) {
            println(e.message)
            throw Exception("Error creating router")
        }

        if (registerResponse.code != 201) throw Exception("Error creating router")

        val responseBody = registerResponse.body?.string()

        val routerId = responseBody?.split(',')?.get(1)?.dropWhile { !it.isDigit() }?.takeWhile { it.isDigit() }?.toIntOrNull()

        require(routerId != null) { "Error creating router" }

        return routerId
    }

    fun getRouters(ids: List<Int>): List<Router> {
        val nodesToConnect: MutableList<Router> = mutableListOf()
        val response = httpUtils.getRequest(json, routerUrl, hashMapOf("ids" to ids.joinToString(",")), "Error getting routers")

        val body = response.body?.string()
        requireNotNull(body)
        val formattedBody = body.split("properties").filter { it.contains("id") }

        formattedBody.forEach { se ->
            val id = se.split(',').first { it.contains("id") }.split(":").last()
            val ip = se.split(',').first { it.contains("ip") }.dropWhile { !it.isDigit() && it != '[' }.dropLast(1)
            val certificateContent = se.split(',').first { it.contains("certificate") }.dropWhile { it != '-' }.dropLastWhile { it != '-' }
            val certificate = crypto.buildCertificate(certificateContent)
            nodesToConnect.add(Router(id.toInt(), ip, certificate))
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
        val deleteRequest =
            Request.Builder()
                .header("Content-Type", json)
                .url("$routerUrl/$routerId?pwd=$pwd")
                .delete()
                .build()

        httpUtils.client.newCall(deleteRequest).execute()
    }
}
