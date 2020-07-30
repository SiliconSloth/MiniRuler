package siliconsloth.miniruler.rules

import siliconsloth.miniruler.*
import siliconsloth.miniruler.engine.RuleEngine

fun RuleEngine.menuRules() {
    rule {
        find<MenuOpen> { menu == Menu.INSTRUCTIONS || menu == Menu.ABOUT }
        fire {
            maintain(KeyRequest(Key.ATTACK))
        }
    }

    rule {
        find<TitleSelection> { option == TitleOption.HOW_TO_PLAY }
        fire {
            maintain(KeyRequest(Key.UP))
        }
    }

    rule {
        find<TitleSelection> { option == TitleOption.ABOUT }
        fire {
            maintain(KeyRequest(Key.DOWN))
        }
    }

    rule {
        find<TitleSelection> { option == TitleOption.START_GAME }
        fire {
            maintain(KeyRequest(Key.ATTACK))
        }
    }
}