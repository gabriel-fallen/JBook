package com.github.qcha.JBook.nonblocking.impl;

import com.github.qcha.JBook.nonblocking.*;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;

public class HalfBlockingServer implements Server {
    private final int port;

    public HalfBlockingServer(final int port) {
        this.port = port;
    }

    public void start() throws IOException {
        System.out.println("Running server loop.");

        // Run acceptor thread
        BlockingQueue<SocketChannel> socketQueue = new ArrayBlockingQueue<>(5); // We're not expecting many concurrent connections.
        var acceptingThread = new BlockingAcceptingThread(port, socketQueue);
        acceptingThread.start();

        // Run workers thread-pool
        var workersPool = Executors.newWorkStealingPool();
        var processingThread = new ProcessingThread(socketQueue, workersPool);
        processingThread.start();

        try {
            acceptingThread.join();
            processingThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace(); // TODO: proper logging
        }
    }
}
