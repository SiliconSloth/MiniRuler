package siliconsloth.miniruler

import com.mojang.ld22.GameListener
import com.mojang.ld22.entity.Mob
import com.mojang.ld22.item.ResourceItem
import com.mojang.ld22.screen.InventoryMenu
import com.mojang.ld22.level.tile.Tile as GameTile
import com.mojang.ld22.screen.Menu as GameMenu
import com.mojang.ld22.entity.Entity as GameEntity
import com.mojang.ld22.item.Item as GameItem
import com.mojang.ld22.screen.TitleMenu
import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.engine.builders.AtomicBuilder
import siliconsloth.miniruler.math.Vector
import java.lang.RuntimeException

class PerceptionHandler(private val engine: RuleEngine): GameListener {
    private var menu: Menu? = null
    private var inventory = mutableListOf<InventoryItem>()
    private var frame = 0

    override fun onMenuChange(oldMenu: GameMenu?, newMenu: GameMenu?) = engine.atomic {
        menu = newMenu?.let { Menu.fromGameMenu(it) }

        oldMenu?.let { delete(MenuOpen(Menu.fromGameMenu(it))) }
        newMenu?.let { insert(MenuOpen(Menu.fromGameMenu(it))) }

        if (oldMenu is TitleMenu) {
            delete(TitleSelection(TitleOption.fromSelection(oldMenu.selected)))
        }
        if (newMenu is TitleMenu) {
            insert(TitleSelection(TitleOption.fromSelection(newMenu.selected)))
        }

        if (oldMenu is InventoryMenu) {
            deleteAll<InventoryItem>()
            inventory.clear()
        }
    }

    override fun onTitleOptionSelect(selection: Int) = engine.atomic {
        insert(TitleSelection(TitleOption.fromSelection(selection)))
    }

    override fun onInventoryRender(items: List<GameItem>) = engine.atomic {
        // Replace the items in inventory with the new ones, if they differ.
        var index = 0
        items.forEach { gameItem ->
            val item = InventoryItem(Item.fromGame(gameItem), if (gameItem is ResourceItem) gameItem.count else 1, index)

            if (index < inventory.size) {
                // Only replace if different.
                if (inventory[index] != item) {
                    replace(inventory[index], item)
                    inventory[index] = item
                }
            } else {
                insert(item)
                inventory.add(item)
            }

            index++
        }

        // Remove any excess items in inventory.
        while (index < inventory.size) {
            delete(inventory[index])
            inventory.removeAt(index)
        }
    }

    override fun onRender(tiles: Array<out Array<GameTile>>, entities: List<GameEntity>,
                          xScroll: Int, yScroll: Int, stamina: Int) = engine.atomic {
        if (menu == Menu.TITLE || menu == Menu.INSTRUCTIONS || menu == Menu.ABOUT) {
            return@atomic
        }

        deleteAll<CameraLocation>()
        insert(CameraLocation(Vector(xScroll, yScroll), frame))

        deleteAll<Sighting>()
        updateTiles(tiles, xScroll % 16, yScroll % 16)
        updateEntities(entities, xScroll, yScroll)

        deleteAll<StaminaLevel>()
        insert(StaminaLevel(stamina))

        frame++
    }

    // Center is relative to tile array.
    private fun AtomicBuilder.updateTiles(tiles: Array<out Array<GameTile>>, xOffset: Int, yOffset: Int) {
        tiles.forEachIndexed { x, column -> column.forEachIndexed { y, tile ->
            insert(Sighting(Entity.fromGame(tile), Vector(x*16 + 8 - xOffset, y*16 + 8 - yOffset), Direction.DOWN, frame))
        } }
    }

    private fun AtomicBuilder.updateEntities(entities: List<GameEntity>, cameraX: Int, cameraY: Int) {
        entities.forEach { entity ->
            val facing = if (entity is Mob) {
                when (entity.dir) {
                    0 -> Direction.DOWN
                    1 -> Direction.UP
                    2 -> Direction.LEFT
                    3 -> Direction.RIGHT
                    else -> throw RuntimeException("Unknown direction ${entity.dir}")
                }
            } else {
                Direction.DOWN
            }
            insert(Sighting(Entity.fromGame(entity), Vector(entity.x - cameraX, entity.y - cameraY), facing, frame))
        }
    }
}