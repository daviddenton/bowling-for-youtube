package com.oneeyedmen.bowling

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import kotlin.test.assertTrue

class BowlingTests {

    @Test
    fun `a game with no players isnt playable`() {
        expectThat(Game()).isA<CompletedGame>()
    }

    @Test
    fun `a game with no frames isnt playable`() {
        expectThat(Game("Fred", "Barney", frameCount = 0)).isA<CompletedGame>()
    }

    @Test
    fun `a game with players is playable`() {
        expectThat(Game("Fred")).isA<PlayableGame>()
        expectThat(Game("Fred", "Barney")).isA<PlayableGame>()
    }

    @Test
    fun `play a game`() {
        var game: Game
        game = Game("Fred", "Barney", frameCount = 2) as PlayableGame
        expectThat(game.currentPlayer).isEqualTo("Fred")
        game = game.roll(PinCount(1)) as PlayableGame
        expectThat(game.currentPlayer).isEqualTo("Fred")
        game = game.roll(PinCount(2)) as PlayableGame
        expectThat(game.currentPlayer).isEqualTo("Barney")
        game = game.roll(PinCount(3)) as PlayableGame
        expectThat(game.currentPlayer).isEqualTo("Barney")
        game = game.roll(PinCount(4)) as PlayableGame
        expectThat(game.currentPlayer).isEqualTo("Fred")
        game = game.roll(PinCount(5)) as PlayableGame
        game = game.roll(PinCount(3)) as PlayableGame
        expectThat(game.currentPlayer).isEqualTo("Barney")
        game = game.roll(PinCount(7)) as PlayableGame
        game = game.roll(PinCount(2))
        assertTrue(game is CompletedGame)
    }

    @Test
    fun `play a game with just one player and one frame`() {
        var game: Game
        game = Game("Fred", frameCount = 1) as PlayableGame
        expectThat(game.currentPlayer).isEqualTo("Fred")
        game = game.roll(PinCount(1)) as PlayableGame
        expectThat(game.currentPlayer).isEqualTo("Fred")
        game = game.roll(PinCount(1))
        assertTrue(game is CompletedGame)
    }

    @Test
    fun `render pathological scorecards`() {
        expectThat(Game().toScorecard()).isEqualTo("")

        expectThat(Game("Fred", "Barney", frameCount = 0).toScorecard()).isEqualTo(
            """
            Fred
            Barney
        """.trimIndent()
        )
    }

    @Test
    fun `render scorecard`() {
        var game: Game
        game = Game("Fred", "Barney", frameCount = 2) as PlayableGame
        expectThat(game.toScorecard()).isEqualTo(
            """
            Fred [ ][ ] 000 [ ][ ] 000
            Barney [ ][ ] 000 [ ][ ] 000
        """.trimIndent()
        )
        game = game.roll(PinCount(1)) as PlayableGame
        expectThat(game.toScorecard()).isEqualTo(
            """
            Fred [1][ ] 000 [ ][ ] 000
            Barney [ ][ ] 000 [ ][ ] 000
        """.trimIndent()
        )
        game = game.roll(PinCount(2)) as PlayableGame
        expectThat(game.toScorecard()).isEqualTo(
            """
            Fred [1][2] 000 [ ][ ] 000
            Barney [ ][ ] 000 [ ][ ] 000
        """.trimIndent()
        )
        game = game.roll(PinCount(10)) as PlayableGame
        expectThat(game.toScorecard()).isEqualTo(
            """
            Fred [1][2] 000 [ ][ ] 000
            Barney [ ][X] 000 [ ][ ] 000
        """.trimIndent()
        )
        game = game.roll(PinCount(9)) as PlayableGame
        expectThat(game.toScorecard()).isEqualTo(
            """
            Fred [1][2] 000 [9][ ] 000
            Barney [ ][X] 000 [ ][ ] 000
        """.trimIndent()
        )
        game = game.roll(PinCount(1)) as PlayableGame
        expectThat(game.toScorecard()).isEqualTo(
            """
            Fred [1][2] 000 [9][/] 000
            Barney [ ][X] 000 [ ][ ] 000
        """.trimIndent()
        )
        game = game.roll(PinCount(0)) as PlayableGame
        expectThat(game.toScorecard()).isEqualTo(
            """
            Fred [1][2] 000 [9][/] 000
            Barney [ ][X] 000 [-][ ] 000
        """.trimIndent()
        )
    }
}