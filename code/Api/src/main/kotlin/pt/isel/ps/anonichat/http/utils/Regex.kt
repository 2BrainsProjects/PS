package pt.isel.ps.anonichat.http.utils

object Regex {
    const val VALID_STRING = "(?=.*[a-zA-Z])[a-zA-Z0-9!?@#_]*$"
    const val VALID_STRING_MSG = "must be in a valid string format"
    const val SECURE_PASSWORD = "(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!\"#\$%&'()*+,-./:;<=>?@\\^_`{|}~]).*$"
    const val SECURE_PASSWORD_MSG = "must have uppercase and lowercase letters, at least a number and a special character"
}
