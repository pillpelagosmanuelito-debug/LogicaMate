# LogicaMate — Base de Datos

Motor: **Room 2.6.1 sobre SQLite** embebido (sin servidor, 100% local). Esquema completo en [`database/schema.sql`](../database/schema.sql) (verificado ejecutándose contra SQLite real, ver `BUILD_REPORT.md`). Datos de ejemplo en [`database/sample_data.sql`](../database/sample_data.sql).

## Diagrama entidad-relación

```mermaid
erDiagram
    user_profile ||--o| user_stats : tiene
    user_profile ||--o{ progress : tiene
    user_profile ||--o{ attempt : registra
    user_profile ||--o{ unlocked_fragment : desbloquea
    user_profile ||--o{ user_badge : desbloquea
    user_profile ||--o{ unlocked_collectible : desbloquea

    logic_chamber ||--o{ challenge : contiene
    logic_chamber ||--o{ progress : "estado por"
    logic_chamber ||--o{ key_fragment : otorga
    logic_chamber ||--o{ collectible_item : origina

    logic_category ||--o{ challenge : clasifica

    challenge ||--o{ challenge_item : compone
    challenge ||--o{ challenge_rule : define
    challenge ||--o{ hint : ofrece
    challenge ||--o{ attempt : recibe
    challenge ||--o| daily_challenge : "es el reto de"

    attempt ||--o{ hint_usage : registra

    key_fragment ||--o{ unlocked_fragment : "se desbloquea como"
    badge ||--o{ user_badge : "se desbloquea como"
    collectible_item ||--o{ unlocked_collectible : "se desbloquea como"

    user_profile {
        int id PK
        string alias
        int avatarId
        long createdAtMillis
        bool soundEnabled
        bool hapticsEnabled
    }
    logic_chamber {
        string id PK
        string displayName
        int orderIndex
    }
    challenge {
        string id PK
        string chamberId FK
        string categoryId FK
        string difficulty
        string interactionType
        string prompt
        bool isSeed
    }
    challenge_item {
        long id PK
        string challengeId FK
        int position
        string pieceEncoded
        string role
    }
    challenge_rule {
        long id PK
        string challengeId FK
        string ruleType
        string paramsEncoded
    }
    attempt {
        long id PK
        string challengeId FK
        int userProfileId FK
        bool isCorrect
        int attemptNumber
        int hintsUsedCount
    }
    progress {
        int userProfileId PK, FK
        string chamberId PK, FK
        string status
        int challengesCompleted
        int totalChallenges
    }
    key_fragment {
        string id PK
        string chamberId FK
    }
    badge {
        string id PK
        string name
    }
    collectible_item {
        string id PK
        string chamberId FK
    }
```

## Notas de diseño

- **`challenge_item` separado de `challenge`** (en vez de un único blob JSON): permite renderizar cada pieza de forma individual e independiente en las pantallas de arrastrar/tocar-y-colocar, y cada fila lleva un `role` (`DISPLAY`, `OPTION`, `SOLUTION`) para distinguir enunciado, banco de piezas y solución.
- **`challenge_rule` separado**: guarda el tipo de regla (p.ej. `SEQUENCE_ARITHMETIC`, `MATRIX_TRANSFORM`, `CLASSIFICATION_RULE`) y sus parámetros serializados (`k1=v1;k2=v2`), permitiendo reconstruir exactamente qué motor y qué configuración validó ese desafío.
- **`progress` con clave primaria compuesta** `(userProfileId, chamberId)`: un perfil, un estado por cámara.
- **`attempt` conserva historial completo** (no solo el último intento): `attemptNumber` permite calcular bonos de "primer intento" y detectar resoluciones "perfectas" (sin pistas, primer intento) para el criterio de cámara "dominada".

## Simplificación documentada (regla anti-reducción-silenciosa)

`GraphErrorType` / los tipos de regla (`ruleType`) **no se modelan como tabla de lookup separada** (a diferencia de, por ejemplo, `logic_category`), sino como texto libre en `challenge_rule.ruleType`, interpretado en tiempo de ejecución por el motor correspondiente (`domain/engine/*.kt`). Se decidió así porque:

1. El conjunto de tipos de regla es fijo, pequeño (≤10 valores) y vive en el código fuente de los motores, no en datos mutables por el usuario.
2. Una tabla de lookup no protegería ninguna integridad referencial real — no hay filas que un usuario pudiera crear o corromper.
3. Está cubierto exhaustivamente por `SeedContentConsistencyTest`, que decodifica cada `ruleType`/`paramsEncoded` contra el motor real y verifica la solución almacenada.

Esta es exactamente la misma simplificación (y el mismo razonamiento) que se aplicó en el proyecto hermano GráficosDivertidos, mantenida aquí por consistencia arquitectónica dentro del mismo portafolio de apps educativas.
