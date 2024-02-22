package pt.isel.daw.gomoku.repository.transaction

import pt.isel.daw.gomoku.repository.TokenRepository
import pt.isel.daw.gomoku.repository.UserRepository

interface Transaction {
    val userRepository: UserRepository
    val tokenRepository: TokenRepository
}
