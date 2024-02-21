package pt.isel.daw.gomoku.http.media

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import pt.isel.daw.gomoku.http.utils.MediaTypes.PROBLEM_MEDIA_TYPE
import java.net.URI

/**
 * Represents a problem that occurred in the server
 * @property type a URI that identifies the problem type
 * @property title the title the problem
 * @property detail the detail of the problem (optional)
 * @property instance a URI that identifies the location of the occurrence of the problem (optional)
 */
data class Problem(
    val type: URI,
    val title: String,
    val detail: String? = null,
    val instance: URI? = null
) {
    /**
     * Converts the problem to a response entity
     * @param status the status of the response
     * @param headers the headers of the response (optional)
     */
    fun toResponse(status: HttpStatusCode, headers: HttpHeaders? = null) =
        ResponseEntity
            .status(status)
            .header("Content-Type", PROBLEM_MEDIA_TYPE)
            .headers(headers)
            .body<Problem>(this)
}
