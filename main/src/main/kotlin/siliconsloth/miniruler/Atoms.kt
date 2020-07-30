package siliconsloth.miniruler

import com.mojang.ld22.entity.*
import com.mojang.ld22.entity.particle.SmashParticle
import com.mojang.ld22.entity.particle.TextParticle
import com.mojang.ld22.item.*
import com.mojang.ld22.item.resource.Resource
import com.mojang.ld22.screen.*
import siliconsloth.miniruler.math.Vector
import com.mojang.ld22.screen.Menu as GameMenu
import com.mojang.ld22.level.tile.Tile as GameTile
import com.mojang.ld22.entity.Entity as GameEntity
import com.mojang.ld22.item.Item as GameItem
import java.security.InvalidParameterException

enum class Direction(val vector: Vector) {
    UP(Vector(0,-1)), DOWN(Vector(0,1)), LEFT(Vector(-1,0)), RIGHT(Vector(1,0))
}

enum class Key {
    UP, DOWN, LEFT, RIGHT, ATTACK, MENU;

    companion object {
        fun fromDirection(direction: Direction): Key =
                when (direction) {
                    Direction.UP -> UP
                    Direction.DOWN -> DOWN
                    Direction.LEFT -> LEFT
                    Direction.RIGHT -> RIGHT
                }
    }
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

enum class Entity(val solid: Boolean = false, val r: Vector = Vector(8,8)) {
    GRASS, ROCK(true), WATER, FLOWER, TREE(true), DIRT, SAND, CACTUS(true), HOLE, TREE_SAPLING, CACTUS_SAPLING,
    FARMLAND, WHEAT, LAVA(true), STAIRS_DOWN, STAIRS_UP, INFINITE_FALL(true), CLOUD, HARD_ROCK(true),
    IRON_ORE(true), GOLD_ORE(true), GEM_ORE(true), CLOUD_CACTUS(true),
    AIR_WIZARD(true, Vector(4,3)), ANVIL(true, Vector(3,2)), CHEST(true, Vector(3,3)), FURNACE(true, Vector(3,2)),
    ITEM(false, Vector(3,3)), LANTERN(true, Vector(3,2)), OVEN(true, Vector(3,2)), PLAYER(true, Vector(4,3)),
    SLIME(true, Vector(4,3)), SPARK(false, Vector(0,0)), WORKBENCH(true, Vector(3,2)), ZOMBIE(true, Vector(4,3)),
    SMASH_PARTICLE(false, Vector(6,6)), TEXT_PARTICLE(false, Vector(6,6));

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

enum class Item {
    WOOD, STONE, FLOWER, ACORN, DIRT, SAND, CACTUS, SEEDS, WHEAT, BREAD, APPLE,
    COAL, IRON_ORE, GOLD_ORE, IRON_INGOT, GOLD_INGOT,
    SLIME, GLASS, CLOTH, CLOUD, GEM,

    WOOD_SHOVEL, WOOD_HOE, WOOD_SWORD, WOOD_PICKAXE, WOOD_AXE,
    ROCK_SHOVEL, ROCK_HOE, ROCK_SWORD, ROCK_PICKAXE, ROCK_AXE,
    IRON_SHOVEL, IRON_HOE, IRON_SWORD, IRON_PICKAXE, IRON_AXE,
    GOLD_SHOVEL, GOLD_HOE, GOLD_SWORD, GOLD_PICKAXE, GOLD_AXE,
    GEM_SHOVEL, GEM_HOE, GEM_SWORD, GEM_PICKAXE, GEM_AXE,
    POWER_GLOVE,

    ANVIL, CHEST, FURNACE, LANTERN, OVEN, WORKBENCH;
    
    companion object {
        fun fromGame(item: GameItem): Item =
                when (item) {
                    is ResourceItem -> fromGame(item)
                    is ToolItem -> fromGame(item)
                    is PowerGloveItem -> fromGame(item)
                    is FurnitureItem -> fromGame(item)
                    else -> throw InvalidParameterException("Unknown item type")
                }

        fun fromGame(item: ResourceItem): Item =
                when (item.resource) {
                    Resource.wood -> WOOD
                    Resource.stone -> STONE
                    Resource.flower -> FLOWER
                    Resource.acorn -> ACORN
                    Resource.dirt -> DIRT
                    Resource.sand -> SAND
                    Resource.cactusFlower -> CACTUS
                    Resource.seeds -> SEEDS
                    Resource.wheat -> WHEAT
                    Resource.bread -> BREAD
                    Resource.apple -> APPLE
                    Resource.coal -> COAL
                    Resource.ironOre -> IRON_ORE
                    Resource.goldOre -> GOLD_ORE
                    Resource.ironIngot -> IRON_INGOT
                    Resource.goldIngot -> GOLD_INGOT
                    Resource.slime -> SLIME
                    Resource.glass -> GLASS
                    Resource.cloth -> CLOTH
                    Resource.cloud -> CLOUD
                    Resource.gem -> GEM
                    else -> throw InvalidParameterException("Unknown resource")
                }
        
        fun fromGame(item: ToolItem): Item =
                when (item.level) {
                    0 -> when (item.type) {
                        ToolType.shovel -> WOOD_SHOVEL
                        ToolType.hoe -> WOOD_HOE
                        ToolType.sword -> WOOD_SWORD
                        ToolType.pickaxe -> WOOD_PICKAXE
                        ToolType.axe -> WOOD_AXE
                        else -> throw InvalidParameterException("Unknown tool type")
                    }
                    
                    1 -> when (item.type) {
                        ToolType.shovel -> ROCK_SHOVEL
                        ToolType.hoe -> ROCK_HOE
                        ToolType.sword -> ROCK_SWORD
                        ToolType.pickaxe -> ROCK_PICKAXE
                        ToolType.axe -> ROCK_AXE
                        else -> throw InvalidParameterException("Unknown tool type")
                    }
                    
                    2 -> when (item.type) {
                        ToolType.shovel -> IRON_SHOVEL
                        ToolType.hoe -> IRON_HOE
                        ToolType.sword -> IRON_SWORD
                        ToolType.pickaxe -> IRON_PICKAXE
                        ToolType.axe -> IRON_AXE
                        else -> throw InvalidParameterException("Unknown tool type")
                    }
                    
                    3 -> when (item.type) {
                        ToolType.shovel -> GOLD_SHOVEL
                        ToolType.hoe -> GOLD_HOE
                        ToolType.sword -> GOLD_SWORD
                        ToolType.pickaxe -> GOLD_PICKAXE
                        ToolType.axe -> GOLD_AXE
                        else -> throw InvalidParameterException("Unknown tool type")
                    }
                    
                    4 -> when (item.type) {
                        ToolType.shovel -> GEM_SHOVEL
                        ToolType.hoe -> GEM_HOE
                        ToolType.sword -> GEM_SWORD
                        ToolType.pickaxe -> GEM_PICKAXE
                        ToolType.axe -> GEM_AXE
                        else -> throw InvalidParameterException("Unknown tool type")
                    }

                    else -> throw InvalidParameterException("Unknown tool level")
                }

        fun fromGame(item: PowerGloveItem): Item = POWER_GLOVE

        fun fromGame(item: FurnitureItem): Item =
                when (item.furniture) {
                    is Anvil -> ANVIL
                    is Chest -> CHEST
                    is Furnace -> FURNACE
                    is Lantern -> LANTERN
                    is Oven -> OVEN
                    is Workbench -> WORKBENCH
                    else -> throw InvalidParameterException("Unknown furniture")
                }
    }
}