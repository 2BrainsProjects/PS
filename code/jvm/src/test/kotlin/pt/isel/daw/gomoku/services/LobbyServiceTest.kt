package pt.isel.daw.gomoku.services

import org.junit.jupiter.api.BeforeEach
import pt.isel.daw.gomoku.domain.exceptions.LobbyException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class LobbyServiceTest : ServicesTest() {

    @BeforeEach
    fun setup() {
        resetLobby()
    }

    @Test
    fun `join a lobby and find a match`() {
        // given: two users and an empty lobby
        val user1 = registerAndGetTestUser()
        val user2 = registerAndGetTestUser()

        // when: user1 joins the lobby and tries to find a match
        lobbyService.joinLobby(user1, testGameConfig)
        val nullGameId = lobbyService.findMatch(user1.id)?.gameId

        // then: user1 couldn't find a match
        assertNull(nullGameId)

        // when: user2 joins the lobby and tries to find a match
        lobbyService.joinLobby(user2, testGameConfig)
        val gameId2 = lobbyService.findMatch(user2.id)?.gameId

        // then: user2 found a match
        assertNotNull(gameId2)

        // when: user1 tries to find a match again
        val gameId1 = lobbyService.findMatch(user1.id)?.gameId

        // then: user1 found a match
        assertNotNull(gameId1)
        assertEquals(gameId1, gameId2)
    }

    @Test
    fun `user trying to join the lobby twice fails`() {
        // given: a user and an empty lobby
        val user1 = registerAndGetTestUser()

        // when: user1 joins the lobby and tries to join again
        lobbyService.joinLobby(user1, testGameConfig)

        // then: user1 couldn't join the lobby again and an exception is thrown
        assertFailsWith<LobbyException.UserAlreadyInLobbyException> {
            lobbyService.joinLobby(user1, testGameConfig)
        }
    }

    @Test
    fun `trying to join the lobby when user is already in a game`() {
        // given: two users and an empty lobby
        val user1 = registerAndGetTestUser()
        val user2 = registerAndGetTestUser()

        // when: user1 and user2 join the lobby, a match is found and user1 tries to join the lobby again
        lobbyService.joinLobby(user1, testGameConfig)
        lobbyService.joinLobby(user2, testGameConfig)

        // then: user1 couldn't join the lobby again and an exception is thrown
        assertFailsWith<LobbyException.UserAlreadyInGameException> {
            lobbyService.joinLobby(user1, testGameConfig)
        }
    }

    @Test
    fun `leave the lobby`() {
        // given: a user in the lobby
        val user = registerAndGetTestUser()

        // when: user joins the lobby and then leaves
        lobbyService.joinLobby(user, testGameConfig)
        lobbyService.leaveLobby(user.id)

        // then: user is not in the lobby anymore
        assertFailsWith<LobbyException.UserNotInLobbyException> {
            lobbyService.leaveLobby(user.id)
        }
    }
}
