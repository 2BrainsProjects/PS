package pt.isel.daw.gomoku.repository.jdbi.utils

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.PostgresPlugin
import pt.isel.daw.gomoku.repository.jdbi.mappers.column.InstantMapper
import pt.isel.daw.gomoku.repository.jdbi.mappers.column.StatsMapper
import pt.isel.daw.gomoku.repository.jdbi.mappers.row.GameMapper
import pt.isel.daw.gomoku.repository.jdbi.mappers.row.PlayerMapper

fun Jdbi.configure(): Jdbi {
    installPlugin(KotlinPlugin())
    installPlugin(PostgresPlugin())

    registerRowMapper(GameMapper())
    registerRowMapper(PlayerMapper())
    registerColumnMapper(InstantMapper())
    registerColumnMapper(StatsMapper())

    return this
}
