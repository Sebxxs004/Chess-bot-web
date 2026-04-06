package com.chess.engine;

import com.chess.engine.board.Board;
import com.chess.engine.moves.Move;
import com.chess.engine.moves.MoveGenerator;
import com.chess.engine.search.Search;
import com.chess.engine.search.TimeManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Consumer;

public final class UCI {

    private final MoveGenerator moveGenerator = new MoveGenerator();
    private final Consumer<String> outputHandler;
    private Board currentBoard = Board.initialPosition();
    private final Search search = new Search(true);
    private Thread searchThread;

    public UCI() {
        this(System.out::println);
    }

    public UCI(Consumer<String> outputHandler) {
        this.outputHandler = outputHandler;
        this.search.setInfoHandler(this::emit);
    }

    public void loop() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while ((line = reader.readLine()) != null) {
            processCommand(line.trim());
        }
    }

    public void processCommand(String commandLine) {
        if (commandLine.isEmpty()) {
            return;
        }

        if ("uci".equals(commandLine)) {
            emit("id name TuNombreEngine");
            emit("id author Sebas");
            emit("uciok");
            return;
        }

        if ("isready".equals(commandLine)) {
            emit("readyok");
            return;
        }

        if ("ucinewgame".equals(commandLine)) {
            stopCurrentSearch();
            search.newGame();
            currentBoard = Board.initialPosition();
            return;
        }

        if (commandLine.startsWith("position ")) {
            handlePosition(commandLine.substring("position ".length()));
            return;
        }

        if (commandLine.startsWith("go")) {
            handleGo(commandLine);
            return;
        }

        if ("stop".equals(commandLine)) {
            stopCurrentSearch();
            return;
        }

        if ("quit".equals(commandLine)) {
            stopCurrentSearch();
        }
    }

    private void handlePosition(String payload) {
        if (payload.startsWith("startpos")) {
            currentBoard = Board.initialPosition();
            String movesPart = payload.substring("startpos".length()).trim();
            if (movesPart.startsWith("moves")) {
                applyMoves(movesPart.substring("moves".length()).trim());
            }
            return;
        }

        if (payload.startsWith("fen ")) {
            String fenAndMoves = payload.substring(4).trim();
            int movesIndex = fenAndMoves.indexOf(" moves ");
            if (movesIndex < 0 && fenAndMoves.endsWith(" moves")) {
                movesIndex = fenAndMoves.length() - " moves".length();
            }
            if (movesIndex < 0 && fenAndMoves.endsWith(" moves.")) {
                movesIndex = fenAndMoves.length() - " moves.".length();
            }
            String fen = movesIndex >= 0 ? fenAndMoves.substring(0, movesIndex).trim() : fenAndMoves;
            currentBoard = Board.fromFen(fen);
            if (movesIndex >= 0) {
                int prefixLength = fenAndMoves.startsWith("moves", movesIndex + 1) ? " moves".length() : " moves ".length();
                String movesPart = fenAndMoves.substring(Math.min(fenAndMoves.length(), movesIndex + prefixLength)).trim();
                if (".".equals(movesPart)) {
                    movesPart = "";
                }
                applyMoves(movesPart);
            }
            return;
        }

        throw new IllegalArgumentException("Unsupported position command: " + payload);
    }

    private void applyMoves(String movesPart) {
        if (movesPart.isEmpty()) {
            return;
        }

        String[] tokens = movesPart.split("\\s+");
        for (String token : tokens) {
            if (!token.isBlank()) {
                currentBoard = applyMove(currentBoard, token);
            }
        }
    }

    private Board applyMove(Board board, String moveUci) {
        List<Move> legalMoves = moveGenerator.generateLegalMoves(board);
        for (Move move : legalMoves) {
            if (move.toUci().equals(moveUci)) {
                return board.makeMove(move);
            }
        }
        throw new IllegalArgumentException("Illegal move in position command: " + moveUci);
    }

    private void handleGo(String commandLine) {
        stopCurrentSearch();

        GoParameters parameters = parseGoParameters(commandLine);
        Board boardSnapshot = currentBoard;

        searchThread = new Thread(() -> {
            Move bestMove = search.startIterativeSearch(boardSnapshot, parameters.depth, parameters.timeBudgetMillis);
            emit("bestmove " + (bestMove == null ? "0000" : bestMove.toUci()));
        }, "uci-search-thread");
        searchThread.start();
    }

    private GoParameters parseGoParameters(String commandLine) {
        int depth = 0;
        long wtime = -1L;
        long btime = -1L;
        long winc = 0L;
        long binc = 0L;
        long movetime = -1L;

        String[] tokens = commandLine.split("\\s+");
        for (int index = 1; index < tokens.length; index++) {
            String token = tokens[index];
            if (index + 1 >= tokens.length) {
                continue;
            }
            String value = tokens[index + 1];
            switch (token) {
                case "depth" -> {
                    depth = Integer.parseInt(value);
                    index++;
                }
                case "wtime" -> {
                    wtime = Long.parseLong(value);
                    index++;
                }
                case "btime" -> {
                    btime = Long.parseLong(value);
                    index++;
                }
                case "winc" -> {
                    winc = Long.parseLong(value);
                    index++;
                }
                case "binc" -> {
                    binc = Long.parseLong(value);
                    index++;
                }
                case "movetime" -> {
                    movetime = Long.parseLong(value);
                    index++;
                }
                default -> {
                }
            }
        }

        long timeBudgetMillis = TimeManager.computeMoveTimeMillis(
                currentBoard.getSideToMove(),
                wtime,
                btime,
                winc,
                binc,
                movetime
        );

        return new GoParameters(depth, timeBudgetMillis);
    }

    private void stopCurrentSearch() {
        if (searchThread != null && searchThread.isAlive()) {
            search.abortSearch();
            try {
                searchThread.join();
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void emit(String message) {
        outputHandler.accept(message);
    }

    private record GoParameters(int depth, long timeBudgetMillis) {
    }
}