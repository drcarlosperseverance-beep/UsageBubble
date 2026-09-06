# Usage Bubble — primera versión

Aplicación Android personal que muestra una burbuja flotante para abrir el
panel de uso de ChatGPT Work/Codex.

## Estado actual

- Proyecto Android nativo, Java.
- Burbuja flotante mediante `SYSTEM_ALERT_WINDOW`.
- Pantalla de configuración.
- WebView separado para iniciar sesión en el panel indicado por el usuario.
- Guarda solamente los últimos porcentajes cuando se implemente el parser.

## Importante

La página de uso no tiene una API pública documentada para terceros. Por eso
el lector debe ajustarse al texto real que aparece después de iniciar sesión.
No se deben copiar contraseñas, códigos ni cookies al chat.

## Próximo paso

Abrir el proyecto, observar el texto exacto que devuelve el panel y completar
el parser de los porcentajes de 5 horas y semanal. Luego se añade la
actualización automática y el diseño final de la burbuja.

## Compilar desde el teléfono mediante GitHub Actions

1. Crea un repositorio nuevo y privado en GitHub.
2. Descomprime este ZIP y sube todos sus archivos al repositorio.
3. Abre la pestaña `Actions`.
4. Selecciona `Build Usage Bubble APK`.
5. Pulsa `Run workflow`.
6. Cuando termine, abre la ejecución y descarga el artefacto `UsageBubble-debug`.
7. Descomprime el artefacto e instala `app-debug.apk` en el teléfono.

El flujo de compilación ya está incluido en `.github/workflows/build-apk.yml`.
