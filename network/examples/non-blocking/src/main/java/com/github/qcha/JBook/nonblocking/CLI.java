package com.github.qcha.JBook.nonblocking;

import com.github.qcha.JBook.nonblocking.impl.HalfBlockingServer;

import java.io.IOException;

public class CLI {
    public static void main(String[] args) {
        System.out.println("Starting non-blocking server...");

        // TODO: parse command-line options
        final int port = 8080;

        final Server server = new HalfBlockingServer(port);
        try {
            server.start();
        } catch (IOException e) {
            System.out.println("Failed to start the server:");
            e.printStackTrace();
        }

        System.out.println("Server stopped.");
    }
}
