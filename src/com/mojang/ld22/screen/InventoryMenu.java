package com.mojang.ld22.screen;

import com.mojang.ld22.entity.Player;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.item.Item;

public class InventoryMenu extends Menu {
	private Player player;
	public int selected = 0;

	public InventoryMenu(Player player) {
		this.player = player;

		if (player.getActiveItem() != null) {
			player.inventory.items.add(0, player.getActiveItem());
			player.setActiveItem(null);
		}
	}

	public void tick() {
		int oldSelected = selected;

		if (game.getInput().menu.clicked) game.setMenu(null);

		if (game.getInput().up.clicked) selected--;
		if (game.getInput().down.clicked) selected++;

		int len = player.inventory.items.size();
		if (len == 0) selected = 0;
		if (selected < 0) selected += len;
		if (selected >= len) selected -= len;

		if (game.getInput().attack.clicked && len > 0) {
			Item item = player.inventory.items.remove(selected);
			player.setActiveItem(item);
			game.setMenu(null);
		}

		if (selected != oldSelected && game.getGameListener() != null) {
			game.getGameListener().onInventorySelect(selected);
		}
	}

	public void render(Screen screen) {
		Font.renderFrame(screen, "inventory", 1, 1, 12, 11);
		renderItemList(screen, 1, 1, 12, 11, player.inventory.items, selected);
		if (game.getGameListener() != null) {
			game.getGameListener().onInventoryRender(player.inventory.items);
		}
	}
}