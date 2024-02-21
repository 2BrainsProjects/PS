package pt.isel.daw.gomoku.repository

import kotlinx.datetime.Instant
import pt.isel.daw.gomoku.domain.user.Token
import pt.isel.daw.gomoku.domain.user.User

interface TokenRepository {
    fun createToken(token: Token, maxTokens: Int)
    fun getTokenHash(userId: Int): String?
    fun updateUserTokenHash(id: Int, tokenHash: String): Boolean
    fun getUserAndTokenByTokenHash(tokenHash: String): Pair<User, Token>?
    fun updateTokenLastUsed(tokenHash: String, now: Instant): Boolean
    fun removeTokenByTokenHash(tokenHash: String): Boolean
}
