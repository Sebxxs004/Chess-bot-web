package com.chess.engine.moves;

import java.util.ArrayList;
import java.util.List;

import com.chess.engine.board.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardConstants;
import com.chess.engine.board.PieceType;

public final class MoveGenerator {

    public List<Move> generateLegalMoves(Board board) {
        List<Move> legalMoves = new ArrayList<>();
        Alliance sideToMove = board.getSideToMove();
        for (Move move : generatePseudoLegalMoves(board, sideToMove)) {
            Board nextBoard = board.makeMove(move);
            if (!nextBoard.isKingInCheck(sideToMove)) {
                legalMoves.add(move);
            }
        }
        return legalMoves;
    }

    private List<Move> generatePseudoLegalMoves(Board board, Alliance sideToMove) {
        List<Move> moves = new ArrayList<>();
        long ownPieces = sideToMove == Alliance.WHITE ? board.getWhitePieces() : board.getBlackPieces();
        long opponentPieces = sideToMove == Alliance.WHITE ? board.getBlackPieces() : board.getWhitePieces();
        long occupancy = board.getAllPieces();

        generatePawnMoves(board, sideToMove, opponentPieces, occupancy, moves);
        generateKnightMoves(board, sideToMove, ownPieces, moves);
        generateSlidingMoves(board, sideToMove, PieceType.BISHOP, occupancy, moves);
        generateSlidingMoves(board, sideToMove, PieceType.ROOK, occupancy, moves);
        generateSlidingMoves(board, sideToMove, PieceType.QUEEN, occupancy, moves);
        generateKingMoves(board, sideToMove, moves);

        return moves;
    }

    private void generatePawnMoves(Board board,
                                   Alliance sideToMove,
                                   long opponentPieces,
                                   long occupancy,
                                   List<Move> moves) {
        long pawns = board.getPieceBitboard(sideToMove, PieceType.PAWN);
        int enPassantSquare = board.getEnPassantSquare();

        while (pawns != 0L) {
            int fromSquare = Long.numberOfTrailingZeros(pawns);
            pawns &= pawns - 1;

            if (sideToMove == Alliance.WHITE) {
                int oneForward = fromSquare + 8;
                if (oneForward < 64 && (occupancy & BoardConstants.bit(oneForward)) == 0L) {
                    if (BoardConstants.rankOf(fromSquare) == 6) {
                        addPromotionMoves(moves, fromSquare, oneForward, null);
                    } else {
                        moves.add(new Move(fromSquare, oneForward, PieceType.PAWN, null, null, MoveType.QUIET));
                        if (BoardConstants.rankOf(fromSquare) == 1) {
                            int twoForward = fromSquare + 16;
                            if ((occupancy & BoardConstants.bit(twoForward)) == 0L) {
                                moves.add(new Move(fromSquare, twoForward, PieceType.PAWN, null, null, MoveType.DOUBLE_PAWN_PUSH));
                            }
                        }
                    }
                }

                long attacks = BoardConstants.WHITE_PAWN_ATTACKS[fromSquare] & opponentPieces;
                while (attacks != 0L) {
                    int toSquare = Long.numberOfTrailingZeros(attacks);
                    attacks &= attacks - 1;
                    PieceType capturedPiece = pieceTypeAt(board, sideToMove.opposite(), toSquare);
                    if (capturedPiece == PieceType.KING) {
                        continue;
                    }
                    if (BoardConstants.rankOf(fromSquare) == 6) {
                        addPromotionMoves(moves, fromSquare, toSquare, capturedPiece);
                    } else {
                        moves.add(new Move(fromSquare, toSquare, PieceType.PAWN, capturedPiece, null, MoveType.CAPTURE));
                    }
                }

                if (enPassantSquare != -1 && (BoardConstants.WHITE_PAWN_ATTACKS[fromSquare] & BoardConstants.bit(enPassantSquare)) != 0L) {
                    moves.add(new Move(fromSquare, enPassantSquare, PieceType.PAWN, PieceType.PAWN, null, MoveType.EN_PASSANT));
                }
            } else {
                int oneForward = fromSquare - 8;
                if (oneForward >= 0 && (occupancy & BoardConstants.bit(oneForward)) == 0L) {
                    if (BoardConstants.rankOf(fromSquare) == 1) {
                        addPromotionMoves(moves, fromSquare, oneForward, null);
                    } else {
                        moves.add(new Move(fromSquare, oneForward, PieceType.PAWN, null, null, MoveType.QUIET));
                        if (BoardConstants.rankOf(fromSquare) == 6) {
                            int twoForward = fromSquare - 16;
                            if ((occupancy & BoardConstants.bit(twoForward)) == 0L) {
                                moves.add(new Move(fromSquare, twoForward, PieceType.PAWN, null, null, MoveType.DOUBLE_PAWN_PUSH));
                            }
                        }
                    }
                }

                long attacks = BoardConstants.BLACK_PAWN_ATTACKS[fromSquare] & opponentPieces;
                while (attacks != 0L) {
                    int toSquare = Long.numberOfTrailingZeros(attacks);
                    attacks &= attacks - 1;
                    PieceType capturedPiece = pieceTypeAt(board, sideToMove.opposite(), toSquare);
                    if (capturedPiece == PieceType.KING) {
                        continue;
                    }
                    if (BoardConstants.rankOf(fromSquare) == 1) {
                        addPromotionMoves(moves, fromSquare, toSquare, capturedPiece);
                    } else {
                        moves.add(new Move(fromSquare, toSquare, PieceType.PAWN, capturedPiece, null, MoveType.CAPTURE));
                    }
                }

                if (enPassantSquare != -1 && (BoardConstants.BLACK_PAWN_ATTACKS[fromSquare] & BoardConstants.bit(enPassantSquare)) != 0L) {
                    moves.add(new Move(fromSquare, enPassantSquare, PieceType.PAWN, PieceType.PAWN, null, MoveType.EN_PASSANT));
                }
            }
        }
    }

    private void generateKnightMoves(Board board,
                                     Alliance sideToMove,
                                     long ownPieces,
                                     List<Move> moves) {
        long knights = board.getPieceBitboard(sideToMove, PieceType.KNIGHT);
        while (knights != 0L) {
            int fromSquare = Long.numberOfTrailingZeros(knights);
            knights &= knights - 1;
            long targets = BoardConstants.KNIGHT_ATTACKS[fromSquare] & ~ownPieces;
            addMovesFromTargets(board, sideToMove, fromSquare, PieceType.KNIGHT, targets, moves);
        }
    }

    private void generateSlidingMoves(Board board,
                                      Alliance sideToMove,
                                      PieceType pieceType,
                                      long occupancy,
                                      List<Move> moves) {
        long pieces = board.getPieceBitboard(sideToMove, pieceType);
        while (pieces != 0L) {
            int fromSquare = Long.numberOfTrailingZeros(pieces);
            pieces &= pieces - 1;

            if (pieceType == PieceType.BISHOP || pieceType == PieceType.QUEEN) {
                generateRayMoves(board, sideToMove, fromSquare, pieceType, occupancy, moves, 9, 7, -7, -9);
            }
            if (pieceType == PieceType.ROOK || pieceType == PieceType.QUEEN) {
                generateRayMoves(board, sideToMove, fromSquare, pieceType, occupancy, moves, 8, -8, 1, -1);
            }
        }
    }

    private void generateKingMoves(Board board,
                                   Alliance sideToMove,
                                   List<Move> moves) {
        long kingBoard = board.getPieceBitboard(sideToMove, PieceType.KING);
        if (kingBoard == 0L) {
            return;
        }

        int fromSquare = Long.numberOfTrailingZeros(kingBoard);
        long ownPieces = sideToMove == Alliance.WHITE ? board.getWhitePieces() : board.getBlackPieces();
        long targets = BoardConstants.KING_ATTACKS[fromSquare] & ~ownPieces;
        addMovesFromTargets(board, sideToMove, fromSquare, PieceType.KING, targets, moves);

        if (board.isKingInCheck(sideToMove)) {
            return;
        }

        if (sideToMove == Alliance.WHITE) {
            if (board.canWhiteKingSideCastle()
                    && (board.getPieceBitboard(Alliance.WHITE, PieceType.ROOK) & BoardConstants.bit(7)) != 0L
                    && (board.getAllPieces() & (BoardConstants.bit(5) | BoardConstants.bit(6))) == 0L
                    && !board.isSquareAttacked(4, Alliance.BLACK)
                    && !board.isSquareAttacked(5, Alliance.BLACK)
                    && !board.isSquareAttacked(6, Alliance.BLACK)) {
                moves.add(new Move(4, 6, PieceType.KING, null, null, MoveType.KING_SIDE_CASTLE));
            }
            if (board.canWhiteQueenSideCastle()
                    && (board.getPieceBitboard(Alliance.WHITE, PieceType.ROOK) & BoardConstants.bit(0)) != 0L
                    && (board.getAllPieces() & (BoardConstants.bit(1) | BoardConstants.bit(2) | BoardConstants.bit(3))) == 0L
                    && !board.isSquareAttacked(4, Alliance.BLACK)
                    && !board.isSquareAttacked(3, Alliance.BLACK)
                    && !board.isSquareAttacked(2, Alliance.BLACK)) {
                moves.add(new Move(4, 2, PieceType.KING, null, null, MoveType.QUEEN_SIDE_CASTLE));
            }
        } else {
            if (board.canBlackKingSideCastle()
                    && (board.getPieceBitboard(Alliance.BLACK, PieceType.ROOK) & BoardConstants.bit(63)) != 0L
                    && (board.getAllPieces() & (BoardConstants.bit(61) | BoardConstants.bit(62))) == 0L
                    && !board.isSquareAttacked(60, Alliance.WHITE)
                    && !board.isSquareAttacked(61, Alliance.WHITE)
                    && !board.isSquareAttacked(62, Alliance.WHITE)) {
                moves.add(new Move(60, 62, PieceType.KING, null, null, MoveType.KING_SIDE_CASTLE));
            }
            if (board.canBlackQueenSideCastle()
                    && (board.getPieceBitboard(Alliance.BLACK, PieceType.ROOK) & BoardConstants.bit(56)) != 0L
                    && (board.getAllPieces() & (BoardConstants.bit(57) | BoardConstants.bit(58) | BoardConstants.bit(59))) == 0L
                    && !board.isSquareAttacked(60, Alliance.WHITE)
                    && !board.isSquareAttacked(59, Alliance.WHITE)
                    && !board.isSquareAttacked(58, Alliance.WHITE)) {
                moves.add(new Move(60, 58, PieceType.KING, null, null, MoveType.QUEEN_SIDE_CASTLE));
            }
        }
    }

    private void addMovesFromTargets(Board board,
                                     Alliance sideToMove,
                                     int fromSquare,
                                     PieceType pieceType,
                                     long targets,
                                     List<Move> moves) {
        while (targets != 0L) {
            int toSquare = Long.numberOfTrailingZeros(targets);
            targets &= targets - 1;
            PieceType capturedPiece = pieceTypeAt(board, sideToMove.opposite(), toSquare);
            if (capturedPiece == PieceType.KING) {
                continue;
            }
            MoveType moveType = capturedPiece == null ? MoveType.QUIET : MoveType.CAPTURE;
            moves.add(new Move(fromSquare, toSquare, pieceType, capturedPiece, null, moveType));
        }
    }

    private void generateRayMoves(Board board,
                                  Alliance sideToMove,
                                  int fromSquare,
                                  PieceType pieceType,
                                  long occupancy,
                                  List<Move> moves,
                                  int... directions) {
        long ownPieces = sideToMove == Alliance.WHITE ? board.getWhitePieces() : board.getBlackPieces();
        for (int direction : directions) {
            int currentSquare = fromSquare;
            while (canStep(currentSquare, direction)) {
                currentSquare += direction;
                long currentBit = BoardConstants.bit(currentSquare);
                if ((ownPieces & currentBit) != 0L) {
                    break;
                }
                PieceType capturedPiece = pieceTypeAt(board, sideToMove.opposite(), currentSquare);
                if (capturedPiece == PieceType.KING) {
                    break;
                }
                MoveType moveType = capturedPiece == null ? MoveType.QUIET : MoveType.CAPTURE;
                moves.add(new Move(fromSquare, currentSquare, pieceType, capturedPiece, null, moveType));
                if ((occupancy & currentBit) != 0L) {
                    break;
                }
            }
        }
    }

    private void addPromotionMoves(List<Move> moves,
                                   int fromSquare,
                                   int toSquare,
                                   PieceType capturedPieceType) {
        MoveType moveType = capturedPieceType == null ? MoveType.PROMOTION : MoveType.PROMOTION_CAPTURE;
        moves.add(new Move(fromSquare, toSquare, PieceType.PAWN, capturedPieceType, PieceType.QUEEN, moveType));
        moves.add(new Move(fromSquare, toSquare, PieceType.PAWN, capturedPieceType, PieceType.ROOK, moveType));
        moves.add(new Move(fromSquare, toSquare, PieceType.PAWN, capturedPieceType, PieceType.BISHOP, moveType));
        moves.add(new Move(fromSquare, toSquare, PieceType.PAWN, capturedPieceType, PieceType.KNIGHT, moveType));
    }

    private PieceType pieceTypeAt(Board board, Alliance alliance, int square) {
        long squareBit = BoardConstants.bit(square);
        for (PieceType pieceType : PieceType.values()) {
            if ((board.getPieceBitboard(alliance, pieceType) & squareBit) != 0L) {
                return pieceType;
            }
        }
        return null;
    }

    private static boolean canStep(int currentSquare, int direction) {
        int file = BoardConstants.fileOf(currentSquare);
        int rank = BoardConstants.rankOf(currentSquare);
        return switch (direction) {
            case 8 -> rank < 7;
            case -8 -> rank > 0;
            case 1 -> file < 7;
            case -1 -> file > 0;
            case 9 -> file < 7 && rank < 7;
            case 7 -> file > 0 && rank < 7;
            case -7 -> file < 7 && rank > 0;
            case -9 -> file > 0 && rank > 0;
            default -> false;
        };
    }
}