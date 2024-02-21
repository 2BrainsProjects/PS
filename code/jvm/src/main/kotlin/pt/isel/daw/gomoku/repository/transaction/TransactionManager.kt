package pt.isel.daw.gomoku.repository.transaction

interface TransactionManager {
    fun <R> run(block: (Transaction) -> R): R
}
