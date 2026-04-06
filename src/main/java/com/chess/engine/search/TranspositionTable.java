package com.chess.engine.search;

import com.chess.engine.moves.Move;

public final class TranspositionTable {

    public static final byte EXACT = 0;
    public static final byte LOWERBOUND = 1;
    public static final byte UPPERBOUND = 2;

    private final long[] keys;
    private final int[] depths;
    private final int[] scores;
    private final byte[] flags;
    private final int[] bestMoves;
    private final int mask;

    public TranspositionTable(int sizePowerOfTwo) {
        int size = 1;
        while (size < sizePowerOfTwo) {
            size <<= 1;
        }
        keys = new long[size];
        depths = new int[size];
        scores = new int[size];
        flags = new byte[size];
        bestMoves = new int[size];
        mask = size - 1;
    }

    public Entry probe(long key) {
        int index = index(key);
        if (keys[index] == key) {
            return new Entry(keys[index], depths[index], scores[index], flags[index], bestMoves[index]);
        }
        return null;
    }

    public void store(long key, int depth, int score, byte flag, Move bestMove) {
        int index = index(key);
        if (keys[index] != 0L && keys[index] != key && depths[index] > depth) {
            return;
        }
        keys[index] = key;
        depths[index] = depth;
        scores[index] = score;
        flags[index] = flag;
        bestMoves[index] = bestMove == null ? 0 : bestMove.encode();
    }

    private int index(long key) {
        return (int) (key ^ (key >>> 32)) & mask;
    }

    public static final class Entry {
        private final long key;
        private final int depth;
        private final int score;
        private final byte flag;
        private final int bestMoveCode;

        public Entry(long key, int depth, int score, byte flag, int bestMoveCode) {
            this.key = key;
            this.depth = depth;
            this.score = score;
            this.flag = flag;
            this.bestMoveCode = bestMoveCode;
        }

        public long getKey() {
            return key;
        }

        public int getDepth() {
            return depth;
        }

        public int getScore() {
            return score;
        }

        public byte getFlag() {
            return flag;
        }

        public Move getBestMove() {
            return bestMoveCode == 0 ? null : Move.fromEncoded(bestMoveCode);
        }
    }
}