package siliconsloth.miniruler

import siliconsloth.miniruler.planner.*
import kotlin.math.max

val ITEM_COUNTS = Item.values().map { it to Variable("itemCount($it)") { LowerBounded(0) } }.toMap()
fun itemCount(item: Item) = ITEM_COUNTS[item] ?: error("Unknown item")

val NEXT_TOS = Entity.values().map { it to Variable("nextTo($it)") { AnyValue<Boolean>() } }.toMap()
fun nextTo(entity: Entity) = NEXT_TOS[entity] ?: error("Unknown entity")

val ERASE_NEXT_TOS = Entity.values().map { nextTo(it) to Set(false) }.toMap()

val MENU = Variable("MENU") { AnyValue<Menu?>() }
val HOLDING = Variable("HOLDING") { AnyValue<Item?>() }

val VARIABLES: Array<Variable<*>> = (ITEM_COUNTS.values + NEXT_TOS.values + listOf(MENU, HOLDING)).toTypedArray()

fun state(domains: Map<Variable<*>, Domain<*>>): State =
        State(VARIABLES, domains)

fun state(vararg domains: Pair<Variable<*>, Domain<*>>): State =
        state(mapOf(*domains))

class Select(val item: Item): Action("Select($item)", state(
        MENU to SingleValue(Menu.INVENTORY),
        itemCount(item) to LowerBounded(1)
), mapOf(
        MENU to Set(null),
        HOLDING to Set(item)
))

class Place(val item: Item, val entity: Entity): Action("Place($item)", state(
        MENU to SingleValue(null),
        HOLDING to SingleValue(item)
), mapOf(
        HOLDING to Set(null),
        nextTo(entity) to Set(true),
        itemCount(item) to Add(-1)
))

class Open(val menu: Menu, val entity: Entity): Action("Open($menu)", state(
        MENU to SingleValue(null),
        nextTo(entity) to SingleValue(true)
), mapOf(
        MENU to Set(menu)
))

class Craft(val result: Item, ingredients: Map<Item, Int>, val menu: Menu = Menu.WORKBENCH):
        Action("Craft($result)", state(
            ingredients.map { itemCount(it.key) to LowerBounded(it.value) }.toMap()
                    .plus(MENU to SingleValue(menu))
), ingredients.map { itemCount(it.key) to Add(-it.value) }.toMap()
        .plus(itemCount(result) to Add(1)), resourceTarget = { _,a ->
            listOf(ResourceTarget(result, (a[itemCount(result)] as LowerBounded).min))
        })

class MineRock(tool: Item?, costMultiplier: Int): Action("MineRock($tool)", state(
        MENU to SingleValue(null),
        HOLDING to SingleValue(tool)
), mapOf(
        itemCount(Item.STONE) to AddArbitrary(),
        itemCount(Item.COAL) to AddArbitrary(),
        MENU to Set(Menu.INVENTORY)
) + ERASE_NEXT_TOS, { b, a ->
    val stoneCost = resourceGainCost(Item.STONE, b, a)
    val coalCost = resourceGainCost(Item.COAL, b, a) * 3
    max(stoneCost, coalCost) * costMultiplier + 20
},
{ _, a -> listOf(ResourceTarget(Item.STONE, (a[itemCount(Item.STONE)] as LowerBounded).min),
                 ResourceTarget(Item.COAL, (a[itemCount(Item.COAL)] as LowerBounded).min)) })

fun resourceGainCost(item: Item, before: State, after: State): Int {
    val countBefore = (before[itemCount(item)] as LowerBounded).min
    val countAfter = (after[itemCount(item)] as LowerBounded).min

    return (countAfter - countBefore)
}

val CHOP_TREES = Action("CHOP_TREES", state(
        MENU to SingleValue(null),
        HOLDING to SingleValue(null)
), mapOf(
        itemCount(Item.WOOD) to AddArbitrary(),
        MENU to Set(Menu.INVENTORY)
) + ERASE_NEXT_TOS,
        { b, a -> resourceGainCost(Item.WOOD, b, a) * 30 + 20 })
{ _, a -> listOf(ResourceTarget(Item.WOOD, (a[itemCount(Item.WOOD)] as LowerBounded).min)) }

val DIG_SAND = Action("DIG_SAND", state(
        MENU to SingleValue(null),
        HOLDING to SingleValue(Item.ROCK_SHOVEL)
), mapOf(
        itemCount(Item.SAND) to AddArbitrary(),
        MENU to Set(Menu.INVENTORY)
) + ERASE_NEXT_TOS,
        { b, a -> resourceGainCost(Item.SAND, b, a) * 20 + 20 })
{ _, a -> listOf(ResourceTarget(Item.SAND, (a[itemCount(Item.SAND)] as LowerBounded).min)) }

val OPEN_INVENTORY = Action("OPEN_INVENTORY", state(
        MENU to SingleValue(null)
), mapOf(
        MENU to Set(Menu.INVENTORY)
))

val CLOSE_INVENTORY = Action("CLOSE_INVENTORY", state(
        MENU to SingleValue(Menu.INVENTORY)
), mapOf(
        MENU to Set(null),
        HOLDING to Set(null)
))

val CLOSE_CRAFTING = Action("CLOSE_CRAFTING", state(
        MENU to SingleValue(Menu.WORKBENCH)
), mapOf(
        MENU to Set(null)
))

val PICK_UP_WORKBENCH = Action("PICK_UP_WORKBENCH", state(
        MENU to SingleValue(null),
        nextTo(Entity.WORKBENCH) to SingleValue(true),
        HOLDING to SingleValue(Item.POWER_GLOVE)
), mapOf(
        nextTo(Entity.WORKBENCH) to Set(false),
        HOLDING to Set(Item.WORKBENCH),
        itemCount(Item.WORKBENCH) to Add(1)
))

val PLACE_ACTIONS = listOf(
        Place(Item.ANVIL, Entity.ANVIL),
        Place(Item.FURNACE, Entity.FURNACE),
        Place(Item.OVEN, Entity.OVEN),
        Place(Item.WORKBENCH, Entity.WORKBENCH)
)

val OPEN_ACTIONS = listOf(
        Open(Menu.ANVIL, Entity.ANVIL),
        Open(Menu.FURNACE, Entity.FURNACE),
        Open(Menu.OVEN, Entity.OVEN),
        Open(Menu.WORKBENCH, Entity.WORKBENCH)
)

val CRAFT_ACTIONS = listOf(
        Craft(Item.WOOD_PICKAXE, mapOf(
                Item.WOOD to 5
        )),
        Craft(Item.ROCK_PICKAXE, mapOf(
                Item.WOOD to 5,
                Item.STONE to 5
        )),
        Craft(Item.ROCK_SHOVEL, mapOf(
                Item.WOOD to 5,
                Item.STONE to 5
        )),
        Craft(Item.FURNACE, mapOf(
                Item.STONE to 20
        )),

        Craft(Item.GLASS, mapOf(
                Item.SAND to 4,
                Item.COAL to 1
        ), menu = Menu.FURNACE)
)

val MINE_ROCK_WITH_HAND = MineRock(null, 50)
val MINE_ROCK_WITH_WOOD = MineRock(Item.WOOD_PICKAXE, 20)
val MINE_ROCK_WITH_ROCK = MineRock(Item.ROCK_PICKAXE, 15)

val ALL_ACTIONS = listOf(CHOP_TREES, DIG_SAND, OPEN_INVENTORY, CLOSE_INVENTORY, CLOSE_CRAFTING,
        PICK_UP_WORKBENCH, MINE_ROCK_WITH_HAND, MINE_ROCK_WITH_WOOD, MINE_ROCK_WITH_ROCK)
        .plus(PLACE_ACTIONS).plus(OPEN_ACTIONS).plus(CRAFT_ACTIONS).plus(Item.values().map { Select(it) })