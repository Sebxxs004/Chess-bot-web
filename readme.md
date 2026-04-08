# Chess Tutor Engine - Estado Actual del Sistema

Aplicacion completa de entrenamiento de ajedrez con motor propio en Java, servidor WebSocket, UI de tutoria en tiempo real, contenedorizacion con Docker y despliegue en Azure Container Apps.

## 1. Stack tecnologico

- Lenguaje: Java 17.
- Build y dependencias: Maven.
- Servidor web: Javalin 6.3.0.
- Logging: SLF4J Simple 2.0.16.
- Testing: JUnit 5.11.0.
- Frontend: HTML, CSS, JavaScript.
- Librerias frontend: chessboard.js, chess.js, jQuery.
- Contenedores: Docker (multi-stage build).
- Cloud: Azure Container Registry + Azure Container Apps.

## 2. Capacidades del motor

- Representacion del tablero con bitboards.
- Generacion de movimientos legales completa.
- Reglas especiales soportadas: enroque, captura al paso, promocion.
- Soporte FEN completo: parseo y serializacion.
- PERFT para validacion de legalidad y cobertura de reglas.

## 3. Busqueda y evaluacion

- Negamax con poda alpha-beta.
- Quiescence search.
- Move ordering.
- Transposition table con Zobrist hashing.
- Iterative deepening.
- Time management para go con depth y controles de tiempo.
- Abortado de busqueda por comando stop.
- Salida UCI de info depth, score cp/mate y pv.

## 4. Protocolo UCI implementado

- uci
- isready
- ucinewgame
- position startpos moves ...
- position fen ... moves ...
- go (depth, wtime, btime, winc, binc, movetime)
- stop
- quit

## 5. Capa web y comunicacion en tiempo real

- El servidor expone archivos estaticos desde src/main/resources/public.
- El canal WebSocket para juego y analisis es /chess.
- La UI abre WebSocket dinamico segun protocolo:
- wss cuando la pagina corre en https.
- ws cuando la pagina corre en http.

## 6. Funcionalidades de la UI de entrenamiento

- Tablero interactivo con drag and drop.
- Barra de evaluacion vertical con porcentajes para blancas y negras.
- Historial de jugadas con navegacion: inicio, anterior, siguiente y actual.
- Modo normal y modo ayuda.
- Clasificacion pedagogica por jugada: Excelente, Inexactitud, Error y Blunder.
- Ayuda contextual con explicacion y sugerencias de correccion.
- Visualizacion de linea de castigo para jugadas marcadas.
- Overlay de fin de partida.
- Branding institucional activo:
- Escudo UDES en esquina superior izquierda.
- Escudo como favicon de la pestana.
- Footer de autoria y derechos.

## 7. Validaciones y pruebas implementadas

- PERFT posicion inicial depth 1: 20.
- PERFT posicion inicial depth 2: 400.
- PERFT posicion inicial depth 3: 8902.
- PERFT posicion inicial depth 4: 197281.
- PERFT Kiwipete depth 3: 97862.
- Validacion de promociones en escenario de finales.
- Round-trip FEN validado.
- Casos tacticos de search validados:
- Deteccion de mate en 1.
- Captura de dama colgante.

## 8. Estructura principal del repositorio

```text
.
|- pom.xml
|- Dockerfile
|- .dockerignore
|- readme.md
|- assets/
|  `- escudo-udes.jpg
`- src/
   |- main/
   |  |- java/com/chess/engine/
   |  |  |- Main.java
   |  |  |- ChessServer.java
   |  |  |- UCI.java
   |  |  |- board/
   |  |  |- moves/
   |  |  |- search/
   |  |  `- debug/
   |  `- resources/public/
   |     |- index.html
   |     `- assets/escudo-udes.jpg
   `- test/java/com/chess/engine/
      |- moves/PerftTest.java
      `- search/SearchTest.java
```

## 9. Requisitos

- JDK 17.
- Maven 3.9 o superior.
- Docker (opcional, para contenedores).
- Azure CLI + extension containerapp (opcional, para despliegue cloud).

## 10. Ejecutar en local

Compilar:

```powershell
.\.tools\apache-maven-3.9.9\bin\mvn.cmd -q -DskipTests package
```

Probar:

```powershell
.\.tools\apache-maven-3.9.9\bin\mvn.cmd -q test
```

Levantar servidor:

```powershell
.\.tools\apache-maven-3.9.9\bin\mvn.cmd -q "org.codehaus.mojo:exec-maven-plugin:3.6.1:java" "-Dexec.mainClass=com.chess.engine.Main"
```

Endpoints locales:

- Web: <http://localhost:7070>
- WebSocket: ws://localhost:7070/chess

Nota: el puerto puede configurarse con la variable de entorno PORT.

## 11. Ejecutar con Docker

Build de imagen:

```powershell
docker build -t chess-bot:latest .
```

Run local:

```powershell
docker run --rm -p 7070:7070 -e PORT=7070 chess-bot:latest
```

## 12. Actualizar despliegue en Azure Container Apps

Build remoto en ACR:

```powershell
az acr build --registry <acr-name> --image chess-bot:latest .
```

Actualizar Container App:

```powershell
az containerapp update --name <app-name> --resource-group <resource-group> --image <acr-login-server>/chess-bot:latest
```

## 13. Estado actual del producto

El sistema ya se encuentra en etapa funcional avanzada: motor validado, interfaz de entrenamiento operativa, feedback pedagogico en tiempo real y despliegue cloud activo.

## 14. Creditos

- Developed by JUAN SEBASTIAN CANO VASQUEZ
- University of Santander
