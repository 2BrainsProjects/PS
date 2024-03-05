package pt.isel.ps.anonichat.domain.router

/**
 * Represents a router in the system
 * @property id The router's id
 * @property ip The router's ip
 * @property certificate The router's certificate path
*/
data class Router(
        val id: Int,
        val ip: String,
        val certificate: String
)
