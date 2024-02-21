package pt.isel.daw.gomoku.http

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import pt.isel.daw.gomoku.GomokuTest
import pt.isel.daw.gomoku.http.controllers.game.models.GetGameStateOutputModel
import pt.isel.daw.gomoku.http.controllers.user.models.GetUserOutputModel
import pt.isel.daw.gomoku.http.controllers.user.models.LoginOutputModel
import pt.isel.daw.gomoku.http.hypermedia.SirenEntity
import pt.isel.daw.gomoku.http.hypermedia.SirenEntityEmbeddedLinkModel
import pt.isel.daw.gomoku.http.hypermedia.getEmbeddedLinks
import pt.isel.daw.gomoku.http.utils.Rels
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

    fun joinLobbyHttp(
        token: String,
        boardSize: Int = 15,
        variant: String = "freestyle",
        opening: String = "freestyle"
    ) = client.post().uri(api("/lobby/join"))
        .header("Authorization", "Bearer $token")
        .bodyValue(
            mapOf(
                "boardSize" to boardSize,
                "variant" to variant,
                "opening" to opening
            )
        )
        .exchange()
        .expectStatus().isOk
        .expectBody<SirenEntity<Unit>>()
        .returnResult().responseHeaders.location?.path

    fun leaveLobbyHttp(token: String) = client.delete().uri(api("/lobby/leave"))
        .header("Authorization", "Bearer $token")
        .exchange()
        .expectStatus().isOk
        .expectBody<SirenEntity<Unit>>()
        .returnResult().responseBody!!

    fun findMatchHttp(token: String) = client.get().uri(api("/lobby/matchmaking"))
        .header("Authorization", "Bearer $token")
        .exchange()
        .expectStatus().is2xxSuccessful
        .expectBody<SirenEntityEmbeddedLinkModel<Unit>>()
        .returnResult().responseBody!!

    fun findMatchAndGetGameLink(token: String) = findMatchHttp(token)
        .getEmbeddedLinks(Rels.Game.GAME).first().href.toString()

    fun getGameHttp(link: String) = client.get().uri(api(link))
        .exchange()
        .expectStatus().isOk
        .expectBody<SirenEntity<GetGameStateOutputModel>>()
        .returnResult().responseBody!!

    fun playHttp(token: String, row: Int, col: Int) = client.put().uri(api("/game/play"))
        .header("Authorization", "Bearer $token")
        .bodyValue(mapOf("row" to row, "col" to col))
        .exchange()
        .expectStatus().isOk
        .expectBody<SirenEntity<Unit>>()
        .returnResult().responseBody!!

    fun getUserHttp(link: String) = client.get().uri(api(link))
        .exchange()
        .expectStatus().isOk
        .expectBody<SirenEntity<GetUserOutputModel>>()
        .returnResult().responseBody!!
}
