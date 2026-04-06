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
}