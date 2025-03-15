package com.cometproject.test.pathfinder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.cometproject.server.game.rooms.objects.RoomObject;
import com.cometproject.server.game.rooms.objects.entities.pathfinding.Pathfinder;
import com.cometproject.server.game.rooms.objects.entities.pathfinding.Square;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.game.rooms.types.mapping.RoomMapping;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import com.cometproject.api.game.rooms.models.IRoomModel;
import com.cometproject.api.game.utilities.Position;

public class PathfinderTest {
    
   @Test
    public void testMakePath() {
        RoomObject roomObject = mock(RoomObject.class);
        Room room = mock(Room.class);
        RoomMapping roomMapping = mock(RoomMapping.class);
        IRoomModel roomModel = mock(IRoomModel.class);
        Position startPosition = new Position(0, 0, 0);
        Position endPosition = new Position(5, 5, 5);

        when(roomObject.getPosition()).thenReturn(startPosition);
        when(roomObject.getRoom()).thenReturn(room);
        when(room.getMapping()).thenReturn(roomMapping);
        when(roomMapping.getModel()).thenReturn(roomModel);
        when(roomModel.getSizeX()).thenReturn(10);
        when(roomModel.getSizeY()).thenReturn(10);
        when(roomMapping.isValidStep(anyInt(), any(Position.class), any(Position.class), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(true);

        Pathfinder pathfinder = new Pathfinder() {};
        List<Square> path = pathfinder.makePath(roomObject, endPosition);

        assertNotNull(path);
        assertFalse(path.isEmpty());
    }

    @Test
    public void testIsValidStep() {
        RoomObject roomObject = mock(RoomObject.class);
        Room room = mock(Room.class);
        RoomMapping roomMapping = mock(RoomMapping.class);
        IRoomModel roomModel = mock(IRoomModel.class);
        Position from = new Position(0, 0, 0);
        Position to = new Position(1, 1, 1);

        when(roomObject.getRoom()).thenReturn(room);
        when(room.getMapping()).thenReturn(roomMapping);
        when(roomMapping.getModel()).thenReturn(roomModel);
        when(roomModel.getSizeX()).thenReturn(10);
        when(roomModel.getSizeY()).thenReturn(10);
        when(roomMapping.isValidStep(anyInt(), any(Position.class), any(Position.class), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(true);

        Pathfinder pathfinder = new Pathfinder() {};
        boolean isValid = pathfinder.isValidStep(roomObject, from, to, false, false);

        assertTrue(isValid);
    }

}
