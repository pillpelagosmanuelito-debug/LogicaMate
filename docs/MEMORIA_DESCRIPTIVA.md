# LogicaMate v1.0.0 — Memoria Descriptiva

**Fecha:** 24 de agosto de 2026
**Público:** niños y niñas de 8 a 12 años
**Área:** razonamiento matemático y pensamiento lógico
**Paquete:** `com.educalab.logicamate`

## 1. Concepto

LogicaMate es **"El Templo de los Patrones"**: una aventura de exploración lógica, no un banco de preguntas con decoración. El niño entra a un templo antiguo lleno de mecanismos, cada uno con una regla distinta que debe **observar, descubrir, manipular, comprobar y resolver**.

La mecánica central es manipulativa, no de opción múltiple: en el contenido semilla, el **0%** de los 135 desafíos usa selección de 4 alternativas — el niño arrastra, ordena, construye, conecta, clasifica e investiga en su lugar.

## 2. Las 10 cámaras del templo

| Cámara | Categoría | Mecánica principal |
|---|---|---|
| Entrada al Templo | Patrones (introductorio) | Descubre una puerta de 2 símbolos |
| Galería de Patrones | Patrones | Completa un mosaico cíclico (1 o 2 propiedades a la vez) |
| Pasadizo de Secuencias | Secuencias | Completa una casilla numérica (aritmética, geométrica, alternante) |
| Sala de Analogías | Analogías | Aplica la misma transformación a una pieza distinta |
| Sala de Clasificación | Clasificación | Ordena símbolos en portales según una regla oculta |
| Sala de Matrices | Matrices | Completa la celda que falta en un panel 2×2/3×3 |
| Sala de Relaciones | Relaciones | Ordena exploradores según pistas de posición |
| Sala de Deducción | Deducción | Investiga pistas para asignar un objeto a cada explorador |
| Taller Constructor | Construcción | Construye libremente una tira que cumpla una regla |
| Cámara Maestra | Mezcla de las 8 anteriores | Solo se desbloquea al completar todas las demás |

## 3. La Llave Lógica

La progresión principal no es una barra genérica: cada una de las 8 cámaras de contenido (todas salvo Entrada y Maestra) otorga, al completarse, un **fragmento de la Llave Lógica** con su propia animación de adquisición. Reunir los 8 fragmentos desbloquea la insignia "Maestro del Templo".

## 4. Colección: Tesoros de la Lógica

24 objetos coleccionables (3 por cámara de contenido: cristal, engranaje, placa) se desbloquean al completar la cámara correspondiente — nunca de forma decorativa o desconectada del progreso real.

## 5. Gamificación

- **XP**: 10/20/35 puntos base según dificultad (inicial/intermedio/avanzado), con -3 XP por pista usada y +5 XP de bono al acertar a la primera.
- **Nivel**: umbral triangular creciente (`50·n·(n+1)/2`).
- **Racha**: se incrementa con actividad en días consecutivos, se reinicia tras un salto de más de un día. Sin vidas, sin esperas, sin presión social ni rankings online.
- **8 insignias**, desbloqueadas por acción real (resolver N desafíos de una categoría, reunir todos los fragmentos, etc.) — nunca por login ni tiempo transcurrido.
- Nunca se etiqueta la inteligencia del niño: los mensajes celebran el **proceso** ("Encontraste la regla", "Probaste otra estrategia y funcionó"), nunca el resultado como rasgo de la persona.

## 6. Pistas progresivas

Cada desafío principal tiene 3 niveles de pista revelados en orden: **Orientación → Enfoque → Regla**. Ninguna pista revela la respuesta directamente (`HintEngine` impide saltar niveles).

## 7. Contenido semilla

135 desafíos (mínimo exigido: 130), generados y **auto-verificados uno a uno** contra la lógica real de los motores antes de guardarse (`tools/generate_seed_content.py`):

| Categoría | Desafíos |
|---|---|
| Patrones | 22 |
| Secuencias | 19 |
| Analogías | 15 |
| Clasificación | 20 |
| Matrices | 19 |
| Relaciones | 15 |
| Deducción | 15 |
| Construcción | 10 |
| **Total** | **135** |

Distribuidos en 10 cámaras (3 en Entrada, 18 en cada una de Patrones/Secuencias/Clasificación/Matrices, 14 en Analogías/Relaciones/Deducción, 8 en Constructor, 10 en la Cámara Maestra).

## 8. Reto Diario

Generado localmente (sin red) mediante `DailyChallengeGenerator`, con semilla determinista derivada de la fecha: todos los desafíos generados para un mismo día son idénticos entre ejecuciones, y cada uno se valida (solución única) antes de mostrarse. Rota entre 6 categorías. Sin vidas ni penalización por no jugar un día.

## 9. Accesibilidad y privacidad

- Ninguna interacción de arrastre es la única vía: Clasificación, Relaciones, Deducción y Construcción usan **toque** (tocar pieza → tocar destino), no gestos de arrastre obligatorios.
- Los estados de cámara (bloqueada/disponible/iniciada/completada/dominada) se comunican con icono + texto, nunca solo con color.
- Sin permisos Android (no INTERNET, no cámara, no micrófono). Sin nombre real, email, teléfono ni localización. Todo el progreso vive únicamente en el dispositivo (Room/SQLite local).

## 10. Simplificaciones documentadas

Siguiendo la regla anti-reducción-silenciosa del proyecto:

- **Ilustración**: se priorizaron vector drawables (formas geométricas reales dibujadas por Canvas/paths) sobre ilustraciones pictóricas complejas — cumple la prioridad #2/#3 de la sección 4 del prompt maestro, no la #1. Documentado también en `docs/BUILD_REPORT.md`.
- **Interacción de arrastre físico**: se implementó gesto de arrastre real (`pointerInput`) como demostración de capacidad, pero por razones de alcance en una sola sesión de generación, las cámaras de completar-hueco (Patrones/Secuencias/Analogías/Matrices) usan **toque para seleccionar + colocar** en vez de arrastre físico continuo. Sigue siendo una manipulación real de piezas (no opción múltiple de 4 botones), con el banco de piezas dibujado con su forma real.
- **`GraphErrorType`/tipos de regla**: no se modelan como tabla Room separada, sino como texto interpretado por el motor correspondiente — ver nota en `docs/BASE_DE_DATOS.md`.
- **Compilación**: no verificada en este entorno de generación (sin Android SDK ni red) — ver `docs/BUILD_REPORT.md` para el detalle completo y honesto.
