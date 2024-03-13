package pt.isel.ps.anonichat.repository.jdbi.transaction

import org.jdbi.v3.core.Jdbi
import org.springframework.stereotype.Component
import pt.isel.ps.anonichat.repository.transaction.Transaction
import pt.isel.ps.anonichat.repository.transaction.TransactionManager

@Component
class JdbiTransactionManager(private val jdbi: Jdbi) : TransactionManager {
    override fun <R> run(block: (Transaction) -> R): R =
        jdbi.inTransaction<R, Exception> { handle ->
            val transaction = JdbiTransaction(handle)
            block(transaction)
        }
}
