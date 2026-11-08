# Planificación (PUD)

## 1. Restricciones y suposiciones

| Parámetro | Valor | Origen |
|---|---|---|
| Equipo | 4 personas | Equipo |
| Jornada | 40 h/semana por persona (8 h/día) | Enunciado |
| Coste hora | 25 €/h | Supuesto (igual que el ejemplo de clase) |
| Coste It0 / ItN | 1.000 € / 2.000 € | Enunciado |
| Correspondencia | 1 RF = 1 CU = 1 iteración | Enunciado y D-004 |
| Fecha de inicio | 05/10/2026 | Sesión de arranque |
| Fecha de terminación | 15/12/2026 (entrega) | Enunciado |
| Licencias de IA | 0 € (GitHub Copilot con licencia de estudiante) | D-001 |

## 2. Estimación RADIT por complejidad (D-014)

Se estiman tres tipos de CU. Sin agente, el esfuerzo es el de un desarrollo tradicional. Con agente, el
equipo sigue haciendo Requisitos, Análisis y Diseño; el agente reduce Implementación (−60 %) y Pruebas
(−40 %), pero aparece el tiempo de **revisión humana** de lo que genera (incluido en I).

| Complejidad | R | A | D | I | P | Total sin IA | I con IA (+revisión) | P con IA | **Total con IA** |
|---|---|---|---|---|---|---|---|---|---|
| Alta (A) | 4 | 6 | 8 | 16 | 8 | 42 h | 6 + 2 = 8 | 5 | **31 h** |
| Media (M) | 3 | 4 | 6 | 10 | 5 | 28 h | 4 + 2 = 6 | 3 | **22 h** |
| Baja (B) | 2 | 2 | 3 | 5 | 3 | 15 h | 2 + 1 = 3 | 2 | **12 h** |

**Sesgo de optimismo de la IA:** las estimaciones que da un agente suelen contar solo el tiempo de generar
código, no el diseño, la revisión, las correcciones ni la integración. Por eso se añade un **15 % de
contingencia** sobre las horas de los CU. _Tarea pendiente del equipo:_ pedir al agente la estimación en
tokens y en € de cada CU y anotarla aquí junto a la nuestra para compararlas.

## 3. Iteraciones, fases y calendario

Duración = horas ÷ (4 personas × 8 h), con solape: mientras se revisa el PR de una iteración se empieza el
análisis de la siguiente. En la fase de Construcción cada iteración agrupa 2 o 3 CU muy cohesionados (D-016);
las horas y el coste de cada CU no cambian.

| It. | Fase | CU (prioridad) | Cx | Horas | Fechas previstas | Coste (€) | Versión del módulo |
|---|---|---|---|---|---|---|---|
| 0 | Inicio | Visión, RF, CU, plan, Maven, CI | — | — | 05/10 – 16/10 | 1.000 | — |
| 1 | Elaboración | CU01 Iniciar sesión | M | 22 | 19/10 – 20/10 | 550 | servidor 1.0.0 · cliente 1.0.0 |
| 2 | Elaboración | CU02 Gestionar pacientes | M | 22 | 20/10 – 21/10 | 550 | servidor 1.1.0 |
| 3 | Elaboración | CU03 Configurar catálogo | M | 22 | 22/10 – 23/10 | 550 | servidor 1.2.0 |
| 4 | Elaboración | CU04 Solicitar prueba | A | 31 | 26/10 – 27/10 | 775 | servidor 1.3.0 |
| 5 | Elaboración | CU05 Gestionar agendas | A | 31 | 27/10 – 28/10 | 775 | servidor 1.4.0 |
| 6 | Elaboración | CU06 Gestionar citas | A | 31 | 29/10 – 30/10 | 775 | servidor 1.5.0 · cliente 1.1.0 |
| | **Hito** | **Línea base de la arquitectura · release v0.1.0** | | | **30/10** | | |
| 7 | Construcción | CU07 Gestionar mis citas + CU12 Notificar y recordar | M+M | 44 | 02/11 – 04/11 | 1.100 | servidor 1.6.0 |
| 8 | Construcción | CU08 Registrar realización + CU09 Informes e imágenes | M+A | 53 | 05/11 – 09/11 | 1.325 | servidor 1.7.0 |
| 9 | Construcción | CU10 Prescribir seguimiento + CU16 Consultar seguimientos | A+B | 43 | 10/11 – 12/11 | 1.075 | servidor 1.8.0 |
| 10 | Construcción | CU11 Listas de espera + CU14 Estado de solicitudes y agendas | M+B | 34 | 13/11 – 17/11 | 850 | servidor 1.9.0 · cliente 1.2.0 (médico) |
| | **Hito** | **Release v0.2.0** (flujo clínico completo y avisos al paciente) | | | **17/11** | | |
| 11 | Construcción | CU13 Consultar historial + CU15 Incidencias y encuestas | B+B | 24 | 18/11 – 19/11 | 600 | servidor 1.10.0 · cliente 1.3.0 (paciente) |
| 12 | Construcción | CU17 Roles y permisos + CU18 Auditar accesos + CU19 Integrar sistemas | M+M+A | 75 | 20/11 – 24/11 | 1.875 | servidor 1.11.0 |
| 13 | Construcción | CU20 Estadísticas del médico + CU21 Informes operativos + CU22 Estadísticas globales | M+M+B | 56 | 25/11 – 27/11 | 1.400 | servidor 1.12.0 · cliente 1.4.0 (administrador) |
| | **Hito** | **Capacidad operacional inicial** | | | **27/11** | | |
| N | Transición | Integración, pruebas de integración, manual, despliegue | — | — | 30/11 – 11/12 | 2.000 | — |
| | **Hito** | **Release v1.0.0 · entrega** | | | **11/12 (entrega 15/12)** | | |

## 4. Coste

| Concepto | Horas | Coste (€) |
|---|---|---|
| It0 (Inicio) | — | 1.000 |
| 6 CU de complejidad alta × 31 h | 186 | 4.650 |
| 11 CU de complejidad media × 22 h | 242 | 6.050 |
| 5 CU de complejidad baja × 12 h | 60 | 1.500 |
| Contingencia 15 % sobre los CU | 73 | 1.830 |
| ItN (Transición) | — | 2.000 |
| Licencias de IA (Copilot estudiante) | — | 0 |
| **Total** | **561 h + It0 + ItN** | **17.030 €** |

Otros costes que se deberían considerar en un proyecto real (no incluidos): licencias de IA de pago si no
hubiera licencia educativa (Copilot Pro ≈ 10 $/mes por persona → ≈ 110 € en 3 meses para 4 personas, o
consumo de API por tokens), Visual Paradigm (licencia académica, 0 €), amortización de equipos,
infraestructura de despliegue en el SESCAM, certificación de interoperabilidad DICOM/HL7/FHIR y
cumplimiento del RGPD/ENS para datos de salud.

### Comparación sin agentes

| Escenario | Horas CU | Contingencia | Total |
|---|---|---|---|
| Sin IA | 6×42 + 11×28 + 5×15 = 635 h | 15 % | 635 × 25 × 1,15 + 3.000 = **21.256 €** |
| Con IA | 488 h | 15 % | **17.030 €** (−20 %) |

El ahorro se concentra en Implementación y Pruebas. Requisitos, Análisis y Diseño no se reducen: son las
tareas del ingeniero. Un modelo especializado en código reduce revisiones (menos horas de I y menos tokens).

## 5. Calendario de liberaciones (Semantic Versioning)

| Versión | Fecha | Contenido |
|---|---|---|
| v0.1.0 | 30/10 | Línea base de la arquitectura: CU01–CU06 (sesión, pacientes, catálogo, solicitudes, agendas, citas) |
| v0.2.0 | 17/11 | Flujo clínico completo y avisos al paciente: It7–It10 (CU07–CU12, CU14, CU16) |
| v1.0.0 | 11/12 | Producto completo: It11–It13 (CU13, CU15, CU17–CU22) + integración (ItN) |

Las versiones del producto están en el pom padre y en las etiquetas de `main`; cada módulo tiene la suya
(ver [Plan de gestión de configuración](Plan-de-gestion-de-configuracion.md)).

## 6. Seguimiento (real frente a planificado)

| It. | Fecha prevista | Fecha real | Horas previstas | Horas reales | Desviación y causa |
|---|---|---|---|---|---|
| _completar al cerrar cada iteración con las horas imputadas_ | | | | | |
