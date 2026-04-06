package com.chess.engine.moves;

import com.chess.engine.board.Board;

public final class Perft {

    private final MoveGenerator moveGenerator = new MoveGenerator();

    public long perft(Board board, int depth) {
        if (depth == 0) {
            return 1L;
        }

        long nodes = 0L;
        for (Move move : moveGenerator.generateLegalMoves(board)) {
            nodes += perft(board.makeMove(move), depth - 1);
        }
        return nodes;
    }
}