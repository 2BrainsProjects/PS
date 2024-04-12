package pt.isel.ps.anonichat

import kotlinx.datetime.Clock
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import pt.isel.ps.anonichat.domain.user.UserDomainConfig
import pt.isel.ps.anonichat.domain.user.utils.Sha256TokenEncoder
import pt.isel.ps.anonichat.http.pipeline.authentication.AuthenticationInterceptor
import pt.isel.ps.anonichat.http.pipeline.authentication.IpArgumentResolver
import pt.isel.ps.anonichat.http.pipeline.authentication.IpInterceptor
import pt.isel.ps.anonichat.http.pipeline.authentication.SessionArgumentResolver
import pt.isel.ps.anonichat.repository.jdbi.utils.configure
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

/**
 * The application's entry point
 */
@SpringBootApplication
class AnonichatApplication {

    @Bean
    fun jdbi(): Jdbi = Jdbi.create(
        PGSimpleDataSource().apply {
            setURL(Environment.getDbUrl())
        }
    ).configure()

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun tokenEncoder() = Sha256TokenEncoder()

    @Bean
    fun clock() = Clock.System

    @Bean
    fun usersDomainConfig() = UserDomainConfig(
        tokenSizeInBytes = 256 / 8,
        tokenTtl = 1.hours,
        tokenRollingTtl = 3.days,
        maxTokensPerUser = 3
    )
}

@Configuration
class PipelineConfigurer(
    val authenticationInterceptor: AuthenticationInterceptor,
    val authenticatedUserArgumentResolver: SessionArgumentResolver,
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authenticationInterceptor)
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(authenticatedUserArgumentResolver)
    }
}

fun main(args: Array<String>) {
    runApplication<AnonichatApplication>(*args)
}
