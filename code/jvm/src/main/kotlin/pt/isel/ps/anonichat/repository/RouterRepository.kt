package pt.isel.ps.anonichat.repository

import pt.isel.ps.anonichat.domain.router.Router

interface RouterRepository {
    fun createRouter(ip: String, certificate: String):Boolean
    fun getRouterByIp(ip: String): Router
    fun getRouterById(id: Int): Router
    fun deleteRouter(id: String): Boolean
    fun isRouter(id: Int): Boolean
    fun lastRouter(): Router
}