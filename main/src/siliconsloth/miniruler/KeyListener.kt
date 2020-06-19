package siliconsloth.miniruler

import com.mojang.ld22.InputHandler
import siliconsloth.miniruler.engine.RuleEngine

class KeyListener(val engine: RuleEngine, val inputHandler: InputHandler) {
    init {
        engine.rule {
            val keyPress by find<KeyPress>()
            fire {
                toggle(keyPress.key, true)
            }
            end {
                toggle(keyPress.key, false)
            }
        }
    }

    private fun toggle(key: Key, pressed: Boolean) =
            when (key) {
                Key.UP -> inputHandler.up
                Key.DOWN -> inputHandler.down
                Key.LEFT -> inputHandler.left
                Key.RIGHT -> inputHandler.right
                Key.ATTACK -> inputHandler.attack
                Key.MENU -> inputHandler.menu
            }.toggle(pressed)
}