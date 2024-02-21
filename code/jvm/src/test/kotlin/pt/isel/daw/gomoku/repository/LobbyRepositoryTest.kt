package pt.isel.daw.gomoku.repository

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LobbyRepositoryTest : RepositoryTest() {

    @Test
    fun `get the players in a lobby`() {
        // given: a lobby and a user
        val lobbyInitSize = lobbyRepository.getLobby().size
        val userId = registerTestUser()

        // when: adding the user to the lobby
        lobbyRepository.addUserToLobby(userId, testGameConfig)

        // then: the user is in the lobby
        assertEquals(lobbyInitSize + 1, lobbyRepository.getLobby().size)
        assertTrue(lobbyRepository.isUserInLobby(userId))
    }

    @Test
    fun `remove user from lobby`() {
        // given: a lobby and a user
        val userId = registerTestUser()
        val gameConfig = testGameConfig
        val initialPlayersInLobby = lobbyRepository.getLobby().size

        // when: adding the user to the lobby
        lobbyRepository.addUserToLobby(userId, gameConfig)

        // then: the user is in the lobby
        assertEquals(lobbyRepository.getLobby().size, initialPlayersInLobby + 1)

        // when: removing the user from the lobby
        lobbyRepository.removeUserFromLobby(userId)

        // then: the user is not in the lobby
        assertEquals(lobbyRepository.getLobby().size, initialPlayersInLobby)
    }
}
