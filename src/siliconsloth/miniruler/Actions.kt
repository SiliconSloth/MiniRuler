package siliconsloth.miniruler

import siliconsloth.miniruler.planner.*

val ITEM_COUNTS = Item.values().map { it to Variable("itemCount($it)") { LowerBounded(0) } }.toMap()
fun itemCount(item: Item) = ITEM_COUNTS[item] ?: error("Unknown item")

val MENU = Variable("MENU") { AnyValue<Menu?>() }
val HOLDING = Variable("HOLDING") { AnyValue<Item?>() }
val NEXT_TO = Variable("NEXT_TO") { AnyValue<Entity?>() }

class Select(val item: Item): Action("Select($item)", State(sortedMapOf(
        MENU to SingleValue(Menu.INVENTORY),
        itemCount(item) to LowerBounded(1)
)), sortedMapOf(
        MENU to Set(null),
        HOLDING to Set(item)
))

class Craft(val result: Item, ingredients: Map<Item, Int>): Action("Craft($result)", State(
        ingredients.map { itemCount(it.key) to LowerBounded(it.value) }.toMap()
                .plus(MENU to SingleValue(Menu.CRAFTING)).toSortedMap()
), ingredients.map { itemCount(it.key) to Add(-it.value) }.toMap()
        .plus(itemCount(result) to Add(1)).toSortedMap())

class MineRock(tool: Item?, costMultiplier: Int): Action("MineRock($tool)", State(sortedMapOf(
        MENU to SingleValue(null),
        HOLDING to SingleValue(tool)
)), sortedMapOf(
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

val CHOP_TREES = Action("CHOP_TREES", State(sortedMapOf(
        MENU to SingleValue(null),
        HOLDING to SingleValue(null)
)), sortedMapOf(
        itemCount(Item.WOOD) to AddArbitrary(),
        NEXT_TO to Set(null),
        MENU to Set(Menu.INVENTORY)
), { b, a -> resourceGainCost(Item.WOOD, b, a) * 30 })
{ b, a -> ResourceTarget(Item.WOOD, (a[itemCount(Item.WOOD)] as LowerBounded).min) }

val DIG_SAND = Action("DIG_SAND", State(sortedMapOf(
        MENU to SingleValue(null),
        HOLDING to SingleValue(Item.ROCK_SHOVEL)
)), sortedMapOf(
        itemCount(Item.SAND) to AddArbitrary(),
        NEXT_TO to Set(null),
        MENU to Set(Menu.INVENTORY)
), { b, a -> resourceGainCost(Item.SAND, b, a) * 20 })
{ b, a -> ResourceTarget(Item.SAND, (a[itemCount(Item.SAND)] as LowerBounded).min) }

val OPEN_INVENTORY = Action("OPEN_INVENTORY", State(sortedMapOf(
        MENU to SingleValue(null)
)), sortedMapOf(
        MENU to Set(Menu.INVENTORY)
))

val CLOSE_INVENTORY = Action("CLOSE_INVENTORY", State(sortedMapOf(
        MENU to SingleValue(Menu.INVENTORY)
)), sortedMapOf(
        MENU to Set(null),
        HOLDING to Set(null)
))

val PLACE_WORKBENCH = Action("PLACE_WORKBENCH", State(sortedMapOf(
        MENU to SingleValue(null),
        HOLDING to SingleValue(Item.WORKBENCH)
)), sortedMapOf(
        HOLDING to Set(null),
        NEXT_TO to Set(Entity.WORKBENCH),
        itemCount(Item.WORKBENCH) to Add(-1)
))

val OPEN_CRAFTING = Action("OPEN_CRAFTING", State(sortedMapOf(
        MENU to SingleValue(null),
        NEXT_TO to SingleValue(Entity.WORKBENCH)
)), sortedMapOf(
        MENU to Set(Menu.CRAFTING)
))

val CLOSE_CRAFTING = Action("CLOSE_CRAFTING", State(sortedMapOf(
        MENU to SingleValue(Menu.CRAFTING)
)), sortedMapOf(
        MENU to Set(null)
))

val PICK_UP_WORKBENCH = Action("PICK_UP_WORKBENCH", State(sortedMapOf(
        MENU to SingleValue(null),
        NEXT_TO to SingleValue(Entity.WORKBENCH),
        HOLDING to SingleValue(Item.POWER_GLOVE)
)), sortedMapOf(
        NEXT_TO to Set(null),
        HOLDING to Set(Item.WORKBENCH),
        itemCount(Item.WORKBENCH) to Add(1)
))

val CRAFT_WOOD_PCIKAXE = Craft(Item.WOOD_PICKAXE, sortedMapOf(
        Item.WOOD to 5
))

val CRAFT_ROCK_PCIKAXE = Craft(Item.ROCK_PICKAXE, sortedMapOf(
        Item.WOOD to 5,
        Item.STONE to 5
))

val CRAFT_ROCK_SHOVEL = Craft(Item.ROCK_SHOVEL, sortedMapOf(
        Item.WOOD to 5,
        Item.STONE to 5
))

val MINE_ROCK_WITH_HAND = MineRock(null, 50)
val MINE_ROCK_WITH_WOOD = MineRock(Item.WOOD_PICKAXE, 20)
val MINE_ROCK_WITH_ROCK = MineRock(Item.ROCK_PICKAXE, 15)

val ALL_ACTIONS = listOf(CHOP_TREES, DIG_SAND, OPEN_INVENTORY, CLOSE_INVENTORY, PLACE_WORKBENCH, OPEN_CRAFTING,
        CRAFT_WOOD_PCIKAXE, CRAFT_ROCK_PCIKAXE, CRAFT_ROCK_SHOVEL,
        CLOSE_CRAFTING, PICK_UP_WORKBENCH, MINE_ROCK_WITH_HAND, MINE_ROCK_WITH_WOOD, MINE_ROCK_WITH_ROCK) +
        Item.values().map { Select(it) }