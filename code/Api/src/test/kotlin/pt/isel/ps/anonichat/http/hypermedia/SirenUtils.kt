package pt.isel.ps.anonichat.http.hypermedia

import com.fasterxml.jackson.annotation.JsonProperty

data class SirenEntityEmbeddedLinkModel<T> (
    @get:JsonProperty("class")
    override val clazz: List<String>? = null,
    override val properties: T? = null,
    override val links: List<Link>? = null,
    override val actions: List<Action>? = null,
    override val entities: List<SubEntity.EmbeddedLink>? = null
) : Entity<T>

data class SirenEntityEmbeddedRepresentationModel<T> (
    @get:JsonProperty("class")
    override val clazz: List<String>? = null,
    override val properties: T? = null,
    override val links: List<Link>? = null,
    override val actions: List<Action>? = null,
    override val entities: List<SubEntity.EmbeddedRepresentation<*>>? = null
) : Entity<T>

fun Entity<*>.getActionLink(rel: String): String? = actions
    ?.firstOrNull { action -> action.name == rel }
    ?.href?.toString()

fun Entity<*>.getLink(rel: String): String? = links
    ?.firstOrNull { link -> link.rel.any { it == rel } }
    ?.href?.toString()

fun Entity<*>.getEmbeddedLinks(vararg rels: String): List<SubEntity.EmbeddedLink> = entities
    ?.filterIsInstance<SubEntity.EmbeddedLink>()
    ?.filter { link -> rels.all { it in link.rel } }
    ?: throw NoSuchElementException("Entity does not have any embedded links")
