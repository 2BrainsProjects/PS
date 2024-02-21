package pt.isel.daw.gomoku.http.utils

object Rels {

    const val SELF = "self"
    const val HOME = "home"

    object User {
        const val HOME = "user-home"
        const val REGISTER = "register"
        const val LOGIN = "login"
        const val LOGOUT = "logout"
        const val USER = "get-user"
        const val USERS = "get-users"
    }

    object Lobby {
        const val JOIN = "join-lobby"
        const val FIND_MATCH = "find-match"
        const val LEAVE = "leave-lobby"
    }

    object Game {
        const val GAME = "get-game"
        const val PLAY = "play-game"
        const val GAMES = "get-games"
        const val LEAVE = "leave-game"
        const val OPPONENT = "get-opponent"
        const val WHITE_PLAYER = "get-white-player"
        const val BLACK_PLAYER = "get-black-player"
    }

    object Collection {
        const val COLLECTION = "collection"
        const val ITEM = "item"
        const val NEXT = "next"
        const val PREV = "prev"
    }
}
