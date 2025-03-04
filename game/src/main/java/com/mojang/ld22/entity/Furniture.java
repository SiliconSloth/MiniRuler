package com.mojang.ld22.entity;

import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.item.FurnitureItem;
import com.mojang.ld22.item.PowerGloveItem;

import java.util.Random;

public class Furniture extends Entity {
	private int pushTime = 0;
	private int pushDir = -1;
	public int col, sprite;
	public String name;
	private Player shouldTake;

	public Furniture(String name) {
		this.name = name;
		xr = 3;
		yr = 3;
	}

	@Override
	public void tick(Random random) {
		if (shouldTake != null) {
			if (shouldTake.getActiveItem() instanceof PowerGloveItem) {
				remove();
				shouldTake.inventory.add(0, shouldTake.getActiveItem());
				shouldTake.setActiveItem(new FurnitureItem(this));
			}
			shouldTake = null;
		}
		if (pushDir == 0) move(0, +1, random);
		if (pushDir == 1) move(0, -1, random);
		if (pushDir == 2) move(-1, 0, random);
		if (pushDir == 3) move(+1, 0, random);
		pushDir = -1;
		if (pushTime > 0) pushTime--;
	}

	@Override
	public void render(Screen screen, Random random) {
		screen.render(x - 8, y - 8 - 4, sprite * 2 + 8 * 32, col, 0);
		screen.render(x - 0, y - 8 - 4, sprite * 2 + 8 * 32 + 1, col, 0);
		screen.render(x - 8, y - 0 - 4, sprite * 2 + 8 * 32 + 32, col, 0);
		screen.render(x - 0, y - 0 - 4, sprite * 2 + 8 * 32 + 33, col, 0);
	}

	@Override
	public boolean blocks(Entity e) {
		return true;
	}

	@Override
	protected void touchedBy(Entity entity, Random random) {
		if (entity instanceof Player && pushTime == 0) {
			pushDir = ((Player) entity).dir;
			pushTime = 10;
		}
	}

	public void take(Player player) {
		shouldTake = player;
	}
}