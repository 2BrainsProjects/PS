package pt.isel.ps.anonichat.http.pipeline.authentication

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import pt.isel.ps.anonichat.domain.utils.Ip

/**
 * Interceptor that handles the ip of the request
 */
@Component
class IpInterceptor : HandlerInterceptor {
    /**
     * Handles the ip of the request
     * @param request The request
     * @param response The response
     * @param handler The handler
     * @return true if the ip is found
     */
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler is HandlerMethod && handler.hasParameterType<Ip>()) {
            val ipAddress = request.remoteAddr
            IpArgumentResolver.addIp(Ip(ipAddress), request)
            return true
        }
        return true
    }

    /**
     * Checks if the handler method has a parameter of type [T]
     * @param T the type of the parameter
     * @return true if the handler method has a parameter of type [T], false otherwise
     */
    private inline fun <reified T : Any> HandlerMethod.hasParameterType() =
        methodParameters.any { it.parameterType == T::class.java }
}
