# Cumplimiento del codelab

Actividad: **PEA: Integración de servicio IA con Edge function de Supabase en una app móvil nativa**.

Fuente: [TinyPrice Codelab del docente](https://epanchanaf.github.io/tinyprice-codelab/).
La implementación parte de los ejemplos de su carpeta `reference/`, revisados y adaptados; no se presenta ese código como una creación independiente del material docente.

| Requisito | Implementación / verificación |
| --- | --- |
| Android nativo, Kotlin y Jetpack Compose | `app/`, paquete `com.apps.iatinyfish` |
| StateFlow, eventos y ViewModel | `presentation/deals/DealsViewModel.kt` |
| Demo y repositorio remoto | `data/repository/`, seleccionados con `useRemoteDeals` |
| Operaciones de red fuera del hilo principal | Retrofit y `Dispatchers.IO` |
| Inicio, carga, resultados, vacío, error, detalle | `DealsApp.kt` |
| Fuente verificable | URL HTTP/HTTPS, acción para abrir navegador |
| Máximo diez resultados | Adaptación en servidor y límite en cliente |
| Supabase Cloud, TypeScript, `Deno.serve` | `supabase/functions/search-products/index.ts` |
| Secreto exclusivo del servidor | `TINYFISH_API_KEY` en Supabase Secrets |
| Consulta entre 2 y 200 caracteres | Validación en app y función |
| Métodos, errores, timeout y cuota | Handler con pruebas automatizadas |
| Skill oficial Android | Instalación con Android CLI; revisar ruta y hash registrados |
| Tests del ViewModel e interfaz Compose | `app/src/test`, `app/src/androidTest` |
| Despliegue y recorrido real | Función desplegada y recorrido real verificado; ver `VALIDACION.md` |

## Decisiones y diferencias documentadas

- El usuario completó el registro de TinyFish con GitHub y pidió continuar con esa cuenta. El codelab propone registro por correo. Se abrió inicialmente el enlace del docente con `ref` intacto; no se ha verificado aún la atribución del beneficio.
- Los precios del modo demo son ficticios y la UI lo señala. Los resultados remotos muestran **Ver en fuente**: Search no garantiza precios estructurados.
- El handler se separa de `index.ts` para poder probarlo sin credenciales. `index.ts` conserva una única llamada a `Deno.serve`.
- Se conserva la configuración pública didáctica `verify_jwt = false`. La cuota en memoria se aplica por instancia; no ofrece protección distribuida para una aplicación pública.
- La publicación en Google Play es una ampliación opcional, no un requisito para terminar el laboratorio.
- Android CLI actual requiere nombrar la skill (`android skills add android-cli --agent=codex --project=.`), instala un `SKILL.md` autocontenido y no produce el archivo de bloqueo que describe el codelab. Se añadió `skills-lock.json` como registro local de procedencia y SHA-256, claramente identificado como tal.
- API 37 se instala con el nombre actual `platforms/android-37.0`. Se conserva el nivel de compilación 37 indicado por el docente.

## Evidencias

Consultar `VALIDACION.md`. Cada prueba debe indicarse como ejecutada, pendiente o fallida; no se sustituye una prueba con credenciales reales por una respuesta simulada.
