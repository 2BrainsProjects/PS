package pt.isel.daw.gomoku.repository.jdbi.transaction

import org.jdbi.v3.core.Handle
import pt.isel.daw.gomoku.repository.jdbi.JdbiTokenRepository
import pt.isel.daw.gomoku.repository.jdbi.JdbiUserRepository
import pt.isel.daw.gomoku.repository.transaction.Transaction

class JdbiTransaction(handle: Handle) : Transaction {
    override val userRepository = JdbiUserRepository(handle)
    override val tokenRepository = JdbiTokenRepository(handle)
}
