package pt.isel.ps.anonichat.http.utils

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
        const val USERS = "/users"
        const val USERS_COUNT = "/users/count"

        fun home() = URI(HOME)
        fun users() = URI(USERS)
        fun login() = URI(LOGIN)
        fun register() = URI(REGISTER)
        fun logout() = URI(LOGOUT)
    }

    object Router {
        const val ROUTERS = "/routers"
        const val ROUTERS_COUNT = "/routers/count"

        fun routers() = URI(ROUTERS)
    }
}
