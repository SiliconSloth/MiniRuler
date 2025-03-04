package com.mojang.ld22;

import com.mojang.ld22.entity.Entity;
import com.mojang.ld22.item.Item;
import com.mojang.ld22.level.tile.Tile;
import com.mojang.ld22.screen.Menu;

import java.util.List;
import java.util.Map;

public interface GameListener {
    void onMenuChange(Menu oldMenu, Menu newMenu);
    void onTitleOptionSelect(int selection);
    void onItemListRender(List<Item> items);
    void onListSelect(int selection);
    void onHaveIndicatorRender(Map<Item, Integer> haveCounts);
    void onActiveItemChange(Item item);
    void onRender(Tile[][] tiles, List<Entity> entities, int xScroll, int yScroll, int stamina);
}
