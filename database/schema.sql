-- LogicaMate v1.0.0 — esquema de base de datos (SQLite, vía Room 2.6.1)
-- Documento de referencia: la app NO ejecuta este archivo directamente —
-- Room genera el esquema real a partir de las entidades Kotlin en
-- data/local/entity/*.kt. Este .sql refleja exactamente esas entidades y
-- sirve como documentación técnica y como base para database/sample_data.sql.
-- Motor: SQLite embebido en Android (sin servidor, 100% local — sección 23).

PRAGMA foreign_keys = ON;

-- ============================================================
-- Perfil y progreso
-- ============================================================

CREATE TABLE user_profile (
    id INTEGER NOT NULL PRIMARY KEY,       -- siempre 1L: un único perfil local (sección 17)
    alias TEXT NOT NULL,
    avatarId INTEGER NOT NULL,             -- 0-7 sobre 8 avatares base locales
    createdAtMillis INTEGER NOT NULL,
    soundEnabled INTEGER NOT NULL DEFAULT 1,
    hapticsEnabled INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE user_stats (
    userProfileId INTEGER NOT NULL PRIMARY KEY,
    totalXp INTEGER NOT NULL,
    currentStreak INTEGER NOT NULL,
    lastActiveDay INTEGER,                 -- día (epochMillis / 86_400_000), NULL si nunca jugó
    FOREIGN KEY (userProfileId) REFERENCES user_profile(id)
);

-- ============================================================
-- Contenido: cámaras, categorías, desafíos
-- ============================================================

CREATE TABLE logic_chamber (
    id TEXT NOT NULL PRIMARY KEY,          -- nombre del enum ChamberId (ENTRANCE, PATTERNS, ...)
    displayName TEXT NOT NULL,
    orderIndex INTEGER NOT NULL,
    iconRes TEXT NOT NULL,
    flavorText TEXT NOT NULL
);

CREATE TABLE logic_category (
    id TEXT NOT NULL PRIMARY KEY,          -- nombre del enum LogicCategory
    displayName TEXT NOT NULL,
    iconRes TEXT NOT NULL
);

CREATE TABLE challenge (
    id TEXT NOT NULL PRIMARY KEY,
    chamberId TEXT NOT NULL,
    categoryId TEXT NOT NULL,
    difficulty TEXT NOT NULL,              -- INITIAL | INTERMEDIATE | ADVANCED
    interactionType TEXT NOT NULL,         -- DRAG_PLACE | ORDER | BUILD | CONNECT | COMPLETE | DISCOVER | CLASSIFY | CORRECT | ACTIVATE | INVESTIGATE | OPTION_SELECT
    prompt TEXT NOT NULL,
    explanation TEXT NOT NULL,
    isSeed INTEGER NOT NULL,               -- 1 = contenido semilla, 0 = generado (Reto Diario)
    orderIndex INTEGER NOT NULL,
    FOREIGN KEY (chamberId) REFERENCES logic_chamber(id),
    FOREIGN KEY (categoryId) REFERENCES logic_category(id)
);
CREATE INDEX idx_challenge_chamberId ON challenge(chamberId);
CREATE INDEX idx_challenge_categoryId ON challenge(categoryId);
CREATE INDEX idx_challenge_difficulty ON challenge(difficulty);

CREATE TABLE challenge_item (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    challengeId TEXT NOT NULL,
    position INTEGER NOT NULL,
    pieceEncoded TEXT NOT NULL,            -- "SHAPE:COLOR:SIZE:COUNT:VALUE" o "BLANK"
    role TEXT NOT NULL,                    -- DISPLAY | OPTION | SOLUTION
    FOREIGN KEY (challengeId) REFERENCES challenge(id) ON DELETE CASCADE
);
CREATE INDEX idx_challenge_item_challengeId ON challenge_item(challengeId);

CREATE TABLE challenge_rule (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    challengeId TEXT NOT NULL,
    ruleType TEXT NOT NULL,                -- p.ej. PATTERN_CYCLE, SEQUENCE_ARITHMETIC, MATRIX_TRANSFORM...
    paramsEncoded TEXT NOT NULL,           -- "k1=v1;k2=v2"
    FOREIGN KEY (challengeId) REFERENCES challenge(id) ON DELETE CASCADE
);
CREATE INDEX idx_challenge_rule_challengeId ON challenge_rule(challengeId);

CREATE TABLE hint (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    challengeId TEXT NOT NULL,
    level INTEGER NOT NULL,                -- 1 (orientación) .. 3 (regla)
    text TEXT NOT NULL,
    FOREIGN KEY (challengeId) REFERENCES challenge(id) ON DELETE CASCADE
);
CREATE INDEX idx_hint_challengeId ON hint(challengeId);

-- ============================================================
-- Intentos e historial
-- ============================================================

CREATE TABLE attempt (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    challengeId TEXT NOT NULL,
    userProfileId INTEGER NOT NULL,
    startedAtMillis INTEGER NOT NULL,
    endedAtMillis INTEGER,
    isCorrect INTEGER NOT NULL,
    attemptNumber INTEGER NOT NULL,        -- 1 = primer intento sobre este desafío
    hintsUsedCount INTEGER NOT NULL,
    submittedSolutionEncoded TEXT NOT NULL,
    FOREIGN KEY (challengeId) REFERENCES challenge(id) ON DELETE CASCADE
);
CREATE INDEX idx_attempt_challengeId ON attempt(challengeId);
CREATE INDEX idx_attempt_userProfileId ON attempt(userProfileId);

CREATE TABLE hint_usage (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    attemptId INTEGER NOT NULL,
    hintLevel INTEGER NOT NULL,
    usedAtMillis INTEGER NOT NULL,
    FOREIGN KEY (attemptId) REFERENCES attempt(id) ON DELETE CASCADE
);
CREATE INDEX idx_hint_usage_attemptId ON hint_usage(attemptId);

CREATE TABLE daily_challenge (
    date TEXT NOT NULL PRIMARY KEY,        -- "yyyy-MM-dd" local
    challengeId TEXT NOT NULL,
    completed INTEGER NOT NULL,
    completedAtMillis INTEGER,
    FOREIGN KEY (challengeId) REFERENCES challenge(id)
);
CREATE INDEX idx_daily_challenge_challengeId ON daily_challenge(challengeId);

-- ============================================================
-- Progresión: fragmentos de la Llave Lógica, estado por cámara
-- ============================================================

CREATE TABLE key_fragment (
    id TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    chamberId TEXT NOT NULL,               -- cámara cuya finalización otorga este fragmento
    orderIndex INTEGER NOT NULL,
    shapeDescriptor TEXT NOT NULL,
    FOREIGN KEY (chamberId) REFERENCES logic_chamber(id)
);
CREATE INDEX idx_key_fragment_chamberId ON key_fragment(chamberId);

CREATE TABLE unlocked_fragment (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    userProfileId INTEGER NOT NULL,
    keyFragmentId TEXT NOT NULL,
    unlockedAtMillis INTEGER NOT NULL,
    FOREIGN KEY (keyFragmentId) REFERENCES key_fragment(id)
);
CREATE INDEX idx_unlocked_fragment_keyFragmentId ON unlocked_fragment(keyFragmentId);
CREATE INDEX idx_unlocked_fragment_userProfileId ON unlocked_fragment(userProfileId);

CREATE TABLE progress (
    userProfileId INTEGER NOT NULL,
    chamberId TEXT NOT NULL,
    status TEXT NOT NULL,                  -- LOCKED | AVAILABLE | STARTED | COMPLETED | MASTERED
    challengesCompleted INTEGER NOT NULL,
    perfectChallenges INTEGER NOT NULL,    -- resueltos a la primera y sin pistas (criterio de "dominado")
    totalChallenges INTEGER NOT NULL,
    xpEarnedInChamber INTEGER NOT NULL,
    PRIMARY KEY (userProfileId, chamberId),
    FOREIGN KEY (chamberId) REFERENCES logic_chamber(id)
);
CREATE INDEX idx_progress_chamberId ON progress(chamberId);
CREATE INDEX idx_progress_userProfileId ON progress(userProfileId);

-- ============================================================
-- Gamificación: insignias y colección
-- ============================================================

CREATE TABLE badge (
    id TEXT NOT NULL PRIMARY KEY,          -- nombre del enum GamificationEngine.BadgeCode
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    iconRes TEXT NOT NULL,
    criteriaDescription TEXT NOT NULL
);

CREATE TABLE user_badge (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    userProfileId INTEGER NOT NULL,
    badgeId TEXT NOT NULL,
    unlockedAtMillis INTEGER NOT NULL,
    FOREIGN KEY (badgeId) REFERENCES badge(id)
);
CREATE INDEX idx_user_badge_badgeId ON user_badge(badgeId);
CREATE INDEX idx_user_badge_userProfileId ON user_badge(userProfileId);

CREATE TABLE collectible_item (
    id TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    chamberId TEXT NOT NULL,
    iconRes TEXT NOT NULL
);

CREATE TABLE unlocked_collectible (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    userProfileId INTEGER NOT NULL,
    collectibleItemId TEXT NOT NULL,
    unlockedAtMillis INTEGER NOT NULL,
    FOREIGN KEY (collectibleItemId) REFERENCES collectible_item(id)
);
CREATE INDEX idx_unlocked_collectible_collectibleItemId ON unlocked_collectible(collectibleItemId);
CREATE INDEX idx_unlocked_collectible_userProfileId ON unlocked_collectible(userProfileId);

-- ============================================================
-- Simplificación documentada (regla anti-reducción-silenciosa)
-- ============================================================
-- GraphErrorType / tipos de regla (ruleType) NO se modelan como tabla de
-- lookup separada: son valores TEXT libres interpretados por el motor
-- correspondiente en tiempo de ejecución (SequenceEngine, PatternEngine,
-- MatrixEngine.CellTransform, etc. — ver domain/engine/*.kt). Se decidió así
-- porque el conjunto de tipos de regla es fijo y pequeño, está totalmente
-- cubierto por SeedContentConsistencyTest, y una tabla de lookup no añadiría
-- integridad real (no hay claim foránea significativa que proteger).
