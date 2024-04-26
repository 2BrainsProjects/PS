package http.siren

/**
 * Represents a link that represent a navigational transition
 * @property rel the relation of the link
 * @property href the URI of the link or the URI template of the link
 * @property title the title of the link (optional)
 * @property type the media type of the link (optional)
 */
data class Link(
    val rel: List<String>,
    val href: String,
    val title: String? = null,
    val type: String? = null
)
