package siliconsloth.miniruler

import com.mojang.ld22.GameListener
import com.mojang.ld22.screen.Menu
import com.mojang.ld22.screen.TitleMenu
import org.kie.api.runtime.KieSession

class PerceptionHandler(val kSession: KieSession): GameListener {
    val titleSelection = TitleSelection(TitleOption.START_GAME)

    override fun onMenuOpen(menu: Menu?) {
        if (menu is TitleMenu) {
            titleSelection.option = TitleOption.fromSelection(menu.selected)
            titleSelection.insert(kSession)
        }
    }

    override fun onMenuClose(menu: Menu?) {
        if (menu is TitleMenu) {
            titleSelection.delete()
        }
    }

    override fun onTitleOptionSelect(selection: Int) {
        titleSelection.option = TitleOption.fromSelection(selection)
        titleSelection.update()
    }
}