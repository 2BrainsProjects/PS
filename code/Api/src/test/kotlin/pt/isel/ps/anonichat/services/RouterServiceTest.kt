package pt.isel.ps.anonichat.services

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouterServiceTest : ServicesTest() {

    private val path = basePath + ROUTERS
    @Test
    fun `register a router`() {
        // given: a user
        val (ip, routerCSR) = testRouterData()

        // when: registering the user
        val (routerId) = routersServices.createRouter(ip, routerCSR, path)

        // then: the user is registered
        val (routers) = routersServices.getRouters(listOf(routerId))
        assertEquals(ip, routers[0].ip)
        assertTrue(routers[0].certificate.isNotEmpty())
    }

    @Test
    fun `delete a router`() {
        // given: a user
        val (ip, routerCSR) = testRouterData()
        val (routerId) = routersServices.createRouter(ip, routerCSR, path)

        // when: deleting the user
        val result = routersServices.deleteRouter(routerId)

        // then: the user is deleted
        assertEquals(true, result)
    }

    @Test
    fun `get a list of routers`() {
        // given: a user
        val (ip, routerCSR) = testRouterData()
        val (routerId) = routersServices.createRouter(ip, routerCSR, path)

        // when: getting the list of users
        val (routers) = routersServices.getRouters(listOf(routerId))

        // then: the user is in the list
        assertEquals(ip, routers[0].ip)
        assertTrue(routers[0].certificate.isNotEmpty())
    }

    @Test
    fun `get the last router id`() {
        // given: a user
        val (ip, routerCSR) = testRouterData()
        val (routerId) = routersServices.createRouter(ip, routerCSR, path)

        // when: getting the last user id
        val lastId = routersServices.getLastId()

        // then: the last user id is the user id
        assertEquals(routerId, lastId)
    }
}