-- LogicaMate v1.0.0 -- datos de ejemplo (subconjunto ilustrativo, NO el banco completo)
-- El banco completo de 135 desafios semilla vive en el codigo fuente
-- (app/src/main/java/.../seed/SeedContent.kt) y se inserta en tiempo de ejecucion
-- por DatabaseSeeder.kt la primera vez que arranca la app. Este archivo es solo
-- para revisar visualmente la forma de los datos sin tener que compilar la app.

INSERT INTO logic_chamber (id, displayName, orderIndex, iconRes, flavorText) VALUES
    ('ENTRANCE', 'Entrada al Templo', 0, 'ic_chamber_entrance', 'Tres simbolos, una puerta.'),
    ('PATTERNS', 'Galeria de Patrones', 1, 'ic_chamber_patterns', 'Los mosaicos esconden ritmos.'),
    ('SEQUENCES', 'Pasadizo de Secuencias', 2, 'ic_chamber_sequences', 'Un pasadizo que avanza con una regla.'),
    ('ANALOGIES', 'Sala de Analogias', 3, 'ic_chamber_analogies', 'Un mecanismo transforma objetos.'),
    ('CLASSIFICATION', 'Sala de Clasificacion', 4, 'ic_chamber_classification', 'Portales que aceptan un tipo.'),
    ('MATRICES', 'Sala de Matrices', 5, 'ic_chamber_matrices', 'Un panel mural con una pieza que falta.'),
    ('RELATIONS', 'Sala de Relaciones', 6, 'ic_chamber_relations', 'Un mapa de posiciones.'),
    ('DEDUCTION', 'Sala de Deduccion', 7, 'ic_chamber_deduction', 'Pistas dispersas por investigar.'),
    ('CONSTRUCTOR', 'Taller Constructor', 8, 'ic_chamber_constructor', 'Aqui construyes tus reglas.'),
    ('MASTER', 'Camara Maestra', 9, 'ic_chamber_master', 'Todo lo aprendido, a la vez.');

INSERT INTO logic_category (id, displayName, iconRes) VALUES
    ('PATTERN', 'Patrones', 'ic_chamber_pattern'),
    ('SEQUENCE', 'Secuencias', 'ic_chamber_sequence'),
    ('ANALOGY', 'Analogias', 'ic_chamber_analogy'),
    ('CLASSIFICATION', 'Clasificacion', 'ic_chamber_classification'),
    ('MATRIX', 'Matrices', 'ic_chamber_matrix'),
    ('RELATION', 'Relaciones', 'ic_chamber_relation'),
    ('DEDUCTION', 'Deduccion', 'ic_chamber_deduction'),
    ('CONSTRUCTION', 'Construccion', 'ic_chamber_construction');

INSERT INTO badge (id, name, description, iconRes, criteriaDescription) VALUES
    ('PRIMER_MECANISMO', 'Primer Mecanismo', 'Resolviste tu primer desafio del templo.', 'ic_badge_primer_mecanismo', 'Resolviste tu primer desafio del templo.'),
    ('CAZADOR_DE_PATRONES', 'Cazador de Patrones', 'Descubriste 12 patrones ocultos.', 'ic_badge_cazador_de_patrones', 'Descubriste 12 patrones ocultos.'),
    ('MAESTRO_DE_SECUENCIAS', 'Maestro de Secuencias', 'Completaste 12 secuencias del pasadizo.', 'ic_badge_maestro_de_secuencias', 'Completaste 12 secuencias del pasadizo.'),
    ('DETECTIVE_DE_RELACIONES', 'Detective de Relaciones', 'Resolviste 8 mapas de posiciones.', 'ic_badge_detective_de_relaciones', 'Resolviste 8 mapas de posiciones.'),
    ('CONSTRUCTOR_LOGICO', 'Constructor Logico', 'Construiste 6 patrones propios validos.', 'ic_badge_constructor_logico', 'Construiste 6 patrones propios validos.'),
    ('EXPLORADOR_DE_MATRICES', 'Explorador de Matrices', 'Completaste 10 paneles murales.', 'ic_badge_explorador_de_matrices', 'Completaste 10 paneles murales.'),
    ('GRAN_DEDUCIDOR', 'Gran Deducidor', 'Resolviste 8 investigaciones completas.', 'ic_badge_gran_deducidor', 'Resolviste 8 investigaciones completas.'),
    ('MAESTRO_DEL_TEMPLO', 'Maestro del Templo', 'Reuniste todos los fragmentos de la Llave Logica.', 'ic_badge_maestro_del_templo', 'Reuniste todos los fragmentos de la Llave Logica.');

INSERT INTO key_fragment (id, name, chamberId, orderIndex, shapeDescriptor) VALUES
    ('FRAG_PATTERNS', 'Fragmento de Galeria de Patrones', 'PATTERNS', 0, 'ic_key_fragment'),
    ('FRAG_SEQUENCES', 'Fragmento de Pasadizo de Secuencias', 'SEQUENCES', 1, 'ic_key_fragment'),
    ('FRAG_ANALOGIES', 'Fragmento de Sala de Analogias', 'ANALOGIES', 2, 'ic_key_fragment'),
    ('FRAG_CLASSIFICATION', 'Fragmento de Sala de Clasificacion', 'CLASSIFICATION', 3, 'ic_key_fragment'),
    ('FRAG_MATRICES', 'Fragmento de Sala de Matrices', 'MATRICES', 4, 'ic_key_fragment'),
    ('FRAG_RELATIONS', 'Fragmento de Sala de Relaciones', 'RELATIONS', 5, 'ic_key_fragment'),
    ('FRAG_DEDUCTION', 'Fragmento de Sala de Deduccion', 'DEDUCTION', 6, 'ic_key_fragment'),
    ('FRAG_CONSTRUCTOR', 'Fragmento de Taller Constructor', 'CONSTRUCTOR', 7, 'ic_key_fragment');

-- 8 de los 135 desafios semilla (uno por categoria), como muestra representativa:
INSERT INTO challenge (id, chamberId, categoryId, difficulty, interactionType, prompt, explanation, isSeed, orderIndex) VALUES
    ('ENT-001', 'ENTRANCE', 'PATTERN', 'INITIAL', 'DISCOVER', 'Una puerta de piedra bloquea el paso. Observa los símbolos: ¿cuál falta?', 'La puerta repite el mismo par de símbolos una y otra vez.', 1, 0),
    ('SEQ-001', 'SEQUENCES', 'SEQUENCE', 'INITIAL', 'COMPLETE', 'Descubre la regla del pasadizo y completa la casilla que falta.', 'Esta secuencia suma siempre el mismo número en cada paso.', 1, 1),
    ('ANA-001', 'ANALOGIES', 'ANALOGY', 'INITIAL', 'COMPLETE', 'Un mecanismo transforma la primera pieza en la segunda. Aplica la misma transformación.', 'La transformación del mecanismo es siempre la misma: la cantidad se duplica.', 1, 2),
    ('CLA-001', 'CLASSIFICATION', 'CLASSIFICATION', 'INITIAL', 'CLASSIFY', 'Separa estos símbolos del templo en grupos según una regla oculta.', 'Cada grupo reúne las piezas que comparten la forma.', 1, 3),
    ('MAT-001', 'MATRICES', 'MATRIX', 'INITIAL', 'COMPLETE', 'Completa la celda que falta en el panel mural.', 'En este panel la cantidad se multiplica por 2 en cada fila, siempre de la misma forma en cada columna.', 1, 4),
    ('REL-001', 'RELATIONS', 'RELATION', 'INITIAL', 'CONNECT', 'Ordena a los exploradores según las pistas del mapa de posiciones.', 'Cada pista elimina posiciones imposibles hasta dejar un único orden válido.', 1, 5),
    ('DED-001', 'DEDUCTION', 'DEDUCTION', 'INITIAL', 'INVESTIGATE', 'Investiga las pistas y descubre qué objeto tiene cada explorador.', 'Combinando todas las pistas negativas solo queda una asignación posible para cada explorador.', 1, 6),
    ('CON-001', 'CONSTRUCTOR', 'CONSTRUCTION', 'INITIAL', 'BUILD', 'Construye una tira que alterne triángulo y círculo, empezando por triángulo.', 'El taller comprueba la ESTRUCTURA completa que construiste, no una sola pieza.', 1, 7);

INSERT INTO challenge_item (challengeId, position, pieceEncoded, role) VALUES
    ('ENT-001', 0, 'STAR:GOLD:MEDIUM:1:', 'DISPLAY'),
    ('ENT-001', 1, 'CIRCLE:GOLD:MEDIUM:1:', 'DISPLAY'),
    ('ENT-001', 2, 'STAR:GOLD:MEDIUM:1:', 'DISPLAY'),
    ('ENT-001', 3, 'CIRCLE:GOLD:MEDIUM:1:', 'DISPLAY'),
    ('ENT-001', 4, 'BLANK', 'DISPLAY'),
    ('ENT-001', 0, 'TRIANGLE:GOLD:MEDIUM:1:', 'OPTION'),
    ('ENT-001', 1, 'CIRCLE:GOLD:MEDIUM:1:', 'OPTION'),
    ('ENT-001', 2, 'SQUARE:GOLD:MEDIUM:1:', 'OPTION'),
    ('ENT-001', 3, 'STAR:GOLD:MEDIUM:1:', 'OPTION'),
    ('ENT-001', 0, 'STAR:GOLD:MEDIUM:1:', 'SOLUTION'),
    ('SEQ-001', 0, 'NONE:GOLD:MEDIUM:1:8', 'DISPLAY'),
    ('SEQ-001', 1, 'NONE:GOLD:MEDIUM:1:12', 'DISPLAY'),
    ('SEQ-001', 2, 'NONE:GOLD:MEDIUM:1:16', 'DISPLAY'),
    ('SEQ-001', 3, 'NONE:GOLD:MEDIUM:1:20', 'DISPLAY'),
    ('SEQ-001', 4, 'NONE:GOLD:MEDIUM:1:24', 'DISPLAY'),
    ('SEQ-001', 5, 'BLANK', 'DISPLAY'),
    ('SEQ-001', 0, 'NONE:GOLD:MEDIUM:1:28', 'SOLUTION'),
    ('ANA-001', 0, 'DIAMOND:STONE:SMALL:1:', 'DISPLAY'),
    ('ANA-001', 1, 'DIAMOND:STONE:SMALL:2:', 'DISPLAY'),
    ('ANA-001', 2, 'HEXAGON:CRYSTAL:SMALL:1:', 'DISPLAY'),
    ('ANA-001', 3, 'BLANK', 'DISPLAY'),
    ('ANA-001', 0, 'HEXAGON:CRYSTAL:SMALL:2:', 'OPTION'),
    ('ANA-001', 1, 'HEXAGON:CRYSTAL:SMALL:1:', 'OPTION'),
    ('ANA-001', 2, 'DIAMOND:STONE:SMALL:2:', 'OPTION'),
    ('ANA-001', 0, 'HEXAGON:CRYSTAL:SMALL:2:', 'SOLUTION'),
    ('CLA-001', 0, 'CIRCLE:CORAL:MEDIUM:1:', 'OPTION'),
    ('CLA-001', 1, 'SQUARE:CRYSTAL:MEDIUM:1:', 'OPTION'),
    ('CLA-001', 2, 'CIRCLE:GOLD:MEDIUM:1:', 'OPTION'),
    ('CLA-001', 3, 'SQUARE:GOLD:MEDIUM:1:', 'OPTION'),
    ('CLA-001', 4, 'TRIANGLE:CRYSTAL:MEDIUM:1:', 'OPTION'),
    ('CLA-001', 5, 'TRIANGLE:MOSS:MEDIUM:1:', 'OPTION'),
    ('MAT-001', 0, 'DIAMOND:GOLD:SMALL:1:', 'DISPLAY'),
    ('MAT-001', 1, 'STAR:GOLD:SMALL:1:', 'DISPLAY'),
    ('MAT-001', 2, 'DIAMOND:GOLD:SMALL:2:', 'DISPLAY'),
    ('MAT-001', 3, 'BLANK', 'DISPLAY'),
    ('MAT-001', 0, 'STAR:GOLD:SMALL:2:', 'OPTION'),
    ('MAT-001', 1, 'STAR:GOLD:SMALL:4:', 'OPTION'),
    ('MAT-001', 2, 'STAR:GOLD:SMALL:1:', 'OPTION'),
    ('MAT-001', 0, 'STAR:GOLD:SMALL:2:', 'SOLUTION'),
    ('CON-001', 0, 'TRIANGLE:GOLD:MEDIUM:1:', 'OPTION'),
    ('CON-001', 1, 'CIRCLE:GOLD:MEDIUM:1:', 'OPTION'),
    ('CON-001', 2, 'SQUARE:GOLD:MEDIUM:1:', 'OPTION');

INSERT INTO challenge_rule (challengeId, ruleType, paramsEncoded) VALUES
    ('ENT-001', 'PATTERN_CYCLE', ''),
    ('SEQ-001', 'SEQUENCE_ARITHMETIC', ''),
    ('ANA-001', 'ANALOGY_TRANSFORM', 'kind=MULTIPLY_COUNT;factor=2'),
    ('CLA-001', 'CLASSIFICATION_RULE', 'property=SHAPE;solution=0,2|1,3|4,5'),
    ('MAT-001', 'MATRIX_TRANSFORM', 'cols=2;kind=MULTIPLY_COUNT;factor=2'),
    ('REL-001', 'RELATION_ORDER', 'items=Nia,Lía,Sira;solution=Nia,Sira,Lía;clues=Nia llegó al templo antes que Sira. Sira llegó al templo antes que Lía.'),
    ('DED-001', 'DEDUCTION_GRID', 'people=Tomás,Bruno,Lía;objects=llave dorada,cristal verde,cristal azul;clues=Tomás no tiene cristal azul. Lía no tiene cristal verde. Lía no tiene cristal azul.;solution=Tomás:cristal verde,Bruno:cristal azul,Lía:llave dorada'),
    ('CON-001', 'ALTERNATE_TWO_SHAPES', 'shapeA=TRIANGLE;shapeB=CIRCLE');

INSERT INTO hint (challengeId, level, text) VALUES
    ('ENT-001', 1, 'Observa antes de mover las piezas.'),
    ('ENT-001', 2, 'Algo se repite aquí. ¿Puedes encontrarlo?'),
    ('ENT-001', 3, 'Los símbolos se repiten de dos en dos.'),
    ('SEQ-001', 1, 'Calcula qué cambia entre cada número y el siguiente.'),
    ('SEQ-001', 2, 'La regla del pasadizo suma siempre el mismo número.'),
    ('SEQ-001', 3, 'Aplica esa misma operación al último número que ves.'),
    ('ANA-001', 1, 'Compara con atención la primera pieza y en qué se convirtió.'),
    ('ANA-001', 2, 'En esta cámara, la cantidad se duplica.'),
    ('ANA-001', 3, 'Aplica exactamente ese mismo cambio a la tercera pieza.'),
    ('CLA-001', 1, 'Compara las piezas de dos en dos: ¿qué comparten algunas y otras no?'),
    ('CLA-001', 2, 'La regla oculta tiene que ver con la forma.'),
    ('CLA-001', 3, 'Todas las piezas de un mismo grupo comparten exactamente ese valor.'),
    ('MAT-001', 1, 'Compara cada fila con la fila anterior, columna por columna.'),
    ('MAT-001', 2, 'En este panel, la cantidad se multiplica por 2 en cada fila.'),
    ('MAT-001', 3, 'Aplica esa misma regla desde la primera fila hasta llegar a la celda vacía.'),
    ('REL-001', 1, 'Empieza por la pista que da una relación directa entre dos exploradores.'),
    ('REL-001', 2, 'Ve descartando posiciones imposibles, una pista a la vez.'),
    ('REL-001', 3, 'Cuando apliques todas las pistas juntas, solo debería quedar un orden posible.'),
    ('DED-001', 1, 'Anota primero lo que cada pista DESCARTA, no lo que confirma.'),
    ('DED-001', 2, 'Cuando a alguien solo le quede una opción posible, esa es la suya.'),
    ('DED-001', 3, 'Resuelve primero a quien le queden menos opciones posibles.'),
    ('CON-001', 1, 'No hay una única pieza correcta: construye toda la tira y luego revisa si cumple la condición.'),
    ('CON-001', 2, 'Cuenta las posiciones mientras colocas piezas para no perder el ritmo de la regla.'),
    ('CON-001', 3, 'Prueba con una tira corta primero y amplíala si funciona.');

-- Perfil de ejemplo con progreso parcial:
INSERT INTO user_profile (id, alias, avatarId, createdAtMillis, soundEnabled, hapticsEnabled) VALUES
    (1, 'Explorador', 0, 1755600000000, 1, 1);

INSERT INTO user_stats (userProfileId, totalXp, currentStreak, lastActiveDay) VALUES
    (1, 145, 3, 20309);

INSERT INTO progress (userProfileId, chamberId, status, challengesCompleted, perfectChallenges, totalChallenges, xpEarnedInChamber) VALUES
    (1, 'ENTRANCE', 'MASTERED', 3, 3, 3, 30),
    (1, 'PATTERNS', 'STARTED', 5, 3, 18, 65),
    (1, 'SEQUENCES', 'AVAILABLE', 0, 0, 18, 0),
    (1, 'ANALOGIES', 'LOCKED', 0, 0, 14, 0);

-- Nota: ningun fragmento de la Llave Logica aparece desbloqueado en esta
-- muestra porque ninguna camara que otorga fragmento (ver key_fragment)
-- esta aun en estado COMPLETED/MASTERED -- así se mantiene coherente con
-- la regla real de ProgressRepository.maybeUnlockKeyFragment().

