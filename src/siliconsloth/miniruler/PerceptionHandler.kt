package siliconsloth.miniruler

 import com.mojang.ld22.Game
 import com.mojang.ld22.GameListener
 import com.mojang.ld22.screen.AboutMenu
 import com.mojang.ld22.screen.InstructionsMenu
 import com.mojang.ld22.level.tile.Tile as GameTile
import com.mojang.ld22.screen.Menu as GameMenu
import com.mojang.ld22.screen.TitleMenu
import org.kie.api.runtime.KieSession

class PerceptionHandler(private val kSession: KieSession): GameListener {
    private val menuOpen = MenuOpen(Menu.TITLE)
    private val titleSelection = TitleSelection(TitleOption.START_GAME)
    private val tileSightings = Array(Game.WIDTH/16 + 1) {
        Array(Game.HEIGHT/16 + 1) {
            TileSighting(Tile.GRASS, 0, 0)
        }
    }

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

    override fun onRender(tiles: Array<out Array<GameTile>>, xOffset: Int, yOffset: Int) {
        val centerX = xOffset + Game.WIDTH / 2
        val centerY = yOffset + (Game.HEIGHT - 8) / 2

        tileSightings.forEachIndexed { x, column -> column.forEachIndexed { y, sighting ->
            if (x < tiles.size && y < tiles[0].size) {
                sighting.tile = Tile.fromGameTile(tiles[x][y])
                sighting.x = x*16 - centerX
                sighting.y = y*16 - centerY

                kSession.insertOrUpdate(sighting)
            } else {
                kSession.deleteIfPresent(sighting)
            }
        } }
    }
}