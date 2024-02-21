package pt.isel.daw.gomoku.http.media.siren

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import java.net.URI

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
    @get:JsonProperty("class")
    override val clazz: List<String>? = null,
    override val properties: T? = null,
    override val links: List<Link>? = null,
    override val actions: List<Action>? = null,
    override val entities: List<SubEntity>? = null
) : Entity<T> {
    fun ok(headers: HttpHeaders? = null) = ResponseEntity.ok().headers(headers).body(this)
    fun created(location: URI, headers: HttpHeaders? = null) = ResponseEntity.created(location).headers(headers).body(this)
    fun accepted(headers: HttpHeaders? = null) = ResponseEntity.accepted().headers(headers).body(this)
    fun noContent(headers: HttpHeaders? = null) = ResponseEntity.noContent().headers(headers).build<Unit>()
}
