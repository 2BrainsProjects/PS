package pt.isel.ps.anonichat.http.pipeline.authentication

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import pt.isel.ps.anonichat.domain.exceptions.UserException.UnauthorizedException
import kotlin.coroutines.ContinuationInterceptor.Key.equals

/**
 * Interceptor that handles the authentication of the user
 */
@Component
class AuthenticationInterceptor(
    private val requestTokenProcessor: RequestTokenProcessor
) : HandlerInterceptor {

    /**
     * Handles the authentication of the user
     * @param request The request
     * @param response The response
     * @param handler The handler
     * @return true if the user is authenticated, false otherwise
     * @throws UnauthorizedException if no token is provided or the token is invalid
     */
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler is HandlerMethod && handler.hasParameterType<Session>()) {
            val token = getToken(request) ?: throw UnauthorizedException("Missing user token")
            val session = requestTokenProcessor.getSession(token)
            return if (session == null) {
                throw UnauthorizedException("Invalid user token")
            } else {
                SessionArgumentResolver.addSession(session, request)
                true
            }
        }
        return true
    }

    /**
     * Gets the token from the request from either the cookie or the authorization header
     * @param request The request
     * @return The token
     */
    fun getToken(request: HttpServletRequest): String? {
        val tokenFromCookie = requestTokenProcessor.parseCookieHeader(request.cookies)
        if (tokenFromCookie != null) return tokenFromCookie
        val header = request.getHeader(AUTHORIZATION_HEADER)
        return requestTokenProcessor.parseAuthorizationHeader(header)
    }

    /**
     * Checks if the handler method has a parameter of type [T]
     * @param T the type of the parameter
     * @return true if the handler method has a parameter of type [T], false otherwise
     */
    private inline fun <reified T : Any> HandlerMethod.hasParameterType() =
        methodParameters.any { it.parameterType == T::class.java }

    companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
        const val WWW_AUTHENTICATE_HEADER = "WWW-Authenticate"
    }
}
