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

    object Collection {
        const val COLLECTION = "collection"
        const val ITEM = "item"
        const val NEXT = "next"
        const val PREV = "prev"
    }
}
