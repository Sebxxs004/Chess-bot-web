package com.chess.engine;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

import java.util.function.Consumer;

public final class ChessServer {

    private static final String UCI_SESSION_KEY = "uci";

    private final int port;
    private Javalin app;

    public ChessServer(int port) {
        this.port = port;
    }

    public void start() {
        app = Javalin.create(config -> {
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = "/public";
                staticFiles.location = Location.CLASSPATH;
            });
        });

        app.ws("/chess", ws -> {
            ws.onConnect(ctx -> {
                Consumer<String> sender = message -> {
                    try {
                        ctx.send(message);
                    } catch (Exception ignored) {
                        // Cliente desconectado durante el envio.
                    }
                };
                UCI uci = new UCI(sender);
                ctx.attribute(UCI_SESSION_KEY, uci);
            });
            ws.onMessage(ctx -> {
                UCI uci = ctx.attribute(UCI_SESSION_KEY);
                if (uci != null) {
                    uci.processCommand(ctx.message());
                }
            });
            ws.onClose(ctx -> {
                UCI uci = ctx.attribute(UCI_SESSION_KEY);
                if (uci != null) {
                    uci.processCommand("quit");
                }
            });
        });

        app.start(port);
    }

    public void stop() {
        if (app != null) {
            app.stop();
            app = null;
        }
    }
}
