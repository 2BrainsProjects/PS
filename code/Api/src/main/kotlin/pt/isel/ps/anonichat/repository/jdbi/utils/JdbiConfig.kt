package pt.isel.ps.anonichat.repository.jdbi.utils

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.PostgresPlugin
import pt.isel.ps.anonichat.repository.jdbi.mappers.column.InstantMapper

fun Jdbi.configure(): Jdbi {
    installPlugin(KotlinPlugin())
    installPlugin(PostgresPlugin())
    registerColumnMapper(InstantMapper())
    return this
}
