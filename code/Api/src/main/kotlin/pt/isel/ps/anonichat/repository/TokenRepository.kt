package pt.isel.ps.anonichat.repository

import kotlinx.datetime.Instant
import pt.isel.ps.anonichat.domain.user.Token
import pt.isel.ps.anonichat.domain.user.User

interface TokenRepository {
    fun createToken(token: Token, maxTokens: Int)
    fun getTokenHash(userId: Int): String?
    fun getUserAndTokenByTokenHash(tokenHash: String): Pair<User, Token>?
    fun updateTokenLastUsed(tokenHash: String, now: Instant): Boolean
    fun removeTokenByTokenHash(tokenHash: String): Boolean
}
