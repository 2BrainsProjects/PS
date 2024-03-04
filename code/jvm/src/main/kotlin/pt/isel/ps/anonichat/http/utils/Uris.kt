package pt.isel.ps.anonichat.http.utils

import org.springframework.web.util.UriTemplate
import java.net.URI

object Uris {

    const val PREFIX = "/api"

    const val HOME = "/"
    fun home() = URI(HOME)

    object User {
        const val HOME = "/me"
        const val REGISTER = "/register"
        const val LOGIN = "/login"
        const val LOGOUT = "/logout"
        const val USER = "/user/{userId}"
        const val USERS = "/users"

        fun home() = URI(HOME)
        fun user(id: Int) = UriTemplate(USER).expand(id)
        fun users() = URI(USERS)
        fun login() = URI(LOGIN)
        fun register() = URI(REGISTER)
        fun logout() = URI(LOGOUT)
    }

    object Router{
        const val ROUTER = "/router/{routerId}"
        const val ROUTERS = "/routers"

        fun router(id: Int) = UriTemplate(ROUTER).expand(id)
        fun routers() = URI(ROUTERS)
    }
}