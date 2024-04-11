package pt.isel.ps.anonichat.repository

import pt.isel.ps.anonichat.domain.router.Router

interface RouterRepository {
    fun createRouter(passwordHash: String): Int
    fun getRouterByIp(ip: String): Router
    fun getRouterById(id: Int): Router
    fun deleteRouter(id: Int): Boolean
    fun isRouter(id: Int): Boolean
    fun lastRouterId(): Int
    fun updateRouter(id: Int, ip: String, certPath: String): Boolean
}
