package pt.isel.daw.gomoku.domain.exceptions

/**
 * Represents a user exception
 * @param msg the exception message
 */
sealed class UserException(msg: String) : Exception(msg) {
    class UserNotFoundException(msg: String) : UserException(msg)
    class UserAlreadyExistsException(msg: String) : UserException(msg)
    class InvalidCredentialsException(msg: String) : UserException(msg)
    class TokenNotFoundException(msg: String) : UserException(msg)
    class InvalidTokenException(msg: String) : UserException(msg)
    class UnauthorizedException(msg: String) : UserException(msg)
}
