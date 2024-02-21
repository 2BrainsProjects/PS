package pt.isel.daw.gomoku.http.utils

import pt.isel.daw.gomoku.http.media.siren.Action
import pt.isel.daw.gomoku.http.media.siren.Action.Field

object Actions {

    object Methods {
        const val GET = "GET"
        const val POST = "POST"
        const val DELETE = "DELETE"
        const val PUT = "PUT"
    }

    object User {

        fun getById(id: Int) = Action(
            name = Rels.User.USER,
            href = Uris.User.user(id),
            method = Methods.GET
        )

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

    object Lobby {

        fun join() = Action(
            name = Rels.Lobby.JOIN,
            href = Uris.Lobby.join(),
            method = Methods.POST,
            fields = listOf(
                Field(
                    name = "boardSize",
                    type = "number"
                ),
                Field(
                    name = "variant",
                    type = "text",
                    title = "Game Variant"
                ),
                Field(
                    name = "opening",
                    type = "text"
                )
            )
        )

        fun findMatch() = Action(
            name = Rels.Lobby.FIND_MATCH,
            href = Uris.Lobby.findMatch(),
            method = Methods.GET
        )

        fun leave() = Action(
            name = Rels.Lobby.LEAVE,
            href = Uris.Lobby.leave(),
            method = Methods.DELETE
        )
    }

    object Game {

        fun getById(id: Int) = Action(
            name = Rels.Game.GAME,
            href = Uris.Game.game(id),
            method = Methods.GET
        )

        fun getOpponent(id: Int) = Action(
            name = Rels.Game.OPPONENT,
            href = Uris.User.user(id),
            method = Methods.GET
        )

        fun getBlackPlayer(id: Int) = Action(
            name = Rels.Game.BLACK_PLAYER,
            href = Uris.User.user(id),
            method = Methods.GET
        )

        fun getWhitePlayer(id: Int) = Action(
            name = Rels.Game.WHITE_PLAYER,
            href = Uris.User.user(id),
            method = Methods.GET
        )

        fun play() = Action(
            name = Rels.Game.PLAY,
            href = Uris.Game.play(),
            method = Methods.PUT,
            fields = listOf(
                Field(
                    name = "row",
                    type = "number"
                ),
                Field(
                    name = "col",
                    type = "number"
                )
            )
        )

        fun leave() = Action(
            name = Rels.Game.LEAVE,
            href = Uris.Game.leave(),
            method = Methods.DELETE
        )

        fun getGames() = Action(
            name = Rels.Game.GAMES,
            href = Uris.Game.games(),
            method = Methods.GET
        )
    }
}
