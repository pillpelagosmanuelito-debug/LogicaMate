#!/usr/bin/env python3
"""
Convierte los .md de docs/ a PDF reales usando reportlab (Platypus), sin
pasar por HTML/CSS intermedio -- evita el bug de "CSS sin envolver en <style>
se filtraba como texto visible" que se detectó en el proyecto hermano
GraficosDivertidos. Soporta: encabezados #/##/###, listas con - o *, tablas
pipe simples, negrita **texto**, código en linea `texto`, y bloques de codigo
```...``` (se muestran en fuente monoespaciada).
"""
import os
import re
import sys

from reportlab.lib.pagesizes import LETTER
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import inch
from reportlab.lib import colors
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, ListFlowable,
    ListItem, PageBreak, HRFlowable,
)

STONE = colors.HexColor("#1B1330")
GOLD = colors.HexColor("#B8860B")
TEXT = colors.HexColor("#221933")

styles = getSampleStyleSheet()
styles.add(ParagraphStyle(name="H1Custom", parent=styles["Heading1"], textColor=STONE, spaceAfter=14, spaceBefore=6))
styles.add(ParagraphStyle(name="H2Custom", parent=styles["Heading2"], textColor=STONE, spaceAfter=10, spaceBefore=14))
styles.add(ParagraphStyle(name="H3Custom", parent=styles["Heading3"], textColor=GOLD, spaceAfter=8, spaceBefore=10))
styles.add(ParagraphStyle(name="BodyCustom", parent=styles["BodyText"], textColor=TEXT, fontSize=10, leading=14, spaceAfter=6))
styles.add(ParagraphStyle(name="CodeCustom", parent=styles["Code"], fontSize=8, leading=11, backColor=colors.HexColor("#F2F0F7")))
styles.add(ParagraphStyle(name="TitlePage", parent=styles["Title"], textColor=STONE, fontSize=26, spaceAfter=6))
styles.add(ParagraphStyle(name="Subtitle", parent=styles["Normal"], textColor=GOLD, fontSize=13, spaceAfter=30))


def inline_md(text):
    text = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    text = re.sub(r"\*\*(.+?)\*\*", r"<b>\1</b>", text)
    text = re.sub(r"`([^`]+?)`", r'<font face="Courier" size="9">\1</font>', text)
    text = re.sub(r"\[([^\]]+)\]\(([^)]+)\)", r"\1", text)  # links -> texto plano
    return text


def parse_table(lines):
    rows = []
    for line in lines:
        if re.match(r"^\s*\|?\s*:?-+:?\s*(\|\s*:?-+:?\s*)+\|?\s*$", line):
            continue
        cells = [c.strip() for c in line.strip().strip("|").split("|")]
        rows.append([Paragraph(inline_md(c), styles["BodyCustom"]) for c in cells])
    return rows


def md_to_flowables(md_text):
    flow = []
    lines = md_text.split("\n")
    i = 0
    list_buffer = []

    def flush_list():
        nonlocal list_buffer
        if list_buffer:
            items = [ListItem(Paragraph(inline_md(t), styles["BodyCustom"])) for t in list_buffer]
            flow.append(ListFlowable(items, bulletType="bullet", leftIndent=18))
            flow.append(Spacer(1, 6))
            list_buffer = []

    while i < len(lines):
        line = lines[i].rstrip()

        if line.startswith("```"):
            flush_list()
            code_lines = []
            i += 1
            while i < len(lines) and not lines[i].startswith("```"):
                code_lines.append(lines[i])
                i += 1
            code_text = "\n".join(code_lines).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            flow.append(Paragraph(code_text.replace("\n", "<br/>"), styles["CodeCustom"]))
            flow.append(Spacer(1, 8))
            i += 1
            continue

        if line.strip().startswith("|"):
            flush_list()
            table_lines = []
            while i < len(lines) and lines[i].strip().startswith("|"):
                table_lines.append(lines[i])
                i += 1
            rows = parse_table(table_lines)
            if rows:
                t = Table(rows, hAlign="LEFT", repeatRows=1)
                t.setStyle(TableStyle([
                    ("BACKGROUND", (0, 0), (-1, 0), STONE),
                    ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
                    ("GRID", (0, 0), (-1, -1), 0.5, colors.HexColor("#CCC5E0")),
                    ("VALIGN", (0, 0), (-1, -1), "TOP"),
                    ("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, colors.HexColor("#F7F5FB")]),
                    ("LEFTPADDING", (0, 0), (-1, -1), 6),
                    ("RIGHTPADDING", (0, 0), (-1, -1), 6),
                    ("TOPPADDING", (0, 0), (-1, -1), 4),
                    ("BOTTOMPADDING", (0, 0), (-1, -1), 4),
                ]))
                flow.append(t)
                flow.append(Spacer(1, 10))
            continue

        if line.startswith("### "):
            flush_list(); flow.append(Paragraph(inline_md(line[4:]), styles["H3Custom"])); i += 1; continue
        if line.startswith("## "):
            flush_list(); flow.append(Paragraph(inline_md(line[3:]), styles["H2Custom"])); i += 1; continue
        if line.startswith("# "):
            flush_list(); flow.append(Paragraph(inline_md(line[2:]), styles["H1Custom"])); i += 1; continue

        if re.match(r"^\s*[-*]\s+", line):
            list_buffer.append(re.sub(r"^\s*[-*]\s+", "", line))
            i += 1
            continue

        if line.strip() in ("---", "***", "___"):
            flush_list()
            flow.append(Spacer(1, 4))
            flow.append(HRFlowable(width="100%", color=colors.HexColor("#CCC5E0")))
            flow.append(Spacer(1, 8))
            i += 1
            continue

        if line.strip() == "":
            flush_list()
            i += 1
            continue

        flush_list()
        flow.append(Paragraph(inline_md(line), styles["BodyCustom"]))
        i += 1

    flush_list()
    return flow


def convert(md_path, pdf_path, title, subtitle):
    with open(md_path, "r", encoding="utf-8") as f:
        md_text = f.read()
    # Quita el primer H1 del cuerpo (ya lo ponemos como portada) si coincide.
    body_lines = md_text.split("\n")
    if body_lines and body_lines[0].startswith("# "):
        body_lines = body_lines[1:]
    body_md = "\n".join(body_lines)

    doc = SimpleDocTemplate(
        pdf_path, pagesize=LETTER,
        leftMargin=0.85 * inch, rightMargin=0.85 * inch, topMargin=0.9 * inch, bottomMargin=0.8 * inch,
        title=title,
    )
    story = [Paragraph(title, styles["TitlePage"]), Paragraph(subtitle, styles["Subtitle"])]
    story += md_to_flowables(body_md)
    doc.build(story)
    print(f"OK: {pdf_path}")


if __name__ == "__main__":
    base = os.path.join(os.path.dirname(__file__), "..")
    docs = os.path.join(base, "docs")
    pdf_dir = os.path.join(docs, "pdf")
    os.makedirs(pdf_dir, exist_ok=True)

    convert(os.path.join(docs, "MEMORIA_DESCRIPTIVA.md"), os.path.join(pdf_dir, "MEMORIA_DESCRIPTIVA.pdf"),
            "LogicaMate v1.0.0", "Memoria Descriptiva")
    convert(os.path.join(docs, "MANUAL_USUARIO.md"), os.path.join(pdf_dir, "MANUAL_USUARIO.pdf"),
            "LogicaMate", "Manual de Usuario")
    convert(os.path.join(docs, "MANUAL_TECNICO.md"), os.path.join(pdf_dir, "MANUAL_TECNICO.pdf"),
            "LogicaMate", "Manual Técnico")
    convert(os.path.join(docs, "BASE_DE_DATOS.md"), os.path.join(pdf_dir, "BASE_DE_DATOS.pdf"),
            "LogicaMate", "Base de Datos")
