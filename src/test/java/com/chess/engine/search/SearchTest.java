package com.chess.engine.search;

import com.chess.engine.board.Board;
import com.chess.engine.moves.Move;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SearchTest {

    private static final String MATE_IN_ONE_FEN = "4k3/6pp/4K3/8/7Q/8/8/8 w - - 0 1";
    private static final String HANGING_QUEEN_FEN = "4k3/3q4/4P3/8/8/8/6K1/8 w - - 0 1";

    @Test
    void findsMateInOne() {
        Search search = new Search();
        Move bestMove = search.getBestMove(Board.fromFen(MATE_IN_ONE_FEN), 2);

        assertNotNull(bestMove);
        assertEquals("h4e7", bestMove.toUci());
    }

    @Test
    void capturesHangingQueenAtDepthTwo() {
        Search search = new Search();
        Move bestMove = search.getBestMove(Board.fromFen(HANGING_QUEEN_FEN), 2);

        assertNotNull(bestMove);
        assertEquals("e6d7", bestMove.toUci());
    }
}