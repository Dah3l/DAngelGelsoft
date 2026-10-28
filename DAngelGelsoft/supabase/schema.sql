-- ============================================================
-- D'Angel Gelsoft — Esquema Supabase (demo)
-- ============================================================
-- Tablas que alimentan la app. El panel web edita estas tablas y la
-- Edge Function `app-config` las sirve como UN solo JSON para la APK.
--
-- Ejecutar en el editor SQL de Supabase (o con la CLI de Supabase).
-- ============================================================

-- 1. Información del negocio (una sola fila, id = 1) ------------
create table if not exists public.business_info (
    id integer primary key,
    nombre text not null,
    eslogan text not null default '',
    descripcion text not null default '',
    ubicacion text not null default '',
    instagram text not null default '',
    telefono_reserva_1 text not null default '',
    telefono_reserva_2 text not null default ''
);

-- 2. Horarios ----------------------------------------------------
create table if not exists public.schedules (
    id bigint generated always as identity primary key,
    titulo text not null,
    dias text not null,
    apertura text not null,
    cierre text not null,
    nota text not null default '',
    orden integer not null default 0
);

-- 3. Precios / modalidades ---------------------------------------
create table if not exists public.prices (
    id bigint generated always as identity primary key,
    nombre text not null,
    descripcion text not null default '',
    precio_cup integer not null,
    unidad text not null default 'por persona',
    orden integer not null default 0
);

-- 4. Avisos y noches especiales ----------------------------------
create table if not exists public.announcements (
    id bigint generated always as identity primary key,
    titulo text not null,
    texto text not null,
    fecha text not null default '',
    activo boolean not null default true,
    orden integer not null default 0
);

-- ============================================================
-- Seguridad (RLS)
-- ============================================================
-- Lectura pública: la app lee el contenido sin login.
-- Escritura: EN ESTA DEMO el panel escribe con la anon key para no
-- obligar al administrador a crear cuentas. Eso es cómodo para la demo
-- pero NO es seguro para producción: antes de publicar, quita las
-- políticas de escritura para anon y usa Supabase Auth (solo el admin
-- autenticado puede insertar/actualizar/borrar).
-- ============================================================
alter table public.business_info enable row level security;
alter table public.schedules enable row level security;
alter table public.prices enable row level security;
alter table public.announcements enable row level security;

-- Lectura pública para las 4 tablas.
create policy "Lectura pública" on public.business_info
    for select using (true);
create policy "Lectura pública" on public.schedules
    for select using (true);
create policy "Lectura pública" on public.prices
    for select using (true);
create policy "Lectura pública" on public.announcements
    for select using (true);

-- Escritura solo para la demo (panel con anon key).
-- ¡PRODUCCIÓN: reemplazar por políticas con auth.uid() del admin!
create policy "Escritura demo" on public.business_info
    for all using (true) with check (true);
create policy "Escritura demo" on public.schedules
    for all using (true) with check (true);
create policy "Escritura demo" on public.prices
    for all using (true) with check (true);
create policy "Escritura demo" on public.announcements
    for all using (true) with check (true);
