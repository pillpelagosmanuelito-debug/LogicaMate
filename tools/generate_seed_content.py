#!/usr/bin/env python3
"""
Genera app/src/main/java/.../seed/SeedContent.kt con el banco de desafíos
semilla de LogicaMate (>=130, sección 20/40 del prompt).

Cada función generate_* replica en Python, término a término, la lógica de
los motores Kotlin equivalentes (domain/engine/*.kt) y AUTO-VERIFICA cada
desafío antes de emitirlo (asserts). Esto no sustituye compilar y ejecutar
los tests JVM reales (ver docs/BUILD_REPORT.md: el entorno de generación no
tiene JDK+Kotlin ni Android SDK), pero da una fuerte garantía de consistencia
interna: si este script termina sin AssertionError, cada desafío semilla es
matemáticamente correcto según las mismas reglas que implementa el motor.
"""
import os
import random

random.seed(20260819)  # determinismo total del contenido semilla

OUT_DIR = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main",
                        "java", "com", "educalab", "logicamate", "seed")
os.makedirs(OUT_DIR, exist_ok=True)

SHAPES = ["TRIANGLE", "CIRCLE", "SQUARE", "STAR", "HEXAGON", "DIAMOND"]
COLORS = ["GOLD", "CRYSTAL", "CORAL", "STONE", "EMBER", "MOSS"]
SIZES = ["SMALL", "MEDIUM", "LARGE"]

challenges = []          # lista de dicts -> se serializan al final
id_counters = {}


def next_id(prefix):
    id_counters[prefix] = id_counters.get(prefix, 0) + 1
    return f"{prefix}-{id_counters[prefix]:03d}"


def piece(shape="CIRCLE", color="GOLD", size="MEDIUM", count=1, value=None):
    return {"shape": shape, "color": color, "size": size, "count": count, "value": value}


BLANK = {"blank": True}


def encode_piece(p):
    if p.get("blank"):
        return "BLANK"
    v = "" if p["value"] is None else str(p["value"])
    return f"{p['shape']}:{p['color']}:{p['size']}:{p['count']}:{v}"


# ------------------------------------------------------------ Mirrors ------
def mirror_next_in_cycle(items):
    """Replica PatternEngine.detectCycleLength + nextInCycle (una propiedad,
    piezas ya *totalmente* iguales entre posiciones equivalentes)."""
    n = len(items)
    for period in range(1, n // 2 + 1):
        if n % period != 0 and (n - (n % period)) < period * 2:
            continue
        ok = all(items[i] == items[i - period] for i in range(period, n))
        if ok and n // period >= 2:
            return items[n - period], period
    return None, None


def mirror_next_of_stream(stream):
    n = len(stream)
    for period in range(1, n // 2 + 1):
        if n // period < 2:
            continue
        ok = all(stream[i] == stream[i - period] for i in range(period, n))
        if ok:
            return stream[n - period]
    return None


def mirror_sequence_rule(values):
    diffs = [b - a for a, b in zip(values, values[1:])]
    if len(set(diffs)) == 1:
        return values[-1] + diffs[0]
    if all(v != 0 for v in values):
        ratios = []
        ok = True
        for a, b in zip(values, values[1:]):
            if a != 0 and b % a == 0:
                ratios.append(b // a)
            else:
                ok = False
        if ok and len(set(ratios)) == 1:
            return values[-1] * ratios[0]
    if len(diffs) >= 3:
        evens = set(diffs[0::2])
        odds = set(diffs[1::2])
        if len(evens) == 1 and len(odds) == 1 and evens != odds:
            last_step_index = len(values) - 1
            step = diffs[0] if last_step_index % 2 == 0 else diffs[1]
            return values[-1] + step
    return None


def mirror_transform_apply(p, kind, params, times=1):
    cur = dict(p)
    for _ in range(times):
        if kind == "MULTIPLY_COUNT":
            cur = {**cur, "count": cur["count"] * int(params["factor"])}
        elif kind == "CYCLE_SIZE":
            order = SIZES
            idx = (order.index(cur["size"]) + 1) % len(order)
            cur = {**cur, "size": order[idx]}
        elif kind == "CYCLE_COLOR":
            order = params["order"].split(",")
            idx = (order.index(cur["color"]) + 1) % len(order)
            cur = {**cur, "color": order[idx]}
        elif kind == "CYCLE_SHAPE":
            order = params["order"].split(",")
            idx = (order.index(cur["shape"]) + 1) % len(order)
            cur = {**cur, "shape": order[idx]}
        else:
            raise ValueError(kind)
    return cur


def mirror_canonical_partition(pieces, prop):
    def key(p):
        if prop == "SHAPE":
            return p["shape"]
        if prop == "COLOR":
            return p["color"]
        if prop == "SIZE":
            return p["size"]
        if prop == "PARITY_COUNT":
            return "EVEN" if p["count"] % 2 == 0 else "ODD"
        raise ValueError(prop)
    groups = {}
    for i, p in enumerate(pieces):
        groups.setdefault(key(p), []).append(i)
    return sorted(sorted(g) for g in groups.values())


def encode_partition(groups):
    return "|".join(",".join(str(i) for i in g) for g in groups)


def mirror_permutations(items):
    if not items:
        yield []
        return
    for i in range(len(items)):
        rest = items[:i] + items[i + 1:]
        for p in mirror_permutations(rest):
            yield [items[i]] + p


def mirror_relation_unique(items, constraints):
    """constraints: list of tuples (kind, a, b_or_pos)"""
    def holds(order, c):
        kind, a, b = c
        if kind == "BEFORE":
            return order.index(a) < order.index(b)
        if kind == "NOT_ADJACENT":
            return abs(order.index(a) - order.index(b)) != 1
        if kind == "IMMEDIATELY_BEFORE":
            return order.index(b) - order.index(a) == 1
        raise ValueError(kind)
    sols = [o for o in mirror_permutations(items) if all(holds(o, c) for c in constraints)]
    return sols[0] if len(sols) == 1 else None


def mirror_deduction_unique(people, objects, clues):
    """clues: list of tuples ('NOT', person, obj) meaning assignment[person] != obj"""
    sols = []
    for perm in mirror_permutations(objects):
        assignment = dict(zip(people, perm))
        if all(assignment[p] != o for (_, p, o) in clues):
            sols.append(assignment)
    return sols[0] if len(sols) == 1 else None


# ------------------------------------------------------------ Generators ---
def add_challenge(**kw):
    challenges.append(kw)


def make_hints(h1, h2, h3):
    return [{"level": 1, "text": h1}, {"level": 2, "text": h2}, {"level": 3, "text": h3}]


def gen_pattern_single(difficulty, chamber="PATTERNS", prefix="PAT"):
    period = 2 if difficulty == "INITIAL" else 3
    prop = random.choice(["COLOR", "SHAPE"]) if difficulty != "INITIAL" else "COLOR"
    if prop == "COLOR":
        chosen = random.sample(COLORS, period)
        base = [piece(shape="CIRCLE", color=c) for c in chosen]
    else:
        chosen = random.sample(SHAPES, period)
        base = [piece(shape=s, color="GOLD") for s in chosen]
    items = [dict(p) for p in (base * 2)]
    answer, det_period = mirror_next_in_cycle(items)
    assert det_period == period, f"periodo detectado {det_period} != {period}"
    assert answer == base[0]
    prop_label = "el color" if prop == "COLOR" else "la forma"
    return add_challenge(
        id=next_id(prefix), chamber=chamber, category="PATTERN", difficulty=difficulty,
        interaction="COMPLETE",
        prompt="Observa el mosaico del templo. ¿Qué pieza completa el patrón?",
        items=items + [BLANK],
        option_pool=[piece(shape=("CIRCLE" if prop == "COLOR" else s), color=(c if prop == "COLOR" else "GOLD"))
                     for c, s in zip(COLORS[:4], SHAPES[:4])] + [dict(answer)],
        solution=[answer],
        rule_type="PATTERN_CYCLE", rule_params={},
        hints=make_hints(
            "Mira qué ocurre entre cada dos piezas seguidas.",
            f"Observa {prop_label}. Se repite un bloque de {period} piezas.",
            f"El patrón repite siempre el mismo bloque de {period} piezas: cuenta desde el principio.",
        ),
        explanation=f"El mosaico repite un bloque de {period} piezas una y otra vez; tras completar el ciclo, vuelve a empezar.",
    )


def gen_pattern_multi(difficulty, chamber="PATTERNS", prefix="PAT"):
    shape_cycle = random.sample(SHAPES, 3)
    color_cycle = random.sample(COLORS, 2)
    n = 6
    shapes = [shape_cycle[i % 3] for i in range(n)]
    colors = [color_cycle[i % 2] for i in range(n)]
    items = [piece(shape=shapes[i], color=colors[i]) for i in range(n)]
    next_shape = mirror_next_of_stream(shapes)
    next_color = mirror_next_of_stream(colors)
    assert next_shape == shape_cycle[0] and next_color == color_cycle[0]
    answer = piece(shape=next_shape, color=next_color)
    return add_challenge(
        id=next_id(prefix), chamber=chamber, category="PATTERN", difficulty="ADVANCED",
        interaction="COMPLETE",
        prompt="Dos reglas actúan a la vez: la forma sigue un ciclo y el color sigue otro. ¿Qué pieza sigue?",
        items=items + [BLANK],
        option_pool=[piece(shape=s, color=c) for s in shape_cycle for c in color_cycle] + [dict(answer)],
        solution=[answer],
        rule_type="PATTERN_MULTI_PROPERTY", rule_params={},
        hints=make_hints(
            "Observa la forma por separado del color: cada una tiene su propio ritmo.",
            "La forma repite un ciclo de 3 piezas; el color, uno de 2.",
            "Cuenta 3 posiciones atrás para la forma y 2 posiciones atrás para el color.",
        ),
        explanation="La forma cicla cada 3 posiciones y el color cada 2: dos reglas independientes funcionando al mismo tiempo.",
    )


SEQ_TEMPLATES = {
    "INITIAL": "arithmetic",
    "INTERMEDIATE": "alternating",
    "ADVANCED": "geometric",
}


def gen_sequence(difficulty, chamber="SEQUENCES", prefix="SEQ"):
    kind = SEQ_TEMPLATES[difficulty]
    if kind == "arithmetic":
        start = random.randint(1, 9)
        step = random.randint(2, 6)
        values = [start + i * step for i in range(5)]
    elif kind == "geometric":
        start = random.randint(1, 4)
        ratio = random.choice([2, 3])
        values = [start * (ratio ** i) for i in range(4)]
    else:  # alternating
        a, b = random.sample(range(1, 7), 2)
        start = random.randint(1, 10)
        values = [start]
        for i in range(4):
            values.append(values[-1] + (a if i % 2 == 0 else b))
    answer = mirror_sequence_rule(values)
    assert answer is not None, f"secuencia ambigua: {values}"
    items = [piece(shape="NONE", value=v) for v in values] + [BLANK]
    rule_label = {"arithmetic": "suma siempre el mismo número", "geometric": "multiplica siempre por el mismo número",
                  "alternating": "alterna dos sumas distintas"}[kind]
    return add_challenge(
        id=next_id(prefix), chamber=chamber, category="SEQUENCE", difficulty=difficulty,
        interaction="COMPLETE",
        prompt="Descubre la regla del pasadizo y completa la casilla que falta.",
        items=items,
        option_pool=[],
        solution=[piece(shape="NONE", value=answer)],
        rule_type=f"SEQUENCE_{kind.upper()}", rule_params={},
        hints=make_hints(
            "Calcula qué cambia entre cada número y el siguiente.",
            f"La regla del pasadizo {rule_label}.",
            "Aplica esa misma operación al último número que ves.",
        ),
        explanation=f"Esta secuencia {rule_label} en cada paso.",
    )


TRANSFORM_KINDS = ["MULTIPLY_COUNT", "CYCLE_SIZE", "CYCLE_SHAPE", "CYCLE_COLOR"]


def random_transform(difficulty):
    if difficulty == "INITIAL":
        kind = "MULTIPLY_COUNT"
        params = {"factor": "2"}
    elif difficulty == "INTERMEDIATE":
        kind = random.choice(["CYCLE_SIZE", "MULTIPLY_COUNT"])
        params = {"factor": "2"} if kind == "MULTIPLY_COUNT" else {}
    else:
        kind = random.choice(["CYCLE_SHAPE", "CYCLE_COLOR"])
        if kind == "CYCLE_SHAPE":
            params = {"order": ",".join(random.sample(SHAPES, 3))}
        else:
            params = {"order": ",".join(random.sample(COLORS, 3))}
    return kind, params


def constrained_shape(kind, params):
    if kind == "CYCLE_SHAPE":
        return random.choice(params["order"].split(","))
    return random.choice(SHAPES)


def constrained_color(kind, params):
    if kind == "CYCLE_COLOR":
        return random.choice(params["order"].split(","))
    return random.choice(COLORS)


def gen_analogy(difficulty, chamber="ANALOGIES", prefix="ANA"):
    kind, params = random_transform(difficulty)
    a_shape = constrained_shape(kind, params)
    a_color = constrained_color(kind, params)
    a = piece(shape=a_shape, color=a_color, size="SMALL", count=1)
    a_prime = mirror_transform_apply(a, kind, params)
    b_shape = constrained_shape(kind, params)
    if kind != "CYCLE_SHAPE":
        candidates = [s for s in SHAPES if s != a_shape]
        b_shape = random.choice(candidates)
    b_color = constrained_color(kind, params)
    b = piece(shape=b_shape, color=b_color, size="SMALL", count=1)
    answer = mirror_transform_apply(b, kind, params)
    assert mirror_transform_apply(a, kind, params) == a_prime
    kind_label = {
        "MULTIPLY_COUNT": "la cantidad se duplica",
        "CYCLE_SIZE": "el tamaño avanza un paso (pequeño→mediano→grande)",
        "CYCLE_SHAPE": "la forma avanza un paso dentro de una secuencia de formas",
        "CYCLE_COLOR": "el color avanza un paso dentro de una secuencia de colores",
    }[kind]
    return add_challenge(
        id=next_id(prefix), chamber=chamber, category="ANALOGY", difficulty=difficulty,
        interaction="COMPLETE",
        prompt="Un mecanismo transforma la primera pieza en la segunda. Aplica la misma transformación.",
        items=[a, a_prime, b, BLANK],
        option_pool=[dict(answer), b, a_prime],
        solution=[answer],
        rule_type="ANALOGY_TRANSFORM", rule_params={"kind": kind, **params},
        hints=make_hints(
            "Compara con atención la primera pieza y en qué se convirtió.",
            f"En esta cámara, {kind_label}.",
            "Aplica exactamente ese mismo cambio a la tercera pieza.",
        ),
        explanation=f"La transformación del mecanismo es siempre la misma: {kind_label}.",
    )


CLASS_SHAPES = ["TRIANGLE", "CIRCLE", "SQUARE"]


def gen_classification(difficulty, chamber="CLASSIFICATION", prefix="CLA"):
    prop = {"INITIAL": "SHAPE", "INTERMEDIATE": "COLOR", "ADVANCED": "PARITY_COUNT"}[difficulty]
    if prop == "SHAPE":
        pieces = []
        for s in CLASS_SHAPES:
            for _ in range(2):
                pieces.append(piece(shape=s, color=random.choice(COLORS)))
    elif prop == "COLOR":
        chosen_colors = random.sample(COLORS, 3)
        pieces = []
        for c in chosen_colors:
            for _ in range(2):
                pieces.append(piece(shape=random.choice(CLASS_SHAPES), color=c))
    else:  # PARITY_COUNT
        pieces = [piece(shape=random.choice(CLASS_SHAPES), count=c) for c in [1, 2, 3, 4, 5, 6]]
    random.shuffle(pieces)
    partition = mirror_canonical_partition(pieces, prop)
    assert 2 <= len(partition) <= 4 and all(len(g) >= 2 for g in partition)
    prop_label = {"SHAPE": "la forma", "COLOR": "el color", "PARITY_COUNT": "si la cantidad es par o impar"}[prop]
    return add_challenge(
        id=next_id(prefix), chamber=chamber, category="CLASSIFICATION", difficulty=difficulty,
        interaction="CLASSIFY",
        prompt="Separa estos símbolos del templo en grupos según una regla oculta.",
        items=[],
        option_pool=pieces,
        solution=[],
        rule_type="CLASSIFICATION_RULE", rule_params={"property": prop, "solution": encode_partition(partition)},
        hints=make_hints(
            "Compara las piezas de dos en dos: ¿qué comparten algunas y otras no?",
            f"La regla oculta tiene que ver con {prop_label}.",
            "Todas las piezas de un mismo grupo comparten exactamente ese valor.",
        ),
        explanation=f"Cada grupo reúne las piezas que comparten {prop_label}.",
    )


def gen_matrix(difficulty, chamber="MATRICES", prefix="MAT"):
    cols = 2 if difficulty == "INITIAL" else 3
    rows = 2 if difficulty != "ADVANCED" else 3
    kind, params = random_transform(difficulty)
    base_color = constrained_color(kind, params) if kind == "CYCLE_COLOR" else "GOLD"
    if kind == "CYCLE_SHAPE":
        used_shapes = random.sample(params["order"].split(","), cols)
    else:
        used_shapes = random.sample(SHAPES, cols)
    base_row = [piece(shape=s, color=base_color, size="SMALL", count=1) for s in used_shapes]
    matrix = [base_row]
    for r in range(1, rows):
        matrix.append([mirror_transform_apply(p, kind, params, times=1) for p in matrix[-1]])
    blank_row = rows - 1
    blank_col = random.randint(0, cols - 1)
    answer = mirror_transform_apply(base_row[blank_col], kind, params, times=blank_row)
    assert answer == matrix[blank_row][blank_col]
    flat = []
    for r in range(rows):
        for c in range(cols):
            flat.append(BLANK if (r == blank_row and c == blank_col) else matrix[r][c])
    kind_label = {
        "MULTIPLY_COUNT": "la cantidad se multiplica por 2 en cada fila",
        "CYCLE_SIZE": "el tamaño avanza un paso en cada fila",
        "CYCLE_SHAPE": "la forma avanza un paso en cada fila",
        "CYCLE_COLOR": "el color avanza un paso en cada fila",
    }[kind]
    return add_challenge(
        id=next_id(prefix), chamber=chamber, category="MATRIX", difficulty=difficulty,
        interaction="COMPLETE",
        prompt="Completa la celda que falta en el panel mural.",
        items=flat,
        option_pool=[dict(answer), mirror_transform_apply(answer, kind, params), base_row[blank_col]],
        solution=[answer],
        rule_type="MATRIX_TRANSFORM", rule_params={"cols": str(cols), "kind": kind, **params},
        hints=make_hints(
            "Compara cada fila con la fila anterior, columna por columna.",
            f"En este panel, {kind_label}.",
            "Aplica esa misma regla desde la primera fila hasta llegar a la celda vacía.",
        ),
        explanation=f"En este panel {kind_label}, siempre de la misma forma en cada columna.",
    )


NAME_POOL = ["Lía", "Tomás", "Nia", "Kael", "Sira", "Bruno"]


def gen_relation(difficulty, chamber="RELATIONS", prefix="REL"):
    n = 3 if difficulty == "INITIAL" else 4
    names = random.sample(NAME_POOL, n)
    secret = names[:]
    random.shuffle(secret)
    # Cadena de precedencia que cubre TODOS los elementos: matemáticamente
    # garantiza una única extensión lineal posible (no hace falta reintentar).
    constraints = []
    for i in range(n - 1):
        use_immediate = difficulty == "ADVANCED" and i == n - 2
        constraints.append(
            ("IMMEDIATELY_BEFORE", secret[i], secret[i + 1]) if use_immediate
            else ("BEFORE", secret[i], secret[i + 1])
        )
    if n == 4:
        # Pista adicional, redundante pero coherente (ya no son adyacentes en la cadena de 4).
        constraints.append(("NOT_ADJACENT", secret[0], secret[3]))
    solution = mirror_relation_unique(names, constraints)
    assert solution is not None, "no se encontró un conjunto de restricciones con solución única"
    assert solution == secret

    def clue_text(c):
        kind, a, b = c
        if kind == "BEFORE":
            return f"{a} llegó al templo antes que {b}."
        if kind == "NOT_ADJACENT":
            return f"{a} y {b} no hicieron fila uno junto al otro."
        return f"{b} llegó justo después de {a}."

    return add_challenge(
        id=next_id(prefix), chamber=chamber, category="RELATION", difficulty=difficulty,
        interaction="CONNECT",
        prompt="Ordena a los exploradores según las pistas del mapa de posiciones.",
        items=[],
        option_pool=[],
        solution=[],
        rule_type="RELATION_ORDER", rule_params={
            "items": ",".join(names),
            "solution": ",".join(solution),
            "clues": " ".join(clue_text(c) for c in constraints),
        },
        hints=make_hints(
            "Empieza por la pista que da una relación directa entre dos exploradores.",
            "Ve descartando posiciones imposibles, una pista a la vez.",
            "Cuando apliques todas las pistas juntas, solo debería quedar un orden posible.",
        ),
        explanation="Cada pista elimina posiciones imposibles hasta dejar un único orden válido.",
    )


OBJ_POOL = ["cristal azul", "cristal verde", "cristal ámbar", "llave dorada", "prisma de luz"]


def gen_deduction(difficulty, chamber="DEDUCTION", prefix="DED"):
    n = 3 if difficulty != "ADVANCED" else 4
    people = random.sample(NAME_POOL, n)
    objects = random.sample(OBJ_POOL, n)
    secret = dict(zip(people, random.sample(objects, n)))
    clues = []
    for p in people:
        for o in objects:
            if secret[p] != o and random.random() < 0.55:
                clues.append(("NOT", p, o))
    solution = mirror_deduction_unique(people, objects, clues)
    tries = 0
    while solution is None and tries < 80:
        extra_p, extra_o = random.choice(people), random.choice(objects)
        if secret[extra_p] != extra_o:
            clues.append(("NOT", extra_p, extra_o))
        solution = mirror_deduction_unique(people, objects, clues)
        tries += 1
    assert solution is not None, "pistas insuficientes para solución única"
    # Deduplica y elimina pistas redundantes manteniendo unicidad (deja el
    # rompecabezas más limpio, sin repetir la misma pista dos veces).
    minimal = list(dict.fromkeys(clues))  # preserva orden, quita duplicados exactos
    for c in list(minimal):
        trial = [x for x in minimal if x != c]
        if mirror_deduction_unique(people, objects, trial) is not None:
            minimal = trial
    clue_texts = [f"{p} no tiene {o}." for (_, p, o) in minimal]
    return add_challenge(
        id=next_id(prefix), chamber=chamber, category="DEDUCTION", difficulty=difficulty,
        interaction="INVESTIGATE",
        prompt="Investiga las pistas y descubre qué objeto tiene cada explorador.",
        items=[],
        option_pool=[],
        solution=[],
        rule_type="DEDUCTION_GRID", rule_params={
            "people": ",".join(people),
            "objects": ",".join(objects),
            "clues": " ".join(clue_texts),
            "solution": ",".join(f"{p}:{secret[p]}" for p in people),
        },
        hints=make_hints(
            "Anota primero lo que cada pista DESCARTA, no lo que confirma.",
            "Cuando a alguien solo le quede una opción posible, esa es la suya.",
            "Resuelve primero a quien le queden menos opciones posibles.",
        ),
        explanation="Combinando todas las pistas negativas solo queda una asignación posible para cada explorador.",
    )


def mirror_construction_goal_ok(built, kind, params):
    if kind == "SHAPE_EVERY_N":
        n = int(params["n"])
        shape = params["shape"]
        if len(built) < n:
            return False
        return all((built[i]["shape"] == shape) == ((i + 1) % n == 0) for i in range(len(built)))
    if kind == "ALTERNATE_TWO_SHAPES":
        a, b = params["shapeA"], params["shapeB"]
        if len(built) < 4:
            return False
        return all(built[i]["shape"] == (a if i % 2 == 0 else b) for i in range(len(built)))
    if kind == "ASCENDING_SIZE_CYCLE":
        order = SIZES
        if len(built) < 3:
            return False
        return all(built[i]["size"] == order[i % 3] for i in range(len(built)))
    if kind == "COUNT_INCREASES_BY":
        step = int(params["step"])
        if len(built) < 3:
            return False
        return all(built[i + 1]["count"] - built[i]["count"] == step for i in range(len(built) - 1))
    raise ValueError(kind)


def gen_construction(difficulty, chamber="CONSTRUCTOR", prefix="CON"):
    options = [
        ("SHAPE_EVERY_N", {"shape": "STAR", "n": "3"},
         "Construye una tira donde aparezca una ESTRELLA cada 3 piezas.",
         [piece(shape="STAR"), piece(shape="TRIANGLE"), piece(shape="CIRCLE"), piece(shape="SQUARE"), piece(shape="HEXAGON")],
         [piece(shape="TRIANGLE"), piece(shape="CIRCLE"), piece(shape="STAR")] * 2),
        ("ALTERNATE_TWO_SHAPES", {"shapeA": "TRIANGLE", "shapeB": "CIRCLE"},
         "Construye una tira que alterne triángulo y círculo, empezando por triángulo.",
         [piece(shape="TRIANGLE"), piece(shape="CIRCLE"), piece(shape="SQUARE")],
         [piece(shape="TRIANGLE"), piece(shape="CIRCLE")] * 3),
        ("ASCENDING_SIZE_CYCLE", {},
         "Construye una tira donde el tamaño crezca en ciclos: pequeño, mediano, grande, y vuelta a empezar.",
         [piece(size="SMALL"), piece(size="MEDIUM"), piece(size="LARGE")],
         [piece(size="SMALL"), piece(size="MEDIUM"), piece(size="LARGE")] * 2),
        ("COUNT_INCREASES_BY", {"step": "2"},
         "Construye una tira donde la cantidad aumente de 2 en 2 en cada pieza.",
         [piece(count=1), piece(count=3), piece(count=5), piece(count=7)],
         [piece(count=1), piece(count=3), piece(count=5)]),
    ]
    kind, params, prompt, palette, example = random.choice(options)
    assert mirror_construction_goal_ok(example, kind, params), f"ejemplo no válido para {kind}"
    return add_challenge(
        id=next_id(prefix), chamber=chamber, category="CONSTRUCTION", difficulty=difficulty,
        interaction="BUILD",
        prompt=prompt,
        items=[],
        option_pool=palette,
        solution=[],
        rule_type=kind, rule_params=params,
        hints=make_hints(
            "No hay una única pieza correcta: construye toda la tira y luego revisa si cumple la condición.",
            "Cuenta las posiciones mientras colocas piezas para no perder el ritmo de la regla.",
            "Prueba con una tira corta primero y amplíala si funciona.",
        ),
        explanation="El taller comprueba la ESTRUCTURA completa que construiste, no una sola pieza.",
    )


def gen_entrance(idx):
    # Puerta de entrada: patrones muy simples de 2 símbolos, period 2, n=4 (spec sección 9).
    chosen = random.sample(SHAPES[:4], 2)
    base = [piece(shape=chosen[0]), piece(shape=chosen[1])]
    items = [dict(p) for p in (base * 2)]
    answer, det_period = mirror_next_in_cycle(items)
    assert det_period == 2 and answer == base[0]
    return add_challenge(
        id=next_id("ENT"), chamber="ENTRANCE", category="PATTERN", difficulty="INITIAL",
        interaction="DISCOVER",
        prompt="Una puerta de piedra bloquea el paso. Observa los símbolos: ¿cuál falta?",
        items=items + [BLANK],
        option_pool=[piece(shape=s) for s in SHAPES[:4]],
        solution=[answer],
        rule_type="PATTERN_CYCLE", rule_params={},
        hints=make_hints(
            "Observa antes de mover las piezas.",
            "Algo se repite aquí. ¿Puedes encontrarlo?",
            "Los símbolos se repiten de dos en dos.",
        ),
        explanation="La puerta repite el mismo par de símbolos una y otra vez.",
    )


# ------------------------------------------------------------ Orquestación -
DIFFS = ["INITIAL", "INTERMEDIATE", "ADVANCED"]

for i in range(3):
    gen_entrance(i)

# PATTERN: 18 (12 single-property repartidas + 6 multi-propiedad avanzadas)
for d in DIFFS:
    for _ in range(4):
        gen_pattern_single(d)
for _ in range(6):
    gen_pattern_multi("ADVANCED")

# SEQUENCE: 18
for d in DIFFS:
    for _ in range(6):
        gen_sequence(d)

# ANALOGY: 14
for d in DIFFS:
    for _ in range(4 if d != "ADVANCED" else 6):
        gen_analogy(d)

# CLASSIFICATION: 18
for d in DIFFS:
    for _ in range(6):
        gen_classification(d)

# MATRIX: 18
for d in DIFFS:
    for _ in range(6):
        gen_matrix(d)

# RELATION: 14
for d in DIFFS:
    for _ in range(4 if d != "ADVANCED" else 6):
        gen_relation(d)

# DEDUCTION: 14
for d in DIFFS:
    for _ in range(4 if d != "ADVANCED" else 6):
        gen_deduction(d)

# CONSTRUCTION: 8
for d in DIFFS:
    for _ in range(3 if d != "ADVANCED" else 2):
        gen_construction(d)

# MASTER: 10 desafíos avanzados mezclando las 8 categorías de contenido.
master_generators = [gen_pattern_single, gen_sequence, gen_analogy, gen_classification,
                      gen_matrix, gen_relation, gen_deduction, gen_construction]
random.shuffle(master_generators)
for i in range(10):
    gen = master_generators[i % len(master_generators)]
    gen("ADVANCED", chamber="MASTER", prefix="MST")

print(f"Total de desafíos generados: {len(challenges)}")
assert len(challenges) >= 130, "No se alcanzó el mínimo de 130 desafíos."

# Reparto de tipos de interacción: opción múltiple no debe superar el 50%.
option_select_count = sum(1 for c in challenges if c["interaction"] == "OPTION_SELECT")
print(f"OPTION_SELECT: {option_select_count} / {len(challenges)} ({option_select_count / len(challenges):.0%})")
assert option_select_count / len(challenges) < 0.5

by_category = {}
for c in challenges:
    by_category.setdefault(c["category"], 0)
    by_category[c["category"]] += 1
print("Distribución por categoría:", by_category)

by_chamber = {}
for c in challenges:
    by_chamber.setdefault(c["chamber"], 0)
    by_chamber[c["chamber"]] += 1
print("Distribución por cámara:", by_chamber)

# ------------------------------------------------------------ Emisión Kotlin
def kt_str(s):
    escaped = s.replace("\\", "\\\\").replace("\"", "\\\"").replace("$", "\\$")
    return f"\"{escaped}\""


def kt_piece_list(pieces):
    parts = ", ".join(f"PieceSpec.decode({kt_str(encode_piece(p))})" for p in pieces)
    return f"listOf({parts})"


def kt_params_map(params):
    if not params:
        return "emptyMap()"
    parts = ", ".join(f"{kt_str(k)} to {kt_str(v)}" for k, v in params.items())
    return f"mapOf({parts})"


def kt_hints(hints):
    parts = ", ".join(f"Hint({h['level']}, {kt_str(h['text'])})" for h in hints)
    return f"listOf({parts})"


def render_challenge(c):
    return f"""    Challenge(
        id = {kt_str(c['id'])},
        chamberId = ChamberId.{c['chamber']},
        category = LogicCategory.{c['category']},
        difficulty = DifficultyLevel.{c['difficulty']},
        interactionType = InteractionType.{c['interaction']},
        prompt = {kt_str(c['prompt'])},
        items = {kt_piece_list(c['items'])},
        optionPool = {kt_piece_list(c['option_pool'])},
        solutionPieces = {kt_piece_list(c['solution'])},
        rule = LogicRule({kt_str(c['rule_type'])}, {kt_params_map(c['rule_params'])}),
        hints = {kt_hints(c['hints'])},
        explanation = {kt_str(c['explanation'])},
        isSeed = true,
    ),"""


header = """package com.educalab.logicamate.seed

import com.educalab.logicamate.domain.model.Challenge
import com.educalab.logicamate.domain.model.ChamberId
import com.educalab.logicamate.domain.model.DifficultyLevel
import com.educalab.logicamate.domain.model.Hint
import com.educalab.logicamate.domain.model.InteractionType
import com.educalab.logicamate.domain.model.LogicCategory
import com.educalab.logicamate.domain.model.LogicRule
import com.educalab.logicamate.domain.model.PieceSpec

/**
 * Banco de contenido semilla de LogicaMate: %(total)d desafíos generados por
 * tools/generate_seed_content.py y AUTO-VERIFICADOS uno a uno en Python
 * contra una réplica exacta de la lógica de domain/engine/*.kt antes de
 * emitirse aquí (ver el propio script para el detalle de cada aserción).
 *
 * NO EDITAR A MANO: cualquier cambio debe hacerse en el generador y
 * volver a ejecutarlo, o se perderá la garantía de consistencia interna.
 *
 * Distribución por categoría: %(by_cat)s
 * Distribución por cámara: %(by_chamber)s
 */
object SeedContent {
    val all: List<Challenge> = listOf(
""" % {"total": len(challenges), "by_cat": by_category, "by_chamber": by_chamber}

footer = """    )
}
"""

body = "\n".join(render_challenge(c) for c in challenges)

with open(os.path.join(OUT_DIR, "SeedContent.kt"), "w", encoding="utf-8") as f:
    f.write(header + body + "\n" + footer)

print(f"SeedContent.kt escrito con {len(challenges)} desafíos en {OUT_DIR}")

# Export JSON plano — usado por database/sample_data.sql y por la documentación,
# para no tener que volver a ejecutar el generador Python para esas tareas.
import json
export_path = os.path.join(os.path.dirname(__file__), "seed_content_export.json")
with open(export_path, "w", encoding="utf-8") as f:
    json.dump(challenges, f, ensure_ascii=False, indent=1)
print(f"Export JSON escrito en {export_path}")


