package pt.isel.ps.anonichat.http.pipeline.authentication

import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import pt.isel.ps.anonichat.domain.utils.Ip

/**
 * Resolves the ip parameter from handlers
 */
@Component
class IpArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter) =
        parameter.parameterType == Ip::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Ip {
        val request = webRequest.getNativeRequest(HttpServletRequest::class.java)
            ?: throw IllegalStateException("HttpServletRequest not found")
        return getIp(request) ?: throw IllegalStateException("Ip not found")
    }

    companion object {
        private const val KEY = "IpArgumentResolver"

        fun addIp(ip: Ip, request: HttpServletRequest) = request.setAttribute(KEY, ip)
        fun getIp(request: HttpServletRequest): Ip? = request.getAttribute(KEY)?.let { it as? Ip }
    }
}
