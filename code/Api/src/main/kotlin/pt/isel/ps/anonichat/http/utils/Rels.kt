package pt.isel.ps.anonichat.http.utils

object Rels {

    const val SELF = "self"
    const val HOME = "home"

    object User {
        const val HOME = "user-home"
        const val REGISTER = "register"
        const val LOGIN = "login"
        const val LOGOUT = "logout"
        const val USERS = "get-users"
        const val USERS_COUNT = "get-users-count"
    }

    object Router {
        const val ROUTERS_COUNT = "get-routers-count"
        const val ROUTERS = "get-routers"
    }

    object Collection {
        const val COLLECTION = "collection"
        const val ITEM = "item"
        const val NEXT = "next"
        const val PREV = "prev"
    }
}