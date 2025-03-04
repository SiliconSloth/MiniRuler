package com.mojang.ld22.entity;

import com.mojang.ld22.entity.particle.TextParticle;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.level.tile.Tile;
import com.mojang.ld22.sound.Sound;

import java.util.Random;

public class Mob extends Entity {
	protected int walkDist = 0;
	protected int dir = 0;
	public int hurtTime = 0;
	protected int xKnockback, yKnockback;
	public int maxHealth = 10;
	public int health = maxHealth;
	public int swimTimer = 0;
	public int tickTime = 0;

	public Mob() {
		x = y = 8;
		xr = 4;
		yr = 3;
	}

	public int getDir() {
		return dir;
	}

	@Override
	public void tick(Random random) {
		tickTime++;
		if (level.getTile(x >> 4, y >> 4) == Tile.lava) {
			hurt(this, 4, dir ^ 1, random);
		}

		if (health <= 0) {
			die(random);
		}
		if (hurtTime > 0) hurtTime--;
	}

	protected void die(Random random) {
		remove();
	}

	@Override
	public boolean move(int xa, int ya, Random random) {
		if (isSwimming()) {
			if (swimTimer++ % 2 == 0) return true;
		}
		if (xKnockback < 0) {
			move2(-1, 0, random);
			xKnockback++;
		}
		if (xKnockback > 0) {
			move2(1, 0, random);
			xKnockback--;
		}
		if (yKnockback < 0) {
			move2(0, -1, random);
			yKnockback++;
		}
		if (yKnockback > 0) {
			move2(0, 1, random);
			yKnockback--;
		}
		if (hurtTime > 0) return true;
		if (xa != 0 || ya != 0) {
			walkDist++;
			if (xa < 0) dir = 2;
			if (xa > 0) dir = 3;
			if (ya < 0) dir = 1;
			if (ya > 0) dir = 0;
		}
		return super.move(xa, ya, random);
	}

	protected boolean isSwimming() {
		Tile tile = level.getTile(x >> 4, y >> 4);
		return tile == Tile.water || tile == Tile.lava;
	}

	@Override
	public boolean blocks(Entity e) {
		return e.isBlockableBy(this);
	}

	@Override
	public void hurt(Tile tile, int x, int y, int damage, Random random) {
		int attackDir = dir ^ 1;
		doHurt(damage, attackDir, random);
	}

	@Override
	public void hurt(Mob mob, int damage, int attackDir, Random random) {
		doHurt(damage, attackDir, random);
	}

	public void heal(int heal, Random random) {
		if (hurtTime > 0) return;

		level.add(new TextParticle("" + heal, x, y, Color.get(-1, 50, 50, 50), random));
		health += heal;
		if (health > maxHealth) health = maxHealth;
	}

	protected void doHurt(int damage, int attackDir, Random random) {
		if (hurtTime > 0) return;

		if (level.player != null) {
			int xd = level.player.x - x;
			int yd = level.player.y - y;
			if (xd * xd + yd * yd < 80 * 80) {
				Sound.monsterHurt.play();
			}
		}
		level.add(new TextParticle("" + damage, x, y, Color.get(-1, 500, 500, 500), random));
		health -= damage;
		if (attackDir == 0) yKnockback = +6;
		if (attackDir == 1) yKnockback = -6;
		if (attackDir == 2) xKnockback = -6;
		if (attackDir == 3) xKnockback = +6;
		hurtTime = 10;
	}

	public boolean findStartPos(Level level, Random random) {
		int x = random.nextInt(level.w);
		int y = random.nextInt(level.h);
		int xx = x * 16 + 8;
		int yy = y * 16 + 8;

		if (level.player != null) {
			int xd = level.player.x - xx;
			int yd = level.player.y - yy;
			if (xd * xd + yd * yd < 80 * 80) return false;
		}

		int r = level.monsterDensity * 16;
		if (level.getEntities(xx - r, yy - r, xx + r, yy + r).size() > 0) return false;

		if (level.getTile(x, y).mayPass(level, x, y, this)) {
			this.x = xx;
			this.y = yy;
			return true;
		}

		return false;
	}
}
