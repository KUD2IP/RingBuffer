package org.example;

import org.example.util.RingBuffer;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;


public class RingBufferTest {

    @Test
    void testSingleThreadedPutAndGet() throws InterruptedException {
        RingBuffer<Integer> buffer = new RingBuffer<>(3);

        buffer.put(1);
        buffer.put(2);
        buffer.put(3);

        assertEquals(1, buffer.get());
        assertEquals(2, buffer.get());
        assertEquals(3, buffer.get());
    }

    @Test
    void testIsEmptyAndIsFull() throws InterruptedException {
        RingBuffer<Integer> buffer = new RingBuffer<>(2);

        assertTrue(buffer.isEmpty());
        assertFalse(buffer.isFull());

        buffer.put(1);
        assertFalse(buffer.isEmpty());
        assertFalse(buffer.isFull());

        buffer.put(2);
        assertFalse(buffer.isEmpty());
        assertTrue(buffer.isFull());

        buffer.get();
        assertFalse(buffer.isFull());
        assertFalse(buffer.isEmpty());

        buffer.get();
        assertTrue(buffer.isEmpty());
    }

    @Test
    void testGetSize() throws InterruptedException {
        RingBuffer<Integer> buffer = new RingBuffer<>(5);

        assertEquals(0, buffer.getSize());

        buffer.put(1);
        buffer.put(2);
        assertEquals(2, buffer.getSize());

        buffer.get();
        assertEquals(1, buffer.getSize());

        buffer.get();
        assertEquals(0, buffer.getSize());
    }

    @Test
    void testBlockingOnFullBuffer() throws InterruptedException {
        RingBuffer<Integer> buffer = new RingBuffer<>(2);

        buffer.put(1);
        buffer.put(2);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = executor.submit(() -> {
            try {
                buffer.put(3);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread.sleep(100);
        assertFalse(future.isDone());

        buffer.get();
        Thread.sleep(100);
        assertTrue(future.isDone());

        executor.shutdown();
    }

    @Test
    void testBlockingOnEmptyBuffer() throws InterruptedException, ExecutionException {
        RingBuffer<Integer> buffer = new RingBuffer<>(2);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Integer> future = executor.submit(() -> {
            try {
                return buffer.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        });

        Thread.sleep(100);
        assertFalse(future.isDone());

        buffer.put(42);
        Thread.sleep(100);
        assertTrue(future.isDone());
        assertEquals(42, future.get());

        executor.shutdown();
    }

    @Test
    void testMultiThreadedPutAndGet() throws InterruptedException, ExecutionException {
        final int bufferSize = 5;
        final int numThreads = 10;
        final int numElements = 20;

        RingBuffer<Integer> buffer = new RingBuffer<>(bufferSize);
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        Callable<Void> producerTask = () -> {
            for (int i = 0; i < numElements; i++) {
                buffer.put(i);
            }
            return null;
        };

        Callable<Void> consumerTask = () -> {
            for (int i = 0; i < numElements; i++) {
                buffer.get();
            }
            return null;
        };

        Future<Void> producer = executor.submit(producerTask);
        Future<Void> consumer = executor.submit(consumerTask);

        producer.get();
        consumer.get();

        assertTrue(buffer.isEmpty());
        executor.shutdown();
    }
}