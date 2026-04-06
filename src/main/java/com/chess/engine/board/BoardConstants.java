package com.chess.engine.board;

public final class BoardConstants {

    public static final long FILE_A = 0x0101010101010101L;
    public static final long FILE_B = FILE_A << 1;
    public static final long FILE_C = FILE_A << 2;
    public static final long FILE_D = FILE_A << 3;
    public static final long FILE_E = FILE_A << 4;
    public static final long FILE_F = FILE_A << 5;
    public static final long FILE_G = FILE_A << 6;
    public static final long FILE_H = FILE_A << 7;

    public static final long RANK_1 = 0x00000000000000FFL;
    public static final long RANK_2 = RANK_1 << 8;
    public static final long RANK_3 = RANK_1 << 16;
    public static final long RANK_4 = RANK_1 << 24;
    public static final long RANK_5 = RANK_1 << 32;
    public static final long RANK_6 = RANK_1 << 40;
    public static final long RANK_7 = RANK_1 << 48;
    public static final long RANK_8 = RANK_1 << 56;

    public static final long[] KNIGHT_ATTACKS = new long[64];
    public static final long[] KING_ATTACKS = new long[64];
    public static final long[] WHITE_PAWN_ATTACKS = new long[64];
    public static final long[] BLACK_PAWN_ATTACKS = new long[64];

    static {
        for (int square = 0; square < 64; square++) {
            KNIGHT_ATTACKS[square] = computeKnightAttacks(square);
            KING_ATTACKS[square] = computeKingAttacks(square);
            WHITE_PAWN_ATTACKS[square] = computeWhitePawnAttacks(square);
            BLACK_PAWN_ATTACKS[square] = computeBlackPawnAttacks(square);
        }
    }

    private BoardConstants() {
    }

    public static long bit(int square) {
        return 1L << square;
    }

    public static int fileOf(int square) {
        return square & 7;
    }

    public static int rankOf(int square) {
        return square >> 3;
    }

    private static long computeKnightAttacks(int square) {
        long attacks = 0L;
        int file = fileOf(square);
        int rank = rankOf(square);
        int[][] offsets = {
                {1, 2}, {2, 1}, {2, -1}, {1, -2},
                {-1, -2}, {-2, -1}, {-2, 1}, {-1, 2}
        };
        for (int[] offset : offsets) {
            int targetFile = file + offset[0];
            int targetRank = rank + offset[1];
            if (targetFile >= 0 && targetFile < 8 && targetRank >= 0 && targetRank < 8) {
                attacks |= bit(targetRank * 8 + targetFile);
            }
        }
        return attacks;
    }

    private static long computeKingAttacks(int square) {
        long attacks = 0L;
        int file = fileOf(square);
        int rank = rankOf(square);
        for (int fileOffset = -1; fileOffset <= 1; fileOffset++) {
            for (int rankOffset = -1; rankOffset <= 1; rankOffset++) {
                if (fileOffset == 0 && rankOffset == 0) {
                    continue;
                }
                int targetFile = file + fileOffset;
                int targetRank = rank + rankOffset;
                if (targetFile >= 0 && targetFile < 8 && targetRank >= 0 && targetRank < 8) {
                    attacks |= bit(targetRank * 8 + targetFile);
                }
            }
        }
        return attacks;
    }

    private static long computeWhitePawnAttacks(int square) {
        long attacks = 0L;
        int file = fileOf(square);
        int rank = rankOf(square);
        if (file > 0 && rank < 7) {
            attacks |= bit((rank + 1) * 8 + file - 1);
        }
        if (file < 7 && rank < 7) {
            attacks |= bit((rank + 1) * 8 + file + 1);
        }
        return attacks;
    }

    private static long computeBlackPawnAttacks(int square) {
        long attacks = 0L;
        int file = fileOf(square);
        int rank = rankOf(square);
        if (file > 0 && rank > 0) {
            attacks |= bit((rank - 1) * 8 + file - 1);
        }
        if (file < 7 && rank > 0) {
            attacks |= bit((rank - 1) * 8 + file + 1);
        }
        return attacks;
    }
}