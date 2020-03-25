package siliconsloth.miniruler

import com.mojang.ld22.entity.*
import com.mojang.ld22.entity.particle.SmashParticle
import com.mojang.ld22.entity.particle.TextParticle
import com.mojang.ld22.screen.*
import com.mojang.ld22.screen.Menu as GameMenu
import com.mojang.ld22.level.tile.Tile as GameTile
import com.mojang.ld22.entity.Entity as GameEntity
import java.security.InvalidParameterException

enum class Key {
    UP, DOWN, LEFT, RIGHT, ATTACK, MENU
}

enum class Direction {
    UP, DOWN, LEFT, RIGHT
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

enum class Entity(val solid: Boolean = false) {
    GRASS, ROCK(true), WATER, FLOWER, TREE(true), DIRT, SAND, CACTUS(true), HOLE, TREE_SAPLING, CACTUS_SAPLING,
    FARMLAND, WHEAT, LAVA(true), STAIRS_DOWN, STAIRS_UP, INFINITE_FALL(true), CLOUD, HARD_ROCK(true),
    IRON_ORE(true), GOLD_ORE(true), GEM_ORE(true), CLOUD_CACTUS(true),
    AIR_WIZARD(true), ANVIL(true), CHEST(true), FURNACE(true), ITEM, LANTERN(true), OVEN(true), PLAYER(true),
    SLIME(true), SPARK, WORKBENCH(true), ZOMBIE(true), SMASH_PARTICLE, TEXT_PARTICLE;

    companion object {
        fun fromGame(tile: GameTile): Entity =
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

        fun fromGame(entity: GameEntity): Entity =
                when (entity) {
                    is AirWizard -> AIR_WIZARD
                    is Anvil -> ANVIL
                    is Chest -> CHEST
                    is Furnace -> FURNACE
                    is ItemEntity -> ITEM
                    is Lantern -> LANTERN
                    is Oven -> OVEN
                    is Player -> PLAYER
                    is Slime -> SLIME
                    is Spark -> SPARK
                    is Workbench -> WORKBENCH
                    is Zombie -> ZOMBIE
                    is SmashParticle -> SMASH_PARTICLE
                    is TextParticle -> TEXT_PARTICLE
                    else -> throw InvalidParameterException("Unknown entity")
                }
    }
}