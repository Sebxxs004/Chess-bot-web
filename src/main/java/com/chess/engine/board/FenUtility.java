package com.chess.engine.board;

public final class FenUtility {

    public static final String INITIAL_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    private FenUtility() {
    }

    public static Board fromFen(String fen) {
        if (fen == null || fen.isBlank()) {
            throw new IllegalArgumentException("FEN string cannot be null or blank");
        }

        String[] parts = fen.trim().split("\\s+");
        if (parts.length != 6) {
            throw new IllegalArgumentException("Invalid FEN: expected 6 space-separated fields");
        }

        PieceState state = parsePiecePlacement(parts[0]);
        boolean whiteToMove = parseSideToMove(parts[1]);
        CastlingRights castlingRights = parseCastlingRights(parts[2]);
        int enPassantSquare = parseEnPassant(parts[3]);
        int halfmoveClock = parseNonNegativeInt(parts[4], "halfmove clock");
        int fullmoveNumber = parsePositiveInt(parts[5], "fullmove number");

        return Board.create(
                state.whitePawns,
                state.whiteKnights,
                state.whiteBishops,
                state.whiteRooks,
                state.whiteQueens,
                state.whiteKing,
                state.blackPawns,
                state.blackKnights,
                state.blackBishops,
                state.blackRooks,
                state.blackQueens,
                state.blackKing,
                whiteToMove,
                castlingRights.whiteKingSide,
                castlingRights.whiteQueenSide,
                castlingRights.blackKingSide,
                castlingRights.blackQueenSide,
                enPassantSquare,
                halfmoveClock,
                fullmoveNumber
        );
    }

    public static String toFen(Board board) {
        StringBuilder fen = new StringBuilder();
        appendPiecePlacement(board, fen);
        fen.append(' ');
        fen.append(board.getSideToMove() == Alliance.WHITE ? 'w' : 'b');
        fen.append(' ');
        appendCastlingRights(board, fen);
        fen.append(' ');
        fen.append(board.getEnPassantSquare() == -1 ? "-" : squareToAlgebraic(board.getEnPassantSquare()));
        fen.append(' ');
        fen.append(board.getHalfmoveClock());
        fen.append(' ');
        fen.append(board.getFullmoveNumber());
        return fen.toString();
    }

    private static PieceState parsePiecePlacement(String placement) {
        String[] ranks = placement.split("/");
        if (ranks.length != 8) {
            throw new IllegalArgumentException("Invalid FEN: piece placement must have 8 ranks");
        }

        PieceState state = new PieceState();
        for (int fenRank = 0; fenRank < 8; fenRank++) {
            int boardRank = 7 - fenRank;
            int file = 0;
            String rank = ranks[fenRank];
            for (int i = 0; i < rank.length(); i++) {
                char symbol = rank.charAt(i);
                if (Character.isDigit(symbol)) {
                    int emptySquares = symbol - '0';
                    if (emptySquares < 1 || emptySquares > 8) {
                        throw new IllegalArgumentException("Invalid FEN: invalid empty-square count '" + symbol + "'");
                    }
                    file += emptySquares;
                } else {
                    if (file > 7) {
                        throw new IllegalArgumentException("Invalid FEN: too many files in rank");
                    }
                    int square = boardRank * 8 + file;
                    state.setPiece(symbol, square);
                    file++;
                }
            }
            if (file != 8) {
                throw new IllegalArgumentException("Invalid FEN: each rank must contain exactly 8 squares");
            }
        }

        if (Long.bitCount(state.whiteKing) != 1 || Long.bitCount(state.blackKing) != 1) {
            throw new IllegalArgumentException("Invalid FEN: both sides must have exactly one king");
        }

        return state;
    }

    private static boolean parseSideToMove(String sideToMove) {
        if ("w".equals(sideToMove)) {
            return true;
        }
        if ("b".equals(sideToMove)) {
            return false;
        }
        throw new IllegalArgumentException("Invalid FEN: side to move must be 'w' or 'b'");
    }

    private static CastlingRights parseCastlingRights(String castling) {
        if ("-".equals(castling)) {
            return new CastlingRights(false, false, false, false);
        }
        boolean whiteKingSide = castling.indexOf('K') >= 0;
        boolean whiteQueenSide = castling.indexOf('Q') >= 0;
        boolean blackKingSide = castling.indexOf('k') >= 0;
        boolean blackQueenSide = castling.indexOf('q') >= 0;
        for (int i = 0; i < castling.length(); i++) {
            char symbol = castling.charAt(i);
            if (symbol != 'K' && symbol != 'Q' && symbol != 'k' && symbol != 'q') {
                throw new IllegalArgumentException("Invalid FEN: invalid castling symbol '" + symbol + "'");
            }
        }
        return new CastlingRights(whiteKingSide, whiteQueenSide, blackKingSide, blackQueenSide);
    }

    private static int parseEnPassant(String enPassant) {
        if ("-".equals(enPassant)) {
            return -1;
        }
        if (enPassant.length() != 2) {
            throw new IllegalArgumentException("Invalid FEN: en passant square must be '-' or algebraic square");
        }
        return algebraicToSquare(enPassant);
    }

    private static int parseNonNegativeInt(String value, String label) {
        try {
            int parsed = Integer.parseInt(value);
            if (parsed < 0) {
                throw new IllegalArgumentException("Invalid FEN: " + label + " must be non-negative");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid FEN: " + label + " is not a valid integer", ex);
        }
    }

    private static int parsePositiveInt(String value, String label) {
        int parsed = parseNonNegativeInt(value, label);
        if (parsed < 1) {
            throw new IllegalArgumentException("Invalid FEN: " + label + " must be >= 1");
        }
        return parsed;
    }

    private static int algebraicToSquare(String algebraic) {
        char fileChar = algebraic.charAt(0);
        char rankChar = algebraic.charAt(1);
        if (fileChar < 'a' || fileChar > 'h' || rankChar < '1' || rankChar > '8') {
            throw new IllegalArgumentException("Invalid FEN: invalid square '" + algebraic + "'");
        }
        int file = fileChar - 'a';
        int rank = rankChar - '1';
        return rank * 8 + file;
    }

    private static String squareToAlgebraic(int square) {
        int file = BoardConstants.fileOf(square);
        int rank = BoardConstants.rankOf(square);
        return String.valueOf((char) ('a' + file)) + (char) ('1' + rank);
    }

    private static void appendPiecePlacement(Board board, StringBuilder fen) {
        for (int rank = 7; rank >= 0; rank--) {
            int emptyCount = 0;
            for (int file = 0; file < 8; file++) {
                int square = rank * 8 + file;
                char piece = pieceAt(board, square);
                if (piece == 0) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    fen.append(piece);
                }
            }
            if (emptyCount > 0) {
                fen.append(emptyCount);
            }
            if (rank > 0) {
                fen.append('/');
            }
        }
    }

    private static char pieceAt(Board board, int square) {
        long bit = BoardConstants.bit(square);
        if ((board.getPieceBitboard(Alliance.WHITE, PieceType.PAWN) & bit) != 0L) {
            return 'P';
        }
        if ((board.getPieceBitboard(Alliance.WHITE, PieceType.KNIGHT) & bit) != 0L) {
            return 'N';
        }
        if ((board.getPieceBitboard(Alliance.WHITE, PieceType.BISHOP) & bit) != 0L) {
            return 'B';
        }
        if ((board.getPieceBitboard(Alliance.WHITE, PieceType.ROOK) & bit) != 0L) {
            return 'R';
        }
        if ((board.getPieceBitboard(Alliance.WHITE, PieceType.QUEEN) & bit) != 0L) {
            return 'Q';
        }
        if ((board.getPieceBitboard(Alliance.WHITE, PieceType.KING) & bit) != 0L) {
            return 'K';
        }
        if ((board.getPieceBitboard(Alliance.BLACK, PieceType.PAWN) & bit) != 0L) {
            return 'p';
        }
        if ((board.getPieceBitboard(Alliance.BLACK, PieceType.KNIGHT) & bit) != 0L) {
            return 'n';
        }
        if ((board.getPieceBitboard(Alliance.BLACK, PieceType.BISHOP) & bit) != 0L) {
            return 'b';
        }
        if ((board.getPieceBitboard(Alliance.BLACK, PieceType.ROOK) & bit) != 0L) {
            return 'r';
        }
        if ((board.getPieceBitboard(Alliance.BLACK, PieceType.QUEEN) & bit) != 0L) {
            return 'q';
        }
        if ((board.getPieceBitboard(Alliance.BLACK, PieceType.KING) & bit) != 0L) {
            return 'k';
        }
        return 0;
    }

    private static void appendCastlingRights(Board board, StringBuilder fen) {
        StringBuilder rights = new StringBuilder();
        if (board.canWhiteKingSideCastle()) {
            rights.append('K');
        }
        if (board.canWhiteQueenSideCastle()) {
            rights.append('Q');
        }
        if (board.canBlackKingSideCastle()) {
            rights.append('k');
        }
        if (board.canBlackQueenSideCastle()) {
            rights.append('q');
        }
        fen.append(rights.isEmpty() ? "-" : rights);
    }

    private static final class PieceState {
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

        private void setPiece(char piece, int square) {
            long bit = BoardConstants.bit(square);
            switch (piece) {
                case 'P' -> whitePawns |= bit;
                case 'N' -> whiteKnights |= bit;
                case 'B' -> whiteBishops |= bit;
                case 'R' -> whiteRooks |= bit;
                case 'Q' -> whiteQueens |= bit;
                case 'K' -> whiteKing |= bit;
                case 'p' -> blackPawns |= bit;
                case 'n' -> blackKnights |= bit;
                case 'b' -> blackBishops |= bit;
                case 'r' -> blackRooks |= bit;
                case 'q' -> blackQueens |= bit;
                case 'k' -> blackKing |= bit;
                default -> throw new IllegalArgumentException("Invalid FEN: unknown piece symbol '" + piece + "'");
            }
        }
    }

    private static final class CastlingRights {
        private final boolean whiteKingSide;
        private final boolean whiteQueenSide;
        private final boolean blackKingSide;
        private final boolean blackQueenSide;

        private CastlingRights(boolean whiteKingSide,
                               boolean whiteQueenSide,
                               boolean blackKingSide,
                               boolean blackQueenSide) {
            this.whiteKingSide = whiteKingSide;
            this.whiteQueenSide = whiteQueenSide;
            this.blackKingSide = blackKingSide;
            this.blackQueenSide = blackQueenSide;
        }
    }
}