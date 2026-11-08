# Equipo y roles

> Completad la columna «Persona» con el nombre y el usuario de GitHub (cuenta @alu.uclm.es).

| Rol | Persona | Responsabilidades |
|---|---|---|
| Coordinador / jefe de proyecto | _Miembro 1_ | Planificación, actas, tablero Kanban, crear issues tras cada reunión, **aprueba los requisitos** |
| Responsable de configuración | _Miembro 2_ | Git Flow, reglas de protección, ramas `release`/`hotfix`, etiquetas, versiones de los pom, CI |
| Responsable de calidad y pruebas | _Miembro 3_ | Plan de calidad, plan de pruebas, revisa cobertura y SOLID en cada PR |
| Arquitecto / analista-diseñador | _Miembro 4_ | Diseño de interfaces (paquete `api`), diagramas PlantUML/Visual Paradigm, `CONTEXT.md` |
| Profesora de prácticas | CarmenOrmeno | Colaboradora del repositorio; supervisa |

Todos los miembros analizan, diseñan, revisan pull requests y supervisan a los agentes.

## Quién aprueba qué (resumen del plan de configuración)

| Elemento de configuración | Responsable | Aprueba |
|---|---|---|
| Requisitos y casos de uso | Coordinador | Todo el equipo en reunión (acta) |
| Interfaces `api` (contratos) | Arquitecto | Coordinador + 1 revisor |
| Componente de cada CU (paquete del servidor) | Asignado en el issue del CU | Otro miembro en el PR |
| Módulo cliente | Asignado en el issue | Otro miembro en el PR |
| Plan de pruebas y casos de prueba | Responsable de calidad | Arquitecto |
| Versiones y releases | Responsable de configuración | Coordinador |

## Humanos y agentes

| Lo hace el equipo (humano) | Lo hace el agente (IA) bajo supervisión |
|---|---|
| Identificar y priorizar requisitos y CU | Proponer borradores de diagramas PlantUML |
| Planificar, estimar y decidir la arquitectura | Implementar las clases que cumplen las interfaces |
| Diseñar interfaces y casos de prueba (técnicas de caja negra) | Implementar los casos de prueba diseñados y ejecutarlos |
| Revisar y aprobar cada PR | Proponer refactorizaciones y explicar errores de compilación |
