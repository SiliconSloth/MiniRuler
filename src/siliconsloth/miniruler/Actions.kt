package siliconsloth.miniruler

import siliconsloth.miniruler.planner.*

val ITEM_COUNTS = Item.values().map { it to Variable("itemCount($it)") { LowerBounded(0) } }.toMap()
fun itemCount(item: Item) = ITEM_COUNTS[item] ?: error("Unknown item")

val MENU = Variable("MENU") { AnyValue<Menu?>() }
val HOLDING = Variable("HOLDING") { AnyValue<Item?>() }
val NEXT_TO = Variable("NEXT_TO") { AnyValue<Entity?>() }

val VARIABLES: Array<Variable<*>> = (ITEM_COUNTS.values + listOf(MENU, HOLDING, NEXT_TO)).toTypedArray()

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

class Craft(val result: Item, ingredients: Map<Item, Int>): Action("Craft($result)", state(
        ingredients.map { itemCount(it.key) to LowerBounded(it.value) }.toMap()
                .plus(MENU to SingleValue(Menu.CRAFTING))
), ingredients.map { itemCount(it.key) to Add(-it.value) }.toMap()
        .plus(itemCount(result) to Add(1)))

class MineRock(tool: Item?, costMultiplier: Int): Action("MineRock($tool)", state(
        MENU to SingleValue(null),
        HOLDING to SingleValue(tool)
), mapOf(
        itemCount(Item.STONE) to AddArbitrary(),
        NEXT_TO to Set(null),
        MENU to Set(Menu.INVENTORY)
), { b, a -> resourceGainCost(Item.STONE, b, a) * costMultiplier },
{ b, a -> ResourceTarget(Item.STONE, (a[itemCount(Item.STONE)] as LowerBounded).min) })

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
        NEXT_TO to Set(null),
        MENU to Set(Menu.INVENTORY)
), { b, a -> resourceGainCost(Item.WOOD, b, a) * 30 })
{ b, a -> ResourceTarget(Item.WOOD, (a[itemCount(Item.WOOD)] as LowerBounded).min) }

val DIG_SAND = Action("DIG_SAND", state(
        MENU to SingleValue(null),
        HOLDING to SingleValue(Item.ROCK_SHOVEL)
), mapOf(
        itemCount(Item.SAND) to AddArbitrary(),
        NEXT_TO to Set(null),
        MENU to Set(Menu.INVENTORY)
), { b, a -> resourceGainCost(Item.SAND, b, a) * 20 })
{ b, a -> ResourceTarget(Item.SAND, (a[itemCount(Item.SAND)] as LowerBounded).min) }

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

val PLACE_WORKBENCH = Action("PLACE_WORKBENCH", state(
        MENU to SingleValue(null),
        HOLDING to SingleValue(Item.WORKBENCH)
), mapOf(
        HOLDING to Set(null),
        NEXT_TO to Set(Entity.WORKBENCH),
        itemCount(Item.WORKBENCH) to Add(-1)
))

val OPEN_CRAFTING = Action("OPEN_CRAFTING", state(
        MENU to SingleValue(null),
        NEXT_TO to SingleValue(Entity.WORKBENCH)
), mapOf(
        MENU to Set(Menu.CRAFTING)
))

val CLOSE_CRAFTING = Action("CLOSE_CRAFTING", state(
        MENU to SingleValue(Menu.CRAFTING)
), mapOf(
        MENU to Set(null)
))

val PICK_UP_WORKBENCH = Action("PICK_UP_WORKBENCH", state(
        MENU to SingleValue(null),
        NEXT_TO to SingleValue(Entity.WORKBENCH),
        HOLDING to SingleValue(Item.POWER_GLOVE)
), mapOf(
        NEXT_TO to Set(null),
        HOLDING to Set(Item.WORKBENCH),
        itemCount(Item.WORKBENCH) to Add(1)
))

val CRAFT_WOOD_PCIKAXE = Craft(Item.WOOD_PICKAXE, mapOf(
        Item.WOOD to 5
))

val CRAFT_ROCK_PCIKAXE = Craft(Item.ROCK_PICKAXE, mapOf(
        Item.WOOD to 5,
        Item.STONE to 5
))

val CRAFT_ROCK_SHOVEL = Craft(Item.ROCK_SHOVEL, mapOf(
        Item.WOOD to 5,
        Item.STONE to 5
))

val MINE_ROCK_WITH_HAND = MineRock(null, 50)
val MINE_ROCK_WITH_WOOD = MineRock(Item.WOOD_PICKAXE, 20)
val MINE_ROCK_WITH_ROCK = MineRock(Item.ROCK_PICKAXE, 15)

val ALL_ACTIONS = listOf(CHOP_TREES, DIG_SAND, OPEN_INVENTORY, CLOSE_INVENTORY, PLACE_WORKBENCH, OPEN_CRAFTING,
        CRAFT_WOOD_PCIKAXE, CRAFT_ROCK_PCIKAXE, CRAFT_ROCK_SHOVEL,
        CLOSE_CRAFTING, PICK_UP_WORKBENCH, MINE_ROCK_WITH_HAND, MINE_ROCK_WITH_WOOD, MINE_ROCK_WITH_ROCK,
        Select(Item.POWER_GLOVE), Select(Item.WORKBENCH), Select(Item.WOOD_PICKAXE), Select(Item.ROCK_PICKAXE),
        Select(Item.ROCK_SHOVEL))