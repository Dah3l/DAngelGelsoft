-- ============================================================
-- D'Angel Gelsoft — Datos de prueba (seed)
-- ============================================================
-- ¡ATENCIÓN! Los precios, modalidades, horarios nocturnos y avisos de
-- abajo son DATOS DE EJEMPLO para la demo. No son los valores reales
-- del negocio: el administrador los cambia él mismo en el panel web
-- y la app los muestra al instante, sin tocar código.
-- ============================================================

-- Información del negocio (datos reales del cliente).
insert into public.business_info
    (id, nombre, eslogan, descripcion, ubicacion, instagram,
     telefono_reserva_1, telefono_reserva_2)
values (
    1,
    'D''Angel Gelsoft',
    'Más que un juego, una misión',
    'El primer campo de gelsoft en Cuba. Ven a vivir la experiencia: escenarios reales, equipo completo y partidas para todos, de día y de noche.',
    'Acuario Nacional, La Habana',
    'd_angel_gelsoft',
    '+53 54753951',
    '+53 58878734'
)
on conflict (id) do update set
    nombre = excluded.nombre,
    eslogan = excluded.eslogan,
    descripcion = excluded.descripcion,
    ubicacion = excluded.ubicacion,
    instagram = excluded.instagram,
    telefono_reserva_1 = excluded.telefono_reserva_1,
    telefono_reserva_2 = excluded.telefono_reserva_2;

-- Horarios (el horario regular es real; el nocturno es de ejemplo).
insert into public.schedules (titulo, dias, apertura, cierre, nota, orden)
values
    ('Horario regular', 'Lunes a domingo', '10:00 AM', '10:00 PM',
     'Abierto todos los días.', 1),
    ('Experiencia nocturna', 'Viernes y sábado', '8:00 PM', '10:00 PM',
     'Sesión especial nocturna. Reserva con antelación por WhatsApp.', 2);

-- Precios y modalidades — TODOS DE PRUEBA, el admin los edita.
insert into public.prices (nombre, descripcion, precio_cup, unidad, orden)
values
    ('Sesión de día',
     'Partida diurna en el campo, incluye equipo y protección.',
     500, 'por persona', 1),
    ('Experiencia nocturna',
     'Sesión especial nocturna con escenarios iluminados.',
     800, 'por persona', 2),
    ('Pack grupo (5 o más)',
     'Precio por persona para grupos de 5 o más jugadores.',
     400, 'por persona', 3);

-- Avisos y noches especiales — DE PRUEBA.
insert into public.announcements (titulo, texto, fecha, activo, orden)
values
    ('Noche especial de gelsoft',
     'Este viernes, sesión nocturna con escenarios iluminados. Reserva tu puesto por WhatsApp.',
     'Viernes', true, 1),
    ('¡El primer campo de gelsoft en Cuba!',
     'Ven a conocer el campo en el Acuario Nacional. Trae a tu equipo: de día o de noche, la misión te espera.',
     'Siempre', true, 2);
