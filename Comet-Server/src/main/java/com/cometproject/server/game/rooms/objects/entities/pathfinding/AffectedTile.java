package com.cometproject.server.game.rooms.objects.entities.pathfinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AffectedTile {
    public int x;
    public int y;

    public AffectedTile(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static List<AffectedTile> getAffectedBothTilesAt(int length, int width, int posX, int posY, int rotation) {
        List<AffectedTile> pointList = new ArrayList<>();

        for (int i = 0; i < length; i++) {
            for (int j = 0; j < width; j++) {
                if (rotation == 0 || rotation == 4) {
                    pointList.add(new AffectedTile(posX + j, posY + i));
                } else if (rotation == 2 || rotation == 6) {
                    pointList.add(new AffectedTile(posX + i, posY + j));
                }
            }
        }

        return pointList;
    }

    public static List<AffectedTile> getAffectedTilesAt(int length, int width, int posX, int posY, int rotation) {
        List<AffectedTile> pointList = new ArrayList<>();

        for (int i = 0; i < length; i++) {
            for (int j = 0; j < width; j++) {
                if (rotation == 0 || rotation == 4) {
                    pointList.add(new AffectedTile(posX + j, posY + i));
                } else if (rotation == 2 || rotation == 6) {
                    pointList.add(new AffectedTile(posX + i, posY + j));
                }
            }
        }

        return pointList;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AffectedTile that = (AffectedTile) o;
        return x == that.x && y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

}