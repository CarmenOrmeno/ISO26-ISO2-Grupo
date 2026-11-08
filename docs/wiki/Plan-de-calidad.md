# Plan de calidad (Quality by Design · ISO/IEC 25010)

## 1. Características seleccionadas y justificación

El producto gestiona **datos clínicos** de pacientes, se integra con sistemas hospitalarios y lo
desarrolla un equipo con apoyo de agentes de IA. Por eso se priorizan (D-012):

| # | Característica / subcaracterística | Por qué es importante en este producto |
|---|---|---|
| 1 | **Seguridad** · confidencialidad, responsabilidad, autenticidad | Datos de salud (RGPD, categoría especial). Cada actor solo debe ver lo suyo y todo acceso debe quedar trazado |
| 2 | **Idoneidad funcional** · corrección y completitud | Una prioridad mal calculada o una cita duplicada tiene impacto asistencial |
| 3 | **Mantenibilidad** · modularidad, testabilidad, modificabilidad | Desarrollo iterativo por CU con agentes: cada iteración debe poder cambiar un componente sin romper otros |
| 4 | **Compatibilidad** · interoperabilidad | El enunciado exige integración con SSO, HCE, admisión y RIS/PACS (DICOM, HL7, FHIR) |

## 2. Implicaciones en requisitos

| Característica | Requisitos funcionales derivados | Requisitos no funcionales derivados |
|---|---|---|
| Seguridad | RF22/CU01 Iniciar sesión; CU17 roles y permisos; CU18 auditoría | RNF-S1: toda operación de la API exige un token de sesión válido y un permiso. RNF-S2: todo acceso a datos clínicos (historial, informes, imágenes) se audita con usuario, acción, recurso y fecha. RNF-S3: un paciente solo accede a sus propios datos; un médico a los de sus solicitudes |
| Idoneidad funcional | Todos los CU planificados se desarrollan y trazan (completitud) | RNF-F1: cobertura de líneas ≥ 75 % por módulo (JaCoCo). RNF-F2: cada CU tiene casos de prueba diseñados con clases de equivalencia y valores límite. RNF-F3: las entidades validan sus invariantes (fechas, prioridades, estados) |
| Mantenibilidad | — | RNF-M1: capas api/controlador/servicio/dominio/persistencia sin saltos (CONTEXT.md). RNF-M2: los servicios dependen de interfaces y reciben las dependencias por constructor (DIP). RNF-M3: el reloj es inyectable (D-010). RNF-M4: un componente (paquete) por CU |
| Compatibilidad | CU19 Integrar sistemas hospitalarios | RNF-C1: cada sistema externo se accede solo a través de su interfaz (`ProveedorSso`, `SistemaHce`, `SistemaAdmision`, `SistemaPacs`). RNF-C2: los identificadores de estudio de imagen siguen el formato DICOM *Study Instance UID* |

## 3. Dónde, cómo y cuándo se controla

| Característica | Dónde (artefacto) | Cómo (técnica / herramienta) | Cuándo |
|---|---|---|---|
| Seguridad | `seguridad.ControlAcceso`, controladores | Pruebas JUnit de acceso denegado por cada rol; revisión del PR con checklist | En cada iteración (disciplina P) y en el PR |
| Idoneidad funcional | Servicios y dominio | Diseño de casos de prueba (T.04.04), JaCoCo, CI en verde obligatorio | En cada PR (CI) y al cerrar cada release |
| Mantenibilidad | Estructura de paquetes, constructores | Revisión SOLID en el PR; las pruebas usan *fakes* (repositorio en memoria), si una clase es difícil de probar se refactoriza | En cada PR; refactorización en la iteración siguiente |
| Compatibilidad | Paquete `integracion` | Pruebas del contrato con los simulados; pruebas de integración en ItN | CU19 e ItN |

## 4. Métricas y umbrales

| Métrica | Herramienta | Umbral |
|---|---|---|
| Pruebas que pasan | Surefire | 100 % |
| Cobertura de líneas (sentencias) | JaCoCo | ≥ 75 % por módulo (regla `check`) |
| Cobertura de ramas (decisiones) | JaCoCo | ≥ 60 % (objetivo, no bloquea) |
| Operaciones de la API sin permiso exigido | Revisión de código | 0 |
| Dependencias de servicio a clases concretas de otro componente | Revisión de código | 0 |

## 5. Refactorización asistida por IA

Tras cada release se pide al agente un análisis de *code smells* (métodos largos, duplicación, clases con
varias responsabilidades) sobre el código fusionado. El equipo decide qué refactorizar; el agente lo aplica
en una rama `feature/refactor-<tema>` y las pruebas existentes garantizan que no cambia el comportamiento.
