package com.cometproject.test.pathfinder;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import java.util.concurrent.TimeUnit;
import com.cometproject.server.game.rooms.objects.RoomObject;
import com.cometproject.server.game.rooms.objects.entities.pathfinding.Pathfinder;
import com.cometproject.api.game.utilities.Position;
import static org.mockito.Mockito.*;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class PathfinderBenchmark {

    private RoomObject roomObject;
    private Position endPosition;
    private Pathfinder pathfinder;

    @Setup
    public void setup() {
        roomObject = mock(RoomObject.class);
        Position startPosition = new Position(0, 0, 0);
        endPosition = new Position(5, 5, 5);

        when(roomObject.getPosition()).thenReturn(startPosition);
        when(roomObject.getRoom().getMapping().isValidStep(anyInt(), any(Position.class), any(Position.class), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(true);

        pathfinder = new Pathfinder() {};
    }

    @Benchmark
    public void benchmarkMakePath() {
        pathfinder.makePath(roomObject, endPosition);
    }

    @Benchmark
    public void benchmarkIsValidStep() {
        Position from = new Position(0, 0, 0);
        Position to = new Position(1, 1, 1);
        pathfinder.isValidStep(roomObject, from, to, false, false);
    }
}