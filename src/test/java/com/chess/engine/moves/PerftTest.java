package com.chess.engine.moves;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.chess.engine.board.Board;

class PerftTest {

    private static final String INITIAL_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    private static final String KIWIPETE_FEN = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1";
    private static final String PROMOTION_ENDGAME_FEN = "4k3/P7/8/8/8/8/8/4K3 w - - 0 1";

    @Test
    void initialPositionPerftDepthOneMatchesCommunityStandard() {
        Board board = Board.fromFen(INITIAL_FEN);
        Perft perft = new Perft();

        assertEquals(20L, perft.perft(board, 1));
    }

    @Test
    void initialPositionPerftDepthTwoMatchesCommunityStandard() {
        Board board = Board.fromFen(INITIAL_FEN);
        Perft perft = new Perft();

        assertEquals(400L, perft.perft(board, 2));
    }

    @Test
    void initialPositionPerftDepthThreeMatchesCommunityStandard() {
        Board board = Board.fromFen(INITIAL_FEN);
        Perft perft = new Perft();

        assertEquals(8902L, perft.perft(board, 3));
    }

    @Test
    void initialPositionPerftDepthFourMatchesCommunityStandard() {
        Board board = Board.fromFen(INITIAL_FEN);
        Perft perft = new Perft();

        assertEquals(197281L, perft.perft(board, 4));
    }

    @Test
    void kiwipetePerftDepthThreeMatchesCommunityStandard() {
        Board board = Board.fromFen(KIWIPETE_FEN);
        Perft perft = new Perft();

        assertEquals(97862L, perft.perft(board, 3));
    }

    @Test
    void endgamePositionGeneratesPromotionMoves() {
        Board board = Board.fromFen(PROMOTION_ENDGAME_FEN);
        MoveGenerator generator = new MoveGenerator();

        List<Move> legalMoves = generator.generateLegalMoves(board);
        long promotions = legalMoves.stream().filter(move -> move.getPromotionPieceType() != null).count();

        assertEquals(4L, promotions);
        assertEquals(9L, new Perft().perft(board, 1));
    }

    @Test
    void fenRoundTripPreservesBoardState() {
        Board board = Board.fromFen(KIWIPETE_FEN);

        assertEquals(KIWIPETE_FEN, board.toFen());
    }

    @Test
    void malformedFenThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Board.fromFen("invalid fen"));
        assertTrue(exception.getMessage().startsWith("Invalid FEN:"));
    }
}