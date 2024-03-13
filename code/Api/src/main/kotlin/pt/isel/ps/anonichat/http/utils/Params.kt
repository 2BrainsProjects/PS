package pt.isel.ps.anonichat.http.utils

import pt.isel.ps.anonichat.http.media.siren.Link
import java.net.URI

class Params(
    page: Int? = null,
    limit: Int? = null,
    sort: String? = null,
    orderBy: String? = null
) {
    init {
        page?.let { require(it > 0) { "Invalid page parameter" } }
        limit?.let {
            require(it >= 0) { "Invalid limit parameter" }
            require(it <= Limit.MAX_VALUE) {
                "Limit parameter cannot be greater than ${Limit.MAX_VALUE}"
            }
        }
        sort?.let {
            require(it in Sort.VALUES) {
                "Invalid sort parameter. Must be one of the following: ${Sort.VALUES.joinToString(", ")}"
            }
        }
        orderBy?.let {
            require(it in OrderBy.VALUES) {
                "Invalid order by parameter. Must be one of the following: ${OrderBy.VALUES.joinToString(", ")}"
            }
        }
    }

    val skip = if (page != null && limit != null) (page - 1) * limit else Skip.DEFAULT
    val limit = limit ?: Limit.DEFAULT
    val orderBy = orderBy?.lowercase() ?: OrderBy.RATING
    val sort = sort?.lowercase() ?: Sort.DESC
    private val page = page ?: Page.DEFAULT

    fun getNextPageLink(uri: URI, totalCount: Int): Link? {
        val totalPages = (totalCount + limit - 1) / limit
        return page.takeIf { it < totalPages }?.let {
            Links.next(
                uri.appendQuery(
                    mapOf(
                        "page" to "${page + 1}",
                        "skip" to "$skip",
                        "limit" to "$limit",
                        "orderBy" to orderBy,
                        "sort" to sort
                    )
                ).toString()
            )
        }
    }

    fun getPrevPageLink(uri: URI): Link? {
        return page.takeIf { it > 1 }?.let {
            Links.prev(
                uri.appendQuery(
                    mapOf(
                        "page" to "${page - 1}",
                        "skip" to "$skip",
                        "limit" to "$limit",
                        "orderBy" to orderBy,
                        "sort" to sort
                    )
                ).toString()
            )
        }
    }

    private object Skip {
        const val DEFAULT = 0
    }

    private object Page {
        const val DEFAULT = 1
    }

    private object Limit {
        const val DEFAULT = 10
        const val MAX_VALUE = 100
    }

    private object Sort {
        const val ASC = "asc"
        const val DESC = "desc"
        val VALUES = listOf(ASC, DESC)
    }

    private object OrderBy {
        const val NAME = "name"
        const val RATING = "rating"
        const val GAMES = "games"
        const val WINS = "wins"
        val VALUES = listOf(NAME, RATING, GAMES, WINS)
    }
}
