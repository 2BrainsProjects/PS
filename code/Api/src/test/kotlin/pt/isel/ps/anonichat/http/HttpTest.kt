package pt.isel.ps.anonichat.http

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import pt.isel.ps.anonichat.AnonichatTest
import pt.isel.ps.anonichat.http.controllers.user.models.GetUsersOutputModel
import pt.isel.ps.anonichat.http.controllers.user.models.LoginOutputModel
import pt.isel.ps.anonichat.http.controllers.user.models.RegisterOutputModel
import pt.isel.ps.anonichat.http.hypermedia.SirenEntity
import pt.isel.ps.anonichat.http.hypermedia.SirenEntityEmbeddedLinkModel
import pt.isel.ps.anonichat.http.hypermedia.SirenEntityEmbeddedRepresentationModel
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HttpTest : AnonichatTest() {

    @LocalServerPort
    var port: Int = 0
    final val client = WebTestClient.bindToServer().baseUrl(api("/")).build()
    final fun api(path: String): String = "http://localhost:$port/api$path"

    fun registerTestUserHttp(
        name: String = testUsername(),
        email: String = testEmail(),
        password: String = testPassword(),
        clientCSR: String = testUserCSR()
    ): RegisterOutputModel {
        val body: MultiValueMap<String, String> = LinkedMultiValueMap()
        body.add("name", name)
        body.add("email", email)
        body.add("password", password)
        body.add("clientCSR", clientCSR)

        return client.post().uri(api("/users"))
            .body(
                BodyInserters.fromFormData(body)
            )
            .exchange()
            .expectStatus().isCreated
            .expectHeader().value("location") {
                assertTrue(it.startsWith("/me"))
            }
            .expectBody<SirenEntityEmbeddedLinkModel<RegisterOutputModel>>()
            .returnResult()
            .responseBody?.properties!!
    }

    fun loginTestUserHttp(
        name: String,
        password: String
    ) =
        client.post().uri(api("/login"))
            .bodyValue(
                mapOf(
                    "name" to name,
                    "password" to password
                )
            )
            .exchange()
            .expectStatus().isOk
            .expectBody<SirenEntity<LoginOutputModel>>()
            .returnResult().responseBody?.properties!!.token

    fun getUsersHttp(ids: List<Int>) = client.get().uri(api("/users?ids=${ids.joinToString(",")}"))
        .exchange()
        .expectStatus().isOk
        .expectBody<SirenEntityEmbeddedRepresentationModel<GetUsersOutputModel>>()
        .returnResult().responseBody
}
