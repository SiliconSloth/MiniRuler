package siliconsloth.miniruler.rules

import siliconsloth.miniruler.GuardedKeyPress
import siliconsloth.miniruler.GuardedKeyRequest
import siliconsloth.miniruler.KeyPress
import siliconsloth.miniruler.KeyRequest
import siliconsloth.miniruler.engine.RuleEngine

fun RuleEngine.keyRules() {
    rule {
        val request by find<KeyRequest>()
        not<GuardedKeyRequest>()

        fire {
            maintain(KeyPress(request.key))
        }
    }

    rule {
        find<GuardedKeyRequest>()
        val request by find<KeyRequest>()

        fire {
            delete(request)
        }
    }

    rule {
        val request by find<GuardedKeyRequest>()
        not<KeyRequest>()
        not<KeyPress>()

        fire {
            insert(GuardedKeyPress(request.key))
        }
    }

    rule {
        val press by find<GuardedKeyPress>()
        not<GuardedKeyRequest> { key == press.key }

        fire {
            delete(press)
        }
    }

    rule {
        val press by find<GuardedKeyPress>()

        fire {
            maintain(KeyPress(press.key))
        }
    }
}