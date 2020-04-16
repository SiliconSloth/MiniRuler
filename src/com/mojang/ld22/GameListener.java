package com.mojang.ld22;

import com.mojang.ld22.entity.Entity;
import com.mojang.ld22.item.Item;
import com.mojang.ld22.level.tile.Tile;
import com.mojang.ld22.screen.Menu;

import java.util.List;

public interface GameListener {
    void onMenuChange(Menu oldMenu, Menu newMenu);
    void onTitleOptionSelect(int selection);
    void onItemListRender(List<Item> items);
    void onListSelect(int selection);
    void onHaveIndicatorRender(Item item, int count);
    void onActiveItemChange(Item item);
    void onRender(Tile[][] tiles, List<Entity> entities, int xScroll, int yScroll, int stamina);
}
