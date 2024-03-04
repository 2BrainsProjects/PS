package pt.isel.ps.anonichat.repository.transaction

interface TransactionManager {
    fun <R> run(block: (Transaction) -> R): R
}
