package com.chess.engine;

public final class Main {

    public static void main(String[] args) {
        int port = parsePort(System.getenv("PORT"), 7070);
        ChessServer server = new ChessServer(port);
        server.start();
        System.out.println("ChessServer iniciado en http://localhost:" + port + " y ws://localhost:" + port + "/chess");
    }

    private static int parsePort(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}