package pt.isel.daw.gomoku.http.utils

import pt.isel.daw.gomoku.http.media.siren.Link

object Links {
    fun self(href: String) = Link(listOf(Rels.SELF), href)
    fun home() = Link(listOf(Rels.HOME), Uris.HOME)
    fun userHome() = Link(listOf(Rels.User.HOME), Uris.User.HOME)
    fun user() = Link(listOf(Rels.User.USER), Uris.User.USER)
    fun next(href: String) = Link(listOf(Rels.Collection.NEXT), href)
    fun prev(href: String) = Link(listOf(Rels.Collection.PREV), href)
}
