package com.github.meshotron2.room_partitioner.service;

import java.util.concurrent.Semaphore;

public class ServerRequest {
    private final String path;
    private final Thread thread;
    private final Semaphore sem;
    private final int totalTransfers;

    public ServerRequest(String path, Thread thread, int partitionCount) throws InterruptedException {
        this.path = path;
        this.thread = thread;
        this.sem = new Semaphore(partitionCount);
        this.totalTransfers = partitionCount;

        sem.acquire(totalTransfers);
    }

    public String getPath() {
        return path;
    }

    public Thread getThread() {
        return thread;
    }

    public void transferComplete() {
        sem.release();
    }

    public void waitFor() throws InterruptedException {
        sem.acquire(totalTransfers);
    }
}

