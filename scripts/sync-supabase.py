#!/usr/bin/env python3
"""Lee las 4 tablas de Supabase (REST, anon key) y genera docs/app-config.json
con el formato exacto que la APK espera (claves en español).
Sin dependencias: solo stdlib."""

import json
import os
import urllib.request

BASE = os.environ["SUPABASE_URL"].rstrip("/")
KEY = os.environ["SUPABASE_ANON_KEY"]
OUT = "docs/app-config.json"


def get(path):
    req = urllib.request.Request(
        BASE + path,
        headers={"apikey": KEY, "Authorization": f"Bearer {KEY}"},
    )
    with urllib.request.urlopen(req, timeout=30) as res:
        return json.loads(res.read().decode("utf-8"))


info_rows = get("/rest/v1/business_info?id=eq.1")
info = info_rows[0] if info_rows else {}
schedules = get("/rest/v1/schedules?order=orden.asc")
prices = get("/rest/v1/prices?order=orden.asc")
announcements = get("/rest/v1/announcements?activo=eq.true&order=orden.asc")
sections = get("/rest/v1/gallery_sections?order=orden.asc")
photos = get("/rest/v1/gallery_photos?order=orden.asc")
bloques = get("/rest/v1/bloques?order=orden.asc")

photos_by_section = {}
for ph in photos:
    photos_by_section.setdefault(ph.get("section_id"), []).append(ph)

config = {
    "info": {
        "nombre": info.get("nombre") or "D'Angel Gelsoft",
        "eslogan": info.get("eslogan") or "Más que un juego, una misión",
        "descripcion": info.get("descripcion") or "",
        "ubicacion": info.get("ubicacion") or "",
        "instagram": info.get("instagram") or "",
        "telefono_reserva_1": info.get("telefono_reserva_1") or "",
        "telefono_reserva_2": info.get("telefono_reserva_2") or "",
        "sitio_web": info.get("sitio_web") or "",
        "correo": info.get("correo") or "",
    },
    "horarios": [
        {
            "titulo": s.get("titulo") or "",
            "dias": s.get("dias") or "",
            "apertura": s.get("apertura") or "",
            "cierre": s.get("cierre") or "",
            "nota": s.get("nota") or "",
        }
        for s in schedules
    ],
    "precios": [
        {
            "nombre": p.get("nombre") or "",
            "descripcion": p.get("descripcion") or "",
            "precio_cup": p.get("precio_cup") or 0,
            "unidad": p.get("unidad") or "por persona",
        }
        for p in prices
    ],
    "anuncios": [
        {
            "id": a.get("id") or 0,
            "titulo": a.get("titulo") or "",
            "texto": a.get("texto") or "",
            "fecha": a.get("fecha") or "",
        }
        for a in announcements
    ],
    "galeria": [
        {
            "id": s.get("id") or 0,
            "nombre": s.get("nombre") or "",
            "fotos": [
                {
                    "id": ph.get("id") or 0,
                    "url": ph.get("url") or "",
                    "titulo": ph.get("titulo") or "",
                }
                for ph in photos_by_section.get(s.get("id"), [])
            ],
        }
        for s in sections
    ],
    "bloques": [
        {
            "id": b.get("id") or 0,
            "nombre": b.get("nombre") or "",
            "hora_inicio": b.get("hora_inicio") or "",
            "hora_fin": b.get("hora_fin") or "",
            "dias": b.get("dias") or "1,2,3,4,5,6",
        }
        for b in bloques
    ],
    "nota_demo": "PRECIOS Y MODALIDADES DE PRUEBA. El administrador los cambia en el panel web.",
}

os.makedirs("docs", exist_ok=True)
with open(OUT, "w", encoding="utf-8") as f:
    json.dump(config, f, ensure_ascii=False, indent=2)
    f.write("\n")

print(f"OK: {OUT} ({len(schedules)} horarios, {len(prices)} precios, {len(announcements)} anuncios, {len(sections)} secciones, {len(bloques)} bloques)")
