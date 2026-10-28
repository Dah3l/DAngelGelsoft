# D'Angel Gelsoft — Demo (APK + panel web + Supabase)

Demo de la app propia de D'Angel Gelsoft, "el primer campo de gelsoft
en Cuba", en el Acuario Nacional, La Habana.

La app es una **landing page en formato APK**: portada destacada,
horarios, precios y modalidades, avisos y noches especiales, redes
sociales, botones directos de reserva y un formulario de reserva que
funciona como **checkout por WhatsApp** (el mensaje de WhatsApp ES la
reserva; no se guarda nada en base de datos).

## Arquitectura (estilo Aguacero)

```
Panel web (panel/index.html)
      │ edita el contenido, sin código
      ▼
Supabase (tablas: business_info, schedules, prices, announcements)
      │ Edge Function `app-config` sirve UN solo JSON
      ▼
APK Android ──► JSON de red → caché local → JSON incluido en la app
```

- El panel escribe en Supabase. La Edge Function lee las 4 tablas y
  devuelve un único JSON (`info + horarios + precios + anuncios`).
- La APK pide ese JSON al abrir. Si no hay conexión (sin VPN, sin
  datos), usa el último JSON descargado; y si nunca descargó nada, usa
  el JSON incluido en `assets/config.json`. Por eso la demo funciona
  sola, antes de que exista el proyecto Supabase.
- Las reservas NO tocan Supabase: el formulario arma el resumen y abre
  WhatsApp (`wa.me`) con el mensaje ya escrito al número de reserva.
  El cliente solo toca enviar.

## Qué es demo y qué es real

- **Real**: nombre, eslogan ("Más que un juego, una misión"),
  ubicación (Acuario Nacional), Instagram (@d_angel_gelsoft), números
  de reserva (+53 54753951 y +53 58878734), logo y fotos del campo.
- **De prueba (ejemplos)**: precios, modalidades, horario nocturno y
  avisos. Están marcados como ejemplos en `supabase/seed.sql`, en el
  panel (aviso visible arriba) y en el campo `nota_demo` del JSON.
  El administrador los cambia él mismo en el panel y la app los muestra
  al instante, sin reconstruir la APK.

## Puesta en marcha, en orden

### 1. Crear el proyecto Supabase

En [supabase.com](https://supabase.com): New project. Anota la URL
del proyecto y la anon key (Project Settings → API).

### 2. Crear las tablas y los datos de prueba

En el editor SQL de Supabase, ejecuta en orden:

1. `supabase/schema.sql` — crea las 4 tablas y la seguridad RLS
   (lectura pública; escritura demo con anon key).
2. `supabase/seed.sql` — inserta la información real del negocio y
   los datos de ejemplo (precios, horarios, avisos).

### 3. Desplegar la Edge Function

Con la CLI de Supabase, desde la carpeta del proyecto:

```
supabase functions deploy app-config
```

La URL queda como:
`https://TU-PROYECTO.supabase.co/functions/v1/app-config`

### 4. Configurar y publicar el panel

1. Abre `panel/index.html` y rellena arriba del `<script>`:
   `SUPABASE_URL`, `SUPABASE_ANON_KEY` (la función usa la URL sola).
2. Publícalo en cualquier hosting estático, por ejemplo
   Cloudflare Pages (arrastrar la carpeta o conectar el repo), Netlify
   o cualquier hosting con HTML estático. No necesita servidor: es un
   solo archivo HTML.
3. Abre el panel, comprueba que carga la información y pulsa
   "Ver JSON actual" para confirmar que la función responde.

> Seguridad: en esta demo el panel escribe con la anon key para no
> obligar al admin a crear cuentas. Antes de publicar de verdad, activa
> Supabase Auth y cambia las políticas de escritura para que solo el
> admin autenticado pueda modificar (ver el comentario en
> `supabase/schema.sql`).

### 5. Conectar la app con el JSON real

En `android/app/src/main/java/com/dangel/gelsoft/data/ConfigRepository.kt`,
cambia la constante `ENDPOINT_URL` por la URL de la función del paso 3.
Mientras siga el placeholder, la app abre directo con los datos
incluidos (comportamiento pensado para la demo).

### 6. Abrir en Android Studio y compilar la APK

1. Abre la carpeta `android/` en Android Studio (Windows).
2. Deja que sincronice Gradle (Gradle 8.7, AGP 8.5.2, Kotlin 2.0.21).
3. `Build > Build App Bundle(s) / APK(s) > Build APK(s)`.
4. El APK queda en `android/app/build/outputs/apk/debug/`.

## Estructura

```
dangel-demo/
├── README.md                  ← esta guía
├── android/                   ← proyecto Android Studio (Kotlin + Compose)
│   ├── README.md              ← cómo funciona el fetch + caché
│   ├── app/src/main/java/com/dangel/gelsoft/
│   │   ├── MainActivity.kt    ← navegación por pestañas
│   │   ├── data/              ← modelo, parseo JSON, fetch + caché
│   │   └── ui/                ← tema (negro + turquesa) y pantallas
│   ├── app/src/main/assets/config.json   ← JSON incluido (demo)
│   └── app/src/main/res/drawable/        ← logo + 4 fotos del campo
├── panel/
│   └── index.html             ← panel de administración (un archivo)
└── supabase/
    ├── schema.sql             ← tablas + RLS
    ├── seed.sql               ← datos (reales + ejemplos marcados)
    └── functions/app-config/index.ts     ← el JSON único para la app
```

## Diseño

Tema oscuro con los colores reales de la marca, sacados del logo:
fondo negro, acento turquesa, texto blanco/cromado. Nada de paleta
verde oliva: la marca es negro + turquesa + cromo. La portada muestra
el logo, el eslogan y fotos reales del campo. Todos los botones y
menús son funcionales, no decorativos.

## Contacto y redes (datos reales del negocio)

- Instagram: @d_angel_gelsoft
- Reservas por WhatsApp: +53 54753951 y +53 58878734
- Horario: 10:00 AM – 10:00 PM, más experiencia nocturna
