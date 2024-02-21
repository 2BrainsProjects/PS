package pt.isel.daw.gomoku.domain.lobby

import pt.isel.daw.gomoku.domain.game.GameConfig
import pt.isel.daw.gomoku.domain.user.Stats
import pt.isel.daw.gomoku.domain.user.User
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LobbyTests {

    @Test
    fun `trying to find a match with a empty lobby`() {
        // given: an empty lobby
        val lobby = Lobby()

        // when: trying to find a match for a user
        val opponent = domain.findMatch(user1, lobby, gameConfig)

        // then: the lobby is empty and the opponent is null
        assertTrue(lobby.players.isEmpty())
        assertEquals(null, opponent)
    }

    @Test
    fun `successfully find a match with a non empty lobby`() {
        // given: a lobby with a player
        val lobby = Lobby(listOf(Player(user2, gameConfig)))

        // when: trying to find a match for a user
        val opponent = domain.findMatch(user1, lobby, gameConfig)

        // then: the lobby still has the player and the opponent is the player in the lobby
        assertEquals(1, lobby.players.size)
        assertEquals(user2, opponent)
    }

    @Test
    fun `cant find a match due to the difference in rating`() {
        // given: a lobby with a player
        val lobby = Lobby(listOf(Player(user3, gameConfig)))

        // when: trying to find a match for a user with a lower rating than the player in the lobby
        val opponent = domain.findMatch(user1, lobby, gameConfig)

        // then: the lobby still has the player and the opponent is null
        assertEquals(1, lobby.players.size)
        assertEquals(null, opponent)
    }

    @Test
    fun `cant find a match due to different game configs`() {
        // given: a lobby with a player
        val lobby = Lobby(listOf(Player(user3, gameConfig2)))

        // when: trying to find a match for a user with a different game config than the player in the lobby
        val opponent = domain.findMatch(user1, lobby, gameConfig)

        // then: the lobby still has the player and the opponent is null
        assertEquals(1, lobby.players.size)
        assertEquals(null, opponent)
    }

    companion object {
        val domain = LobbyDomain()
        val gameConfig = GameConfig(15, "FREESTYLE", "FREESTYLE")
        val gameConfig2 = GameConfig(19, "FREESTYLE", "FREESTYLE")

        private val stats = Stats(0, 0, 0, 0, 0)
        private val buffedStats = Stats(1000, 500, 400, 99, 1)

        val user1 = User(1, "user1", "11231rwasd", "11231rwasd@gmail.com", stats)
        val user2 = User(2, "user2", "adw123d431", "adw123d431@gmail.com", stats)
        val user3 = User(3, "user3", "asdsadwa12", "asdsadwa12@gmail.com", buffedStats)
    }
}
