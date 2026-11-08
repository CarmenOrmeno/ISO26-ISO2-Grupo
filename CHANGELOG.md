# Registro de cambios

Formato basado en *Keep a Changelog*; versiones según *Semantic Versioning* (ver
[Plan de gestión de configuración](docs/wiki/Plan-de-gestion-de-configuracion.md)).

## [0.1.0] · 2026-11-08 · Línea base de la arquitectura (fin de Elaboración)
Módulos: servidor 1.5.0 · cliente 1.1.0

### Añadido
- CU01 Iniciar sesión con SSO simulado, roles, permisos por rol y caducidad de sesión.
- CU02 Gestionar pacientes (alta, modificación, baja lógica, búsqueda por DNI).
- CU03 Catálogo de tipos de prueba y parámetros del sistema.
- CU04 Solicitar prueba con indicación, justificación y prioridad.
- CU05 Gestionar agendas diarias por sala, modalidad y médico.
- CU06 Gestionar citas (asignar, reprogramar, cancelar).
- Cliente de consola MVC con menú de admisión.
- Integración continua con Maven, Surefire y JaCoCo (umbral 75 %).
