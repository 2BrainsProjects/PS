package pt.isel.daw.gomoku.http.controllers.user.models

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import pt.isel.daw.gomoku.http.utils.Regex

data class LoginInputModel(

    @field:Pattern(regexp = Regex.VALID_STRING, message = Regex.VALID_STRING_MSG)
    val name: String? = null,

    @field:Email
    val email: String? = null,

    @field:NotBlank
    val password: String
)
