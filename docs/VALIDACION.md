# Registro de validación

Fecha de verificación: **22 de octubre de 2026**, horario de Colombia/Ecuador continental (UTC−5).

## Ejecutado

- Node.js 24: `npm test` — **10 pruebas aprobadas** del handler con un proveedor simulado.
- Gradle Wrapper 9.6.0 generado con la distribución oficial y SHA-256 comprobado.
- Android Studio y JDK 21 detectados; SDK local y AVD `Lemonade_API_35` disponibles.
- Android CLI oficial instalado y skill `android-cli` descargada.
- Android SDK Platform **37.0** instalado desde el catálogo oficial. Se mantuvieron AGP 9.4.1, Gradle 9.6.0 y compileSdk 37.
- `:app:assembleDebug :app:testDebugUnitTest` — **BUILD SUCCESSFUL**, 8 pruebas del ViewModel aprobadas.
- `:app:connectedDebugAndroidTest` — **BUILD SUCCESSFUL**, 3 pruebas Compose aprobadas en `Lemonade_API_35` (Android 15 / API 35).
- Compilación remota con `useRemoteDeals=true` y URL del proyecto — **BUILD SUCCESSFUL**.
- Función `search-products` desplegada con Supabase CLI 2.121.0 en `yprwaxdreyjemazkgjft`.
- Se comprobó la existencia del secreto por su nombre y huella; no se leyó su valor desde Supabase.
- POST real con `Nintendo Switch OLED` — **HTTP 200, 6 resultados**.
- GET — **405**; consulta de un carácter — **400**; JSON incompleto — **400**.
- Recorrido real Android → Supabase → TinyFish → Android: muestra carga y 6 resultados web.
- Se abrió el detalle de una tarjeta y se volvió a la lista.
- **Abrir fuente web** inició el navegador del emulador con la URL de la tienda Tía. Se verificó el envío de la URL al navegador; esto no acredita disponibilidad ni precio de la página externa.
- Tema claro y oscuro inspeccionados visualmente; evidencias en `evidencias/`.
- Rotación horizontal y regreso a vertical: se conservaron consulta y resultados.
- Red del emulador desconectada: mensaje de error y botón Reintentar visibles. Se restauraron WiFi/datos y el reintento recuperó los resultados.
- Consulta vacía desde la UI: validación visible sin resultado remoto.
- Segunda búsqueda real, `Laptop gamer`: resultados web visibles desde Android.
- Revisión de los 49 archivos preparados para publicar: sin claves TinyFish, tokens de Supabase/GitHub ni contraseñas. `local.properties` está ignorado por Git.

## Publicación y alcance

- Repositorio público: `CLOG-U/PEA-Integracion-IA-Edge-Function-Supabase-App-Movil-Nativa`.
- La función usa la configuración pública didáctica del codelab. No incluye autenticación ni límites distribuidos para producción.
- El modo demo contiene precios ficticios; la búsqueda remota conserva las fuentes y no inventa precios.
- Gemini CLI es opcional y no se ejecutó. La skill oficial Android se instaló y se utilizó desde este agente.

Los tests del handler usan `test-only-key`, un valor ficticio. No consumen la API real de TinyFish.
