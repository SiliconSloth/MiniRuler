package siliconsloth.miniruler

 import com.mojang.ld22.Game
 import com.mojang.ld22.GameListener
 import com.mojang.ld22.level.tile.Tile as GameTile
import com.mojang.ld22.screen.Menu as GameMenu
 import com.mojang.ld22.entity.Entity as GameEntity
import com.mojang.ld22.screen.TitleMenu
 import siliconsloth.miniruler.engine.FactUpdater
 import siliconsloth.miniruler.engine.RuleEngine

class PerceptionHandler(private val engine: RuleEngine): GameListener {
    private var menu: Menu? = null
    private var titleSelection: TitleSelection? = null
    private val tileSightings = Array(Game.WIDTH/16 + 1) {
        Array<TileSighting?>(Game.HEIGHT/16 + 1) {
            null
        }
    }
    private val entitySightings = mutableListOf<EntitySighting>()
    private var cameraLocation: CameraLocation? = null
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
        titleSelection?.let { delete(it) }
        titleSelection = TitleSelection(TitleOption.fromSelection(selection)).also { insert(it) }
    }

    override fun onRender(tiles: Array<out Array<GameTile>>, entities: List<GameEntity>, xScroll: Int, yScroll: Int) = engine.atomic {
        if (menu == Menu.TITLE || menu == Menu.INSTRUCTIONS || menu == Menu.ABOUT) {
            return@atomic
        }

        cameraLocation?.let { delete(it) }
        cameraLocation = CameraLocation(xScroll, yScroll, frame).also { insert(it) }

        updateTiles(tiles, xScroll % 16, yScroll % 16)
        updateEntities(entities, xScroll, yScroll)

        frame++
    }

    // Center is relative to tile array.
    private fun FactUpdater<in TileSighting>.updateTiles(tiles: Array<out Array<GameTile>>, xOffset: Int, yOffset: Int) {
        tileSightings.forEachIndexed { x, column -> column.forEachIndexed { y, sighting ->
            sighting?.let { delete(it) }

            if (x < tiles.size && y < tiles[0].size) {
                tileSightings[x][y] = TileSighting(Tile.fromGameTile(tiles[x][y]), x*16 - xOffset, y*16 - yOffset, frame)
                        .also { insert(it) }
            }
        } }
    }

    private fun FactUpdater<in EntitySighting>.updateEntities(entities: List<GameEntity>, cameraX: Int, cameraY: Int) {
        entitySightings.forEach { delete(it) }
        entitySightings.clear()

        entities.forEach { entity ->
            EntitySighting(Entity.fromGameEntity(entity), entity.x - cameraX, entity.y - cameraY, frame)
                    .also { entitySightings.add(it) }.also { insert(it) }
        }
    }
}