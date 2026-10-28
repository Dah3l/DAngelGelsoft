// ============================================================
// D'Angel Gelsoft — Edge Function `app-config`
// ============================================================
// Sirve UN solo JSON con todo lo que la app necesita:
//   { info, horarios, precios, anuncios }
//
// Despliegue (CLI de Supabase, dentro de la carpeta del proyecto):
//   supabase functions deploy app-config
//
// La función usa SUPABASE_URL y SUPABASE_ANON_KEY, variables que
// Supabase ya pone en el entorno de las Edge Functions. Como las
// tablas tienen lectura pública, la anon key basta.
// ============================================================

import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers":
    "authorization, x-client-info, apikey, content-type",
};

Deno.serve(async (req) => {
  // Respuesta a la comprobación CORS del navegador.
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const supabase = createClient(
      Deno.env.get("SUPABASE_URL")!,
      Deno.env.get("SUPABASE_ANON_KEY")!,
    );

    // Las 4 lecturas en paralelo; cada tabla ya viene ordenada.
    const [infoRes, horariosRes, preciosRes, anunciosRes] = await Promise.all([
      supabase.from("business_info").select("*").eq("id", 1).maybeSingle(),
      supabase.from("schedules").select("*").order("orden", { ascending: true }),
      supabase.from("prices").select("*").order("orden", { ascending: true }),
      supabase
        .from("announcements")
        .select("*")
        .eq("activo", true)
        .order("orden", { ascending: true }),
    ]);

    const error =
      infoRes.error || horariosRes.error || preciosRes.error || anunciosRes.error;
    if (error) throw error;

    const info = infoRes.data ?? {};
    const json = {
      info: {
        nombre: info.nombre ?? "D'Angel Gelsoft",
        eslogan: info.eslogan ?? "Más que un juego, una misión",
        descripcion: info.descripcion ?? "",
        ubicacion: info.ubicacion ?? "",
        instagram: info.instagram ?? "",
        telefono_reserva_1: info.telefono_reserva_1 ?? "",
        telefono_reserva_2: info.telefono_reserva_2 ?? "",
      },
      horarios: (horariosRes.data ?? []).map((s) => ({
        titulo: s.titulo,
        dias: s.dias,
        apertura: s.apertura,
        cierre: s.cierre,
        nota: s.nota ?? "",
      })),
      precios: (preciosRes.data ?? []).map((p) => ({
        nombre: p.nombre,
        descripcion: p.descripcion ?? "",
        precio_cup: p.precio_cup,
        unidad: p.unidad ?? "por persona",
      })),
      anuncios: (anunciosRes.data ?? []).map((a) => ({
        titulo: a.titulo,
        texto: a.texto,
        fecha: a.fecha ?? "",
      })),
      // La app ignora este campo; existe para dejar claro que los
      // precios de la demo son ejemplos que el admin cambia en el panel.
      nota_demo:
        "PRECIOS Y MODALIDADES DE PRUEBA. El administrador los cambia en el panel web.",
    };

    return new Response(JSON.stringify(json), {
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  } catch (e) {
    return new Response(JSON.stringify({ error: String(e) }), {
      status: 500,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  }
});
