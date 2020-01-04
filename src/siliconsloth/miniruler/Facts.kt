package siliconsloth.miniruler

import org.kie.api.runtime.KieSession
import org.kie.api.runtime.rule.FactHandle

abstract class Fact

abstract class Perception: Fact()
class MenuOpen(var menu: Menu): Perception()
class TitleSelection(var option: TitleOption): Perception()

abstract class Sighting(var x: Int, var y: Int): Perception()
class TileSighting(var tile: Tile, x: Int, y: Int): Sighting(x, y)
class EntitySighting(var entity: Entity, x: Int, y: Int): Sighting(x, y)

abstract class Action: Fact()
class KeyPress(val key: Key): Action()