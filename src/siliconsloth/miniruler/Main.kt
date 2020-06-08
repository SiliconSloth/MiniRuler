package siliconsloth.miniruler

import com.mojang.ld22.Game
import siliconsloth.miniruler.engine.RuleEngine
import siliconsloth.miniruler.engine.filters.AreaFilter
import siliconsloth.miniruler.engine.stores.SpatialMap
import siliconsloth.miniruler.math.Box
import siliconsloth.miniruler.math.Vector
import siliconsloth.miniruler.pathfinder.PathFinder
import siliconsloth.miniruler.planner.Enumeration
import siliconsloth.miniruler.planner.LowerBounded
import siliconsloth.miniruler.planner.Planner
import siliconsloth.miniruler.rules.*

fun main() {
    val engine = RuleEngine()
    val spatialStore = SpatialMap<Memory>()
    engine.addFactStore(spatialStore)

    val goal = state(itemCount(Item.WORKBENCH) to LowerBounded(1), itemCount(Item.ROCK_PICKAXE) to LowerBounded(1)/*,
                            itemCount(Item.GLASS) to LowerBounded(4)*/, itemCount(Item.FURNACE) to LowerBounded(1))
    val planner = Planner(goal, ALL_ACTIONS)

    val pathFinder = PathFinder(spatialStore)

    val game = Game.startWindowedGame(PerceptionHandler(engine))
    KeyListener(engine, game.botInput)
    Visualizer(engine, pathFinder).display()
//    KeyTracer(engine).display()

    engine.menuRules()
    engine.memoryRules()
    engine.navigationRules(pathFinder)
    engine.attackRules()
    engine.inventoryMemoryRules()
    engine.inventoryRules()
    engine.planningRules(planner)
    engine.keyRules()
}

fun <T: Spatial> screenFilter(camera: () -> Vector) = AreaFilter<T> { Box(
        (camera() / 16) * 16 + 8,
        ((camera() + Vector(Game.WIDTH, Game.HEIGHT)) / 16) * 16 + 7
) }