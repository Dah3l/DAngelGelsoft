# D'Angel Gelsoft — App Android (demo)

Landing page en formato APK: portada, horarios, precios, avisos y
reservas por WhatsApp. Todo el contenido viene de un único JSON.

## Cómo llega el contenido a la app (fetch + caché)

`ConfigRepository.load()` sigue este orden, siempre:

1. **Red**: pide el JSON al endpoint de la Edge Function `app-config`
   (ver `data/ConfigRepository.kt`, constante `ENDPOINT_URL`).
   Si responde bien, guarda ese JSON en la caché local.
2. **Caché local**: si no hay red (sin VPN, sin conexión), usa el último
   JSON descargado, guardado en `filesDir/app_config.json`.
3. **JSON incluido**: si nunca se descargó nada, usa
   `app/src/main/assets/config.json`, que viene dentro del APK.
   Por eso la demo funciona sola, antes de que exista Supabase.

Mientras `ENDPOINT_URL` siga siendo el placeholder
(`https://TU-PROYECTO.supabase.co/...`), el paso 1 se salta a propósito
y la app abre directo con los datos incluidos. Para conectarla de verdad,
pon la URL real de la función desplegada en esa constante y reconstruye.

## Pantallas

- **Inicio**: logo, eslogan, foto del campo, botones funcionales
  (WhatsApp, llamar, Instagram) y galería de fotos reales.
- **Horarios**: horario regular, experiencia nocturna, ubicación y contactos.
- **Precios**: modalidades y precios que publica el negocio.
- **Avisos**: noches especiales y avisos.
- **Reservar**: formulario (nombre, teléfono, día, hora, sesión,
  jugadores). Al confirmar, abre WhatsApp con el mensaje de la reserva
  ya escrito al número de reserva. **No guarda la reserva en ninguna
  base de datos**: el mensaje de WhatsApp ES la reserva.

## Compilar

1. Abre la carpeta `android/` en Android Studio.
2. Deja que sincronice Gradle (Gradle 8.7, AGP 8.5.2, Kotlin 2.0.21).
3. `Build > Build App Bundle(s) / APK(s) > Build APK(s)`.
4. El APK queda en `app/build/outputs/apk/debug/`.

## Diseño

Tema oscuro con los colores reales de la marca: fondo negro, acento
turquesa del logo, texto blanco/cromado. Fotos y logo en `res/drawable/`
(`logo_dangel`, `foto_campo_1` a `foto_campo_4`).
