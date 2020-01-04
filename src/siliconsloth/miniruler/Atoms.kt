package siliconsloth.miniruler

import com.mojang.ld22.InputHandler
import com.mojang.ld22.level.tile.GrassTile
import com.mojang.ld22.level.tile.RockTile
import com.mojang.ld22.screen.*
import com.mojang.ld22.screen.Menu as GameMenu
import com.mojang.ld22.level.tile.Tile as GameTile
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

enum class Tile {
    GRASS, ROCK, WATER, FLOWER, TREE, DIRT, SAND, CACTUS, HOLE, TREE_SAPLING, CACTUS_SAPLING,
    FARMLAND, WHEAT, LAVA, STAIRS_DOWN, STAIRS_UP, INFINITE_FALL, CLOUD, HARD_ROCK,
    IRON_ORE, GOLD_ORE, GEM_ORE, CLOUD_CACTUS;

    companion object {
        fun fromGameTile(tile: GameTile): Tile =
                when (tile) {
                    GameTile.grass -> GRASS
                    GameTile.rock -> ROCK
                    GameTile.water -> WATER
                    GameTile.flower -> FLOWER
                    GameTile.tree -> TREE
                    GameTile.dirt -> DIRT
                    GameTile.sand -> SAND
                    GameTile.cactus -> CACTUS
                    GameTile.hole -> HOLE
                    GameTile.treeSapling -> TREE_SAPLING
                    GameTile.cactusSapling -> CACTUS_SAPLING
                    GameTile.farmland -> FARMLAND
                    GameTile.wheat -> WHEAT
                    GameTile.lava -> LAVA
                    GameTile.stairsDown -> STAIRS_DOWN
                    GameTile.stairsUp -> STAIRS_UP
                    GameTile.infiniteFall -> INFINITE_FALL
                    GameTile.cloud -> CLOUD
                    GameTile.hardRock -> HARD_ROCK
                    GameTile.ironOre -> IRON_ORE
                    GameTile.goldOre -> GOLD_ORE
                    GameTile.gemOre -> GEM_ORE
                    GameTile.cloudCactus -> CLOUD_CACTUS
                    else -> throw InvalidParameterException("Unknown tile")
                }
    }
}