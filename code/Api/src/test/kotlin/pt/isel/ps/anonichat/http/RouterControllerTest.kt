package pt.isel.ps.anonichat.http

import org.springframework.test.web.reactive.server.expectBody
import pt.isel.ps.anonichat.http.controllers.router.models.GetRoutersCountOutputModel
import pt.isel.ps.anonichat.http.controllers.router.models.GetRoutersOutputModel
import pt.isel.ps.anonichat.http.controllers.router.models.RegisterOutputModel
import pt.isel.ps.anonichat.http.hypermedia.SirenEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RouterControllerTest : HttpTest() {

    @Test
    fun `register router`() {

        val routerCSR = testRouterData().second

        val router = client.post().uri(api("/routers"))
            .bodyValue(
                mapOf("routerCSR" to routerCSR)
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody<SirenEntity<RegisterOutputModel>>()
            .returnResult().responseBody?.properties

        val routerId = router?.routerId
        val certificateContent = router?.certificateContent
        assertNotNull(routerId)
        assertNotNull(certificateContent)
        assertTrue(routerId > 0)
        assertTrue(certificateContent.isNotEmpty())
        assertTrue(certificateContent.isNotBlank())
    }

    @Test
    fun `get routers`() {
        val routerCSR = testRouterData().second

        val routerId = client.post().uri(api("/routers"))
            .bodyValue(
                mapOf("routerCSR" to routerCSR)
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody<SirenEntity<RegisterOutputModel>>()
            .returnResult().responseBody?.properties?.routerId

        val response = client.get().uri(api("/routers?ids=$routerId"))
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

        val routerCSR = testRouterData().second

        val routerId = client.post().uri(api("/routers"))
            .bodyValue(
                mapOf("routerCSR" to routerCSR)
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody<SirenEntity<RegisterOutputModel>>()
            .returnResult().responseBody?.properties?.routerId

        val nextMaxId = client.get().uri(api("/routers/count"))
            .exchange()
            .expectStatus().isOk
            .expectBody<SirenEntity<GetRoutersCountOutputModel>>()
            .returnResult().responseBody?.properties?.maxId

        assertNotNull(maxId)
        assertNotNull(nextMaxId)
        assertNotNull(routerId)
        assertTrue(nextMaxId >= routerId)
        assertTrue(routerId > maxId)
        assertTrue(nextMaxId > maxId)
    }
}