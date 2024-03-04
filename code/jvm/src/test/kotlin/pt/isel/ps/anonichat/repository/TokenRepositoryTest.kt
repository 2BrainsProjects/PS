package pt.isel.ps.anonichat.repository

import kotlinx.datetime.Clock
import pt.isel.ps.anonichat.domain.user.Token
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TokenRepositoryTest : RepositoryTest() {

    @Test
    fun `can create, get a token, updated last used and delete token`() {
        // given: a user
        val userId = registerTestUser()
        val tokenValue = generateToken()
        val tokenHash = hashToken(tokenValue)
        val token = Token(tokenHash, userId, Clock.System.now(), Clock.System.now())

        // when: creating a token
        tokenRepository.createToken(token, MAX_TOKENS_PER_USER)

        // then: the token retrieved with the user id is the same as the one created
        val gotTokenHash = tokenRepository.getTokenHash(userId)
        assertEquals(tokenHash, gotTokenHash)

        // when: getting the user and token by the token hash
        val (user, gotToken) = tokenRepository.getUserAndTokenByTokenHash(tokenHash)!!

        // then: the user and token retrieved are the same as the ones created
        assertEquals(userId, user.id)
        assertEquals(tokenHash, gotToken.tokenHash)
        assertEquals(token.createdAt.epochSeconds, gotToken.createdAt.epochSeconds)
        assertEquals(token.lastUsedAt.epochSeconds, gotToken.lastUsedAt.epochSeconds)

        // when: updating the token last used
        val updatedToken = Token(tokenHash, userId, Clock.System.now(), Clock.System.now())
        tokenRepository.updateTokenLastUsed(tokenHash, updatedToken.lastUsedAt)

        // then: the token last used is updated
        val (_, gotUpdatedToken) = tokenRepository.getUserAndTokenByTokenHash(tokenHash)!!
        assertEquals(updatedToken.lastUsedAt.epochSeconds, gotUpdatedToken.lastUsedAt.epochSeconds)

        // when: removing the token from a user
        tokenRepository.removeTokenByTokenHash(tokenHash)

        // then: the token is removed and the user no longer has that token
        assertNull(tokenRepository.getTokenHash(userId))
        assertNull(tokenRepository.getUserAndTokenByTokenHash(tokenHash))
    }
}
