@file:Suppress("ArrayInDataClass")

package com.oneeyedmen.bowling

sealed interface Game {

    val playerNames: List<String>
    val frames: List<Frame>

    companion object {
        operator fun invoke(vararg playerNames: String, frameCount: Int = 10) =
            when {
                playerNames.isEmpty() || frameCount == 0 -> CompletedGame(playerNames.toList(), emptyList())
                else -> PlayableGame(playerNames.toList(), frameCount)
            }
    }
}

data class PlayableGame(
    override val playerNames: List<String>,
    private val frameCount: Int,
    override val frames: List<Frame> = playerNames.map { PlayableFrame(playerNames.map { NewPlayerFrame }) }
) : Game {
    val currentPlayer: String
        get() {
            val currentIndex =
                frames.filterIsInstance<PlayableFrame>()
                    .map { it.frames.indexOfFirst { it is PlayablePlayerFrame } }
                    .first()
            return when {
                currentIndex == -1 -> playerNames.first()
                else -> playerNames[currentIndex]
            }
        }

    fun roll(count: PinCount): Game {
        val current = frames.filterIsInstance<PlayableFrame>().first()
        val next = current.roll(count)
        return when {
            next is CompletedFrame && frames.isLast(current) ->
                CompletedGame(playerNames, frames.replace(current, next))

            else -> PlayableGame(playerNames, frameCount, frames.replace(current, next))
        }
    }
}

fun Game.toScorecard() = playerNames
    .mapIndexed { i, next ->
        "$next " + frames.joinToString(" ") {
            it.frames[i].toString() + " 000"
        }
    }
    .joinToString("\n") { it.trim() }

@JvmInline
value class PinCount(val value: Int) {
    init {
        require(value in 0..10) { value.toString() }
    }

    override fun toString() = if (value == 0) "-" else value.toString()
}

sealed interface Frame {
    val frames: List<PlayerFrame>
}

class CompletedFrame(override val frames: List<PlayerFrame>) : Frame

data class CompletedGame(override val playerNames: List<String>, override val frames: List<Frame>) : Game

data class PlayableFrame(override val frames: List<PlayerFrame>) : Frame {
    fun roll(count: PinCount): Frame {
        val start = frames.filterIsInstance<PlayablePlayerFrame>().first()
        val next = start.roll(count)
        return when {
            next is CompletedPlayerFrame && frames.isLast(start) -> CompletedFrame(frames.replace(start, next))
            else -> PlayableFrame(frames.replace(start, next))
        }
    }
}

sealed interface PlayerFrame {
    val score: PinCount
}

sealed interface PlayablePlayerFrame : PlayerFrame {
    fun roll(count: PinCount): PlayerFrame
}

sealed interface CompletedPlayerFrame : PlayerFrame

data object NewPlayerFrame : PlayablePlayerFrame {
    override fun roll(count: PinCount) = when (count.value) {
        10 -> Strike
        else -> Open(count)
    }

    override val score = PinCount(0)

    override fun toString() = "[ ][ ]"
}

data class Open(override val score: PinCount) : PlayablePlayerFrame {
    override fun roll(count: PinCount) = when {
        score.value + count.value == 10 -> Spare(score)
        else -> Standard(score, count)
    }

    override fun toString() = "[$score][ ]"
}

data object Strike : CompletedPlayerFrame {
    override val score = PinCount(10)
    override fun toString() = "[ ][X]"
}

data class Spare(private val first: PinCount) : CompletedPlayerFrame {
    override val score = PinCount((10))
    override fun toString() = "[$first][/]"
}

data class Standard(val first: PinCount, val second: PinCount) : CompletedPlayerFrame {
    override val score = PinCount(first.value + second.value)
    override fun toString() = "[$first][$second]"
}

fun <T> List<T>.isLast(current: T) = last() == current

private fun <E> List<E>.replace(start: E, next: E) = toMutableList().also {
    it[indexOf(start)] = next
}
