# Usage Bubble — versión funcional

Aplicación Android personal que muestra una burbuja flotante para abrir el
panel de uso de ChatGPT Work/Codex.

## Funciones

- Proyecto Android nativo, Java.
- Burbuja flotante mediante `SYSTEM_ALERT_WINDOW`.
- Pantalla de configuración.
- WebView separado para iniciar sesión en el panel indicado por el usuario.
- Lee porcentajes en español o inglés, incluso cuando aparecen después de cargar la página.
- Actualiza en segundo plano cada 2 minutos o más, según la configuración.
- Muestra el tiempo transcurrido desde la última lectura válida.
- La burbuja se puede arrastrar y conserva su posición.
- Conserva el último dato válido si una actualización falla.
- Usa una firma de desarrollo estable para permitir futuras actualizaciones.

## Importante

La página de uso no tiene una API pública documentada para terceros. Por eso
el lector debe ajustarse al texto real que aparece después de iniciar sesión.
No se deben copiar contraseñas, códigos ni cookies al chat.

## Uso

Primero abre el panel e inicia sesión. Después activa la burbuja. La app mantiene
una WebView mínima en un servicio visible mediante notificación y vuelve a leer
el panel con el intervalo configurado. Si Android fuerza el cierre del servicio,
hay que volver a activar la burbuja.

## Compilar desde el teléfono mediante GitHub Actions

1. Crea un repositorio nuevo y privado en GitHub.
2. Descomprime este ZIP y sube todos sus archivos al repositorio.
3. Abre la pestaña `Actions`.
4. Selecciona `Build Usage Bubble APK`.
5. Pulsa `Run workflow`.
6. Cuando termine, abre la ejecución y descarga el artefacto `UsageBubble-debug`.
7. Descomprime el artefacto e instala `app-debug.apk` en el teléfono.

El flujo de compilación ya está incluido en `.github/workflows/build-apk.yml`.
