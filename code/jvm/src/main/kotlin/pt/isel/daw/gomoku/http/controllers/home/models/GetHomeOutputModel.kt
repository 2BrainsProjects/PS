package pt.isel.daw.gomoku.http.controllers.home.models

data class GetHomeOutputModel(
    val title: String,
    val version: String,
    val description: String,
    val authors: List<AuthorModel>,
    val repository: String
)
