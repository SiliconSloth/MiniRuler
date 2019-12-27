package siliconsloth.miniruler

import com.mojang.ld22.GameListener
import com.mojang.ld22.screen.Menu
import com.mojang.ld22.screen.TitleMenu
import org.kie.api.runtime.KieSession
import org.kie.api.runtime.rule.FactHandle

class PerceptionHandler(val kSession: KieSession): GameListener {
    val titleSelection = TitleSelection("")

    override fun onMenuOpen(menu: Menu?) {
        if (menu is TitleMenu) {
            titleSelection.option = TitleMenu.options[menu.selected];
            titleSelection.insert(kSession)
        }
    }

    override fun onMenuClose(menu: Menu?) {
        if (menu is TitleMenu) {
            titleSelection.delete()
        }
    }

    override fun onTitleOptionSelect(option: String) {
        titleSelection.option = option
        titleSelection.update()
    }
}