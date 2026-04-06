package com.chess.engine.board;

public enum Alliance {
    WHITE,
    BLACK;

    public Alliance opposite() {
        return this == WHITE ? BLACK : WHITE;
    }

    public boolean isWhite() {
        return this == WHITE;
    }
}