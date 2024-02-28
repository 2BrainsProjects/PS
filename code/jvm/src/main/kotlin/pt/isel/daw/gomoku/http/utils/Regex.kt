package pt.isel.daw.gomoku.http.utils

object Regex {
    const val VALID_STRING = "(?=.*[a-zA-Z])[a-zA-Z0-9!?@#_]*$"
    const val VALID_STRING_MSG = "must be in a valid string format"
    const val SECURE_PASSWORD = "(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#\$%^&*()\\-__+.]).*$"
    const val SECURE_PASSWORD_MSG = "must have uppercase and lowercase letters, at least a number and a special character"
    const val VALID_IP = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    const val VALID_IP_MSG = "must be a valid IP address"
}
