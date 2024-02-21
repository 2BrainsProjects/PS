package pt.isel.daw.gomoku.http.controllers.user.models

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import pt.isel.daw.gomoku.domain.user.UserDomain.Companion.MAX_EMAIL_LENGTH
import pt.isel.daw.gomoku.domain.user.UserDomain.Companion.MAX_PASSWORD_LENGTH
import pt.isel.daw.gomoku.domain.user.UserDomain.Companion.MAX_USERNAME_LENGTH
import pt.isel.daw.gomoku.domain.user.UserDomain.Companion.MIN_EMAIL_LENGTH
import pt.isel.daw.gomoku.domain.user.UserDomain.Companion.MIN_PASSWORD_LENGTH
import pt.isel.daw.gomoku.domain.user.UserDomain.Companion.MIN_USERNAME_LENGTH
import pt.isel.daw.gomoku.http.utils.Regex

data class RegisterInputModel(
    @field:NotBlank
    @field:Size(min = MIN_USERNAME_LENGTH, max = MAX_USERNAME_LENGTH)
    @field:Pattern(regexp = Regex.VALID_STRING, message = Regex.VALID_STRING_MSG)
    val name: String,

    @field:NotBlank
    @field:Size(min = MIN_EMAIL_LENGTH, max = MAX_EMAIL_LENGTH)
    @field:Email
    val email: String,

    @field:NotBlank
    @field:Size(min = MIN_PASSWORD_LENGTH, max = MAX_PASSWORD_LENGTH)
    @field:Pattern(regexp = Regex.SECURE_PASSWORD, message = Regex.SECURE_PASSWORD_MSG)
    val password: String
)
