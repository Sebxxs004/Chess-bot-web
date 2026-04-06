package com.chess.engine.moves;

import java.util.List;
import java.util.Locale;

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

    public PerftDivideResult perftDivide(Board board, int depth) {
        if (depth < 1) {
            throw new IllegalArgumentException("Depth for divide must be >= 1");
        }

        long startNanos = System.nanoTime();
        List<Move> rootMoves = moveGenerator.generateLegalMoves(board);
        long totalNodes = 0L;

        for (Move move : rootMoves) {
            long branchNodes = perft(board.makeMove(move), depth - 1);
            totalNodes += branchNodes;
            System.out.println(move.toUci() + ": " + branchNodes);
        }

        long elapsedNanos = System.nanoTime() - startNanos;
        double elapsedMs = elapsedNanos / 1_000_000.0;

        System.out.println("Total nodes: " + totalNodes);
        System.out.println("Root legal moves: " + rootMoves.size());
        System.out.println(String.format(Locale.ROOT, "Elapsed: %.3f ms", elapsedMs));

        return new PerftDivideResult(totalNodes, rootMoves.size(), elapsedNanos);
    }

    public static final class PerftDivideResult {
        private final long totalNodes;
        private final int rootLegalMoves;
        private final long elapsedNanos;

        public PerftDivideResult(long totalNodes, int rootLegalMoves, long elapsedNanos) {
            this.totalNodes = totalNodes;
            this.rootLegalMoves = rootLegalMoves;
            this.elapsedNanos = elapsedNanos;
        }

        public long getTotalNodes() {
            return totalNodes;
        }

        public int getRootLegalMoves() {
            return rootLegalMoves;
        }

        public long getElapsedNanos() {
            return elapsedNanos;
        }
    }
}