package pt.isel.ps.anonichat.http

import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.client.reactive.ReactorClientHttpConnector
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
import reactor.netty.http.client.HttpClient
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HttpTest : AnonichatTest() {

    @LocalServerPort
    var port: Int = 0
    private val sslContext = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build()
    private val httpClient = HttpClient.create().secure{ sslContextSpec -> sslContextSpec.sslContext(sslContext) }
    val client = WebTestClient.bindToServer(ReactorClientHttpConnector(httpClient)).baseUrl(api("/")).build()
    final fun api(path: String): String = "https://localhost:$port/api$path"

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
