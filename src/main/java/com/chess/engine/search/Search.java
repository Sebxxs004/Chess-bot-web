package com.chess.engine.search;

import com.chess.engine.board.Board;
import com.chess.engine.moves.Move;
import com.chess.engine.moves.MoveGenerator;

import java.util.List;

public final class Search {

    private static final int MATE_SCORE = 100_000;

    private final MoveGenerator moveGenerator = new MoveGenerator();
    private final Evaluator evaluator = new Evaluator();
    private final MoveOrdering moveOrdering = new MoveOrdering();
    private final TranspositionTable transpositionTable;
    private final boolean useTranspositionTable;
    private final SearchDiagnostics diagnostics = new SearchDiagnostics();
    private long nodesVisited;

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

    public int searchMoves(Board board, int depth, int alpha, int beta) {
        nodesVisited = 0L;
        diagnostics.reset();
        return negamax(board, depth, alpha, beta, 0);
    }

    public Move getBestMove(Board board, int depth) {
        nodesVisited = 0L;
        diagnostics.reset();
        TranspositionTable.Entry entry = useTranspositionTable ? transpositionTable.probe(board.getZobristKey()) : null;
        Move hashMove = entry == null ? null : entry.getBestMove();
        List<Move> legalMoves = hashMove == null
                ? moveOrdering.orderMoves(moveGenerator.generateLegalMoves(board))
                : moveOrdering.orderMoves(moveGenerator.generateLegalMoves(board), hashMove);
        if (legalMoves.isEmpty()) {
            return null;
        }

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

        return bestMove;
    }

    private int negamax(Board board, int depth, int alpha, int beta, int ply) {
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
}