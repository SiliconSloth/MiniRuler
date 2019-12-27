package siliconsloth.miniruler

import com.mojang.ld22.InputHandler
import org.kie.api.event.rule.ObjectDeletedEvent
import org.kie.api.event.rule.ObjectInsertedEvent
import org.kie.api.event.rule.ObjectUpdatedEvent
import org.kie.api.event.rule.RuleRuntimeEventListener
import java.lang.RuntimeException

class KeyListener(val inputHandler: InputHandler): RuleRuntimeEventListener {
    override fun objectInserted(event: ObjectInsertedEvent) {
        (event.`object` as? KeyPress)?.let {
            toggle(it.key, true)
        }
    }

    override fun objectDeleted(event: ObjectDeletedEvent) {
        (event.oldObject as? KeyPress)?.let {
            toggle(it.key, false)
        }
    }

    override fun objectUpdated(event: ObjectUpdatedEvent) {
        (event.oldObject as? KeyPress)?.let {
            toggle(it.key, false)
        }

        (event.`object` as? KeyPress)?.let {
            toggle(it.key, true)
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