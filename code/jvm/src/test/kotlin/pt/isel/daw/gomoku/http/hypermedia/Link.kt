package pt.isel.daw.gomoku.http.hypermedia

import java.net.URI

data class Link(
    val rel: List<String>,
    val href: URI,
    val title: String? = null,
    val type: String? = null
)
