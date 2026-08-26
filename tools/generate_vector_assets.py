#!/usr/bin/env python3
"""
Genera los vector drawables (Android XML, 24x24 viewport salvo indicado) usados
en toda la app LogicaMate: iconos de cámara, insignias, fragmentos de la Llave
Lógica y decoraciones. Son 100% locales (sin red, sin PNG externos), consistentes
con la prioridad #2 de recursos visuales del prompt maestro (SVG/vector drawables).

Uso: python3 generate_vector_assets.py
Escribe en ../app/src/main/res/drawable/
"""
import math
import os

OUT = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "res", "drawable")
os.makedirs(OUT, exist_ok=True)

GOLD = "#F2B705"
CRYSTAL = "#3ADBC6"
STONE = "#8B7CC9"
CORAL = "#F26B4D"
WHITE = "#FFFFFF"


def circle_path(cx, cy, r):
    # Two arcs make a full circle in vector pathData.
    return (f"M {cx-r},{cy} "
            f"A {r},{r} 0 1,1 {cx+r},{cy} "
            f"A {r},{r} 0 1,1 {cx-r},{cy} Z")


def poly_path(points):
    pts = " L ".join(f"{x},{y}" for x, y in points)
    return f"M {pts} Z"


def vector_xml(paths, viewport=24, size=24):
    """paths: list of (pathData, fillColor, strokeColor_or_None, strokeWidth)"""
    body = []
    for pd, fill, stroke, sw in paths:
        stroke_attrs = ""
        if stroke:
            stroke_attrs = f' android:strokeColor="{stroke}" android:strokeWidth="{sw}" android:strokeLineCap="round" android:strokeLineJoin="round"'
        fill_attr = f' android:fillColor="{fill}"' if fill else ""
        body.append(f'    <path android:pathData="{pd}"{fill_attr}{stroke_attrs} />')
    return (
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
        f'    android:width="{size}dp"\n'
        f'    android:height="{size}dp"\n'
        f'    android:viewportWidth="{viewport}"\n'
        f'    android:viewportHeight="{viewport}">\n'
        + "\n".join(body) +
        '\n</vector>\n'
    )


def write(name, content):
    with open(os.path.join(OUT, f"{name}.xml"), "w") as f:
        f.write(content)
    print("wrote", name)


# ---------------------------------------------------------------- Chambers --
# ic_chamber_entrance: puerta de piedra con arco
write("ic_chamber_entrance", vector_xml([
    (poly_path([(4, 21), (4, 10), (12, 3), (20, 10), (20, 21)]), STONE, None, 0),
    (poly_path([(7, 21), (7, 12), (12, 7.5), (17, 12), (17, 21)]), GOLD, None, 0),
    (circle_path(12, 16, 1.4), STONE, None, 0),
]))

# ic_chamber_patterns: mosaico de piezas repetidas
write("ic_chamber_patterns", vector_xml([
    (circle_path(6, 6, 3), GOLD, None, 0),
    (poly_path([(15, 3), (21, 3), (21, 9), (15, 9)]), CRYSTAL, None, 0),
    (poly_path([(6, 15), (9, 21), (3, 21)]), CORAL, None, 0),
    (circle_path(18, 18, 3), STONE, None, 0),
]))

# ic_chamber_sequences: pasadizo de casillas con flecha
write("ic_chamber_sequences", vector_xml([
    (poly_path([(2, 10), (2, 14), (6, 14), (6, 10)]), CRYSTAL, None, 0),
    (poly_path([(9, 10), (9, 14), (13, 14), (13, 10)]), GOLD, None, 0),
    (poly_path([(16, 9), (16, 15), (21, 12)]), CORAL, None, 0),
]))

# ic_chamber_analogies: dos flechas de transformacion
write("ic_chamber_analogies", vector_xml([
    (circle_path(5, 6, 2.6), GOLD, None, 0),
    ("M 8,6 L 15,6", None, CRYSTAL, 1.6),
    (poly_path([(14, 4.3), (17, 6), (14, 7.7)]), CRYSTAL, None, 0),
    (poly_path([(3.6, 15.6), (6.4, 15.6), (6.4, 18.4), (3.6, 18.4)]), STONE, None, 0),
    (poly_path([(8.6, 14.6), (8.6, 19.4), (11.4, 19.4), (11.4, 17.6), (13, 17.6), (13, 16.4), (11.4, 16.4), (11.4, 14.6)]), CORAL, None, 0),
]))

# ic_chamber_classification: embudo con formas
write("ic_chamber_classification", vector_xml([
    (poly_path([(3, 4), (21, 4), (13.5, 13), (13.5, 20), (10.5, 20), (10.5, 13)]), STONE, None, 0),
    (circle_path(7, 3.2, 1.3), GOLD, None, 0),
    (circle_path(17, 3.2, 1.3), CRYSTAL, None, 0),
]))

# ic_chamber_matrices: cuadricula 2x2
write("ic_chamber_matrices", vector_xml([
    (poly_path([(3, 3), (11, 3), (11, 11), (3, 11)]), GOLD, None, 0),
    (poly_path([(13, 3), (21, 3), (21, 11), (13, 11)]), CRYSTAL, None, 0),
    (poly_path([(3, 13), (11, 13), (11, 21), (3, 21)]), CORAL, None, 0),
    (poly_path([(13, 13), (21, 13), (21, 21), (13, 21)]), STONE, None, 0),
]))

# ic_chamber_relations: nodos conectados
write("ic_chamber_relations", vector_xml([
    ("M 4,6 L 12,18 M 12,18 L 20,6 M 4,6 L 20,6", None, STONE, 1.4),
    (circle_path(4, 6, 2.2), GOLD, None, 0),
    (circle_path(20, 6, 2.2), CRYSTAL, None, 0),
    (circle_path(12, 18, 2.2), CORAL, None, 0),
]))

# ic_chamber_deduction: lupa
write("ic_chamber_deduction", vector_xml([
    (circle_path(10, 10, 6.4), None, GOLD, 2.0),
    ("M 14.6,14.6 L 21,21", None, GOLD, 2.2),
]))

# ic_chamber_constructor: martillo + engranaje
write("ic_chamber_constructor", vector_xml([
    (poly_path([(3, 15), (9, 9), (11, 11), (5, 17)]), STONE, None, 0),
    (poly_path([(9, 9), (13, 5), (17, 3), (19, 5), (17, 9), (13, 13)]), CORAL, None, 0),
    (circle_path(18, 18, 3), GOLD, None, 0),
]))

# ic_chamber_master: gran puerta con estrella
write("ic_chamber_master", vector_xml([
    (poly_path([(3, 21), (3, 9), (12, 2), (21, 9), (21, 21)]), STONE, None, 0),
    (poly_path([
        (12, 8), (13.2, 11), (16.5, 11.2), (14, 13.3), (14.8, 16.5),
        (12, 14.7), (9.2, 16.5), (10, 13.3), (7.5, 11.2), (10.8, 11)
    ]), GOLD, None, 0),
]))

# ---------------------------------------------------------------- Misc UI ---
# ic_key_fragment: fragmento de llave (usado x4 con distinto color via tint)
write("ic_key_fragment", vector_xml([
    (circle_path(8, 8, 5), GOLD, None, 0),
    (circle_path(8, 8, 2), STONE, None, 0),
    (poly_path([(11.5, 11.5), (20, 20), (20, 22), (18, 22), (18, 20), (16, 20), (16, 18), (14, 18), (14, 16), (11.5, 13.5)]), GOLD, None, 0),
]))

# ic_xp_star
write("ic_xp_star", vector_xml([
    (poly_path([
        (12, 2), (14.7, 8.6), (22, 9.3), (16.5, 14), (18.2, 21.2),
        (12, 17.3), (5.8, 21.2), (7.5, 14), (2, 9.3), (9.3, 8.6)
    ]), GOLD, None, 0),
]))

# ic_streak_flame
write("ic_streak_flame", vector_xml([
    (poly_path([
        (12, 2), (16, 9), (14, 9), (17, 14), (14.5, 13.2), (16, 22),
        (8, 22), (9.5, 16), (7, 17.5), (9, 12), (7, 12.5), (11, 5)
    ]), CORAL, None, 0),
]))

# ic_mascot_explorer: pequeño explorador logico (cabeza redonda + antena de cristal)
write("ic_mascot_explorer", vector_xml([
    (circle_path(12, 13, 7), STONE, None, 0),
    (circle_path(9, 12, 1.4), WHITE, None, 0),
    (circle_path(15, 12, 1.4), WHITE, None, 0),
    ("M 9,16 Q 12,18.4 15,16", None, WHITE, 1.2),
    ("M 12,6 L 12,3", None, CRYSTAL, 1.4),
    (circle_path(12, 2.4, 1.2), CRYSTAL, None, 0),
]))

# ic_lock_state: candado (estado bloqueado de camara)
write("ic_lock_state", vector_xml([
    (poly_path([(6, 11), (18, 11), (18, 21), (6, 21)]), STONE, None, 0),
    ("M 8,11 L 8,7 A 4,4 0 0 1 16,7 L 16,11", None, GOLD, 1.8),
]))

# ic_check_complete: check de completado
write("ic_check_complete", vector_xml([
    (circle_path(12, 12, 10), CRYSTAL, None, 0),
    ("M 7,12.5 L 10.5,16 L 17,8.5", None, WHITE, 2.0),
]))

# ic_mastered_gem: gema (estado dominado)
write("ic_mastered_gem", vector_xml([
    (poly_path([(12, 2), (19, 9), (15, 22), (9, 22), (5, 9)]), CRYSTAL, None, 0),
    (poly_path([(12, 2), (19, 9), (12, 12), (5, 9)]), GOLD, None, 0),
]))

# ------------------------------------------------------------- Insignias ---
# Cada insignia usa una silueta distinta (no solo color) sobre un medallon comun.
def medal(shape_paths, ring_color=GOLD):
    return vector_xml([
        (circle_path(12, 12, 11), ring_color, None, 0),
        (circle_path(12, 12, 8.7), "#1B1330", None, 0),
    ] + shape_paths)

write("ic_badge_primer_mecanismo", medal([
    (poly_path([(8, 15), (8, 9), (12, 6), (16, 9), (16, 15)]), CRYSTAL, None, 0),
]))
write("ic_badge_cazador_patrones", medal([
    (circle_path(9, 9, 2), CORAL, None, 0),
    (circle_path(15, 9, 2), GOLD, None, 0),
    (circle_path(9, 15, 2), GOLD, None, 0),
    (circle_path(15, 15, 2), CORAL, None, 0),
]))
write("ic_badge_maestro_secuencias", medal([
    ("M 6,16 L 10,10 L 14,13 L 18,7", None, CRYSTAL, 1.8),
]))
write("ic_badge_detective_relaciones", medal([
    (circle_path(9, 9, 5.6), None, GOLD, 1.8),
    ("M 13,13 L 17,17", None, GOLD, 2.0),
]))
write("ic_badge_constructor_logico", medal([
    (poly_path([(6, 16), (11, 8), (13, 9.2), (8, 17.2)]), CORAL, None, 0),
    (poly_path([(12, 9), (15, 6), (18, 7), (17, 10)]), GOLD, None, 0),
]))
write("ic_badge_explorador_matrices", medal([
    (poly_path([(6, 6), (11, 6), (11, 11), (6, 11)]), GOLD, None, 0),
    (poly_path([(13, 13), (18, 13), (18, 18), (13, 18)]), CRYSTAL, None, 0),
]))
write("ic_badge_gran_deducidor", medal([
    (poly_path([(12, 6), (14, 11), (12, 11), (12, 15)]), GOLD, None, 0),
    (circle_path(12, 17.5, 1.1), GOLD, None, 0),
]))
write("ic_badge_maestro_templo", medal([
    (poly_path([
        (12, 6), (13.4, 9.7), (17.4, 10), (14.3, 12.5), (15.3, 16.4),
        (12, 14.2), (8.7, 16.4), (9.7, 12.5), (6.6, 10), (10.6, 9.7)
    ]), GOLD, None, 0),
], ring_color=CRYSTAL))

# --------------------------------------------------------- Coleccionables --
write("ic_collectible_cristal", vector_xml([
    (poly_path([(12, 2), (18, 9), (14, 22), (10, 22), (6, 9)]), CRYSTAL, None, 0),
]))
write("ic_collectible_engranaje", vector_xml([
    (circle_path(12, 12, 4), STONE, None, 0),
    (poly_path([(11, 2), (13, 2), (13, 6), (11, 6)]), STONE, None, 0),
    (poly_path([(11, 18), (13, 18), (13, 22), (11, 22)]), STONE, None, 0),
    (poly_path([(2, 11), (6, 11), (6, 13), (2, 13)]), STONE, None, 0),
    (poly_path([(18, 11), (22, 11), (22, 13), (18, 13)]), STONE, None, 0),
]))
write("ic_collectible_placa", vector_xml([
    (poly_path([(4, 5), (20, 5), (20, 19), (4, 19)]), STONE, None, 0),
    (poly_path([(7, 9), (17, 9), (17, 11), (7, 11)]), GOLD, None, 0),
    (poly_path([(7, 13), (13, 13), (13, 15), (7, 15)]), GOLD, None, 0),
]))
write("ic_collectible_herramienta", vector_xml([
    ("M 4,20 L 12,12", None, STONE, 2.2),
    (circle_path(15.5, 8.5, 4), CORAL, None, 0),
    (circle_path(15.5, 8.5, 1.6), "#1B1330", None, 0),
]))

print("Vector assets generated:", len(os.listdir(OUT)))
