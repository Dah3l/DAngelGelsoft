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

config = {
    "info": {
        "nombre": info.get("nombre") or "D'Angel Gelsoft",
        "eslogan": info.get("eslogan") or "Más que un juego, una misión",
        "descripcion": info.get("descripcion") or "",
        "ubicacion": info.get("ubicacion") or "",
        "instagram": info.get("instagram") or "",
        "telefono_reserva_1": info.get("telefono_reserva_1") or "",
        "telefono_reserva_2": info.get("telefono_reserva_2") or "",
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
            "titulo": a.get("titulo") or "",
            "texto": a.get("texto") or "",
            "fecha": a.get("fecha") or "",
        }
        for a in announcements
    ],
    "nota_demo": "PRECIOS Y MODALIDADES DE PRUEBA. El administrador los cambia en el panel web.",
}

os.makedirs("docs", exist_ok=True)
with open(OUT, "w", encoding="utf-8") as f:
    json.dump(config, f, ensure_ascii=False, indent=2)
    f.write("\n")

print(f"OK: {OUT} ({len(schedules)} horarios, {len(prices)} precios, {len(announcements)} anuncios)")
