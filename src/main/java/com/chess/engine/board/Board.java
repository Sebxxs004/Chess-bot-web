package com.chess.engine.board;

import com.chess.engine.moves.Move;
import com.chess.engine.moves.MoveType;

public final class Board {

    private final long whitePawns;
    private final long whiteKnights;
    private final long whiteBishops;
    private final long whiteRooks;
    private final long whiteQueens;
    private final long whiteKing;

    private final long blackPawns;
    private final long blackKnights;
    private final long blackBishops;
    private final long blackRooks;
    private final long blackQueens;
    private final long blackKing;

    private final boolean whiteToMove;
    private final boolean whiteKingSideCastle;
    private final boolean whiteQueenSideCastle;
    private final boolean blackKingSideCastle;
    private final boolean blackQueenSideCastle;
    private final int enPassantSquare;
    private final int halfmoveClock;
    private final int fullmoveNumber;

    private Board(long whitePawns,
                  long whiteKnights,
                  long whiteBishops,
                  long whiteRooks,
                  long whiteQueens,
                  long whiteKing,
                  long blackPawns,
                  long blackKnights,
                  long blackBishops,
                  long blackRooks,
                  long blackQueens,
                  long blackKing,
                  boolean whiteToMove,
                  boolean whiteKingSideCastle,
                  boolean whiteQueenSideCastle,
                  boolean blackKingSideCastle,
                  boolean blackQueenSideCastle,
                  int enPassantSquare,
                  int halfmoveClock,
                  int fullmoveNumber) {
        this.whitePawns = whitePawns;
        this.whiteKnights = whiteKnights;
        this.whiteBishops = whiteBishops;
        this.whiteRooks = whiteRooks;
        this.whiteQueens = whiteQueens;
        this.whiteKing = whiteKing;
        this.blackPawns = blackPawns;
        this.blackKnights = blackKnights;
        this.blackBishops = blackBishops;
        this.blackRooks = blackRooks;
        this.blackQueens = blackQueens;
        this.blackKing = blackKing;
        this.whiteToMove = whiteToMove;
        this.whiteKingSideCastle = whiteKingSideCastle;
        this.whiteQueenSideCastle = whiteQueenSideCastle;
        this.blackKingSideCastle = blackKingSideCastle;
        this.blackQueenSideCastle = blackQueenSideCastle;
        this.enPassantSquare = enPassantSquare;
        this.halfmoveClock = halfmoveClock;
        this.fullmoveNumber = fullmoveNumber;
    }

    public static Board initialPosition() {
        return new Board(
                BoardConstants.RANK_2,
                BoardConstants.bit(1) | BoardConstants.bit(6),
                BoardConstants.bit(2) | BoardConstants.bit(5),
                BoardConstants.bit(0) | BoardConstants.bit(7),
                BoardConstants.bit(3),
                BoardConstants.bit(4),
                BoardConstants.RANK_7,
                BoardConstants.bit(57) | BoardConstants.bit(62),
                BoardConstants.bit(58) | BoardConstants.bit(61),
                BoardConstants.bit(56) | BoardConstants.bit(63),
                BoardConstants.bit(59),
                BoardConstants.bit(60),
                true,
                true,
                true,
                true,
                true,
                -1
                ,0,
                1
        );
    }

    static Board create(long whitePawns,
                        long whiteKnights,
                        long whiteBishops,
                        long whiteRooks,
                        long whiteQueens,
                        long whiteKing,
                        long blackPawns,
                        long blackKnights,
                        long blackBishops,
                        long blackRooks,
                        long blackQueens,
                        long blackKing,
                        boolean whiteToMove,
                        boolean whiteKingSideCastle,
                        boolean whiteQueenSideCastle,
                        boolean blackKingSideCastle,
                        boolean blackQueenSideCastle,
                        int enPassantSquare,
                        int halfmoveClock,
                        int fullmoveNumber) {
        return new Board(
                whitePawns,
                whiteKnights,
                whiteBishops,
                whiteRooks,
                whiteQueens,
                whiteKing,
                blackPawns,
                blackKnights,
                blackBishops,
                blackRooks,
                blackQueens,
                blackKing,
                whiteToMove,
                whiteKingSideCastle,
                whiteQueenSideCastle,
                blackKingSideCastle,
                blackQueenSideCastle,
                enPassantSquare,
                halfmoveClock,
                fullmoveNumber
        );
    }

    public static Board fromFen(String fen) {
        return FenUtility.fromFen(fen);
    }

    public String toFen() {
        return FenUtility.toFen(this);
    }

    public Alliance getSideToMove() {
        return whiteToMove ? Alliance.WHITE : Alliance.BLACK;
    }

    public long getWhitePieces() {
        return whitePawns | whiteKnights | whiteBishops | whiteRooks | whiteQueens | whiteKing;
    }

    public long getBlackPieces() {
        return blackPawns | blackKnights | blackBishops | blackRooks | blackQueens | blackKing;
    }

    public long getAllPieces() {
        return getWhitePieces() | getBlackPieces();
    }

    public long getPieceBitboard(Alliance alliance, PieceType pieceType) {
        if (alliance == Alliance.WHITE) {
            return switch (pieceType) {
                case PAWN -> whitePawns;
                case KNIGHT -> whiteKnights;
                case BISHOP -> whiteBishops;
                case ROOK -> whiteRooks;
                case QUEEN -> whiteQueens;
                case KING -> whiteKing;
            };
        }

        return switch (pieceType) {
            case PAWN -> blackPawns;
            case KNIGHT -> blackKnights;
            case BISHOP -> blackBishops;
            case ROOK -> blackRooks;
            case QUEEN -> blackQueens;
            case KING -> blackKing;
        };
    }

    public int getEnPassantSquare() {
        return enPassantSquare;
    }

    public int getHalfmoveClock() {
        return halfmoveClock;
    }

    public int getFullmoveNumber() {
        return fullmoveNumber;
    }

    public boolean canWhiteKingSideCastle() {
        return whiteKingSideCastle;
    }

    public boolean canWhiteQueenSideCastle() {
        return whiteQueenSideCastle;
    }

    public boolean canBlackKingSideCastle() {
        return blackKingSideCastle;
    }

    public boolean canBlackQueenSideCastle() {
        return blackQueenSideCastle;
    }

    public boolean isKingInCheck(Alliance alliance) {
        long kingBoard = getPieceBitboard(alliance, PieceType.KING);
        if (kingBoard == 0L) {
            return false;
        }
        int kingSquare = Long.numberOfTrailingZeros(kingBoard);
        return isSquareAttacked(kingSquare, alliance.opposite());
    }

    public boolean isSquareAttacked(int square, Alliance byAlliance) {
        if (byAlliance == Alliance.WHITE) {
            if (square >= 7 && BoardConstants.fileOf(square) != 7 && (whitePawns & BoardConstants.bit(square - 7)) != 0L) {
                return true;
            }
            if (square >= 9 && BoardConstants.fileOf(square) != 0 && (whitePawns & BoardConstants.bit(square - 9)) != 0L) {
                return true;
            }
        } else {
            if (square <= 56 && BoardConstants.fileOf(square) != 0 && (blackPawns & BoardConstants.bit(square + 7)) != 0L) {
                return true;
            }
            if (square <= 54 && BoardConstants.fileOf(square) != 7 && (blackPawns & BoardConstants.bit(square + 9)) != 0L) {
                return true;
            }
        }

        long knightAttackers = byAlliance == Alliance.WHITE ? whiteKnights : blackKnights;
        if ((BoardConstants.KNIGHT_ATTACKS[square] & knightAttackers) != 0L) {
            return true;
        }

        long kingAttackers = byAlliance == Alliance.WHITE ? whiteKing : blackKing;
        if ((BoardConstants.KING_ATTACKS[square] & kingAttackers) != 0L) {
            return true;
        }

        return isAttackedBySlidingPieces(square, byAlliance, true) || isAttackedBySlidingPieces(square, byAlliance, false);
    }

    public Board makeMove(Move move) {
        MutableState state = new MutableState(this);
        state.applyMove(move);
        return state.toBoard();
    }

    private boolean isAttackedBySlidingPieces(int square, Alliance byAlliance, boolean bishopLike) {
        long bishops = byAlliance == Alliance.WHITE ? whiteBishops : blackBishops;
        long rooks = byAlliance == Alliance.WHITE ? whiteRooks : blackRooks;
        long queens = byAlliance == Alliance.WHITE ? whiteQueens : blackQueens;
        long occupancy = getAllPieces();

        int[] directions = bishopLike ? new int[]{9, 7, -7, -9} : new int[]{8, -8, 1, -1};
        for (int direction : directions) {
            int current = square;
            while (canStep(current, direction)) {
                current += direction;
                long currentBit = BoardConstants.bit(current);
                if ((occupancy & currentBit) != 0L) {
                    return bishopLike
                            ? ((bishops & currentBit) != 0L || (queens & currentBit) != 0L)
                            : ((rooks & currentBit) != 0L || (queens & currentBit) != 0L);
                }
            }
        }
        return false;
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

    private static final class MutableState {
        private long whitePawns;
        private long whiteKnights;
        private long whiteBishops;
        private long whiteRooks;
        private long whiteQueens;
        private long whiteKing;

        private long blackPawns;
        private long blackKnights;
        private long blackBishops;
        private long blackRooks;
        private long blackQueens;
        private long blackKing;

        private boolean whiteToMove;
        private boolean whiteKingSideCastle;
        private boolean whiteQueenSideCastle;
        private boolean blackKingSideCastle;
        private boolean blackQueenSideCastle;
        private int enPassantSquare;
        private int halfmoveClock;
        private int fullmoveNumber;

        private MutableState(Board board) {
            this.whitePawns = board.whitePawns;
            this.whiteKnights = board.whiteKnights;
            this.whiteBishops = board.whiteBishops;
            this.whiteRooks = board.whiteRooks;
            this.whiteQueens = board.whiteQueens;
            this.whiteKing = board.whiteKing;
            this.blackPawns = board.blackPawns;
            this.blackKnights = board.blackKnights;
            this.blackBishops = board.blackBishops;
            this.blackRooks = board.blackRooks;
            this.blackQueens = board.blackQueens;
            this.blackKing = board.blackKing;
            this.whiteToMove = board.whiteToMove;
            this.whiteKingSideCastle = board.whiteKingSideCastle;
            this.whiteQueenSideCastle = board.whiteQueenSideCastle;
            this.blackKingSideCastle = board.blackKingSideCastle;
            this.blackQueenSideCastle = board.blackQueenSideCastle;
            this.enPassantSquare = board.enPassantSquare;
            this.halfmoveClock = board.halfmoveClock;
            this.fullmoveNumber = board.fullmoveNumber;
        }

        private void applyMove(Move move) {
            Alliance movingAlliance = whiteToMove ? Alliance.WHITE : Alliance.BLACK;
            Alliance opponentAlliance = movingAlliance.opposite();

            long fromBit = BoardConstants.bit(move.getFromSquare());
            long toBit = BoardConstants.bit(move.getToSquare());

            removePiece(movingAlliance, move.getPieceType(), fromBit);

            if (move.getMoveType() == MoveType.EN_PASSANT) {
                int capturedSquare = movingAlliance == Alliance.WHITE ? move.getToSquare() - 8 : move.getToSquare() + 8;
                removePiece(opponentAlliance, PieceType.PAWN, BoardConstants.bit(capturedSquare));
            } else if (move.getCapturedPieceType() != null) {
                removePiece(opponentAlliance, move.getCapturedPieceType(), toBit);
            }

            if (move.getMoveType() == MoveType.KING_SIDE_CASTLE) {
                if (movingAlliance == Alliance.WHITE) {
                    whiteKing |= BoardConstants.bit(6);
                    whiteRooks &= ~BoardConstants.bit(7);
                    whiteRooks |= BoardConstants.bit(5);
                } else {
                    blackKing |= BoardConstants.bit(62);
                    blackRooks &= ~BoardConstants.bit(63);
                    blackRooks |= BoardConstants.bit(61);
                }
            } else if (move.getMoveType() == MoveType.QUEEN_SIDE_CASTLE) {
                if (movingAlliance == Alliance.WHITE) {
                    whiteKing |= BoardConstants.bit(2);
                    whiteRooks &= ~BoardConstants.bit(0);
                    whiteRooks |= BoardConstants.bit(3);
                } else {
                    blackKing |= BoardConstants.bit(58);
                    blackRooks &= ~BoardConstants.bit(56);
                    blackRooks |= BoardConstants.bit(59);
                }
            } else if (move.getPromotionPieceType() != null) {
                addPiece(movingAlliance, move.getPromotionPieceType(), toBit);
            } else {
                addPiece(movingAlliance, move.getPieceType(), toBit);
            }

            updateCastlingRights(movingAlliance, move);

            enPassantSquare = move.getMoveType() == MoveType.DOUBLE_PAWN_PUSH
                    ? (movingAlliance == Alliance.WHITE ? move.getFromSquare() + 8 : move.getFromSquare() - 8)
                    : -1;

            if (move.getPieceType() == PieceType.PAWN || move.getCapturedPieceType() != null || move.getMoveType() == MoveType.EN_PASSANT) {
                halfmoveClock = 0;
            } else {
                halfmoveClock++;
            }

            if (movingAlliance == Alliance.BLACK) {
                fullmoveNumber++;
            }

            whiteToMove = !whiteToMove;
        }

        private void updateCastlingRights(Alliance movingAlliance, Move move) {
            if (movingAlliance == Alliance.WHITE) {
                if (move.getPieceType() == PieceType.KING) {
                    whiteKingSideCastle = false;
                    whiteQueenSideCastle = false;
                }
                if (move.getPieceType() == PieceType.ROOK) {
                    if (move.getFromSquare() == 0) {
                        whiteQueenSideCastle = false;
                    } else if (move.getFromSquare() == 7) {
                        whiteKingSideCastle = false;
                    }
                }
                if (move.getCapturedPieceType() == PieceType.ROOK) {
                    if (move.getToSquare() == 56) {
                        blackQueenSideCastle = false;
                    } else if (move.getToSquare() == 63) {
                        blackKingSideCastle = false;
                    }
                }
            } else {
                if (move.getPieceType() == PieceType.KING) {
                    blackKingSideCastle = false;
                    blackQueenSideCastle = false;
                }
                if (move.getPieceType() == PieceType.ROOK) {
                    if (move.getFromSquare() == 56) {
                        blackQueenSideCastle = false;
                    } else if (move.getFromSquare() == 63) {
                        blackKingSideCastle = false;
                    }
                }
                if (move.getCapturedPieceType() == PieceType.ROOK) {
                    if (move.getToSquare() == 0) {
                        whiteQueenSideCastle = false;
                    } else if (move.getToSquare() == 7) {
                        whiteKingSideCastle = false;
                    }
                }
            }
        }

        private void removePiece(Alliance alliance, PieceType pieceType, long bit) {
            if (alliance == Alliance.WHITE) {
                switch (pieceType) {
                    case PAWN -> whitePawns &= ~bit;
                    case KNIGHT -> whiteKnights &= ~bit;
                    case BISHOP -> whiteBishops &= ~bit;
                    case ROOK -> whiteRooks &= ~bit;
                    case QUEEN -> whiteQueens &= ~bit;
                    case KING -> whiteKing &= ~bit;
                }
            } else {
                switch (pieceType) {
                    case PAWN -> blackPawns &= ~bit;
                    case KNIGHT -> blackKnights &= ~bit;
                    case BISHOP -> blackBishops &= ~bit;
                    case ROOK -> blackRooks &= ~bit;
                    case QUEEN -> blackQueens &= ~bit;
                    case KING -> blackKing &= ~bit;
                }
            }
        }

        private void addPiece(Alliance alliance, PieceType pieceType, long bit) {
            if (alliance == Alliance.WHITE) {
                switch (pieceType) {
                    case PAWN -> whitePawns |= bit;
                    case KNIGHT -> whiteKnights |= bit;
                    case BISHOP -> whiteBishops |= bit;
                    case ROOK -> whiteRooks |= bit;
                    case QUEEN -> whiteQueens |= bit;
                    case KING -> whiteKing |= bit;
                }
            } else {
                switch (pieceType) {
                    case PAWN -> blackPawns |= bit;
                    case KNIGHT -> blackKnights |= bit;
                    case BISHOP -> blackBishops |= bit;
                    case ROOK -> blackRooks |= bit;
                    case QUEEN -> blackQueens |= bit;
                    case KING -> blackKing |= bit;
                }
            }
        }

        private Board toBoard() {
            return new Board(
                    whitePawns,
                    whiteKnights,
                    whiteBishops,
                    whiteRooks,
                    whiteQueens,
                    whiteKing,
                    blackPawns,
                    blackKnights,
                    blackBishops,
                    blackRooks,
                    blackQueens,
                    blackKing,
                    whiteToMove,
                    whiteKingSideCastle,
                    whiteQueenSideCastle,
                    blackKingSideCastle,
                    blackQueenSideCastle,
                    enPassantSquare,
                    halfmoveClock,
                    fullmoveNumber
            );
        }
    }
}