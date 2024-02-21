package pt.isel.daw.gomoku.http.hypermedia

import com.fasterxml.jackson.annotation.JsonProperty

data class SirenEntity<T>(
    @get:JsonProperty("class")
    override val clazz: List<String>? = null,
    override val properties: T? = null,
    override val links: List<Link>? = null,
    override val actions: List<Action>? = null,
    override val entities: List<SubEntity>? = null
) : Entity<T>
