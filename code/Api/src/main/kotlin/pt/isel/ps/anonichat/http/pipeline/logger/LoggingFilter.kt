package pt.isel.ps.anonichat.http.pipeline.logger

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import pt.isel.ps.anonichat.http.pipeline.logger.Logger.logger

/**
 * The filter responsible for logging the requests and responses
 */
@Component
class LoggingFilter : Filter {

    /**
     * Logs the request, calls the next filter and logs the response
     */
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        request as HttpServletRequest
        response as HttpServletResponse
        logger.info(
            "Incoming Request: method={}, uri={}",
            request.method,
            request.requestURI
        )
        chain.doFilter(request, response)
        logger.info(
            "Outgoing Response: status={}, content-type={}",
            response.status,
            response.contentType
        )
    }
}
