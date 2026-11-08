# CONTEXT.md · Reglas de arquitectura para los agentes

Este fichero es el **contexto persistente** que se da a cualquier agente de IA (Copilot, Claude Code…)
antes de pedirle que implemente algo. Son los guardarraíles de la arquitectura: el agente **no** decide
la arquitectura, implementa los contratos que ha diseñado el equipo.

## 1. Producto
Sistema de gestión de Radiodiagnóstico del SESCAM. Enunciado y requisitos en
[docs/wiki/Requisitos-y-casos-de-uso.md](docs/wiki/Requisitos-y-casos-de-uso.md).

## 2. Reglas generales
1. Java 17, Maven multimódulo (`radiodiagnostico` → `servidor`, `cliente`). No añadir módulos ni dependencias
   sin una entrada nueva en `DECISIONS.md`.
2. Única dependencia permitida: `org.junit.jupiter:junit-jupiter` (scope test). Nada de JUnit 4, Spring,
   Lombok ni Mockito.
3. Una iteración trabaja **solo** el caso de uso de su issue. No tocar otros CU.
4. Todo el código en `es.uclm.esi.iso2.servidor.<componente>` o `es.uclm.esi.iso2.cliente.<capa>`.
5. Mensajes de commit: `tipo(CUxx): descripción (#issue)` con tipo ∈ {feat, fix, test, docs, build, ci, refactor}.

## 3. Capas del servidor (no saltarse capas)
```
cliente ──► api (interfaces) ──► controlador ──► servicio ──► dominio
                                     │                └──► persistencia (Repositorio)
                                     └──► seguridad.ControlAcceso + auditoria
```
- **api**: solo interfaces `ApiXxx`; cada método recibe el `token` de sesión. Es el contrato con el cliente.
- **controlador**: `ControladorXxx implements ApiXxx`. Exige el permiso con `ControlAcceso`, registra en
  auditoría los accesos a datos clínicos y delega en el servicio. **Sin reglas de negocio.**
- **servicio**: reglas de negocio. Depende de interfaces (`Repositorio`, `Notificador`, `SistemaPacs`…),
  nunca de implementaciones concretas (DIP). Recibe dependencias por constructor.
- **dominio**: entidades y valores. Validan sus invariantes en el constructor y lanzan
  `IllegalArgumentException` si un dato es inválido.
- **persistencia**: `Repositorio<T, ID>`; implementación `RepositorioEnMemoria`.
- **integracion**: interfaces de sistemas externos y sus adaptadores simulados.
- `Servidor` es la **raíz de composición**: el único sitio donde se hace `new` de implementaciones.

## 4. Excepciones
- `ReglaNegocioException`: se incumple una regla del negocio (p. ej. cancelar una cita realizada).
- `NoEncontradoException`: no existe la entidad pedida.
- `AccesoDenegadoException`: sesión inválida o falta el permiso.

## 5. Principios SOLID que se revisan en cada pull request
- **S**: una clase, una responsabilidad (los controladores no validan negocio; los servicios no imprimen).
- **O**: nuevos tipos de prueba, roles o canales de notificación se añaden por configuración o por una
  nueva implementación de la interfaz, sin modificar los servicios.
- **L**: los simulados (`SsoSimulado`, `PacsSimulado`…) cumplen el contrato de su interfaz.
- **I**: una interfaz `ApiXxx` por área funcional, no una fachada gigante.
- **D**: servicios y controladores dependen de interfaces.

## 6. Pruebas
- JUnit Jupiter (`org.junit.jupiter.api`). Clases `XxxTest` en el mismo paquete que la clase probada.
- Patrón Preparar / Ejecutar / Comprobar, `@DisplayName` en español.
- El **diseño** de los casos (clases de equivalencia, valores límite, conjetura de errores) lo hace el
  equipo y está en [docs/wiki/Plan-de-pruebas.md](docs/wiki/Plan-de-pruebas.md). El agente los implementa
  y **no modifica el código de producción** para que pasen.
- Cobertura mínima de líneas: 75 % por módulo (JaCoCo).
