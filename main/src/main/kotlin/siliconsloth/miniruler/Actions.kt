package siliconsloth.miniruler

import siliconsloth.miniruler.planner.*
import kotlin.math.max

val ITEM_COUNTS = Item.values().map { it to Variable("itemCount($it)") { LowerBounded(0) } }.toMap()
fun itemCount(item: Item) = ITEM_COUNTS[item] ?: error("Unknown item")

val NEXT_TOS = Entity.values().map { it to Variable("nextTo($it)") { AnyValue<Boolean>() } }.toMap()
fun nextTo(entity: Entity) = NEXT_TOS[entity] ?: error("Unknown entity")

val ERASE_NEXT_TOS = Entity.values().map { nextTo(it) to SetTo(false) }.toMap()

val MENU = Variable("MENU") { AnyValue<Menu?>() }
val HOLDING = Variable("HOLDING") { AnyValue<Item?>() }

val VARIABLES: Array<Variable<*>> = (ITEM_COUNTS.values + NEXT_TOS.values + listOf(MENU, HOLDING)).toTypedArray()

fun state(domains: Map<Variable<*>, Domain<*>>): State =
        State(VARIABLES, domains)

fun state(vararg domains: Pair<Variable<*>, Domain<*>>): State =
        state(mapOf(*domains))

class Select(val item: Item): Action("Select($item)", state(
        MENU to Enumeration(Menu.INVENTORY),
        itemCount(item) to LowerBounded(1)
), mapOf(
        MENU to SetTo(null),
        HOLDING to SetTo(item)
))

class Place(val item: Item, val entity: Entity): Action("Place($item)", state(
        MENU to Enumeration<Menu?>(null),
        HOLDING to Enumeration(item)
), mapOf(
        HOLDING to SetTo(null),
        nextTo(entity) to SetTo(true),
        itemCount(item) to Add(-1)
))

class Open(val menu: Menu, val entity: Entity): Action("Open($menu)", state(
        MENU to Enumeration<Menu?>(null),
        nextTo(entity) to Enumeration(true)
), mapOf(
        MENU to SetTo(menu)
))

class PickUp(val entity: Entity, val item: Item): Action("PickUp($entity)", state(
        MENU to Enumeration<Menu?>(null),
        nextTo(entity) to Enumeration(true),
        HOLDING to Enumeration(Item.POWER_GLOVE)
), mapOf(
        nextTo(entity) to SetTo(false),
        HOLDING to SetTo(item),
        itemCount(item) to Add(1)
))

class Craft(val result: Item, ingredients: Map<Item, Int>, val menu: Menu = Menu.WORKBENCH):
        Action("Craft($result)", state(
            ingredients.map { itemCount(it.key) to LowerBounded(it.value) }.toMap()
                    .plus(MENU to Enumeration(menu))
), ingredients.map { itemCount(it.key) to Add(-it.value) }.toMap()
        .plus(itemCount(result) to Add(1)), resourceTarget = { _,a ->
            listOf(ResourceTarget(result, (a[itemCount(result)] as LowerBounded).min))
        })

class MineRock(tool: Item?, costMultiplier: Int): Action("MineRock($tool)", state(
        MENU to Enumeration<Menu?>(null),
        HOLDING to Enumeration(tool)
), mapOf(
        itemCount(Item.STONE) to AddArbitrary(),
        itemCount(Item.COAL) to AddArbitrary(),
        MENU to SetTo(Menu.INVENTORY)
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
        MENU to Enumeration<Menu?>(null),
        HOLDING to Enumeration<Item?>(null)
), mapOf(
        itemCount(Item.WOOD) to AddArbitrary(),
        MENU to SetTo(Menu.INVENTORY)
) + ERASE_NEXT_TOS,
        { b, a -> resourceGainCost(Item.WOOD, b, a) * 30 + 20 })
{ _, a -> listOf(ResourceTarget(Item.WOOD, (a[itemCount(Item.WOOD)] as LowerBounded).min)) }

val DIG_SAND = Action("DIG_SAND", state(
        MENU to Enumeration<Menu?>(null),
        HOLDING to Enumeration(Item.ROCK_SHOVEL)
), mapOf(
        itemCount(Item.SAND) to AddArbitrary(),
        MENU to SetTo(Menu.INVENTORY)
) + ERASE_NEXT_TOS,
        { b, a -> resourceGainCost(Item.SAND, b, a) * 20 + 20 })
{ _, a -> listOf(ResourceTarget(Item.SAND, (a[itemCount(Item.SAND)] as LowerBounded).min)) }

val OPEN_INVENTORY = Action("OPEN_INVENTORY", state(
        MENU to Enumeration<Menu?>(null)
), mapOf(
        MENU to SetTo(Menu.INVENTORY)
))

val CLOSE_INVENTORY = Action("CLOSE_INVENTORY", state(
        MENU to Enumeration(Menu.INVENTORY)
), mapOf(
        MENU to SetTo(null),
        HOLDING to SetTo(null)
))

val CLOSE_CRAFTING = Action("CLOSE_CRAFTING", state(
        MENU to Enumeration(Menu.values().filter { it.isCrafting }.toSet())
), mapOf(
        MENU to SetTo(null)
))

val PLACE_ACTIONS = listOf(
        Place(Item.ANVIL, Entity.ANVIL),
        Place(Item.FURNACE, Entity.FURNACE),
        Place(Item.OVEN, Entity.OVEN),
        Place(Item.WORKBENCH, Entity.WORKBENCH)
).associateBy { it.item }

val OPEN_ACTIONS = listOf(
        Open(Menu.ANVIL, Entity.ANVIL),
        Open(Menu.FURNACE, Entity.FURNACE),
        Open(Menu.OVEN, Entity.OVEN),
        Open(Menu.WORKBENCH, Entity.WORKBENCH)
).associateBy { it.menu }

val PICK_UP_ACTIONS = listOf(
        PickUp(Entity.ANVIL, Item.ANVIL),
        PickUp(Entity.FURNACE, Item.FURNACE),
        PickUp(Entity.OVEN, Item.OVEN),
        PickUp(Entity.WORKBENCH, Item.WORKBENCH)
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
).associateBy { it.result }

val MINE_ROCK_WITH_HAND = MineRock(null, 50)
val MINE_ROCK_WITH_WOOD = MineRock(Item.WOOD_PICKAXE, 20)
val MINE_ROCK_WITH_ROCK = MineRock(Item.ROCK_PICKAXE, 15)

val ALL_ACTIONS = listOf(CHOP_TREES, DIG_SAND, OPEN_INVENTORY, CLOSE_INVENTORY, CLOSE_CRAFTING,
        MINE_ROCK_WITH_HAND, MINE_ROCK_WITH_WOOD, MINE_ROCK_WITH_ROCK)
        .plus(PLACE_ACTIONS.values).plus(OPEN_ACTIONS.values).plus(PICK_UP_ACTIONS).plus(CRAFT_ACTIONS.values)
        .plus(Item.values().map { Select(it) })