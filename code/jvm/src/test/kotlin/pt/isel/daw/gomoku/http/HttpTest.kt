package pt.isel.daw.gomoku.http

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import pt.isel.daw.gomoku.GomokuTest
import pt.isel.daw.gomoku.http.controllers.user.models.GetUserOutputModel
import pt.isel.daw.gomoku.http.controllers.user.models.LoginOutputModel
import pt.isel.daw.gomoku.http.hypermedia.SirenEntity
import pt.isel.daw.gomoku.http.hypermedia.SirenEntityEmbeddedLinkModel
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HttpTest : GomokuTest() {

    @LocalServerPort
    var port: Int = 0
    final val client = WebTestClient.bindToServer().baseUrl(api("/")).build()
    final fun api(path: String): String = "http://localhost:$port/api$path"

    fun registerTestUserHttp(
        name: String = testUsername(),
        email: String = testEmail(),
        password: String = testPassword()
    ): String = client.post().uri(api("/register"))
        .bodyValue(
            mapOf(
                "name" to name,
                "email" to email,
                "password" to password
            )
        )
        .exchange()
        .expectStatus().isCreated
        .expectHeader().value("location") {
            assertTrue(it.startsWith("/user"))
        }
        .expectBody<SirenEntityEmbeddedLinkModel<Unit>>()
        .returnResult()
        .responseBody?.entities?.first()?.href.toString()

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

    fun getUserHttp(link: String) = client.get().uri(api(link))
        .exchange()
        .expectStatus().isOk
        .expectBody<SirenEntity<GetUserOutputModel>>()
        .returnResult().responseBody!!
}
