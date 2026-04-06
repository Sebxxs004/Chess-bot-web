package com.chess.engine.search;

import com.chess.engine.board.Alliance;

public final class TimeManager {

    private TimeManager() {
    }

    public static long computeMoveTimeMillis(Alliance sideToMove,
                                             long wtime,
                                             long btime,
                                             long winc,
                                             long binc,
                                             long movetime) {
        if (movetime > 0) {
            return Math.max(10L, movetime);
        }

        long remaining = sideToMove == Alliance.WHITE ? wtime : btime;
        long increment = sideToMove == Alliance.WHITE ? winc : binc;

        if (remaining <= 0) {
            return 1_000L;
        }

        long base = remaining / 20;
        long bonus = increment / 2;
        long allocated = base + bonus;

        long maxSafe = Math.max(20L, remaining - 20L);
        allocated = Math.min(allocated, maxSafe);
        allocated = Math.max(allocated, 50L);
        return allocated;
    }
}