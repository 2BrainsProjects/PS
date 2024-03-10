package pt.isel.ps.anonichat.http

import org.springframework.test.web.reactive.server.expectBody
import pt.isel.ps.anonichat.http.controllers.router.models.GetRoutersCountOutputModel
import pt.isel.ps.anonichat.http.controllers.router.models.GetRoutersOutputModel
import pt.isel.ps.anonichat.http.hypermedia.SirenEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RouterControllerTest : HttpTest() {
    @Test
    fun `get routers`() {
        val response = client.get().uri(api("/routers?ids=1"))
            .exchange()
            .expectStatus().isOk
            .expectBody<SirenEntity<GetRoutersOutputModel>>()
            .returnResult().responseBody?.properties?.count

        assertNotNull(response)
        assertTrue(response > 0)
    }

    @Test
    fun `get router's max id`() {
        val maxId = client.get().uri(api("/routers/count"))
            .exchange()
            .expectStatus().isOk
            .expectBody<SirenEntity<GetRoutersCountOutputModel>>()
            .returnResult().responseBody?.properties?.maxId

        //val routerId = registerTestRouter()
        //val newMaxId = routersRepository.lastRouterId()

        //assertEquals(routerId, newMaxId)
        //assertTrue(newMaxId > maxId)
        assertNotNull(maxId)
        assertTrue(maxId > 0)
    }
}