package pt.isel.daw.gomoku.services.models

import kotlinx.datetime.Instant

data class TokenModel(
    val value: String,
    val expiration: Instant
)
