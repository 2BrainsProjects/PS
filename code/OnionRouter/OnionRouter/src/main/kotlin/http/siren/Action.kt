package http.siren
import java.net.URI

const val JSON_MEDIA_TYPE = "application/json"

/**
 * Represents action that can be performed by an entity
 * @property name the name of the action
 * @property href the URI of the action
 * @property method the HTTP method of the action (optional)
 * @property type the media type of the action (optional)
 * @property fields the fields of the action (optional)
 */
data class Action(
    val name: String,
    val href: URI,
    val method: String? = null,
    val type: String = JSON_MEDIA_TYPE,
    val fields: List<Field>? = null
) {
    /**
     * A field that is part of an action
     * @property name the name of the field
     * @property type the type of the field
     * @property value the value of the field (optional)
     * @property title the title of the field (optional)
     */
    data class Field(
        val name: String,
        val type: String,
        val value: String? = null,
        val title: String? = null
    )
}
