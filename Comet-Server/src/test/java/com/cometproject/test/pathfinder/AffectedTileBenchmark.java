package com.cometproject.test.pathfinder;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.Benchmark;
import java.util.concurrent.TimeUnit;
import com.cometproject.server.game.rooms.objects.entities.pathfinding.AffectedTile;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class AffectedTileBenchmark {

    static void main(String[] args) throws Exception{
        Main.main(args);
    }

    @Benchmark
    public void benchmarkGetAffectedBothTilesAt() {
        AffectedTile.getAffectedBothTilesAt(10, 10, 0, 0, 0);
    }

    @Benchmark
    public void benchmarkGetAffectedTilesAt() {
        AffectedTile.getAffectedTilesAt(10, 10, 0, 0, 0);
    }

}