package com.cometproject.api.game.utilities;

import com.cometproject.api.game.furniture.types.IFurnitureDefinition;
import java.text.MessageFormat;

public class Position {
	
	public static final int NORTH = 0;
	public static final int NORTH_EAST = 1;
	public static final int EAST = 2;
	public static final int SOUTH_EAST = 3;
	public static final int SOUTH = 4;
	public static final int SOUTH_WEST = 5;
	public static final int WEST = 6;
	public static final int NORTH_WEST = 7;
	
	public static final int[] COLLIDE_TILES = new int[] { NORTH, EAST, SOUTH, WEST };
	public static final int[] DIAGONAL_TILES = new int[] { NORTH_EAST, SOUTH_EAST, SOUTH_WEST, NORTH_WEST };
	
	private int x;
	private int y;
	private double z;
	private int flag = -1;
	
	public Position(int x, int y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Position(Position old) {
		this.x = old.getX();
		this.y = old.getY();
		this.z = old.getZ();
	}
	
	public Position() {
		this.x = 0;
		this.y = 0;
		this.z = 0.0D;
	}
	
	public Position(int x, int y) {
		this.x = x;
		this.y = y;
		this.z = 0.0D;
	}
	
	public Position add(Position other) {
		return new Position(other.getX() + getX(), other.getY() + getY(), other.getZ() + getZ());
	}
	
	public Position subtract(Position other) {
		return new Position(other.getX() - getX(), other.getY() - getY(), other.getZ() - getZ());
	}
	
	public int getDistanceSquared(Position point) {
		int dx = this.getX() - point.getX();
		int dy = this.getY() - point.getY();
		
		return (dx * dx) + (dy * dy);
	}
	
	public static String validateWallPosition(String position) {
		try {
			String[] data = position.split(" ");
			if (data[2].equals("l") || data[2].equals("r")) {
				String[] width = data[0].substring(3).split(",");
				int widthX = Integer.parseInt(width[0]);
				int widthY = Integer.parseInt(width[1]);
				
				String[] length = data[1].substring(2).split(",");
				int lengthX = Integer.parseInt(length[0]);
				int lengthY = Integer.parseInt(length[1]);
				return MessageFormat.format(":w={0},{1} l={2},{3} {4}", widthX, widthY, lengthX, lengthY, data[2]);
			}
		} catch (Exception ignored) {
		
		}
		
		return null;
	}
	
	public static double calculateHeight(IFurnitureDefinition definition) {
		if (definition.getInteraction().equals("gate")) {
			return 0;
		} else if (definition.canSit()) {
			return 0;
		}
		
		return definition.getHeight();
	}
	
	public static int calculateRotation(Position from, Position to) {
		return calculateRotation(from.x, from.y, to.x, to.y, false);
	}
	
	public static int calculateRotation(int x, int y, int newX, int newY, boolean reversed) {
		int rotation = 0;
		
		if (x > newX && y > newY) rotation = 7;
		else if (x < newX && y < newY) rotation = 3;
		else if (x > newX && y < newY) rotation = 5;
		else if (x < newX && y > newY) rotation = 1;
		else if (x > newX) rotation = 6;
		else if (x < newX) rotation = 2;
		else if (y < newY) rotation = 4;
		else if (y > newY) rotation = 0;
		
		if (reversed) {
			rotation = rotation > 3 ? rotation - 4 : rotation + 4;
		}
		
		return rotation;
	}
	
	public Position squareInFront(int angle) {
		return calculatePosition(this.x, this.y, angle, false, 1);
	}
	
	public Position squareInFront(Direction direction) {
		return squareInFront(direction.num);
	}
	
	public Position squareInFront(int angle, int distance) {
		return calculatePosition(this.x, this.y, angle, false, distance);
	}
	
	public Position squareBehind(int angle) {
		return calculatePosition(this.x, this.y, angle, true, 1);
	}
	
	public static int getInvertedRotation(int currentRotation) {
		switch (currentRotation) {
			case NORTH_EAST, SOUTH_WEST -> {
				return NORTH_WEST;
			}
			case NORTH_WEST -> {
				return SOUTH_WEST;
			}
			case SOUTH_EAST -> {
				return NORTH_EAST;
			}
			case NORTH -> {
				return SOUTH;
			}
			case SOUTH -> {
				return NORTH;
			}
			case EAST -> {
				return WEST;
			}
			case WEST -> {
				return EAST;
			}
		}
		
		return currentRotation;
	}
	
	public static Position calculatePosition(int x, int y, int angle, boolean isReversed, int distance) {
		switch (angle) {
			case 0 -> y -= !isReversed ? distance : -distance;
			case 1 -> {
				if (!isReversed) {
					x += distance;
					y -= distance;
				} else {
					x -= distance;
					y += distance;
				}
			}
			case 2 -> x += !isReversed ? distance : -distance;
			case 3 -> {
				if (!isReversed) {
					x += distance;
					y += distance;
				} else {
					x -= distance;
					y -= distance;
				}
			}
			case 4 -> y += !isReversed ? distance : -distance;
			case 5 -> {
				if (!isReversed) {
					x -= distance;
					y += distance;
				} else {
					x++;
					y--;
				}
			}
			case 6 -> x -= !isReversed ? distance : -distance;
			case 7 -> {
				if (!isReversed) {
					x -= distance;
					y -= distance;
				} else {
					x += distance;
					y += distance;
				}
			}
		}
		
		return new Position(x, y);
	}
	
	public double distanceTo(Position pos) {
		return Math.abs(this.getX() - pos.getX()) + Math.abs(this.getY() - pos.getY());
	}
	
	public boolean touching(Position pos) {
		if (!(Math.abs(this.getX() - pos.getX()) > 1 || Math.abs(this.getY() - pos.getY()) > 1)) {
			return true;
		}
		
		return this.getX() == pos.getX() && this.getY() == pos.getY();
	}
	
	public Position copy() {
		return new Position(this.x, this.y, this.z);
	}
	
	@Override
	public String toString() {
		return MessageFormat.format("({0}, {1}, {2})", this.getX(), this.getY(), this.getZ());
	}
	
	public int getX() {
		return this.x;
	}
	
	public int getY() {
		return this.y;
	}
	
	public double getZ() {
		return this.z;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public void incrementX(int amount) {
		this.x += amount;
	}
	
	public void incrementY(int amount) {
		this.y += amount;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	public void setZ(double z) {
		this.z = z;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Position) {
			return ((Position) o).getX() == this.getX() && ((Position) o).getY() == this.getY();
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return 0;
	}
	
	public int getFlag() {
		return flag;
	}
	
	public void setFlag(int flag) {
		this.flag = flag;
	}
	
}
