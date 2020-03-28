package com.github.qcha.JBook.nonblocking.impl;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

public class ProcessingThread extends Thread {
    private final BlockingQueue<SocketChannel> incomingQueue;
    private final ExecutorService workersPool;
    private final Selector selector;

    public ProcessingThread(BlockingQueue<SocketChannel> incomingQueue, ExecutorService workersPool) throws IOException {
        this.incomingQueue = incomingQueue;
        this.workersPool = workersPool;
        selector = Selector.open();
    }

    @Override
    public void run() {
        while (true) {
            try {
                wrapIncoming();
                scheduleWorkers();
            } catch (IOException e) {
                e.printStackTrace(); // TODO: proper logging
            }

            try {
                // Somewhat controversial thing in a non-blocking server
                // but we need to let workers some time to proceed
                sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace(); // TODO: proper logging
            }
        }
    }

    private void wrapIncoming() throws IOException {
        var sc = incomingQueue.poll();
        while (sc != null) {
            sc.configureBlocking(false);
            new RWMachine(sc, selector); // Registers itself with the selector and attaches itself to a key, no need to keep the reference.
            sc = incomingQueue.poll();
        }
    }

    private void scheduleWorkers() throws IOException {
        int ready = selector.selectNow();
        if (ready > 0) {
            var selected = selector.selectedKeys();
            for (var key : selected) {
                var machine = ((RWMachine) key.attachment());
                workersPool.submit(machine);
            }
            selected.clear();
        }
    }
}
