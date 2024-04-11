package pt.isel.ps.anonichat.domain.exceptions

sealed class RouterException(msg: String) : Exception(msg) {
    class InvalidCredentialsException(msg: String) : RouterException(msg)
}