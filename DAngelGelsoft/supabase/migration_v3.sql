-- D'Angel Gelsoft · Migración v3: precio por bloque + % de adelanto
-- Ejecutar en el SQL editor de Supabase (una sola vez).

-- Precio por persona (CUP) de cada bloque de reserva.
alter table bloques
  add column if not exists precio numeric default 0;

-- Porciento del total que se cobra por adelantado (ej. 50).
alter table business_info
  add column if not exists adelanto_pct integer default 50;
