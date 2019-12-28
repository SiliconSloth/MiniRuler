package siliconsloth.miniruler

import com.mojang.ld22.InputHandler
import com.mojang.ld22.screen.*
import com.mojang.ld22.screen.Menu as GameMenu
import java.security.InvalidParameterException

enum class Key {
    UP, DOWN, LEFT, RIGHT, ATTACK, MENU
}

enum class Menu {
    ABOUT, CONTAINER, CRAFTING, DEAD, INSTRUCTIONS, INVENTORY, LEVEL_TRANSITION, TITLE, WON;

    companion object {
        fun fromGameMenu(menu: GameMenu): Menu =
                when (menu) {
                    is AboutMenu -> ABOUT
                    is ContainerMenu -> CONTAINER
                    is CraftingMenu -> CRAFTING
                    is DeadMenu -> DEAD
                    is InstructionsMenu -> INSTRUCTIONS
                    is InventoryMenu -> INVENTORY
                    is LevelTransitionMenu -> LEVEL_TRANSITION
                    is TitleMenu -> TITLE
                    is WonMenu -> WON
                    else -> throw InvalidParameterException("Unknown menu")
                }
    }
}

enum class TitleOption {
    START_GAME, HOW_TO_PLAY, ABOUT;

    companion object {
        fun fromSelection(selection: Int): TitleOption =
                when (selection) {
                    0 -> START_GAME
                    1 -> HOW_TO_PLAY
                    2 -> ABOUT
                    else -> throw InvalidParameterException("Unknown title selection")
                }
    }
}