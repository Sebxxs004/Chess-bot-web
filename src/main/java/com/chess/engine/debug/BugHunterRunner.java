package com.chess.engine.debug;

import java.util.List;

import com.chess.engine.board.Board;
import com.chess.engine.moves.Move;
import com.chess.engine.moves.MoveGenerator;
import com.chess.engine.moves.Perft;

public final class BugHunterRunner {

    private static final String KIWIPETE_FEN = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1";

    public static void main(String[] args) {
        MoveGenerator moveGenerator = new MoveGenerator();
        Perft perft = new Perft();

        System.out.println("=== Branch after e2a6 ===");
        printBlackRepliesAfterWhiteMove(moveGenerator, perft, KIWIPETE_FEN, "e2a6");

        System.out.println();
        System.out.println("=== Branch after e2b5 ===");
        printBlackRepliesAfterWhiteMove(moveGenerator, perft, KIWIPETE_FEN, "e2b5");
    }

    private static void printBlackRepliesAfterWhiteMove(MoveGenerator moveGenerator,
                                                        Perft perft,
                                                        String fen,
                                                        String whiteMoveUci) {
        Board board = Board.fromFen(fen);
        Move whiteMove = findMove(moveGenerator.generateLegalMoves(board), whiteMoveUci);
        if (whiteMove == null) {
            throw new IllegalStateException("Could not find legal move: " + whiteMoveUci);
        }

        Board afterWhiteMove = board.makeMove(whiteMove);
        System.out.println("FEN after " + whiteMoveUci + ": " + afterWhiteMove.toFen());
        perft.perftDivide(afterWhiteMove, 1);
    }

    private static Move findMove(List<Move> moves, String uci) {
        for (Move move : moves) {
            if (move.toUci().equals(uci)) {
                return move;
            }
        }
        return null;
    }
}