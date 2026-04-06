package com.chess.engine.search;

import com.chess.engine.board.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardConstants;
import com.chess.engine.board.PieceType;

public final class Evaluator {

    private static final int PAWN_VALUE = 100;
    private static final int KNIGHT_VALUE = 320;
    private static final int BISHOP_VALUE = 330;
    private static final int ROOK_VALUE = 500;
    private static final int QUEEN_VALUE = 900;

    private static final int[] PAWN_PST = new int[64];
    private static final int[] KNIGHT_PST = new int[64];
    private static final int[] BISHOP_PST = new int[64];
    private static final int[] ROOK_PST = new int[64];
    private static final int[] QUEEN_PST = new int[64];
    private static final int[] KING_PST = new int[64];

    static {
        for (int square = 0; square < 64; square++) {
            int file = BoardConstants.fileOf(square);
            int rank = BoardConstants.rankOf(square);
            int fileDistance = Math.min(Math.abs(file - 3), Math.abs(file - 4));
            int rankDistance = Math.min(Math.abs(rank - 3), Math.abs(rank - 4));
            int centerDistance = fileDistance + rankDistance;

            PAWN_PST[square] = rank * 6 - Math.abs(file - 3) - Math.abs(file - 4);
            KNIGHT_PST[square] = 30 - centerDistance * 10;
            BISHOP_PST[square] = 12 - centerDistance * 4;
            ROOK_PST[square] = rank * 2;
            QUEEN_PST[square] = 6 - centerDistance * 2;
            KING_PST[square] = -centerDistance * 8;
        }
    }

    public int evaluate(Board board) {
        int whiteScore = materialAndPosition(board, Alliance.WHITE);
        int blackScore = materialAndPosition(board, Alliance.BLACK);
        return whiteScore - blackScore;
    }

    private int materialAndPosition(Board board, Alliance alliance) {
        int score = 0;
        score += scorePieces(board, alliance, PieceType.PAWN, PAWN_VALUE, PAWN_PST);
        score += scorePieces(board, alliance, PieceType.KNIGHT, KNIGHT_VALUE, KNIGHT_PST);
        score += scorePieces(board, alliance, PieceType.BISHOP, BISHOP_VALUE, BISHOP_PST);
        score += scorePieces(board, alliance, PieceType.ROOK, ROOK_VALUE, ROOK_PST);
        score += scorePieces(board, alliance, PieceType.QUEEN, QUEEN_VALUE, QUEEN_PST);
        score += scorePieces(board, alliance, PieceType.KING, 0, KING_PST);
        return score;
    }

    private int scorePieces(Board board, Alliance alliance, PieceType pieceType, int pieceValue, int[] pst) {
        long pieces = board.getPieceBitboard(alliance, pieceType);
        int score = 0;
        while (pieces != 0L) {
            int square = Long.numberOfTrailingZeros(pieces);
            pieces &= pieces - 1;
            score += pieceValue + pst[alliance == Alliance.WHITE ? square : mirrorSquare(square)];
        }
        return score;
    }

    private int mirrorSquare(int square) {
        int file = BoardConstants.fileOf(square);
        int rank = BoardConstants.rankOf(square);
        int mirroredRank = 7 - rank;
        return mirroredRank * 8 + file;
    }
}