package pt.isel.ps.anonichat.repository.transaction

import pt.isel.ps.anonichat.repository.TokenRepository
import pt.isel.ps.anonichat.repository.UserRepository

interface Transaction {
    val userRepository: UserRepository
    val tokenRepository: TokenRepository
}
