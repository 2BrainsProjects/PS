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

        fun getUsers() = Action(
            name = Rels.User.USERS,
            href = Uris.User.users(),
            method = Methods.GET
        )

        fun register() = Action(
            name = Rels.User.REGISTER,
            href = Uris.User.register(),
            method = Methods.POST,
            fields = listOf(
                Field(
                    name = "username",
                    type = "text",
                    title = "Username"
                ),
                Field(
                    name = "email",
                    type = "text",
                    title = "E-mail"
                ),
                Field(
                    name = "password",
                    type = "password",
                    title = "Password"
                )
            )
        )

        fun login() = Action(
            name = Rels.User.LOGIN,
            href = Uris.User.login(),
            method = Methods.POST,
            fields = listOf(
                Field(
                    name = "usernameOrEmail",
                    type = "text",
                    title = "Username or Email"
                ),
                Field(
                    name = "password",
                    type = "password",
                    title = "Password"
                )
            )
        )

        fun logout() = Action(
            name = Rels.User.LOGOUT,
            href = Uris.User.logout(),
            method = Methods.POST
        )
    }

    object Router {

        fun getRouters() = Action(
            name = Rels.Router.ROUTERS,
            href = Uris.Router.routers(),
            method = Methods.GET
        )
    }
}
