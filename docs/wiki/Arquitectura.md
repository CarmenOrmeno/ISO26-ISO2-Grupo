# 🏛️ Arquitectura (línea base)

[🏠 Inicio](Home.md)

> Módulos Maven, capas del servidor, componentes por caso de uso, ciclo de vida de una solicitud y persistencia en MySQL.

---

Las reglas que deben respetar humanos y agentes están en [CONTEXT.md](../../CONTEXT.md). Decisiones
relacionadas: D-002, D-007, D-008, D-009, D-010, D-011.

## 📦 1. Módulos Maven

```
radiodiagnostico (pom padre, producto)
├── servidor (jar) ── modelo, servicios, controladores y contratos api
└── cliente  (jar) ── vistas JavaFX (FXML) y de consola + controladores; depende de servidor y solo usa su paquete api
```

## 🧅 2. Capas del servidor

```mermaid
flowchart LR
    subgraph CLI["💻 cliente"]
        V["Vista"] --> CC["Controlador cliente"]
    end
    subgraph SRV["🖥️ servidor"]
        API{{"«interface»<br/>ApiXxx"}}
        CTRL["ControladorXxx"]
        CA["ControlAcceso"]
        AUD["Auditor"]
        SRVI{{"«interface»<br/>ServicioXxx"}}
        IMPL["ServicioXxxImpl"]
        DOM["Dominio"]
        REP{{"«interface»<br/>Repositorio&lt;T,ID&gt;"}}
        MEM["RepositorioEnMemoria"]
        SQL[("RepositorioMySql<br/>🐬 MySQL")]
        FAB["FabricaRepositorios"]
        EXT{{"«interface»<br/>SistemaPacs / SistemaHce / …"}}
        SIM["Simulados"]
        SERV["Servidor<br/>(raíz de composición)"]
        CTRL -. implementa .-> API
        CTRL --> CA --> AUD
        CTRL --> SRVI
        IMPL -. implementa .-> SRVI
        IMPL --> DOM
        IMPL --> REP
        MEM -. implementa .-> REP
        SQL -. implementa .-> REP
        SERV --> FAB
        FAB -. crea .-> MEM
        FAB -. crea .-> SQL
        IMPL --> EXT
        SIM -. implementa .-> EXT
        SERV -. crea .-> CTRL
    end
    CC --> API
```

Cada componente expone **dos** interfaces:
- `ServicioXxx` (contrato interno): lo usan otros componentes del servidor. Sin seguridad.
- `ApiXxx` (contrato externo): lo usa el cliente. Cada método recibe el *token* y exige un permiso.

## 🧩 3. Componentes (paquetes de `es.uclm.esi.iso2.servidor`)

| Paquete | Responsabilidad | CU |
|---|---|---|
| `comun` | Repositorio genérico, `Secuencia` de ids, excepciones, validaciones | — |
| `persistencia` | `FabricaRepositorios` (memoria o MySQL según el entorno), `RepositorioMySql`, `ConfiguracionBd` | D-018 |
| `seguridad` | Usuarios, sesiones, roles, permisos (política configurable), control de acceso, interfaz `Auditor` | CU01, CU17, CU18 |
| `integracion` | Interfaces de SSO, HCE, Admisión, RIS/PACS (`SistemaExterno`) y simulados | CU01, CU09, CU19 |
| `interoperabilidad` | Sincronizaciones con Admisión y HCE y estado de los sistemas externos | CU19 |
| `pacientes` | Alta, modificación y baja de pacientes | CU02 |
| `catalogo` | Tipos de prueba, modalidades, prioridades y parámetros | CU03 |
| `solicitudes` | Solicitudes de prueba y su ciclo de vida | CU04, CU14 |
| `agendas` | Agendas diarias y huecos | CU05, CU14 |
| `citas` | Citas: asignar, reprogramar, cancelar, confirmar; recordatorios de citas (tarea programada) | CU06, CU07, CU12 |
| `realizacion` | Registro de realización e incidencias técnicas | CU08 |
| `informes` | Informes radiológicos e imágenes | CU09 |
| `seguimiento` | Pruebas de seguimiento periódicas | CU10, CU16 |
| `esperas` | Listas de espera, prioridades y huecos | CU11 |
| `notificaciones` | Buzón de notificaciones del paciente y `Notificador` | CU12 |
| `historial` | Historial de pruebas del paciente | CU13 |
| `atencion` | Incidencias del paciente y encuestas | CU15 |
| `auditoria` | Registro y consulta de accesos | CU18 |
| `estadisticas` | Estadísticas del médico, informes operativos y globales | CU20–CU22 |
| `api` / `controlador` | Contratos con el cliente y su implementación | todos |

### Cliente (D-017)

| Paquete de `es.uclm.esi.iso2.cliente` | Responsabilidad |
|---|---|
| `controlador` | `Accion` (operación de la API con sus `Campo` tipados), `Valores` (interpreta lo escrito), `Resultado`, catálogo de acciones por rol (`AccionesPaciente`, `AccionesMedico`, `AccionesAdmision`, `AccionesAdministrador`; el médico tiene las del paciente, D-005), `SesionCliente` (CU01) y `AplicacionCliente` (controlador de la consola) |
| `vista.fx` | JavaFX: `login.fxml` y `principal.fxml` con sus controladores de vista. El formulario de cada acción se genera a partir de sus campos |
| `vista` | Consola (`--consola`), usada también en las pruebas de sistema |

Las dos vistas usan los mismos controladores. Añadir una operación al cliente es añadir una `Accion` al
catálogo del rol, sin tocar las vistas (OCP). El cliente solo depende de la interfaz `api.Apis` (acceso a los contratos
`ApiXxx`), que implementa `Servidor`; solo `Main`, la raíz de composición del cliente, crea el `Servidor`.

## 🔄 4. Ciclo de vida de una solicitud

```mermaid
stateDiagram-v2
    [*] --> SOLICITADA : CU04 / CU10
    SOLICITADA --> CITADA : CU06 asignar
    CITADA --> SOLICITADA : CU06/CU07 cancelar cita
    CITADA --> REALIZADA : CU08
    REALIZADA --> INFORMADA : CU09
    SOLICITADA --> CANCELADA
    INFORMADA --> [*]
```

## 🐬 5. Persistencia en MySQL (D-018)

- Cada almacén (pacientes, solicitudes, citas…) es una **tabla** `(orden, id, datos)` que la aplicación crea al
  arrancar. Cada fila guarda un **agregado** completo serializado (por ejemplo, una agenda con sus huecos),
  identificado por su id; `orden` conserva el orden de inserción.
- Al leer, un **filtro de deserialización** solo admite clases del servidor y del JDK.
- Los ids de cada componente los genera `Secuencia`, que continúa desde el mayor id guardado.
- Se elige con variables de entorno: si existe `RADIO_DB_URL` se usa MySQL; si no, memoria.

| Variable | Ejemplo |
|---|---|
| `RADIO_DB_URL` | `jdbc:mysql://127.0.0.1:3306/radiodiagnostico` |
| `RADIO_DB_TEST_URL` | `jdbc:mysql://127.0.0.1:3306/radiodiagnostico_test` (pruebas de integración) |
| `RADIO_DB_USUARIO` / `RADIO_DB_CLAVE` | usuario y contraseña de la aplicación (nunca en el repositorio) |

Los sistemas externos simulados (SSO, HCE, Admisión, PACS) y las sesiones siguen en memoria: no son datos de la aplicación.
---

[🏠 Inicio](Home.md) · [⬆️ Arriba](#)
