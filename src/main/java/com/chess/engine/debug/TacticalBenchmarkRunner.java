package com.chess.engine.debug;

import com.chess.engine.board.Board;
import com.chess.engine.search.Search;

public final class TacticalBenchmarkRunner {

    private static final String TACTICAL_FEN = "1k1r4/pp1b1R2/3q2pp/4p3/2B5/4Q3/PPP2B2/2K5 b - - 0 1";

    public static void main(String[] args) {
        Board board = Board.fromFen(TACTICAL_FEN);

        runBenchmark("TT off", new Search(false), board, 5);
        System.out.println();
        runBenchmark("TT on", new Search(true), board, 5);
    }

    private static void runBenchmark(String label, Search search, Board board, int depth) {
        long start = System.nanoTime();
        int score = search.searchMoves(board, depth, Integer.MIN_VALUE + 1, Integer.MAX_VALUE);
        long elapsed = System.nanoTime() - start;

        System.out.println(label);
        System.out.println("score=" + score);
        System.out.println("nodes=" + search.getNodesVisited());
        System.out.println("alphaBetaNodes=" + search.getDiagnostics().getAlphaBetaNodes());
        System.out.println("quiescenceNodes=" + search.getDiagnostics().getQuiescenceNodes());
        System.out.println("ttHits=" + search.getDiagnostics().getTtHits());
        System.out.println("ttCutoffs=" + search.getDiagnostics().getTtCutoffs());
        System.out.println("elapsed_ms=" + (elapsed / 1_000_000.0));
    }
}