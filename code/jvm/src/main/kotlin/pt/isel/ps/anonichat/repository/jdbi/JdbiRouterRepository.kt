package pt.isel.ps.anonichat.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.ps.anonichat.domain.router.Router
import pt.isel.ps.anonichat.repository.RouterRepository

class JdbiRouterRepository(
    private val handle: Handle
) : RouterRepository {

    /**
     * Creates a new router
     * @param ip The router's ip
     * @param certificate The router's certificate
     * @return The row
     */
    override fun createRouter(ip: String, certificate: String): Int =
        handle.createUpdate("insert into dbo.Router (ip, certificate) values (:ip, :certificate)")
            .bind("ip", ip)
            .bind("certificate", certificate)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .single()

    /**
     * Gets a router by ip
     * @param ip The router's ip
     * @return The router
     */
    override fun getRouterByIp(ip: String): Router =
        handle.createQuery("select * from dbo.Router where ip = :ip")
            .bind("ip", ip)
            .mapTo<Router>()
            .single()

    /**
     * Gets a router by id
     * @param id The router's id
     * @return The router
     */
    override fun getRouterById(id: Int): Router =
        handle.createQuery("select * from dbo.Router where id = :id")
            .bind("id", id)
            .mapTo<Router>()
            .single()

    /**
     * Deletes a router
     * @param id The router's id
     * @return If the router was deleted
     */
    override fun deleteRouter(id: Int): Boolean =
        handle.createUpdate("delete from dbo.Router where id = :id")
            .bind("id", id)
            .execute() == 1

    /**
     * Checks if a router exists
     * @param id The router's id
     * @return If the router exists
     */
    override fun isRouter(id: Int): Boolean =
        handle.createQuery("select * from dbo.Router where id = :id")
            .bind("id", id)
            .mapTo<Int>()
            .singleOrNull() != null

    /**
     * Gets the last router's id
     * @return The last router's id
     */
    override fun lastRouterId(): Int =
        handle.createQuery("select id from dbo.Router order by id desc limit 1")
            .mapTo<Int>()
            .single()
}