package siliconsloth.miniruler

import siliconsloth.miniruler.planner.*

val WOOD_COUNT = Variable { LowerBounded(0) }
val PICK_COUNT = Variable { LowerBounded(0) }
val MENU = Variable { AnyValue<Menu?>() }
val HOLDING = Variable { AnyValue<Item?>() }
val NEXT_TO = Variable { AnyValue<Entity?>() }

val CHOP_TREES = Action("CHOP_TREES", State(mapOf(
        MENU to SingleValue(null),
        HOLDING to SingleValue(null)
)), mapOf(
        WOOD_COUNT to AddArbitrary(),
        NEXT_TO to Set(null)
))

val OPEN_INVENTORY = Action("OPEN_INVENTORY", State(mapOf(
        MENU to SingleValue(null)
)), mapOf(
        MENU to Set(Menu.INVENTORY)
))

val SELECT_WORKBENCH = Action("SELECT_WORKBENCH", State(mapOf(
        MENU to SingleValue(Menu.INVENTORY)
)), mapOf(
        MENU to Set(null),
        HOLDING to Set(Item.WORKBENCH)
))

val PLACE_WORKBENCH = Action("PLACE_WORKBENCH", State(mapOf(
        MENU to SingleValue(null),
        HOLDING to SingleValue(Item.WORKBENCH)
)), mapOf(
        HOLDING to Set(null),
        NEXT_TO to Set(Entity.WORKBENCH)
))

val OPEN_CRAFTING = Action("OPEN_CRAFTING", State(mapOf(
        MENU to SingleValue(null),
        NEXT_TO to SingleValue(Entity.WORKBENCH)
)), mapOf(
        MENU to Set(Menu.CRAFTING)
))

val CRAFT_PCIKAXE = Action("CRAFT_PICKAXE", State(mapOf(
        MENU to SingleValue(Menu.CRAFTING),
        WOOD_COUNT to LowerBounded(5)
)), mapOf(
        WOOD_COUNT to Add(-5),
        PICK_COUNT to Add(1)
))

val ALL_ACTIONS = listOf(CHOP_TREES, OPEN_INVENTORY, SELECT_WORKBENCH, PLACE_WORKBENCH, OPEN_CRAFTING, CRAFT_PCIKAXE)