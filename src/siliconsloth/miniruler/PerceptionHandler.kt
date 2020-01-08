package siliconsloth.miniruler

 import com.mojang.ld22.Game
 import com.mojang.ld22.GameListener
 import com.mojang.ld22.level.tile.Tile as GameTile
import com.mojang.ld22.screen.Menu as GameMenu
 import com.mojang.ld22.entity.Entity as GameEntity
import com.mojang.ld22.screen.TitleMenu
import org.kie.api.runtime.KieSession

class PerceptionHandler(private val kSession: KieSession): GameListener {
    private val menuOpen = MenuOpen(Menu.TITLE)
    private val titleSelection = TitleSelection(TitleOption.START_GAME)
    private val tileSightings = Array(Game.WIDTH/16 + 1) {
        Array(Game.HEIGHT/16 + 1) {
            TileSighting(Tile.GRASS, 0, 0, 0)
        }
    }
    private val entitySightings = mutableMapOf<GameEntity, EntitySighting>()
    private val cameraLocation = CameraLocation(0, 0, 0)
    private var frame = 0

    override fun onMenuChange(oldMenu: GameMenu?, newMenu: GameMenu?) {
        if (oldMenu != null && newMenu != null) {
            menuOpen.menu = Menu.fromGameMenu(newMenu)
            kSession.update(menuOpen)
        } else if (newMenu != null) {
            menuOpen.menu = Menu.fromGameMenu(newMenu)
            kSession.insert(menuOpen)
        } else {
            kSession.delete(menuOpen)
        }

        if (oldMenu is TitleMenu) {
            kSession.delete(titleSelection)
        }
        if (newMenu is TitleMenu) {
            titleSelection.option = TitleOption.fromSelection(newMenu.selected)
            kSession.insert(titleSelection)
        }
    }

    override fun onTitleOptionSelect(selection: Int) {
        titleSelection.option = TitleOption.fromSelection(selection)
        kSession.update(titleSelection)
    }

    override fun onRender(tiles: Array<out Array<GameTile>>, entities: List<GameEntity>, xScroll: Int, yScroll: Int) {
        cameraLocation.x = xScroll
        cameraLocation.y = yScroll
        cameraLocation.frame = frame
        kSession.insertOrUpdate(cameraLocation)

        updateTiles(tiles, xScroll % 16, yScroll % 16)
        updateEntities(entities, xScroll, yScroll)

        frame++
    }

    // Center is relative to tile array.
    private fun updateTiles(tiles: Array<out Array<GameTile>>, xOffset: Int, yOffset: Int) {
        tileSightings.forEachIndexed { x, column -> column.forEachIndexed { y, sighting ->
            if (x < tiles.size && y < tiles[0].size) {
                sighting.tile = Tile.fromGameTile(tiles[x][y])
                sighting.x = x*16 - xOffset
                sighting.y = y*16 - yOffset
                sighting.frame = frame

//                if (kSession.getFactHandle(sighting) == null) {
                System.err.println("${sighting.x}, ${sighting.y}: ${sighting.frame}");
                    kSession.insertOrUpdate(sighting)
                System.err.println("Oof ${sighting.x}, ${sighting.y}: ${sighting.frame}");
//                }
            } else {
                kSession.deleteIfPresent(sighting)
            }
        } }
    }

    private fun updateEntities(entities: List<GameEntity>, cameraX: Int, cameraY: Int) {
        val remaining = HashSet(entitySightings.keys)
        entities.forEach { entity ->
            entitySightings[entity]?.let {
                it.x = entity.x - cameraX
                it.y = entity.y - cameraY
                it.frame = frame
                kSession.update(it)
                remaining.remove(entity)
            } ?: {
                val sighting = EntitySighting(
                        Entity.fromGameEntity(entity),
                        entity.x - cameraX,
                        entity.y - cameraY,
                        frame
                )
                entitySightings[entity] = sighting
                kSession.insert(sighting)
            } ()
        }

        remaining.forEach {
            kSession.delete(entitySightings[it]!!)
            entitySightings.remove(it)
        }
    }
}