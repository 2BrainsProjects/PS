package pt.isel.ps.anonichat.repository.jdbi.transaction

import org.jdbi.v3.core.Handle
import pt.isel.ps.anonichat.repository.jdbi.JdbiMessageRepository
import pt.isel.ps.anonichat.repository.jdbi.JdbiRouterRepository
import pt.isel.ps.anonichat.repository.jdbi.JdbiTokenRepository
import pt.isel.ps.anonichat.repository.jdbi.JdbiUserRepository
import pt.isel.ps.anonichat.repository.transaction.Transaction

class JdbiTransaction(handle: Handle) : Transaction {
    override val userRepository = JdbiUserRepository(handle)
    override val tokenRepository = JdbiTokenRepository(handle)
    override val routerRepository = JdbiRouterRepository(handle)
    override val messageRepository = JdbiMessageRepository(handle)
}
