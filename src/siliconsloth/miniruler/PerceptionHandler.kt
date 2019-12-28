package siliconsloth.miniruler

import com.mojang.ld22.GameListener
import com.mojang.ld22.screen.Menu as GameMenu
import com.mojang.ld22.screen.TitleMenu
import org.kie.api.runtime.KieSession

class PerceptionHandler(val kSession: KieSession): GameListener {
    private val menuOpen = MenuOpen(Menu.TITLE)
    private val titleSelection = TitleSelection(TitleOption.START_GAME)

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
}