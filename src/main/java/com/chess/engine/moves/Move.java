package com.chess.engine.moves;

import com.chess.engine.board.PieceType;

public final class Move {

    private final int fromSquare;
    private final int toSquare;
    private final PieceType pieceType;
    private final PieceType capturedPieceType;
    private final PieceType promotionPieceType;
    private final MoveType moveType;

    public Move(int fromSquare,
                int toSquare,
                PieceType pieceType,
                PieceType capturedPieceType,
                PieceType promotionPieceType,
                MoveType moveType) {
        this.fromSquare = fromSquare;
        this.toSquare = toSquare;
        this.pieceType = pieceType;
        this.capturedPieceType = capturedPieceType;
        this.promotionPieceType = promotionPieceType;
        this.moveType = moveType;
    }

    public int getFromSquare() {
        return fromSquare;
    }

    public int getToSquare() {
        return toSquare;
    }

    public PieceType getPieceType() {
        return pieceType;
    }

    public PieceType getCapturedPieceType() {
        return capturedPieceType;
    }

    public PieceType getPromotionPieceType() {
        return promotionPieceType;
    }

    public MoveType getMoveType() {
        return moveType;
    }

    public boolean isCapture() {
        return capturedPieceType != null || moveType == MoveType.EN_PASSANT || moveType == MoveType.PROMOTION_CAPTURE;
    }

    public int encode() {
        int code = 0;
        code |= (fromSquare & 0x3F);
        code |= (toSquare & 0x3F) << 6;
        code |= (pieceType.ordinal() & 0x7) << 12;
        code |= (capturedPieceType == null ? 7 : capturedPieceType.ordinal() & 0x7) << 15;
        code |= (promotionPieceType == null ? 7 : promotionPieceType.ordinal() & 0x7) << 18;
        code |= (moveType.ordinal() & 0x7) << 21;
        return code;
    }

    public static Move fromEncoded(int code) {
        int fromSquare = code & 0x3F;
        int toSquare = (code >>> 6) & 0x3F;
        PieceType pieceType = PieceType.values()[(code >>> 12) & 0x7];
        int capturedOrdinal = (code >>> 15) & 0x7;
        PieceType capturedPieceType = capturedOrdinal == 7 ? null : PieceType.values()[capturedOrdinal];
        int promotionOrdinal = (code >>> 18) & 0x7;
        PieceType promotionPieceType = promotionOrdinal == 7 ? null : PieceType.values()[promotionOrdinal];
        MoveType moveType = MoveType.values()[(code >>> 21) & 0x7];
        return new Move(fromSquare, toSquare, pieceType, capturedPieceType, promotionPieceType, moveType);
    }

    public String toUci() {
        StringBuilder notation = new StringBuilder();
        notation.append(squareToAlgebraic(fromSquare));
        notation.append(squareToAlgebraic(toSquare));
        if (promotionPieceType != null) {
            notation.append(switch (promotionPieceType) {
                case QUEEN -> 'q';
                case ROOK -> 'r';
                case BISHOP -> 'b';
                case KNIGHT -> 'n';
                case PAWN, KING -> throw new IllegalStateException("Invalid promotion piece type: " + promotionPieceType);
            });
        }
        return notation.toString();
    }

    @Override
    public String toString() {
        return toUci();
    }

    private static String squareToAlgebraic(int square) {
        int file = square & 7;
        int rank = square >> 3;
        return String.valueOf((char) ('a' + file)) + (char) ('1' + rank);
    }
}