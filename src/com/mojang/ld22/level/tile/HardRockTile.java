package com.mojang.ld22.level.tile;

import com.mojang.ld22.entity.Entity;
import com.mojang.ld22.entity.ItemEntity;
import com.mojang.ld22.entity.Mob;
import com.mojang.ld22.entity.Player;
import com.mojang.ld22.entity.particle.SmashParticle;
import com.mojang.ld22.entity.particle.TextParticle;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.item.Item;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.ToolItem;
import com.mojang.ld22.item.ToolType;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.level.Level;

import java.util.Random;

public class HardRockTile extends Tile {
	public HardRockTile(int id) {
		super(id);
	}

	@Override
	public void render(Screen screen, Level level, int x, int y) {
		int col = Color.get(334, 334, 223, 223);
		int transitionColor = Color.get(001, 334, 445, level.dirtColor);

		boolean u = level.getTile(x, y - 1) != this;
		boolean d = level.getTile(x, y + 1) != this;
		boolean l = level.getTile(x - 1, y) != this;
		boolean r = level.getTile(x + 1, y) != this;

		boolean ul = level.getTile(x - 1, y - 1) != this;
		boolean dl = level.getTile(x - 1, y + 1) != this;
		boolean ur = level.getTile(x + 1, y - 1) != this;
		boolean dr = level.getTile(x + 1, y + 1) != this;

		if (!u && !l) {
			if (!ul)
				screen.render(x * 16 + 0, y * 16 + 0, 0, col, 0);
			else
				screen.render(x * 16 + 0, y * 16 + 0, 7 + 0 * 32, transitionColor, 3);
		} else
			screen.render(x * 16 + 0, y * 16 + 0, (l ? 6 : 5) + (u ? 2 : 1) * 32, transitionColor, 3);

		if (!u && !r) {
			if (!ur)
				screen.render(x * 16 + 8, y * 16 + 0, 1, col, 0);
			else
				screen.render(x * 16 + 8, y * 16 + 0, 8 + 0 * 32, transitionColor, 3);
		} else
			screen.render(x * 16 + 8, y * 16 + 0, (r ? 4 : 5) + (u ? 2 : 1) * 32, transitionColor, 3);

		if (!d && !l) {
			if (!dl)
				screen.render(x * 16 + 0, y * 16 + 8, 2, col, 0);
			else
				screen.render(x * 16 + 0, y * 16 + 8, 7 + 1 * 32, transitionColor, 3);
		} else
			screen.render(x * 16 + 0, y * 16 + 8, (l ? 6 : 5) + (d ? 0 : 1) * 32, transitionColor, 3);
		if (!d && !r) {
			if (!dr)
				screen.render(x * 16 + 8, y * 16 + 8, 3, col, 0);
			else
				screen.render(x * 16 + 8, y * 16 + 8, 8 + 1 * 32, transitionColor, 3);
		} else
			screen.render(x * 16 + 8, y * 16 + 8, (r ? 4 : 5) + (d ? 0 : 1) * 32, transitionColor, 3);
	}

	@Override
	public boolean mayPass(Level level, int x, int y, Entity e) {
		return false;
	}

	@Override
	public void hurt(Level level, int x, int y, Mob source, int dmg, int attackDir, Random random) {
		hurt(level, x, y, 0, random);
	}

	@Override
	public boolean interact(Level level, int xt, int yt, Player player, Item item, int attackDir, Random random) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.pickaxe && tool.level == 4) {
				if (player.payStamina(4 - tool.level)) {
					hurt(level, xt, yt, random.nextInt(10) + (tool.level) * 5 + 10, random);
					return true;
				}
			}
		}
		return false;
	}

	public void hurt(Level level, int x, int y, int dmg, Random random) {
		int damage = level.getData(x, y) + dmg;
		level.add(new SmashParticle(x * 16 + 8, y * 16 + 8));
		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.get(-1, 500, 500, 500), random));
		if (damage >= 200) {
			int count = random.nextInt(4) + 1;
			for (int i = 0; i < count; i++) {
				level.add(new ItemEntity(new ResourceItem(Resource.stone), x * 16 + random.nextInt(10) + 3, y * 16 + random.nextInt(10) + 3, random));
			}
			count = random.nextInt(2);
			for (int i = 0; i < count; i++) {
				level.add(new ItemEntity(new ResourceItem(Resource.coal), x * 16 + random.nextInt(10) + 3, y * 16 + random.nextInt(10) + 3, random));
			}
			level.setTile(x, y, Tile.dirt, 0);
		} else {
			level.setData(x, y, damage);
		}
	}

	@Override
	public void tick(Level level, int xt, int yt, Random random) {
		int damage = level.getData(xt, yt);
		if (damage > 0) level.setData(xt, yt, damage - 1);
	}
}
