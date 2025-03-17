package org.example.util;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RingBuffer<T> {
    private final T[] buffer;
    private int writeIndex = 0;
    private int readIndex = 0;
    private int size = 0;

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
        lock.lock();
        try {
            return size == 0;
        } finally {
            lock.unlock();
        }
    }

    public boolean isFull() {
        lock.lock();
        try {
            return size == buffer.length;
        } finally {
            lock.unlock();
        }
    }

    public int getSize() {
        lock.lock();
        try {
            return size;
        } finally {
            lock.unlock();
        }
    }
}