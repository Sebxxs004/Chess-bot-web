package com.chess.engine.search;

import com.chess.engine.board.Board;
import com.chess.engine.moves.Move;
import com.chess.engine.moves.MoveGenerator;

import java.util.List;
import java.util.function.Consumer;

public final class Search {

    private static final int MATE_SCORE = 100_000;
    private static final int DEFAULT_MAX_DEPTH = 64;

    private final MoveGenerator moveGenerator = new MoveGenerator();
    private final Evaluator evaluator = new Evaluator();
    private final MoveOrdering moveOrdering = new MoveOrdering();
    private final TranspositionTable transpositionTable;
    private final boolean useTranspositionTable;
    private final SearchDiagnostics diagnostics = new SearchDiagnostics();
    private Consumer<String> infoHandler = ignored -> {
    };
    private long nodesVisited;
    private volatile boolean abortSearch;
    private long stopTimeNanos;

    public Search() {
        this(true);
    }

    public Search(boolean useTranspositionTable) {
        this.useTranspositionTable = useTranspositionTable;
        this.transpositionTable = useTranspositionTable ? new TranspositionTable(1 << 20) : null;
    }

    public long getNodesVisited() {
        return nodesVisited;
    }

    public SearchDiagnostics getDiagnostics() {
        return diagnostics;
    }

    public void setInfoHandler(Consumer<String> infoHandler) {
        this.infoHandler = infoHandler == null ? ignored -> {
        } : infoHandler;
    }

    public void abortSearch() {
        abortSearch = true;
    }

    public void newGame() {
        abortSearch = false;
        if (useTranspositionTable) {
            transpositionTable.clear();
        }
    }

    public int searchMoves(Board board, int depth, int alpha, int beta) {
        nodesVisited = 0L;
        diagnostics.reset();
        abortSearch = false;
        stopTimeNanos = Long.MAX_VALUE;
        return negamax(board, depth, alpha, beta, 0);
    }

    public Move getBestMove(Board board, int depth) {
        return startIterativeSearch(board, depth, 0L);
    }

    public Move startIterativeSearch(Board board, int maxDepth, long timeBudgetMillis) {
        nodesVisited = 0L;
        diagnostics.reset();
        abortSearch = false;
        stopTimeNanos = timeBudgetMillis > 0 ? System.nanoTime() + (timeBudgetMillis * 1_000_000L) : Long.MAX_VALUE;

        TranspositionTable.Entry rootEntry = useTranspositionTable ? transpositionTable.probe(board.getZobristKey()) : null;
        Move rootHashMove = rootEntry == null ? null : rootEntry.getBestMove();
        List<Move> legalMoves = rootHashMove == null
                ? moveOrdering.orderMoves(moveGenerator.generateLegalMoves(board))
                : moveOrdering.orderMoves(moveGenerator.generateLegalMoves(board), rootHashMove);
        if (legalMoves.isEmpty()) {
            return null;
        }

        int effectiveMaxDepth = maxDepth > 0 ? maxDepth : DEFAULT_MAX_DEPTH;
        Move bestOverallMove = legalMoves.get(0);

        for (int depth = 1; depth <= effectiveMaxDepth; depth++) {
            if (shouldAbort()) {
                break;
            }
            try {
                RootSearchResult depthResult = searchRoot(board, depth, legalMoves);
                if (depthResult != null && depthResult.bestMove() != null) {
                    bestOverallMove = depthResult.bestMove();
                    emitInfo(depth, depthResult.score(), depthResult.bestMove());
                }
            } catch (SearchAbortedException ignored) {
                break;
            }
        }

        return bestOverallMove;
    }

    private RootSearchResult searchRoot(Board board, int depth, List<Move> rootMoves) {
        TranspositionTable.Entry entry = useTranspositionTable ? transpositionTable.probe(board.getZobristKey()) : null;
        Move hashMove = entry == null ? null : entry.getBestMove();
        List<Move> legalMoves = hashMove == null
                ? moveOrdering.orderMoves(rootMoves)
                : moveOrdering.orderMoves(rootMoves, hashMove);

        Move bestMove = legalMoves.get(0);
        int bestScore = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE + 1;
        int beta = Integer.MAX_VALUE;

        for (Move move : legalMoves) {
            int score = -negamax(board.makeMove(move), depth - 1, -beta, -alpha, 1);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
            if (score > alpha) {
                alpha = score;
            }
        }

        return new RootSearchResult(bestMove, bestScore);
    }

    private int negamax(Board board, int depth, int alpha, int beta, int ply) {
        checkAbort();
        nodesVisited++;
        diagnostics.incrementAlphaBetaNodes();

        int alphaOriginal = alpha;
        long key = board.getZobristKey();
        TranspositionTable.Entry entry = useTranspositionTable ? transpositionTable.probe(key) : null;
        Move hashMove = entry == null ? null : entry.getBestMove();
        if (entry != null) {
            diagnostics.incrementTtHits();
        }
        if (entry != null && entry.getDepth() >= depth) {
            if (entry.getFlag() == TranspositionTable.EXACT) {
                diagnostics.incrementTtCutoffs();
                return entry.getScore();
            }
            if (entry.getFlag() == TranspositionTable.LOWERBOUND) {
                alpha = Math.max(alpha, entry.getScore());
            } else if (entry.getFlag() == TranspositionTable.UPPERBOUND) {
                beta = Math.min(beta, entry.getScore());
            }
            if (alpha >= beta) {
                diagnostics.incrementTtCutoffs();
                return entry.getScore();
            }
        }

        List<Move> legalMoves = hashMove == null
                ? moveOrdering.orderMoves(moveGenerator.generateLegalMoves(board))
                : moveOrdering.orderMoves(moveGenerator.generateLegalMoves(board), hashMove);
        if (legalMoves.isEmpty()) {
            if (board.isKingInCheck(board.getSideToMove())) {
                int score = -MATE_SCORE + ply;
                storeEntry(key, depth, score, TranspositionTable.EXACT, null);
                return score;
            }
            storeEntry(key, depth, 0, TranspositionTable.EXACT, null);
            return 0;
        }

        if (depth == 0) {
            int score = quiescenceSearch(board, alpha, beta, ply);
            storeEntry(key, depth, score, TranspositionTable.EXACT, hashMove);
            return score;
        }

        int bestScore = Integer.MIN_VALUE;
        Move bestMove = null;
        for (Move move : legalMoves) {
            int score = -negamax(board.makeMove(move), depth - 1, -beta, -alpha, ply + 1);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
            if (score > alpha) {
                alpha = score;
            }
            if (alpha >= beta) {
                break;
            }
        }

        byte flag;
        if (bestScore <= alphaOriginal) {
            flag = TranspositionTable.UPPERBOUND;
        } else if (bestScore >= beta) {
            flag = TranspositionTable.LOWERBOUND;
        } else {
            flag = TranspositionTable.EXACT;
        }
        storeEntry(key, depth, bestScore, flag, bestMove);

        return bestScore;
    }

    private int quiescenceSearch(Board board, int alpha, int beta, int ply) {
        checkAbort();
        nodesVisited++;
        diagnostics.incrementQuiescenceNodes();
        int standPat = board.getSideToMove().isWhite() ? evaluator.evaluate(board) : -evaluator.evaluate(board);
        if (standPat >= beta) {
            return beta;
        }
        if (standPat > alpha) {
            alpha = standPat;
        }

        List<Move> moves = board.isKingInCheck(board.getSideToMove())
                ? moveOrdering.orderMoves(moveGenerator.generateLegalMoves(board))
                : moveOrdering.orderMoves(moveGenerator.generateLegalCaptures(board));

        for (Move move : moves) {
            int score = -quiescenceSearch(board.makeMove(move), -beta, -alpha, ply + 1);
            if (score >= beta) {
                return beta;
            }
            if (score > alpha) {
                alpha = score;
            }
        }

        return alpha;
    }

    private void storeEntry(long key, int depth, int score, byte flag, Move bestMove) {
        if (useTranspositionTable) {
            transpositionTable.store(key, depth, score, flag, bestMove);
        }
    }

    private void checkAbort() {
        if ((nodesVisited & 2047L) != 0L) {
            return;
        }
        if (shouldAbort()) {
            throw SearchAbortedException.INSTANCE;
        }
    }

    private boolean shouldAbort() {
        return abortSearch || System.nanoTime() >= stopTimeNanos;
    }

    private void emitInfo(int depth, int score, Move bestMove) {
        if (bestMove == null) {
            return;
        }
        String scorePart = toUciScore(score);
        infoHandler.accept("info depth " + depth + " " + scorePart + " pv " + bestMove.toUci());
    }

    private String toUciScore(int score) {
        int mateThreshold = MATE_SCORE - 1_000;
        if (Math.abs(score) >= mateThreshold) {
            int pliesToMate = Math.max(0, MATE_SCORE - Math.abs(score));
            int mateMoves = Math.max(1, (pliesToMate + 1) / 2);
            int signedMateMoves = score > 0 ? mateMoves : -mateMoves;
            return "score mate " + signedMateMoves;
        }
        return "score cp " + score;
    }

    private record RootSearchResult(Move bestMove, int score) {
    }

    private static final class SearchAbortedException extends RuntimeException {
        private static final SearchAbortedException INSTANCE = new SearchAbortedException();

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }
}