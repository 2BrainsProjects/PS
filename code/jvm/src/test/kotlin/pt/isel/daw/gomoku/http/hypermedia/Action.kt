package pt.isel.daw.gomoku.http.hypermedia

import pt.isel.daw.gomoku.http.utils.MediaTypes.JSON_MEDIA_TYPE
import java.net.URI

data class Action(
    val name: String,
    val href: URI,
    val method: String? = null,
    val type: String = JSON_MEDIA_TYPE,
    val fields: List<Field>? = null
) {
    data class Field(
        val name: String,
        val type: String,
        val value: String? = null,
        val title: String? = null
    )
}
