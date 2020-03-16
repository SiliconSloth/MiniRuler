package siliconsloth.miniruler

import com.mojang.ld22.GameListener
import com.mojang.ld22.entity.Mob
import com.mojang.ld22.level.tile.Tile as GameTile
import com.mojang.ld22.screen.Menu as GameMenu
import com.mojang.ld22.entity.Entity as GameEntity
import com.mojang.ld22.screen.TitleMenu
import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.engine.builders.AtomicBuilder
import siliconsloth.miniruler.math.Vector
import java.lang.RuntimeException

class PerceptionHandler(private val engine: RuleEngine): GameListener {
    private var menu: Menu? = null
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
    }

    override fun onTitleOptionSelect(selection: Int) = engine.atomic {
        deleteAll<TitleSelection>()
        insert(TitleSelection(TitleOption.fromSelection(selection)))
    }

    override fun onRender(tiles: Array<out Array<GameTile>>, entities: List<GameEntity>,
                          xScroll: Int, yScroll: Int, stamina: Int) = engine.atomic {
        if (menu == Menu.TITLE || menu == Menu.INSTRUCTIONS || menu == Menu.ABOUT) {
            return@atomic
        }

        deleteAll<CameraLocation>()
        insert(CameraLocation(Vector(xScroll, yScroll), frame))

        updateTiles(tiles, xScroll % 16, yScroll % 16)
        updateEntities(entities, xScroll, yScroll)

        deleteAll<StaminaLevel>()
        insert(StaminaLevel(stamina))

        frame++
    }

    // Center is relative to tile array.
    private fun AtomicBuilder.updateTiles(tiles: Array<out Array<GameTile>>, xOffset: Int, yOffset: Int) {
        deleteAll<TileSighting>()

        tiles.forEachIndexed { x, column -> column.forEachIndexed { y, tile ->
            insert(TileSighting(Tile.fromGameTile(tile), Vector(x*16 + 8 - xOffset, y*16 + 8 - yOffset), frame))
        } }
    }

    private fun AtomicBuilder.updateEntities(entities: List<GameEntity>, cameraX: Int, cameraY: Int) {
        deleteAll<EntitySighting>()

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
            insert(EntitySighting(Entity.fromGameEntity(entity), Vector(entity.x - cameraX, entity.y - cameraY), facing, frame))
        }
    }
}