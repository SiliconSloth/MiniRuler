package siliconsloth.miniruler

import com.mojang.ld22.InputHandler

enum class TitleOption {
    START_GAME, HOW_TO_PLAY, ABOUT;

    companion object {
        fun fromSelection(selection: Int): TitleOption =
            when (selection) {
                0 -> START_GAME
                1 -> HOW_TO_PLAY
                2 -> ABOUT
                else -> throw RuntimeException("Unknown title selection")
            }
    }
}

enum class Key {
    UP, DOWN, LEFT, RIGHT, ATTACK, MENU
}