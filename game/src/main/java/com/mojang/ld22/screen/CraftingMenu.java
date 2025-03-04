package com.mojang.ld22.screen;

import java.util.*;

import com.mojang.ld22.crafting.Recipe;
import com.mojang.ld22.entity.Player;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.item.Item;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.sound.Sound;

public class CraftingMenu extends Menu {
	private Player player;
	public int selected = 0;

	private List<Recipe> recipes;

	public CraftingMenu(List<Recipe> recipes, Player player) {
		this.recipes = new ArrayList<Recipe>(recipes);
		this.player = player;

		for (int i = 0; i < recipes.size(); i++) {
			this.recipes.get(i).checkCanCraft(player);
		}

		Collections.sort(this.recipes, new Comparator<Recipe>() {
			public int compare(Recipe r1, Recipe r2) {
				if (r1.canCraft && !r2.canCraft) return -1;
				if (!r1.canCraft && r2.canCraft) return 1;
				return 0;
			}
		});
	}

	public void tick() {
		int oldSelected = selected;

		if (game.getInput().menu.clicked) game.setMenu(null);

		if (game.getInput().up.clicked) selected--;
		if (game.getInput().down.clicked) selected++;

		int len = recipes.size();
		if (len == 0) selected = 0;
		if (selected < 0) selected += len;
		if (selected >= len) selected -= len;

		if (game.getInput().attack.clicked && len > 0) {
			Recipe r = recipes.get(selected);
			r.checkCanCraft(player);
			if (r.canCraft) {
				r.deductCost(player);
				r.craft(player);
				Sound.craft.play();
			}
			for (int i = 0; i < recipes.size(); i++) {
				recipes.get(i).checkCanCraft(player);
			}
		}

		if (selected != oldSelected && game.getGameListener() != null) {
			game.getGameListener().onListSelect(selected);
		}
	}

	public void render(Screen screen) {
		Font.renderFrame(screen, "Have", 12, 1, 19, 3);
		Font.renderFrame(screen, "Cost", 12, 4, 19, 11);
		Font.renderFrame(screen, "Crafting", 0, 1, 11, 11);
		renderItemList(screen, 0, 1, 11, 11, recipes, selected);

		if (game.getGameListener() != null) {
			List<Item> items = new LinkedList<>();
			for (Recipe recipe : recipes) {
				items.add(recipe.resultTemplate);
			}
			game.getGameListener().onItemListRender(items);
		}

		if (recipes.size() > 0) {
			Recipe recipe = recipes.get(selected);
			int hasResultItems = player.inventory.count(recipe.resultTemplate);
			int xo = 13 * 8;
			screen.render(xo, 2 * 8, recipe.resultTemplate.getSprite(), recipe.resultTemplate.getColor(), 0);
			Font.draw("" + hasResultItems, screen, xo + 8, 2 * 8, Color.get(-1, 555, 555, 555));

			Map<Item, Integer> costCounts = new HashMap<>();
			costCounts.put(recipe.resultTemplate, hasResultItems);

			List<Item> costs = recipe.costs;
			for (int i = 0; i < costs.size(); i++) {
				Item item = costs.get(i);
				int yo = (5 + i) * 8;
				screen.render(xo, yo, item.getSprite(), item.getColor(), 0);
				int requiredAmt = 1;
				if (item instanceof ResourceItem) {
					requiredAmt = ((ResourceItem) item).count;
				}
				int has = player.inventory.count(item);
				int color = Color.get(-1, 555, 555, 555);
				if (has < requiredAmt) {
					color = Color.get(-1, 222, 222, 222);
				}
				if (has > 99) has = 99;
				Font.draw("" + requiredAmt + "/" + has, screen, xo + 8, yo, color);

				costCounts.put(item, has);
			}

			if (game.getGameListener() != null) {
				game.getGameListener().onHaveIndicatorRender(costCounts);
			}
		}
		// renderItemList(screen, 12, 4, 19, 11, recipes.get(selected).costs, -1);
	}

	public List<Recipe> getRecipes() {
		return recipes;
	}
}