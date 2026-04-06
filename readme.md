# ♟️ Chess Tutor Engine - Proyecto de Entrenamiento Asistido

## 📖 Visión General
Este proyecto no es solo un motor de ajedrez (Chess Engine); es un **Tutor de Ajedrez Interactivo**. El objetivo es construir un backend robusto que sea capaz de jugar, evaluar posiciones y, lo más importante, detectar errores humanos para generar retroalimentación en tiempo real y ejercicios de repetición espaciada.

## 🏗️ Arquitectura y Stack Tecnológico
* **Lenguaje Core (Motor y Lógica):** Java (Orientado a objetos, fuertemente tipado, excelente manejo de memoria y concurrencia).
* **Gestión de Proyecto:** Maven o Gradle.
* **Paradigma de Diseño:** Clean Architecture (Separación estricta entre la lógica de generación de movimientos, el algoritmo de búsqueda y el módulo del tutor).
* **Frontend / UI:** Interfaz web interactiva (HTML/CSS/JS o framework a definir) enfocada en una excelente UI/UX para el aprendizaje. Se comunicará con el backend en Java vía API REST o WebSockets.
* **Base de Datos:** PostgreSQL/MySQL (Para guardar historial de usuarios, posiciones FEN de errores y progreso de Elo).

## 🗺️ Fases de Desarrollo (Roadmap)

### Fase 1: El Motor Central (Board & Move Generation)
* Representación del tablero usando Bitboards (enteros de 64 bits).
* Generador de movimientos legales completos (deslizantes, saltos, enroque, captura al paso, coronación).
* **Testing Crítico:** Superar pruebas **PERFT** (Performance Test) para asegurar que el motor conoce el 100% de las reglas y genera el número exacto de nodos posibles por cada profundidad.

### Fase 2: El Cerebro Analítico (Search & Evaluation)
* Implementación de Minimax con Poda Alfa-Beta (Alpha-Beta Pruning).
* Evaluación Heurística (Piece-Square Tables y valor de material).
* Ordenamiento de movimientos (Move Ordering) para optimizar la poda.
* **Testing Crítico:** Unit testing (JUnit) de la función de evaluación para asegurar que detecta jaques mates obvios y ganancias de material en 2-3 movimientos de profundidad.

### Fase 3: El Módulo Tutor (Business Logic)
* **Limitación de Elo:** Adaptación del algoritmo para simular niveles humanos (limitando la profundidad o inyectando ruido en la evaluación).
* **Detección de Blunders:** Lógica para comparar la jugada del usuario contra la mejor jugada del motor. Si la diferencia de puntaje excede el umbral "X", se marca como error grave.
* **Generador de Puzzles:** Extracción de la posición (FEN) del blunder para la base de datos de entrenamiento.
* **Testing Crítico:** Pruebas de integración simulando partidas pregrabadas para verificar que el sistema de alertas se dispara correctamente.

### Fase 4: La Interfaz de Entrenamiento (UI/UX)
* Desarrollo del tablero interactivo.
* Integración del "Semáforo de Jugadas" (Feedback visual por movimiento).
* Modo "Castigo y Corrección" (Takebacks obligatorios en caso de error grave).