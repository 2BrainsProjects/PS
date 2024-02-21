package pt.isel.daw.gomoku.http.media.siren

import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URI

/**
 * Represents a sub-entity of an entity
 */
sealed class SubEntity {

    /**
     * Represents a link that represent a navigational transition
     * @property rel the relation of the link
     * @property href the URI of the link
     */
    data class EmbeddedLink(
        val rel: List<String>,
        val href: URI
    ) : SubEntity()

    /**
     * Represents a sub-entity that is embedded in the entity
     * @property rel the relation of the sub-entity
     * @property clazz the classes of the sub-entity (optional)
     * @property properties the properties of the sub-entity (optional)
     * @property links the links of the sub-entity (optional)
     * @property actions the actions of the sub-entity (optional)
     * @property entities the sub-entities of the sub-entity (optional)
     */
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
