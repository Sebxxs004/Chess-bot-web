package com.chess.engine.search;

import com.chess.engine.board.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardConstants;
import com.chess.engine.board.PieceType;

import java.util.SplittableRandom;

public final class Zobrist {

    private static final long[][] PIECE_SQUARE_KEYS = new long[12][64];
    private static final long[] CASTLING_KEYS = new long[4];
    private static final long[] EN_PASSANT_FILE_KEYS = new long[8];
    private static final long SIDE_TO_MOVE_KEY;

    static {
        SplittableRandom random = new SplittableRandom(0xC0FFEE_F00DBA5EL);
        for (int piece = 0; piece < PIECE_SQUARE_KEYS.length; piece++) {
            for (int square = 0; square < 64; square++) {
                PIECE_SQUARE_KEYS[piece][square] = random.nextLong();
            }
        }
        for (int index = 0; index < CASTLING_KEYS.length; index++) {
            CASTLING_KEYS[index] = random.nextLong();
        }
        for (int file = 0; file < EN_PASSANT_FILE_KEYS.length; file++) {
            EN_PASSANT_FILE_KEYS[file] = random.nextLong();
        }
        SIDE_TO_MOVE_KEY = random.nextLong();
    }

    private Zobrist() {
    }

    public static long computeKey(Board board) {
        long key = 0L;
        key ^= piecesKey(board.getPieceBitboard(Alliance.WHITE, PieceType.PAWN), 0);
        key ^= piecesKey(board.getPieceBitboard(Alliance.WHITE, PieceType.KNIGHT), 1);
        key ^= piecesKey(board.getPieceBitboard(Alliance.WHITE, PieceType.BISHOP), 2);
        key ^= piecesKey(board.getPieceBitboard(Alliance.WHITE, PieceType.ROOK), 3);
        key ^= piecesKey(board.getPieceBitboard(Alliance.WHITE, PieceType.QUEEN), 4);
        key ^= piecesKey(board.getPieceBitboard(Alliance.WHITE, PieceType.KING), 5);
        key ^= piecesKey(board.getPieceBitboard(Alliance.BLACK, PieceType.PAWN), 6);
        key ^= piecesKey(board.getPieceBitboard(Alliance.BLACK, PieceType.KNIGHT), 7);
        key ^= piecesKey(board.getPieceBitboard(Alliance.BLACK, PieceType.BISHOP), 8);
        key ^= piecesKey(board.getPieceBitboard(Alliance.BLACK, PieceType.ROOK), 9);
        key ^= piecesKey(board.getPieceBitboard(Alliance.BLACK, PieceType.QUEEN), 10);
        key ^= piecesKey(board.getPieceBitboard(Alliance.BLACK, PieceType.KING), 11);

        if (board.getSideToMove() == Alliance.WHITE) {
            key ^= SIDE_TO_MOVE_KEY;
        }
        if (board.canWhiteKingSideCastle()) {
            key ^= CASTLING_KEYS[0];
        }
        if (board.canWhiteQueenSideCastle()) {
            key ^= CASTLING_KEYS[1];
        }
        if (board.canBlackKingSideCastle()) {
            key ^= CASTLING_KEYS[2];
        }
        if (board.canBlackQueenSideCastle()) {
            key ^= CASTLING_KEYS[3];
        }
        if (board.getEnPassantSquare() != -1) {
            key ^= EN_PASSANT_FILE_KEYS[BoardConstants.fileOf(board.getEnPassantSquare())];
        }
        return key;
    }

    public static long pieceSquareKey(Alliance alliance, PieceType pieceType, int square) {
        return PIECE_SQUARE_KEYS[pieceIndex(alliance, pieceType)][square];
    }

    public static long sideToMoveKey() {
        return SIDE_TO_MOVE_KEY;
    }

    public static long castlingKey(int index) {
        return CASTLING_KEYS[index];
    }

    public static long enPassantFileKey(int file) {
        return EN_PASSANT_FILE_KEYS[file];
    }

    public static int pieceIndex(Alliance alliance, PieceType pieceType) {
        int offset = alliance == Alliance.WHITE ? 0 : 6;
        return offset + pieceType.ordinal();
    }

    private static long piecesKey(long bitboard, int pieceIndex) {
        long key = 0L;
        long pieces = bitboard;
        while (pieces != 0L) {
            int square = Long.numberOfTrailingZeros(pieces);
            pieces &= pieces - 1;
            key ^= PIECE_SQUARE_KEYS[pieceIndex][square];
        }
        return key;
    }
}