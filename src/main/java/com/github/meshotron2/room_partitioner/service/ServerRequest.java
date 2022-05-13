package com.github.meshotron2.room_partitioner.service;

import java.util.concurrent.atomic.AtomicInteger;

public class ServerRequest {
    private final String path;
    private final Thread thread;
    private final AtomicInteger transferredCount;
    private final int totalTransfers;

    public ServerRequest(String path, Thread thread, int partitionCount) {
        this.path = path;
        this.thread = thread;
        this.transferredCount = new AtomicInteger(0);
        this.totalTransfers = partitionCount;
    }

    public String getPath() {
        return path;
    }

    public Thread getThread() {
        return thread;
    }

    public void transferComplete() {
        transferredCount.incrementAndGet();
    }

    public void waitFor() throws InterruptedException {
        while (transferredCount.get() < totalTransfers) {
            Thread.sleep(250);
        }
    }
}

