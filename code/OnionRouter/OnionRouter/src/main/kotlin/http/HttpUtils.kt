package http

import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class HttpUtils {
    val client = OkHttpClient()
    val json = "application/json"

    /**
     * This function creates a body for the request
     * @param fields the fields of the body
     * @return the body created
     */
    fun createBody(fields: HashMap<String, String>): FormBody {
        val formBody = FormBody.Builder()
        fields.forEach { (k, v) -> formBody.add(k, v) }
        return formBody.build()
    }

    fun getRequest(
        mediaType: String,
        uri: String,
        query: HashMap<String, String>? = null,
        lazyMessage: String,
    ): Response {
        val request =
            createGetRequest(
                mediaType,
                uri,
                query,
            )
        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw Exception(lazyMessage)
            return response
        } catch (e: Exception) {
            println(e.message)
            throw Exception(lazyMessage)
        }
    }

    /**
     * This function create a post request to the API
     * @param mediaType the media type of the request
     * @param url the url of the request
     * @param query the query of the request
     * @return the request created
     */
    private fun createGetRequest(
        mediaType: String,
        url: String,
        query: HashMap<String, String>? = null,
    ): Request {
        val finalUrl =
            if (query != null) {
                url + "?" + query.map { (k, v) -> "$k=$v" }.joinToString("&")
            } else {
                url
            }
        return Request.Builder()
            .header("Content-Type", mediaType)
            .url(finalUrl)
            .get()
            .build()
    }

    /**
     * This function create a post request to the API
     * @param mediaType the media type of the request
     * @param url the url of the request
     * @param body the body of the request
     * @return the request created
     */
    fun createPostRequest(
        mediaType: String,
        url: String,
        body: FormBody,
    ) = Request.Builder()
        .header("Content-Type", mediaType)
        .url(url)
        .post(body)
        .build()

    fun getRoutersResponseBody(ids: List<Int>) =
        getRequest(json, "http://localhost:8080/api/routers", hashMapOf("ids" to ids.joinToString(",")), "Router not found")
}
