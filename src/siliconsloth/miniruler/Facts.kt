package siliconsloth.miniruler

import org.kie.api.runtime.KieSession
import org.kie.api.runtime.rule.FactHandle

abstract class Fact

abstract class Perception: Fact()
class MenuOpen(var menu: Menu): Perception()
class TitleSelection(var option: TitleOption): Perception()

abstract class Action: Fact()
class KeyPress(val key: Key): Action()