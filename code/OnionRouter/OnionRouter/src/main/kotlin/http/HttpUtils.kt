package http

import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class HttpUtils {
    val client = OkHttpClient()

    /**
     * This function creates a body for the request
     * @param fields the fields of the body
     * @return the body created
     */
    private fun createBody(fields: HashMap<String, String>): FormBody {
        val formBody = FormBody.Builder()
        fields.forEach { (k, v) -> formBody.add(k, v) }
        return formBody.build()
    }

    /**
     * This method makes a get request to the API
     * @param mediaType the media type of the request
     * @param uri the uri of the request
     * @param query the query of the request
     * @param lazyMessage the message to be shown if the request fails
     * @return the response of the request
     */
    fun getRequest(
        headers: HashMap<String, String>,
        uri: String,
        query: HashMap<String, String>? = null,
        lazyMessage: String,
    ): Response {
        val finalQuery = query?.let { "?" + query.map { (k, v) -> "$k=$v" }.joinToString("&") } ?: ""
        val request =
            createGetRequest(
                headers,
                uri,
                finalQuery,
            )
        return handleRequest(request, lazyMessage)
    }

    /**
     * This method makes a delete request to the API
     * @param mediaType the media type of the request
     * @param uri the uri of the request
     * @param query the query of the request
     * @param body the body of the request
     * @param lazyMessage the message to be shown if the request fails
     * @return the response of the request
     */
    fun deleteRequest(
        headers: HashMap<String, String>,
        uri: String,
        query: HashMap<String, String>?,
        lazyMessage: String,
    ): Response {
        val finalQuery = query?.let { "?" + query.map { (k, v) -> "$k=$v" }.joinToString("&") } ?: ""
        val request =
            createDeleteRequest(
                headers,
                uri,
                finalQuery,
            )
        return handleRequest(request, lazyMessage)
    }

    /**
     * This method makes a post request to the API
     * @param headers the media type of the request
     * @param uri the uri of the request
     * @param body the body of the request
     * @param lazyMessage the message to be shown if the request fails
     * @return the response of the request
     */
    fun postRequest(
        headers: HashMap<String, String>,
        uri: String,
        body: HashMap<String, String>,
        lazyMessage: String,
    ): Response {
        val formBody = createBody(body)
        val request =
            createPostRequest(
                headers,
                uri,
                formBody,
            )
        println("request done")
        return handleRequest(request, lazyMessage)
    }

    /**
     * This function handles the request
     * @param request the request to be handled
     * @param lazyMessage the message to be shown if the request fails
     * @return the response of the request
     */
    private fun handleRequest(
        request: Request,
        lazyMessage: String,
    ): Response {
        var body: String? = null
        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                body = response.body?.string()
                throw Exception(body)
            }
            return response
        } catch (e: Exception) {
            println(e.message)
            throw Exception(body ?: lazyMessage)
        }
    }

    /**
     * This function create a get request to the API
     * @param mediaType the media type of the request
     * @param url the url of the request
     * @param query the query of the request
     * @return the request created
     */
    private fun createGetRequest(
        headers: HashMap<String, String>,
        url: String,
        query: String,
    ): Request {
        val requestBuilder = Request.Builder()
        headers.forEach { (header, value) -> requestBuilder.header(header, value) }

        return requestBuilder
            .url(url + query)
            .get()
            .build()
    }

    /**
     * This function create a get request to the API
     * @param mediaType the media type of the request
     * @param url the url of the request
     * @param body the body of the request
     * @return the request created
     */
    private fun createPostRequest(
        headers: HashMap<String, String>,
        url: String,
        body: FormBody,
    ): Request {
        val requestBuilder = Request.Builder()
        headers.forEach { (header, value) -> requestBuilder.header(header, value) }

        return requestBuilder
            .url(url)
            .post(body)
            .build()
    }

    /**
     * This function create a delete request to the API
     * @param mediaType the media type of the request
     * @param url the url of the request
     * @param query the query of the request
     * @return the request created
     */
    private fun createDeleteRequest(
        headers: HashMap<String, String>,
        url: String,
        query: String,
    ): Request {
        val requestBuilder = Request.Builder()
        headers.forEach { (header, value) -> requestBuilder.header(header, value) }

        return requestBuilder
            .url(url + query)
            .delete()
            .build()
    }
}
