package pt.isel.ps.anonichat.http

import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import pt.isel.ps.anonichat.http.controllers.router.models.GetRoutersCountOutputModel
import pt.isel.ps.anonichat.http.controllers.router.models.GetRoutersOutputModel
import pt.isel.ps.anonichat.http.controllers.router.models.RegisterOutputModel
import pt.isel.ps.anonichat.http.hypermedia.SirenEntity
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RouterControllerTest : HttpTest() {

    @Test
    fun `register router`() {
        val (ip, routerCSR, pwd) = testRouterData()

        val body: MultiValueMap<String, String> = LinkedMultiValueMap()
        body.add("routerCSR", routerCSR)
        body.add("pwd", pwd)
        body.add("ip", ip)

        val router = client.post().uri(api("/routers"))
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(
                BodyInserters.fromFormData(body)
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody<SirenEntity<RegisterOutputModel>>()
            .returnResult().responseBody?.properties

        val routerId = router?.routerId
        assertNotNull(routerId)
        assertTrue(routerId > 0)
    }

    @Test
    fun `get routers`() {
        val (ip, routerCSR, pwd) = testRouterData()

        val body: MultiValueMap<String, String> = LinkedMultiValueMap()
        body.add("routerCSR", routerCSR)
        body.add("pwd", pwd)
        body.add("ip", ip)

        val routerId = client.post().uri(api("/routers"))
            .body(
                BodyInserters.fromFormData(body)
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

        val (ip, routerCSR, pwd) = testRouterData()

        val body: MultiValueMap<String, String> = LinkedMultiValueMap()
        body.add("routerCSR", routerCSR)
        body.add("pwd", pwd)
        body.add("ip", ip)

        val routerId = client.post().uri(api("/routers"))
            .body(
                BodyInserters.fromFormData(body)
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

    @Test
    fun `delete router`() {
        val (ip, routerCSR, pwd) = testRouterData()

        val body: MultiValueMap<String, String> = LinkedMultiValueMap()
        body.add("routerCSR", routerCSR)
        body.add("pwd", pwd)
        body.add("ip", ip)

        val routerId = client.post().uri(api("/routers"))
            .body(
                BodyInserters.fromFormData(body)
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody<SirenEntity<RegisterOutputModel>>()
            .returnResult().responseBody?.properties?.routerId

        client.delete().uri(api("/routers/$routerId?pwd=$pwd"))
            .exchange()
            .expectStatus().isOk
    }
}
