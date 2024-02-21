package pt.isel.daw.gomoku.http.hypermedia

import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URI

open class SubEntity {

    data class EmbeddedLink(
        val rel: List<String>,
        val href: URI
    ) : SubEntity()

    data class EmbeddedRepresentation<T>(
        val rel: List<String>,
        @get:JsonProperty("class")
        override val clazz: List<String>? = null,
        override val properties: T? = null,
        override val links: List<Link>? = null,
        override val actions: List<Action>? = null,
        override val entities: List<SubEntity>? = null
    ) : Entity<T>, SubEntity()
}
