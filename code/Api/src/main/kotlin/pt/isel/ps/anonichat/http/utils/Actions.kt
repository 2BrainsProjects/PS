package pt.isel.ps.anonichat.http.utils

import pt.isel.ps.anonichat.http.media.siren.Action
import pt.isel.ps.anonichat.http.media.siren.Action.Field

object Actions {

    object Methods {
        const val GET = "GET"
        const val POST = "POST"
        const val DELETE = "DELETE"
        const val PUT = "PUT"
    }

    object User {

        fun logout() = Action(
            name = Rels.User.LOGOUT,
            href = Uris.User.logout(),
            method = Methods.POST
        )
    }
}
