package com.github.qcha.JBook.nonblocking.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;

public class BlockingAcceptingThread extends Thread {
    private final int port;
    private final BlockingQueue<SocketChannel> queue;

    public BlockingAcceptingThread(int port, BlockingQueue<SocketChannel> queue) {
        this.port = port;
        this.queue = queue;
    }

    @Override
    public void run() {
        ServerSocketChannel ssc;
        try {
            ssc = ServerSocketChannel.open();
            ssc.bind(new InetSocketAddress(port));
        } catch (IOException e) {
            e.printStackTrace(); // TODO: proper logging
            return;
        }

        while (true) {
            try {
                var sc = ssc.accept();
                queue.add(sc);
                System.out.println("New connection accepted and enqueued.");
            } catch (IOException e) {
                e.printStackTrace(); // TODO: proper logging
            }
        }
    }
}
