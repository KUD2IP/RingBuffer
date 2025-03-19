package org.example.util;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RingBuffer<T> {
    private final T[] buffer;
    private int writeIndex = 0;
    private int readIndex = 0;
    private volatile int size = 0;

    private final Lock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();


    public RingBuffer(int capacity) {
        buffer = (T[]) new Object[capacity];
    }

    public void put(T element) throws InterruptedException {
        lock.lock();
        try {
            while (size == buffer.length) {
                notFull.await();
            }
            buffer[writeIndex] = element;
            writeIndex = (writeIndex + 1) % buffer.length;
            size++;
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public T get() throws InterruptedException {
        lock.lock();
        try {
            while (size == 0) {
                notEmpty.await();
            }
            T element = buffer[readIndex];
            readIndex = (readIndex + 1) % buffer.length;
            size--;
            notFull.signal();
            return element;
        } finally {
            lock.unlock();
        }
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean isFull() {
        return size == buffer.length;
    }

    public int getSize() {
        return size;
    }
}