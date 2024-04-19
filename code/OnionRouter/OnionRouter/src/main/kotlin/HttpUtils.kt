import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class HttpUtils {
    val client = OkHttpClient()
    val JSON = "application/json"
    val apiUri = "http://localhost:8080/api"

    /**
     * This function creates a body for the request
     * @param fields the fields of the body
     * @return the body created
     */
    fun createBody(fields: HashMap<String, String>): FormBody {
        val formBody =  FormBody.Builder()
        fields.forEach { (k, v) -> formBody.add(k, v) }
        return formBody.build()
    }

    /**
     * This function create a post request to the API
     * @param mediaType the media type of the request
     * @param url the url of the request
     * @param body the body of the request
     * @return the request created
     */
    fun createGetRequest(mediaType: String, url: String, query: HashMap<String, String>) =
        Request.Builder()
            .header("Content-Type", mediaType)
            .url(url + "?" + query.map { (k, v) -> "$k=$v" }.joinToString("&"))
            .get()
            .build()

    /**
     * This function create a post request to the API
     * @param mediaType the media type of the request
     * @param url the url of the request
     * @param body the body of the request
     * @return the request created
     */
    fun createPostRequest(mediaType: String, url: String, body: FormBody) =
        Request.Builder()
            .header("Content-Type", mediaType)
            .url(url)
            .post(body)
            .build()
}
