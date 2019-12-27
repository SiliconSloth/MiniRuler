package siliconsloth.miniruler

import org.kie.api.runtime.KieSession
import org.kie.api.runtime.rule.FactHandle

abstract class Fact {
    var kSession: KieSession? = null
    var handle: FactHandle? = null

    fun insert(kSession: KieSession) {
        this.kSession = kSession
        handle = kSession.insert(this)
    }

    fun update() {
        this.kSession!!.update(handle, this)
    }

    fun delete() {
        this.kSession!!.delete(handle)
        kSession = null
        handle = null
    }
}

abstract class Perception: Fact()
class TitleSelection(var option: String): Perception()