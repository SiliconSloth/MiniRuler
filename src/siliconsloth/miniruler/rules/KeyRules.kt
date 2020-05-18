package siliconsloth.miniruler.rules

import siliconsloth.miniruler.*
import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.engine.filters.EqualityFilter

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

    rule {
        not<JiggleRequest>()
        find<MoveRequest> { direction == Direction.UP }

        fire {
            maintain(KeyRequest(Key.UP))
        }
    }

    rule {
        not<JiggleRequest>()
        find<MoveRequest> { direction == Direction.DOWN }
        not<MoveRequest> { direction == Direction.UP }

        fire {
            maintain(KeyRequest(Key.DOWN))
        }
    }

    rule {
        not<JiggleRequest>()
        find<MoveRequest> { direction == Direction.LEFT }

        fire {
            maintain(KeyRequest(Key.LEFT))
        }
    }

    rule {
        not<JiggleRequest>()
        find<MoveRequest> { direction == Direction.RIGHT }
        not<MoveRequest> { direction == Direction.LEFT }

        fire {
            maintain(KeyRequest(Key.RIGHT))
        }
    }

    rule {
        val upRequest = KeyRequest(Key.UP)
        val downRequest = KeyRequest(Key.DOWN)
        val leftRequest = KeyRequest(Key.LEFT)
        val rightRequest = KeyRequest(Key.RIGHT)

        find<JiggleRequest>()
        // Reevaluate every time the player moves.
        find<Memory> { entity == Entity.PLAYER }

        fire {
            if (exists(EqualityFilter { downRequest })) {
                delete(downRequest)
                maintain(upRequest)
            } else {
                delete(upRequest)
                maintain(downRequest)
            }

            if (exists(EqualityFilter { leftRequest })) {
                delete(leftRequest)
                maintain(rightRequest)
            } else {
                delete(rightRequest)
                maintain(leftRequest)
            }
        }
    }

    rule {
        val spam by find<KeySpam>()
        not<KeyRequest> { key == spam.key }
        delay = 2

        fire {
            maintain(KeyRequest(spam.key))
        }
    }
}