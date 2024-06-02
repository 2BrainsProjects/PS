package pt.isel.ps.anonichat.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouterRepositoryTest : RepositoryTest() {

    @Test
    fun `create a router`() {
        // given: an ip and a certificate
        val password = testPassword()
        val ip = testIp()
        val certificate = testCertificate()

        val id = routersRepository.createRouter(password)

        routersRepository.updateRouter(id, ip, certificate)

        val router = routersRepository.getRouterByIp(ip)
        assertEquals(router.passwordHash, password)
        assertEquals(router.certificate, certificate)
        assertEquals(router.ip, ip)
        assertTrue(routersRepository.isRouter(router.id))

        val routerById = routersRepository.getRouterById(router.id)
        assertEquals(router, routerById)
    }

    @Test
    fun `get last router`() {
        registerTestRouter()

        val maxId = routersRepository.lastRouterId()

        val routerId = registerTestRouter()
        val newMaxId = routersRepository.lastRouterId()

        assertEquals(routerId, newMaxId)
        assertTrue(newMaxId > maxId)
    }

    @Test
    fun `delete a router`() {
        val ip = testIp()
        val certificate = testCertificate()

        val id = routersRepository.createRouter(ip)

        routersRepository.updateRouter(id, ip, certificate)
        val router = routersRepository.getRouterByIp(ip)

        assertEquals(router.ip, ip)
        assertEquals(router.certificate, certificate)

        assertTrue(routersRepository.deleteRouter(router.id))
        assertTrue(!routersRepository.isRouter(router.id))
    }
}
