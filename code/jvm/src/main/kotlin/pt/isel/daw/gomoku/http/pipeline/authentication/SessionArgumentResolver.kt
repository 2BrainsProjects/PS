package pt.isel.daw.gomoku.http.pipeline.authentication

import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

/**
 * Resolves the session parameter from handlers
 */
@Component
class SessionArgumentResolver : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter) =
        parameter.parameterType == Session::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Session {
        val request = webRequest.getNativeRequest(HttpServletRequest::class.java)
            ?: throw IllegalStateException("HttpServletRequest not found")
        return getSession(request) ?: throw IllegalStateException("User not authenticated")
    }

    companion object {
        private const val KEY = "AuthenticatedUserArgumentResolver"

        fun addSession(session: Session, request: HttpServletRequest) = request.setAttribute(KEY, session)
        fun getSession(request: HttpServletRequest): Session? = request.getAttribute(KEY)?.let { it as? Session }
    }
}
