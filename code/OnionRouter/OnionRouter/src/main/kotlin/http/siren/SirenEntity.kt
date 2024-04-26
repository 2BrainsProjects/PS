package http.siren

import com.google.gson.annotations.SerializedName

/**
 * Represents a hypermedia entity
 * @property clazz the classes of the entity (optional)
 * @property properties the properties of the entity (optional)
 * @property links the links of the entity (optional)
 * @property actions the actions of the entity (optional)
 * @property entities the sub-entities of the entity (optional)
 * @param T the type of the properties of the entity
 */
data class SirenEntity<T>(
    @SerializedName("class")
    override val clazz: List<String>? = null,
    override val properties: T? = null,
    override val links: List<Link>? = null,
    override val actions: List<Action>? = null,
    override val entities: List<SubEntity.EmbeddedRepresentation<*>>? = null
) : Entity<T>
