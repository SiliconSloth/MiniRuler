package siliconsloth.miniruler

import com.mojang.ld22.Game
import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.engine.filters.AreaFilter
import siliconsloth.miniruler.engine.stores.SpatialMap
import siliconsloth.miniruler.math.Box
import siliconsloth.miniruler.math.Vector
import siliconsloth.miniruler.planner.LowerBounded
import siliconsloth.miniruler.planner.Planner
import siliconsloth.miniruler.planner.SingleValue
import siliconsloth.miniruler.planner.State
import siliconsloth.miniruler.rules.*

fun main() {
    val engine = RuleEngine()
    engine.addFactStore(SpatialMap<Memory>())

    val goal = State(mapOf(itemCount(Item.STONE) to LowerBounded(100), itemCount(Item.WORKBENCH) to LowerBounded(1)))
    val planner = Planner(goal, ALL_ACTIONS)

    val game = Game.startWindowedGame(PerceptionHandler(engine))
    KeyListener(engine, game.botInput)
    Visualizer(engine).display()

    engine.menuRules()
    engine.memoryRules()
    engine.navigationRules()
    engine.attackRules()
    engine.inventoryMemoryRules()
    engine.inventoryRules()
    engine.planningRules(planner)
}

fun <T: Spatial> screenFilter(camera: () -> Vector) = AreaFilter<T> { Box(
        (camera() / 16) * 16 + 8,
        ((camera() + Vector(Game.WIDTH, Game.HEIGHT)) / 16) * 16 + 7
) }