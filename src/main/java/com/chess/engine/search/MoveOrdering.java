package com.chess.engine.search;

import com.chess.engine.moves.Move;

import java.util.Comparator;
import java.util.List;

public final class MoveOrdering {

    private static final int CAPTURE_BASE = 1_000_000;
    private static final int PROMOTION_BASE = 100_000;
    private static final int CASTLE_BONUS = 1_000;

    public List<Move> orderMoves(List<Move> moves) {
        moves.sort(Comparator.comparingInt(this::scoreMove).reversed());
        return moves;
    }

    public List<Move> orderMoves(List<Move> moves, Move preferredMove) {
        moves.sort((left, right) -> {
            boolean leftPreferred = isSameMove(left, preferredMove);
            boolean rightPreferred = isSameMove(right, preferredMove);
            if (leftPreferred != rightPreferred) {
                return leftPreferred ? -1 : 1;
            }
            int leftScore = scoreMove(left);
            int rightScore = scoreMove(right);
            return Integer.compare(rightScore, leftScore);
        });
        return moves;
    }

    public List<Move> orderCaptureMoves(List<Move> moves) {
        moves.removeIf(move -> !move.isCapture());
        return orderMoves(moves);
    }

    private int scoreMove(Move move) {
        if (move == null) {
            return Integer.MIN_VALUE;
        }

        int score = 0;

        if (move.getCapturedPieceType() != null) {
            score += CAPTURE_BASE;
            score += pieceValue(move.getCapturedPieceType()) * 10;
            score -= pieceValue(move.getPieceType());
        }

        if (move.getPromotionPieceType() != null) {
            score += PROMOTION_BASE;
            score += pieceValue(move.getPromotionPieceType());
        }

        if (move.getMoveType().name().contains("CASTLE")) {
            score += CASTLE_BONUS;
        }

        if (move.isCapture() && move.getCapturedPieceType() != null) {
            score += pieceValue(move.getCapturedPieceType()) - pieceValue(move.getPieceType());
        }

        return score;
    }

    private int pieceValue(com.chess.engine.board.PieceType pieceType) {
        return switch (pieceType) {
            case PAWN -> 100;
            case KNIGHT -> 320;
            case BISHOP -> 330;
            case ROOK -> 500;
            case QUEEN -> 900;
            case KING -> 20_000;
        };
    }

    private boolean isSameMove(Move left, Move right) {
        if (left == null || right == null) {
            return false;
        }
        return left.getFromSquare() == right.getFromSquare()
                && left.getToSquare() == right.getToSquare()
                && left.getPieceType() == right.getPieceType()
                && left.getCapturedPieceType() == right.getCapturedPieceType()
                && left.getPromotionPieceType() == right.getPromotionPieceType()
                && left.getMoveType() == right.getMoveType();
    }
}