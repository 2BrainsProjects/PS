package pt.isel.daw.gomoku.http.utils

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

    object Game {
        const val GAME = "/game/{gameId}"
        const val PLAY = "/game/play"
        const val GAMES = "/games"
        const val LEAVE = "/game/leave"

        fun game(id: Int) = UriTemplate(GAME).expand(id)
        fun play() = URI(PLAY)
        fun games() = URI(GAMES)
        fun leave() = URI(LEAVE)
    }

    object Lobby {
        const val JOIN = "/lobby/join"
        const val FIND_MATCH = "/lobby/matchmaking"
        const val LEAVE = "/lobby/leave"

        fun join() = URI(JOIN)
        fun findMatch() = URI(FIND_MATCH)
        fun leave() = URI(LEAVE)
    }
}
