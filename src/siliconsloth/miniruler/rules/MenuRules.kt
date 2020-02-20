package siliconsloth.miniruler.rules

import siliconsloth.miniruler.*
import siliconsloth.miniruler.engine.RuleEngine

fun RuleEngine.menuRules() {
    rule {
        find<MenuOpen> { menu == Menu.INSTRUCTIONS || menu == Menu.ABOUT }
        fire {
            maintain(KeyPress(Key.ATTACK))
        }
    }

    rule {
        find<TitleSelection> { option == TitleOption.HOW_TO_PLAY }
        fire {
            maintain(KeyPress(Key.UP))
        }
    }

    rule {
        find<TitleSelection> { option == TitleOption.ABOUT }
        fire {
            maintain(KeyPress(Key.DOWN))
        }
    }

    rule {
        find<TitleSelection> { option == TitleOption.START_GAME }
        fire {
            maintain(KeyPress(Key.ATTACK))
        }
    }
}