package com.cometproject.test;

import com.cometproject.server.utilities.collections.ConcurrentHashSet;
import junit.framework.TestCase;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConcurrentHashSetTest extends TestCase {
    private ConcurrentHashSet<String> set;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        set = new ConcurrentHashSet<>();
    }

    public void testAdd() {
        assertTrue(set.add("test"));
        assertTrue(set.contains("test"));
    }

    public void testRemove() {
        set.add("test");
        assertTrue(set.remove("test"));
        assertFalse(set.contains("test"));
    }

    public void testContains() {
        set.add("test");
        assertTrue(set.contains("test"));
        assertFalse(set.contains("notInSet"));
    }

    public void testSize() {
        assertEquals(0, set.size());
        set.add("test");
        assertEquals(1, set.size());
    }

    public void testIsEmpty() {
        assertTrue(set.isEmpty());
        set.add("test");
        assertFalse(set.isEmpty());
    }

    public void testClear() {
        set.add("test");
        set.clear();
        assertTrue(set.isEmpty());
    }

    public void testContainsAll() {
        set.add("test1");
        set.add("test2");
        Collection<String> collection = Arrays.asList("test1", "test2");
        assertTrue(set.containsAll(collection));
    }

    public void testRemoveAll() {
        set.add("test1");
        set.add("test2");
        Collection<String> collection = Arrays.asList("test1", "test2");
        assertTrue(set.removeAll(collection));
        assertTrue(set.isEmpty());
    }

    public void testRetainAll() {
        set.add("test1");
        set.add("test2");
        Collection<String> collection = List.of("test1");
        assertTrue(set.retainAll(collection));
        assertTrue(set.contains("test1"));
        assertFalse(set.contains("test2"));
    }

    public void testToArray() {
        set.add("test1");
        set.add("test2");
        Object[] array = set.toArray();
        assertEquals(2, array.length);
    }

    public void testToString() {
        set.add("test1");
        set.add("test2");
        String str = set.toString();
        assertTrue(str.contains("test1"));
        assertTrue(str.contains("test2"));
    }

    public void testEquals() {
        set.add("test1");
        set.add("test2");
        ConcurrentHashSet<String> otherSet = new ConcurrentHashSet<>();
        otherSet.add("test1");
        otherSet.add("test2");
			assertEquals(set, otherSet);
    }

    public void testHashCode() {
        set.add("test1");
        set.add("test2");
        ConcurrentHashSet<String> otherSet = new ConcurrentHashSet<>();
        otherSet.add("test1");
        otherSet.add("test2");
        assertEquals(set.hashCode(), otherSet.hashCode());
    }

    public void testConcurrentAdd() throws InterruptedException {
        int threadCount = 10;
        int itemsPerThread = 100;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        IntStream.range(0, threadCount).forEach(i -> {
            executor.submit(() -> {
            IntStream.range(0, itemsPerThread).forEach(j -> {
                set.add("item" + i + "-" + j);
            });
            latch.countDown();
            });
        });

        latch.await();
        executor.shutdown();

        assertEquals(threadCount * itemsPerThread, set.size());
    }

    public void testConcurrentRemove() throws InterruptedException {
        int threadCount = 10;
        int itemsPerThread = 100;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        IntStream.range(0, threadCount).forEach(i -> {
            IntStream.range(0, itemsPerThread).forEach(j -> {
            set.add("item" + i + "-" + j);
            });
        });

        IntStream.range(0, threadCount).forEach(i -> {
            executor.submit(() -> {
            IntStream.range(0, itemsPerThread).forEach(j -> {
                set.remove("item" + i + "-" + j);
            });
            latch.countDown();
            });
        });

        latch.await();
        executor.shutdown();

        assertTrue(set.isEmpty());
    }

}