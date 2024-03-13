package pt.isel.ps.anonichat.domain.user.utils

interface TokenEncoder {
    fun matches(token: String, validationInfo: String): Boolean
    fun hash(input: String): String
}
