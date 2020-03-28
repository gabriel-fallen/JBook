package com.github.qcha.JBook.nonblocking.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class RWMachine implements Runnable {
    private final SocketChannel socketChannel;
    private final SelectionKey selectionKey;
    private final ByteBuffer readBuffer = ByteBuffer.allocateDirect(1024); // reading packets exactly 1KiB long
    private final ByteBuffer writeBuffer = ByteBuffer.allocateDirect(1026); // additional two bytes for the actual length

    public RWMachine(SocketChannel socketChannel, Selector selector) throws IOException {
        this.socketChannel = socketChannel;
        selectionKey = socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, this);
    }

    @Override
    public void run() {
        try {
            tryReading();
            tryWriting();
        } catch (IOException e) {
            e.printStackTrace(); // TODO: proper logging
        }
    }

    private void tryReading() throws IOException {
        while (readBuffer.hasRemaining()) {
            switch (socketChannel.read(readBuffer)) {
                case -1:
                    // connection closed, unregister
                    close();
                    return;
                case 0:
                    // can't read no more
                    return;
            }
        }

        if (writeBuffer.position() == 0) {
            // Read buffer is full and write buffer is empty.
            writeBuffer.clear(); // "resizing" buffer to full capacity
            short length = 0;
            for (byte b = readBuffer.get(length); b != 0 && length < 1024; b = readBuffer.get(length)) {
                writeBuffer.put(2 + length, b);
                length++;
            }
            readBuffer.clear();
            writeBuffer.putShort(0, length);
            // And just to be sure write buffer is prepared for writing to a channel.
            writeBuffer.position(0);
            writeBuffer.limit(2 + length);
        }
        // Otherwise both buffers are filled up and we can't read.
    }

    private void tryWriting() throws IOException {
        while (writeBuffer.hasRemaining()) {
            switch (socketChannel.write(writeBuffer)) {
                case -1:
                    // connection closed, unregister
                    close();
                    return;
                case 0:
                    // can't write more
                    return;
            }
        }

        // we've written all data, make the buffer "empty"
        writeBuffer.position(0);
        writeBuffer.limit(0);
    }

    private void close() throws IOException {
        selectionKey.cancel();
        selectionKey.attach(null); // Helping GC. :)
        socketChannel.close();
    }
}
