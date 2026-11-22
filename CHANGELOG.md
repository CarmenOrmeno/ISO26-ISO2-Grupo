# Registro de cambios

Formato basado en *Keep a Changelog*; versiones según *Semantic Versioning* (ver
[Plan de gestión de configuración](docs/wiki/Plan-de-gestion-de-configuracion.md)).

## [0.2.0] · 2026-11-22 · Flujo clínico completo y avisos al paciente (servidor)
Módulos: servidor 1.9.0 · cliente 1.1.1

### Añadido
- CU07 Gestionar mis citas: el paciente consulta, confirma, pide cambio o cancela sus citas.
- CU12 Notificar y recordar: buzón del paciente, avisos de citas e informes y recordatorios programados.
- CU08 Registrar realización e incidencias, con el estudio en el PACS simulado.
- CU09 Informes radiológicos e imágenes; el médico solo accede a sus pacientes (RNF-S3).
- CU10 Prescribir seguimientos periódicos que generan solicitudes de revisión (tarea programada).
- CU16 Consultar seguimientos y sus revisiones (paciente).
- CU11 Listas de espera por prioridad y antigüedad, plazos, cambio de prioridad y citación en el primer hueco.
- CU14 Estado de las solicitudes del médico, su cita vigente, sus agendas y los huecos libres.
- Sitio de informes unificado (`mvn site site:stage`) con el informe de pruebas agregado.
- Issues por caso de uso con sub-issues de servidor y cliente, e hitos por versión (D-019).

### Cambiado
- El cliente no cambia de funcionalidad: se reempaqueta como 1.1.1 con el servidor 1.9.0 (D-020).
- Decisiones nuevas: cliente JavaFX (D-017) y persistencia MySQL (D-018), previstas para la v1.0.0.

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
