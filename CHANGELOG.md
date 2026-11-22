# Registro de cambios

Formato basado en *Keep a Changelog*; versiones según *Semantic Versioning* (ver
[Plan de gestión de configuración](docs/wiki/Plan-de-gestion-de-configuracion.md)).

## [1.0.1] · 2026-11-22 · Iteración de transición (ItN) y calidad del diseño
Módulos: servidor 1.12.1 · cliente 2.0.1

### Añadido
- Pruebas de integración de extremo a extremo: ciclo completo de una prueba con los cuatro roles a través del
  cliente y el mismo ciclo contra MySQL con reinicio.
- Manual de usuario y de despliegue en la wiki, con capturas de la vista JavaFX.
- Resultados finales de las pruebas y la cobertura en el Plan de pruebas.

### Corregido
- Si MySQL está configurado pero no responde, el cliente explica la causa en lugar de mostrar un volcado de pila.
- El informe de cobertura del sitio excluye la vista JavaFX, igual que la comprobación del build.
- Las entidades que devuelve la API ya no se pueden modificar desde fuera de su paquete: un cliente podía
  cambiar el estado del servidor saltándose el control de acceso y la auditoría.

### Cambiado
- `RepositorioEnMemoria` pasa a `persistencia` (junto a `RepositorioMySql`) y el paquete `interoperabilidad`
  se renombra a `sincronizacion`. Sin cambios en la API.

## [1.0.0] · 2026-11-22 · Producto completo (fin de Construcción)
Módulos: servidor 1.12.0 · cliente 2.0.0

### Añadido
- CU13 Consultar historial y CU15 Incidencias y encuestas (portal del paciente).
- CU17 Roles y permisos configurables y persistentes; CU18 Auditoría de accesos a datos clínicos y de accesos denegados.
- CU19 Integración con SSO, HCE, Admisión y RIS/PACS (simulados), con sincronizaciones tolerantes a fallos.
- CU20 Estadísticas del médico, CU21 Informes operativos y CU22 Estadísticas globales.
- Persistencia en MySQL (D-018).
- Cliente JavaFX (D-017) con las acciones de los cuatro roles para todos los CU; la consola se conserva con `--consola`.

### Cambiado
- El cliente pasa a **2.0.0** (versión mayor, D-017): vista gráfica por defecto; el menú de consola de admisión
  se sustituye por el catálogo de acciones común a las dos vistas.
- Calidad del diseño: sin ciclos entre paquetes del servidor, el cliente depende solo de las interfaces `api`
  (`api.Apis`), el control de acceso exige siempre un auditor y `Servidor` no expone los simulados.
- Un issue y una rama por caso de uso (D-019).

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
