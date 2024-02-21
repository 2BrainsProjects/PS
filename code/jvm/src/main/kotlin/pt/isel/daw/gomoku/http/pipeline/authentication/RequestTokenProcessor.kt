package pt.isel.daw.gomoku.http.pipeline.authentication

import jakarta.servlet.http.Cookie
import org.springframework.stereotype.Component
import pt.isel.daw.gomoku.services.UserService

/**
 * The processor responsible for extracting the session from the request
 */
@Component
class RequestTokenProcessor(
    val usersServices: UserService
) {

    /**
     * Gets the session from the authentication header
     * @param token the token provided in the authorization/cookie header
     * @return The session
     */
    fun getSession(token: String): Session? = usersServices.getUserByToken(token)?.let { user -> Session(user, token) }

    /**
     * Parses the token from value of the authentication header
     * @param value The value of the header
     * @return The token
     */
    internal fun parseAuthorizationHeader(value: String?): String? {
        if (value.isNullOrBlank()) return null
        return value.trim().split(" ").let {
            if (it.size != 2 || it[0].lowercase() != SCHEME) null else it[1]
        }
    }

    internal fun parseCookieHeader(cookies: Array<Cookie>?): String? {
        val cookie = cookies?.firstOrNull { it.name == TOKEN } ?: return null
        return cookie.value
    }

    companion object {
        const val TOKEN = "token"
        const val SCHEME = "bearer"
    }
}
