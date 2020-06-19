package com.mojang.ld22.level.tile;

import com.mojang.ld22.entity.AirWizard;
import com.mojang.ld22.entity.Entity;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.level.Level;

import java.util.Random;

public class InfiniteFallTile extends Tile {
	public InfiniteFallTile(int id) {
		super(id);
	}

	@Override
	public void render(Screen screen, Level level, int x, int y) {
	}

	@Override
	public void tick(Level level, int xt, int yt, Random random) {
	}

	@Override
	public boolean mayPass(Level level, int x, int y, Entity e) {
		if (e instanceof AirWizard) return true;
		return false;
	}
}
