package com.cometproject.api.game.utilities;

public enum Direction {
	North, NorthEast, East, SouthEast, South, SouthWest, West, NorthWest;
	
	public static final Direction[] VALUES = Direction.values();
	public static final Direction NEUTRAL = Direction.East;
	public final int num;
	public final int modX;
	public final int modY;
	
	Direction() {
		this.num = this.ordinal();
		switch (this.num) {
			
			case 0 -> {
				this.modX = 0;
				this.modY = -1;
			}
			
			case 1 -> {
				this.modX = +1;
				this.modY = -1;
			}
			
			case 2 -> {
				this.modX = +1;
				this.modY = 0;
			}
			
			case 3 -> {
				this.modX = +1;
				this.modY = +1;
			}
			
			case 4 -> {
				this.modX = 0;
				this.modY = +1;
			}
			
			case 5 -> {
				this.modX = -1;
				this.modY = +1;
			}
			
			case 6 -> {
				this.modX = -1;
				this.modY = 0;
			}
			
			case 7 -> {
				this.modX = -1;
				this.modY = -1;
			}
			
			default -> {
				this.modX = 0;
				this.modY = 0;
			}
		}
	}
	
	public static Direction get(int num) {
		return VALUES[num];
	}
	
	public static Direction random() {
		return VALUES[RandomUtil.getRandomInt(0, 7)];
	}
	
	public static Direction calculate(int x, int y, int x2, int y2) {
		if (x > x2) {
			if (y == y2) return West;
			else if (y < y2) return SouthWest;
			else return NorthWest;
		} else if (x < x2) {
			if (y == y2) return East;
			else if (y < y2) return SouthEast;
			else return NorthEast;
		} else {
			return y < y2 ? South : North;
		}
	}
	
	public final Direction invert() {
		return VALUES[(this.num + (VALUES.length / 2)) % VALUES.length];
	}
	
	public final Direction transform(Direction dir) {
		return VALUES[(this.num + dir.num) % VALUES.length];
	}
}
