package siliconsloth.miniruler

import siliconsloth.miniruler.planner.*

val ITEM_COUNTS = Item.values().map { it to Variable("itemCount($it)") { LowerBounded(0) } }.toMap()
fun itemCount(item: Item) = ITEM_COUNTS[item] ?: error("Unknown item")

val MENU = Variable("MENU") { AnyValue<Menu?>() }
val HOLDING = Variable("HOLDING") { AnyValue<Item?>() }
val NEXT_TO = Variable("NEXT_TO") { AnyValue<Entity?>() }

class Select(val item: Item): Action("Select($item)", State(mapOf(
        MENU to SingleValue(Menu.INVENTORY),
        itemCount(item) to LowerBounded(1)
)), mapOf(
        MENU to Set(null),
        HOLDING to Set(item)
))

val CHOP_TREES = Action("CHOP_TREES", State(mapOf(
        MENU to SingleValue(null),
        HOLDING to SingleValue(null)
)), mapOf(
        itemCount(Item.WOOD) to AddArbitrary(),
        NEXT_TO to Set(null)
))

val OPEN_INVENTORY = Action("OPEN_INVENTORY", State(mapOf(
        MENU to SingleValue(null)
)), mapOf(
        MENU to Set(Menu.INVENTORY)
))

val CLOSE_INVENTORY = Action("CLOSE_INVENTORY", State(mapOf(
        MENU to SingleValue(Menu.INVENTORY)
)), mapOf(
        MENU to Set(null)
))

val PLACE_WORKBENCH = Action("PLACE_WORKBENCH", State(mapOf(
        MENU to SingleValue(null),
        HOLDING to SingleValue(Item.WORKBENCH)
)), mapOf(
        HOLDING to Set(null),
        NEXT_TO to Set(Entity.WORKBENCH),
        itemCount(Item.WORKBENCH) to Add(-1)
))

val OPEN_CRAFTING = Action("OPEN_CRAFTING", State(mapOf(
        MENU to SingleValue(null),
        NEXT_TO to SingleValue(Entity.WORKBENCH)
)), mapOf(
        MENU to Set(Menu.CRAFTING)
))

val CRAFT_PCIKAXE = Action("CRAFT_PICKAXE", State(mapOf(
        MENU to SingleValue(Menu.CRAFTING),
        itemCount(Item.WOOD) to LowerBounded(5)
)), mapOf(
        itemCount(Item.WOOD) to Add(-5),
        itemCount(Item.WOOD_PICKAXE) to Add(1)
))

val CLOSE_CRAFTING = Action("CLOSE_CRAFTING", State(mapOf(
        MENU to SingleValue(Menu.CRAFTING)
)), mapOf(
        MENU to Set(null)
))

val PICK_UP_WORKBENCH = Action("PICK_UP_WORKBENCH", State(mapOf(
        MENU to SingleValue(null),
        NEXT_TO to SingleValue(Entity.WORKBENCH),
        HOLDING to SingleValue(Item.POWER_GLOVE)
)), mapOf(
        NEXT_TO to Set(null),
        HOLDING to Set(Item.WORKBENCH),
        itemCount(Item.WORKBENCH) to Add(1)
))

val MINE_ROCK = Action("MINE_ROCK", State(mapOf(
        MENU to SingleValue(null),
        HOLDING to SingleValue(Item.WOOD_PICKAXE)
)), mapOf(
        itemCount(Item.STONE) to AddArbitrary(),
        NEXT_TO to Set(null)
))

val ALL_ACTIONS = listOf(CHOP_TREES, OPEN_INVENTORY, CLOSE_INVENTORY, PLACE_WORKBENCH, OPEN_CRAFTING, CRAFT_PCIKAXE,
        CLOSE_CRAFTING, PICK_UP_WORKBENCH, MINE_ROCK) + Item.values().map { Select(it) }