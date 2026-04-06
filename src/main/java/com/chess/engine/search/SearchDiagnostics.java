package com.chess.engine.search;

public final class SearchDiagnostics {

    private long alphaBetaNodes;
    private long quiescenceNodes;
    private long ttHits;
    private long ttCutoffs;

    public void reset() {
        alphaBetaNodes = 0L;
        quiescenceNodes = 0L;
        ttHits = 0L;
        ttCutoffs = 0L;
    }

    public void incrementAlphaBetaNodes() {
        alphaBetaNodes++;
    }

    public void incrementQuiescenceNodes() {
        quiescenceNodes++;
    }

    public void incrementTtHits() {
        ttHits++;
    }

    public void incrementTtCutoffs() {
        ttCutoffs++;
    }

    public long getAlphaBetaNodes() {
        return alphaBetaNodes;
    }

    public long getQuiescenceNodes() {
        return quiescenceNodes;
    }

    public long getTtHits() {
        return ttHits;
    }

    public long getTtCutoffs() {
        return ttCutoffs;
    }
}