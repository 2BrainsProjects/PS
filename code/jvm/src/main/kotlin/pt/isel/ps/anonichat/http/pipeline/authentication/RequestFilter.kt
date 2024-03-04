package pt.isel.ps.anonichat.http.pipeline.authentication

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.annotation.WebFilter
import jakarta.servlet.http.HttpFilter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import java.io.IOException

@Component
@WebFilter("/login")
class IpFilter : HttpFilter() {
    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {

        val ipAddress = request.remoteAddr
        request.setAttribute("ip", ipAddress)
        chain.doFilter(request, response)
    }
}