package com.chess.engine;

public final class Main {

    public static void main(String[] args) {
        ChessServer server = new ChessServer(7070);
        server.start();
        System.out.println("ChessServer iniciado en http://localhost:7070 y ws://localhost:7070/chess");
    }
}