package com.cometproject.test.pathfinder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import java.util.List;
import com.cometproject.server.game.rooms.objects.entities.pathfinding.AffectedTile;

public class AffectedTileTest {
    
    @Test
    public void testGetAffectedBothTilesAt() {
        List<AffectedTile> tiles = AffectedTile.getAffectedBothTilesAt(2, 2, 0, 0, 0);
        assertEquals(4, tiles.size());
        assertTrue(tiles.contains(new AffectedTile(0, 0)));
        assertTrue(tiles.contains(new AffectedTile(0, 1)));
        assertTrue(tiles.contains(new AffectedTile(1, 0)));
        assertTrue(tiles.contains(new AffectedTile(1, 1)));
    }

    @Test
    public void testGetAffectedTilesAt() {
        List<AffectedTile> tiles = AffectedTile.getAffectedTilesAt(2, 1, 0, 0, 0);
        assertEquals(2, tiles.size());
        assertTrue(tiles.contains(new AffectedTile(0, 0)));
        assertTrue(tiles.contains(new AffectedTile(0, 1)));
    }

}