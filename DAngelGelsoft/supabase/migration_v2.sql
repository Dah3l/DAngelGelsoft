-- ============================================================
-- D'Angel Gelsoft — Migración v2 (rediseño app)
-- ============================================================
-- Ejecutar en el editor SQL de Supabase DESPUÉS de schema.sql.
-- Añade: contacto extendido, galería con secciones, bloques de
-- reserva, tabla de reservas y vista pública de disponibilidad.
-- El bucket `galeria` se crea en el dashboard (Storage → New bucket,
-- público). Las fotos de prueba iniciales las sube el admin desde el
-- panel web (pestaña Galería).
-- ============================================================

-- 1. Contacto extendido -----------------------------------------
alter table public.business_info
    add column if not exists sitio_web text not null default '',
    add column if not exists correo text not null default '';

-- 2. Galería: secciones ------------------------------------------
create table if not exists public.gallery_sections (
    id bigint generated always as identity primary key,
    nombre text not null,
    orden bigint not null default 0
);

-- 3. Galería: fotos ----------------------------------------------
create table if not exists public.gallery_photos (
    id bigint generated always as identity primary key,
    section_id bigint not null references public.gallery_sections(id) on delete cascade,
    url text not null,
    titulo text not null default '',
    orden bigint not null default 0
);
create index if not exists gallery_photos_section_idx
    on public.gallery_photos (section_id, orden);

-- Secciones iniciales de prueba (el admin las edita en el panel).
insert into public.gallery_sections (nombre, orden) values
    ('Armas', 1),
    ('Eventos', 2),
    ('Clientes', 3)
on conflict do nothing;

-- 4. Bloques de reserva -------------------------------------------
-- dias: 1=Lun 2=Mar 3=Mié 4=Jue 5=Vie 6=Sáb 7=Dom (separados por coma)
create table if not exists public.bloques (
    id bigint generated always as identity primary key,
    nombre text not null,
    hora_inicio text not null,
    hora_fin text not null,
    dias text not null default '1,2,3,4,5,6',
    orden bigint not null default 0
);

-- Bloques iniciales de prueba (el admin los edita en el panel).
insert into public.bloques (nombre, hora_inicio, hora_fin, dias, orden) values
    ('Mañana', '10:00 AM', '1:00 PM', '1,2,3,4,5,6', 1),
    ('Tarde', '2:00 PM', '5:00 PM', '1,2,3,4,5,6', 2),
    ('Noche', '8:00 PM', '10:00 PM', '5,6', 3)
on conflict do nothing;

-- 5. Reservas ------------------------------------------------------
-- estado: 'pendiente' | 'confirmada' | 'rechazada'
create table if not exists public.reservas (
    id bigint generated always as identity primary key,
    bloque_id bigint not null references public.bloques(id) on delete cascade,
    fecha date not null,
    nombre text not null,
    telefono text not null,
    personas integer not null default 1,
    estado text not null default 'pendiente',
    creado_en timestamptz not null default now()
);

-- Candado anti doble-reserva: un mismo día + bloque solo puede tener
-- UNA reserva no rechazada. Si dos llegan al mismo segundo, la segunda
-- falla y la app le pide al usuario elegir otro bloque.
create unique index if not exists reservas_unicas
    on public.reservas (fecha, bloque_id)
    where estado != 'rechazada';

-- 6. Vista pública de disponibilidad --------------------------------
-- La app consulta SOLO esta vista (fecha/bloque/estado): nunca expone
-- nombres ni teléfonos de otros clientes.
create or replace view public.reservation_slots as
    select fecha, bloque_id, estado from public.reservas;

-- ============================================================
-- Seguridad (RLS) — demo: escritura con anon key, como el resto.
-- ============================================================
alter table public.gallery_sections enable row level security;
alter table public.gallery_photos enable row level security;
alter table public.bloques enable row level security;
alter table public.reservas enable row level security;

-- Lectura pública de catálogos.
create policy "lectura publica" on public.gallery_sections
    for select using (true);
create policy "lectura publica" on public.gallery_photos
    for select using (true);
create policy "lectura publica" on public.bloques
    for select using (true);

-- Escritura demo (panel): igual que las tablas v1.
create policy "escritura demo" on public.gallery_sections
    for all using (true) with check (true);
create policy "escritura demo" on public.gallery_photos
    for all using (true) with check (true);
create policy "escritura demo" on public.bloques
    for all using (true) with check (true);

-- Reservas: la app inserta con anon; el panel gestiona con anon (demo).
-- Nadie lee la tabla completa con anon: la disponibilidad sale de la vista.
create policy "gestion demo" on public.reservas
    for all using (true) with check (true);

-- La vista la lee anon sin exponer datos personales.
grant select on public.reservation_slots to anon;

-- Bucket `galeria` (crearlo en el dashboard como público): el panel
-- sube fotos con la anon key (demo).
create policy "subida demo" on storage.objects
    for insert with check (bucket_id = 'galeria');
