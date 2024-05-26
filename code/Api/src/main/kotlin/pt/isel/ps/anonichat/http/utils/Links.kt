package pt.isel.ps.anonichat.http.utils

import pt.isel.ps.anonichat.http.media.siren.Link

object Links {
    fun self(href: String) = Link(listOf(Rels.SELF), href)
    fun userHome() = Link(listOf(Rels.User.HOME), Uris.User.HOME)
}
