package pt.isel.daw.gomoku.domain.user

/**
 * Represents the statistics of a user
 * @property rating The rating of the user
 * @property gamesPlayed The number of games played by the user
 * @property wins The number of games won by the user
 * @property draws The number of games drawn by the user
 * @property losses The number of games lost by the user
 */
data class Stats(
    val rating: Int,
    val gamesPlayed: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int
) {
    init {
        require(rating >= 0) { "Rating cannot be negative" }
        require(gamesPlayed >= 0) { "Games played cannot be negative" }
        require(wins >= 0) { "Wins cannot be negative" }
        require(draws >= 0) { "Draws cannot be negative" }
        require(losses >= 0) { "Losses cannot be negative" }
    }
}
