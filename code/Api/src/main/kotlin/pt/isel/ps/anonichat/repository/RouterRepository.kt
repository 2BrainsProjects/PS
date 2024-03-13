package pt.isel.ps.anonichat.repository

import pt.isel.ps.anonichat.domain.router.Router

interface RouterRepository {
    fun createRouter(ip: String): Int
    fun getRouterByIp(ip: String): Router
    fun getRouterById(id: Int): Router
    fun deleteRouter(id: Int): Boolean
    fun isRouter(id: Int): Boolean
    fun lastRouterId(): Int
    fun updateCert(id: Int, certPath: String): Boolean
}
