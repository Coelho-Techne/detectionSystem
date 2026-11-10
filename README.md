# Detection System — Documentação do Código

> Documentação gerada a partir da leitura integral do código-fonte em 2026-09-25 (branch `master`, incluindo as alterações ainda não commitadas).
> O código é sempre a fonte de verdade — atualize o seu arquivo conforme o projeto evoluir (caso tenha atualizações novas e você não baixou).
> Caminhos de pacote são relativos a `src/main/java/com/coelhotechne/detection_system/`.

## Sumário

1. [Visão geral](#1-visão-geral)
2. [Stack e build](#2-stack-e-build)
3. [Configuração e execução](#3-configuração-e-execução)
4. [Arquitetura e convenções](#4-arquitetura-e-convenções)
5. [Mapa de endpoints REST](#5-mapa-de-endpoints-rest)
6. [Módulos](#6-módulos)
    - [6.1 Raiz da aplicação](#61-raiz-da-aplicação)
    - [6.2 globalClass — infraestrutura compartilhada](#62-globalclass--infraestrutura-compartilhada)
    - [6.3 security — autenticação e autorização](#63-security--autenticação-e-autorização)
    - [6.4 sensor — sensores IoT via MQTT](#64-sensor--sensores-iot-via-mqtt)
    - [6.5 cam — câmeras](#65-cam--câmeras)
    - [6.6 connection — abstração de conexão/rede](#66-connection--abstração-de-conexãorede)
    - [6.7 device — dispositivos alvo de notificação](#67-device--dispositivos-alvo-de-notificação)
    - [6.8 zone — zonas monitoradas](#68-zone--zonas-monitoradas)
    - [6.9 location — locais](#69-location--locais)
    - [6.10 telemetry — medições](#610-telemetry--medições)
    - [6.11 detection — detecções](#611-detection--detecções)
    - [6.12 event — eventos do sistema](#612-event--eventos-do-sistema)
    - [6.13 batterysupply — alimentação elétrica](#613-batterysupply--alimentação-elétrica)
    - [6.14 installation — dados de instalação](#614-installation--dados-de-instalação)
7. [Fluxos principais](#7-fluxos-principais)
8. [Testes](#8-testes)
9. [Pontos de atenção encontrados na leitura](#9-pontos-de-atenção-encontrados-na-leitura)

---

## 1. Visão geral

API REST em Java/Spring Boot para um sistema de detecção *smart home* da Coelho Techne. O sistema:

- **Recebe eventos de sensores IoT** (detecção, status, telemetria) via broker **MQTT**, processa o diagnóstico e mantém o estado de cada sensor.
- **Cadastra e homologa câmeras** (fixas e PTZ), incluindo um teste real de conexão RTSP (handshake `DESCRIBE` com autenticação Basic/Digest).
- **Organiza o ambiente** em zonas (`Zone`) e locais (`Location`).
- **Registra telemetria** manual/automática por sensor e zona.
- **Autentica usuários humanos** por JWT e **dispositivos** (câmeras/sensores) por chave de acesso em headers.
- Mantém **dispositivos de notificação** (`Device`: smartwatch, TV, celular…), que são *destino* de alertas, não fonte de detecção.

Entidades de detecção (`Detection`) e eventos (`Event`) já estão modeladas, mas ainda não têm serviço/controller.

## 2. Stack e build

| Item | Valor |
|---|---|
| Linguagem | Java 17 |
| Framework | Spring Boot 4.1.0 (parent) |
| Build | Maven (wrapper `mvnw` / `mvnw.cmd`) |
| Banco | MySQL (`mysql-connector-j`), JPA/Hibernate |
| Segurança | Spring Security + JWT (`jjwt` 0.11.5) + BCrypt |
| Mensageria | Eclipse Paho MQTT v3 1.2.5, Spring Integration (MQTT/HTTP/JPA/STOMP/WebSocket — declarados, mas a ingestão usa Paho diretamente) |
| Documentação da API | springdoc-openapi 2.8.13 (Swagger UI em `/swagger-ui/**`) |
| Observabilidade | Spring Boot Actuator (`health`, `info`, `metrics`) + `HealthIndicator` MQTT próprio |
| Outros | Lombok, Bean Validation, Thymeleaf (declarado, sem templates), WebSocket (desabilitado por propriedade) |

## 3. Configuração e execução

### Subir a aplicação

```powershell
$env:APP_JWT_SECRET = "<segredo com pelo menos 32 caracteres>"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

O perfil `dev` é necessário: é nele que existe o bean `DevCamCredentialResolver` (ver [9](#9-pontos-de-atenção-encontrados-na-leitura)).

### `application.properties`

| Propriedade | Função |
|---|---|
| `spring.datasource.*` | Conexão MySQL `localhost:3306/detectionSystem`, usuário `root` |
| `spring.jpa.hibernate.ddl-auto` | Declarada duas vezes (`update` e `validate`); vale a última, **`validate`** — o schema precisa existir |
| `spring.jpa.open-in-view=false` | Sessão JPA não fica aberta na view; lazy loading fora de transação falha |
| `spring.jackson.property-naming-strategy=SNAKE_CASE` | JSON em `snake_case` |
| `management.endpoints.web.exposure.include` | Expõe `health`, `info`, `metrics` |
| `sensor.diagnostics.*` | Limiar de *out of spec* de transferência (95) e intervalo de heartbeat de escrita (60s) → `SensorDiagnosticsThresholds` |
| `sensor.power.*` | Limiares de bateria baixa (30%) / crítica (15%) e heartbeat (15m) → `PowerSupplyThresholds` |
| `sensor.inactivity.*` | Janela (5m) e intervalo de varredura (30s) do `SensorInactivityScanner` |
| `sensor.mqtt.*` | Broker, client id, threads/fila de ingestão, silêncio máximo p/ health → `SensorMqttProperties` |

### `application-dev.properties`

Repete a datasource, liga `ddl-auto=update` e `show_sql`, e desliga WebSocket. A configuração de broker MQTT de dev/prod está no próprio `application.properties` (bloco comentado para prod: `ssl://`, usuário e `MQTT_PASSWORD`).

### Variáveis/propriedades adicionais lidas pelo código

| Chave | Onde | Padrão |
|---|---|---|
| `app.jwt.secret` (env `APP_JWT_SECRET`) | `JwtProperties` | obrigatório, ≥ 32 caracteres |
| `app.jwt.expiration` | `JwtProperties` | 1 hora |
| `app.cors.allowed-origins` | `CorsConfig` | `http://localhost:5173` |
| `springdoc.api-docs.enabled` | `SecurityConfig` | `true` (libera Swagger sem autenticação) |

## 4. Arquitetura e convenções

**Package-by-feature**, cada módulo com camadas:

```
<modulo>/
├── api/            controllers REST + DTOs (records) + mappers
├── application/    serviços (interface + Impl) — regras de caso de uso, transações
├── domain/         entidades JPA, enums, value objects, regras de negócio ricas
├── exceptions/     exceções HTTP (ErrorResponseException + ProblemDetail)
└── infrastructure/ repositórios Spring Data e adaptadores externos
```

Convenções recorrentes:

- **Entidades** estendem `BaseEntity` (UUID, `@Version`, auditoria). Identidade (`equals/hashCode`) apenas pelo `uuid`.
- **DTOs** são `record`s; muitos têm também métodos estáticos `toEntity`/`toResponse` duplicando o mapper (legado — os serviços usam os `*Mapper`).
- **Mappers** implementam `GenericMapper<Entidade, Response, Request>` e são `@Component`.
- **Exceções** estendem `ErrorResponseException` com um `ProblemDetail` (RFC 7807: `title`, `detail`, propriedades extras). O `GlobalExceptionHandler` devolve o corpo como está.
- **Concorrência otimista**: `@Version` + tradução de `OptimisticLockingFailureException` para `409 Conflict`; no sensor, também `ETag`/`If-Match`.
- **Autorização** com `@PreAuthorize` nos controllers (`@EnableMethodSecurity`).

## 5. Mapa de endpoints REST

Autenticação: **JWT** = header `Authorization: Bearer <token>`; **ADMIN** = JWT de usuário com role `ADMIN`.
Por padrão, qualquer rota não liberada no `SecurityConfig` exige autenticação.

| Método | Rota | Acesso | O que faz |
|---|---|---|---|
| POST | `/api/v1/auth/login` | público | Login e-mail/senha → JWT |
| GET | `/api/v1/sensors` | autenticado | Lista paginada (padrão 50, ordenado por `name`) |
| GET | `/api/v1/sensors/{id}` | autenticado | Busca sensor; devolve `ETag` = versão |
| POST | `/api/v1/sensors` | ADMIN | Cria sensor |
| PUT | `/api/v1/sensors/{id}` | ADMIN | Substitui sensor; **exige `If-Match`** (428 se faltar, 412 se divergir) |
| PATCH | `/api/v1/sensors/{id}` | ADMIN | Alteração parcial (nulo = não altera); `If-Match` opcional |
| POST | `/api/v1/sensors/{id}/maintenance` | ADMIN | Coloca em `MAINTENANCE_REQUIRED` |
| DELETE | `/api/v1/sensors/{id}/maintenance` | ADMIN | Sai da manutenção → `INITIALIZING` |
| DELETE | `/api/v1/sensors/{id}` | ADMIN | Remove (409 se o sensor estiver operacional) |
| POST | `/api/v1/sensors/{id}/commands/{command}` | ADMIN | Publica comando MQTT (`READ_NOW`, `RESTART`, `CALIBRATE`, `SELF_TEST`, `MUTE`) → 202 |
| GET | `/api/v1/cam-fixed` | autenticado | Câmeras fixas, paginado |
| GET | `/api/v1/cam-fixed/all` | autenticado | Todas as câmeras fixas |
| GET | `/api/v1/cam-fixed/{id}` | autenticado | Câmera fixa por id |
| POST / PUT / DELETE | `/api/v1/cam-fixed[/{id}]` | ADMIN | CRUD de câmera fixa |
| POST | `/api/v1/cam/{camId}/auth` | ver §9 | Câmera se autentica com `access_key` |
| POST | `/api/v1/cam/{camId}/homologation/test` | autenticado | Roda teste de conexão RTSP |
| POST | `/api/v1/cam/{camId}/homologation/approve` | ADMIN | Aprova homologação |
| POST | `/api/v1/cam/{camId}/homologation/reject` | ADMIN | Rejeita (corpo: `reason`) |
| GET / POST / PUT / DELETE | `/api/v1/device[/{id}]` | ADMIN | CRUD de dispositivos de notificação |
| POST | `/device/{id}/authenticate` | autenticado | Dispositivo valida sua chave |
| POST | `/device/{id}/rotate-key` | ADMIN | Gera nova chave para o dispositivo |
| GET | `/api/v1/zone[/{id}]` | autenticado | Lista / busca zonas |
| POST / PUT / DELETE | `/api/v1/zone[/{id}]` | ADMIN | CRUD de zonas (delete 409 se houver sensores) |
| GET | `/api/v1/location[/{id}]` | autenticado | Lista / busca locais |
| POST / PUT / DELETE | `/api/v1/location[/{id}]` | ver §9 | CRUD de locais |
| GET | `/api/v1/telemetry` | autenticado | Paginado (20, por `measuredAt` desc) |
| GET | `/api/v1/telemetry/{id}` | autenticado | Por id |
| POST / PUT / DELETE | `/api/v1/telemetry[/{id}]` | autenticado | CRUD manual de telemetria |
| GET | `/api/v1/telemetry/zone/{zoneId}/{telemetryId}` | autenticado | Telemetria validando que pertence à zona |
| GET | `/api/v1/telemetry/sensor/{sensorId}/{telemetryId}` | autenticado | Idem, por sensor |
| GET | `/api/v1/telemetry/zone/{zoneId}/sensor/{sensorId}/{telemetryId}` | autenticado | Idem, zona + sensor |
| GET | `/api/v1/telemetry/sensor/{sensorId}/latest` | autenticado | Histórico do sensor, mais recente primeiro |
| GET | `/actuator/health` etc. | autenticado | Actuator (inclui indicador `mqtt`) |
| GET | `/swagger-ui/**`, `/v3/api-docs/**` | público (se docs habilitados) | OpenAPI |

---

## 6. Módulos

### 6.1 Raiz da aplicação

| Classe | O que faz |
|---|---|
| `DetectionSystemApplication` | `main` do Spring Boot. `@ConfigurationPropertiesScan` registra os records de configuração (`SensorMqttProperties`, `SensorDiagnosticsThresholds`, `PowerSupplyThresholds`). |

### 6.2 `globalClass` — infraestrutura compartilhada

| Classe | O que faz |
|---|---|
| `config/CorsConfig` | Libera CORS em `/api/**` para as origens de `app.cors.allowed-origins` (padrão: front Vite em `localhost:5173`), métodos GET/POST/PUT/PATCH/DELETE/OPTIONS, com credenciais. |
| `entities/BaseEntity` | `@MappedSuperclass` de todas as entidades: `uuid` (gerado, PK), `version` (`@Version`, lock otimista), `createdBy`, `lastModifiedBy`, `createdAt`, `updatedAt` (auditoria Spring Data). Sem setters nesses campos; `equals/hashCode` só pelo `uuid`. |
| `exceptions/ErrorResponse` | Record simples `(code, message)` usado pelo `ConnectionExceptionHandler`. |
| `exceptions/GlobalExceptionHandler` | `@RestControllerAdvice`: (1) devolve qualquer `ErrorResponseException` com seu status/headers/`ProblemDetail`; (2) converte `TransactionSystemException` cuja causa é `IllegalStateException` (validações `@PrePersist`/`@PreUpdate`) em **422**. |
| `mapper/GenericMapper<E,R,Q>` | Contrato de mapeamento Request→Entidade e Entidade→Response, com `toResponseList` padrão. |

### 6.3 `security` — autenticação e autorização

Há **dois mundos de autenticação** que convivem no mesmo `SecurityFilterChain`:

1. **Usuários humanos** → login com e-mail/senha, recebem JWT.
2. **Dispositivos** (câmera/sensor) → headers `X-Device-Id` + `X-Access-Key`.

| Classe | O que faz |
|---|---|
| `config/SecurityConfig` | Monta a cadeia: CSRF off, sessão **stateless**, CORS, regras de rota (login e `*/auth/**` públicos; `*/homologation/**` para `TECHNICIAN`/`ADMIN`; Swagger público se habilitado; resto autenticado), 401 via `HttpStatusEntryPoint`. Registra `BCryptPasswordEncoder`, `DaoAuthenticationProvider` e um `ProviderManager` com os 3 providers (usuário, câmera, sensor). Insere os filtros JWT, câmera e sensor antes do `UsernamePasswordAuthenticationFilter`. |
| `config/TimeConfig` | Bean `Clock` UTC (injetável, facilita testes do JWT). |
| `api/controller/AuthController` | `POST /api/v1/auth/login`: autentica no `AuthenticationManager`, extrai a role da authority `ROLE_*` e devolve `LoginResponse` com o token Bearer. Falha → 401. |
| `api/dto/LoginRequest` | `email` (validado) + `password`. |
| `api/dto/LoginResponse` | `token`, `tokenType` ("Bearer"), `email`, `role`. Fábrica `bearer(...)`. |
| `application/CustomUserDetailsService` | Carrega `User` por e-mail para o Spring Security. |
| `domain/User` | Entidade `app_user`: `email` (único), `passwordHash` (BCrypt), `role`, `enabled`, `fullName`. |
| `domain/CustomUserDetails` | Adapta `User` para `UserDetails`; authority = `ROLE_<ROLE>`; `isEnabled` reflete `user.enabled`. |
| `domain/enums/Role` | `ADMIN`, `HOME_USER`, `DEVICE_USER`, `AI_USER`, `DEVELOPER`, `TECHNICIAN`, com rótulo e helper `authority()`. |
| `domain/jwt/JwtProperties` | `@ConfigurationProperties("app.jwt")` validado: `secret` (≥32) e `expiration` (horas, padrão 1h). |
| `domain/jwt/JwtTokenProvider` | Gera JWT HMAC-SHA (subject = e-mail, claim `role`, `iat`, `exp`) e faz `parse` seguro (retorna `Optional.empty()` em token inválido/expirado). |
| `domain/jwt/JwtAuthenticationFilter` | Lê `Authorization: Bearer`, valida o token, recarrega o usuário (garante que ainda existe e está habilitado) e popula o `SecurityContext`. Nunca lança — token ruim = requisição anônima. |
| `config/device/DeviceAccessKeyAuthenticationFilter` | Filtro abstrato: para URIs com um prefixo específico, lê `X-Device-Id` (UUID) e `X-Access-Key`, autentica via `AuthenticationManager` e popula o contexto. Erros são silenciosos (segue sem autenticação). |
| `config/device/CamAccessKeyAuthenticationFilter` | Especialização para prefixo `/api/v1/cam/`. |
| `config/device/SensorAccessKeyAuthenticationFilter` | Especialização para prefixo `/api/v1/sensor/`. |
| `config/device/DeviceAuthenticationToken` | Token abstrato com dois estados: não autenticado (id + chave) e autenticado (principal + authorities, sem credencial). |
| `config/device/CamDeviceAuthenticationToken` / `SensorDeviceAuthenticationToken` | Tipos concretos que permitem ao `ProviderManager` escolher o provider certo. |
| `config/device/CamDeviceAuthenticationProvider` | Valida a chave via `CamAuthService`; sucesso → principal `CAM:<id>`, authority `DEVICE_CAM`. |
| `config/device/SensorDeviceAuthenticationProvider` | Valida via `SensorAuthService`; sucesso → principal `SENSOR:<id>`, authority `DEVICE_SENSOR`. |
| `exceptions/RoleNotFoundException` (404), `RoleUnauthorizedException` (401), `UserNotFoundException` (404) | Exceções ProblemDetail do módulo (ainda não usadas). |
| `infrastructure/UserRepository` | `findByEmail`, `existsByEmail`. |

> Não existe endpoint de cadastro de usuário: usuários precisam ser inseridos direto no banco (senha já em BCrypt).

### 6.4 `sensor` — sensores IoT via MQTT

É o módulo mais completo. Um sensor pertence obrigatoriamente a uma zona e é endereçado no MQTT por `home/<zona>/<sensor>/<tipo>`.

#### Domínio

| Classe | O que faz |
|---|---|
| `domain/Sensor` | Entidade `sensor` (única por `name`+`zone_id`). Campos: `name` (≤15 chars, seguro para tópico MQTT), `sensorNiche`, `sensorStatus` (inicia `INITIALIZING`), `activationTime`, `memoryUsed`, `dataTransferValue`, `dataDescription`, `lastCommunication`, `lastReadingAt`, `installation`, `zone`, `powerSupply`, `accessKey` (hash, oculto no JSON). Campos de telemetria têm `@OptimisticLock(excluded=true)` para não gerar conflito de versão a cada leitura. **Regras:** `applyDiagnostics` (telemetria → status: `FAULT` se `status=false`, mantém `MAINTENANCE_REQUIRED`, `OUT_OF_SPECIFICATION` se transferência > limiar, senão `OK`); `applyReportedStatus` (status enviado pelo próprio sensor, respeitando manutenção); descarta leituras fora de ordem; só persiste se o status mudou ou se venceu o *heartbeat*; `requestMaintenance`/`clearMaintenance`; `markDisconnected`. Valida o nome em `@PrePersist/@PreUpdate`. |
| `domain/SensorDiagnosticsThresholds` | Config `sensor.diagnostics`: limiar de transferência (95) e heartbeat de escrita (60s). |
| `domain/enums/SensorStatus` | `DISCONNECTED`, `INITIALIZING`, `OK`, `OUT_OF_SPECIFICATION`, `MAINTENANCE_REQUIRED`, `FAULT`, com código numérico (usado no payload MQTT), `isOperational()` e `isDeviceReportable()`. |
| `domain/enums/SensorNiche` | Categoria do sensor (SECURITY, PRESENCE, HYDRAULIC, GAS, FIRE, AIR_QUALITY… 20 nichos + UNKNOWN), com código numérico. Usado para escolher o handler de detecção. |
| `domain/enums/SensorCommand` | Comandos enviados ao dispositivo: `READ_NOW`, `RESTART`, `CALIBRATE`, `SELF_TEST`, `MUTE` (+ `wireValue`). |
| `domain/payload/SensorDetectionPayload` | JSON de detecção: `category`, `description`, `detected_at`. |
| `domain/payload/SensorStatusPayload` | JSON de status: `status_code` → `SensorStatus`. |
| `domain/payload/SensorTelemetryPayload` | JSON de telemetria: `status`, `memoru_used` (sic), `data_transfer_value`, `data_description`, `power_percentege` (sic), `power_charging`. |

#### Eventos internos (`sensor/event`)

`SensorEvent` é uma *sealed interface* (`eventId`, `sensorId`, `occurredAt`) com as implementações:

| Evento | Direção | Significado |
|---|---|---|
| `SensorTelemetryEvent` | entrada | Telemetria recebida via MQTT |
| `SensorStatusReportedEvent` | entrada | Status informado pelo próprio sensor |
| `SensorDetectionEvent` | entrada/saída | Detecção recebida; é republicada como evento Spring |
| `SensorStatusEvent` | **saída** | Mudança de status calculada pelo servidor (auditoria) |
| `SensorPowerSupplyEvent` | saída | Mudança de estado de alimentação (definido, ainda não publicado) |

#### Pipeline MQTT (`sensor/mqtt` + `sensor/application/event`)

| Classe | O que faz |
|---|---|
| `mqtt/SensorMqttProperties` | Config `sensor.mqtt.*` com padrões (clean session, persistência em disco, 4 threads, fila 5000, reconexão 5s). |
| `mqtt/MqttSensorClient` | Cliente Paho. No `@PostConstruct` conecta com retry, assina `home/+/+/detection`, `home/+/+/status`, `home/+/+/telemetry` (QoS 1) e re-assina ao reconectar. Cada mensagem vai para um pool de ingestão limitado (`CallerRunsPolicy` = *backpressure*). Expõe `publishCommand`, `isConnected`, `isSubscriptionHealthy`, `secondsSinceLastMessage`. Encerra drenando a fila por até 20s. Avisa em log se conectar sem credenciais. |
| `mqtt/MqttHealthIndicator` | Health `mqtt` do Actuator: UP se conectado, inscrito e sem silêncio maior que `max-silence-seconds`. |
| `mqtt/RawSensorTopic` | Quebra `home/<zona>/<sensor>/<tipo>` em partes. |
| `mqtt/SensorTopic` | Tópico já resolvido: `(sensorId, eventType)`. |
| `application/event/SensorEventHandler` | Entrada do pipeline: parse do tópico → busca o sensor por nome + zona → cria o evento → processa. |
| `application/event/SensorEventFactory` | Converte `(tipo, payload JSON)` no `SensorEvent` correto; tipo desconhecido → `UnsupportedSensorEventException`, JSON inválido → `SensorEventParseException`. |
| `application/event/SensorEventProcessor` | Chama o applier com **até 3 tentativas** em caso de conflito otimista, com backoff + jitter. |
| `application/event/SensorEventApplier` | Aplica o evento dentro de transação: telemetria (diagnóstico + leitura de bateria), status reportado, detecção (delegando ao `SensorNicheReadingHandler` do nicho, se houver, e publicando o evento Spring). Publica `SensorStatusEvent` quando o status muda. |
| `application/event/SensorNicheReadingHandler` | Extensão por nicho: implemente e registre como bean para interpretar detecções de um nicho (nenhuma implementação existe ainda). |
| `application/event/SensorInactivityScanner` | Tarefa `@Scheduled` que marca como `DISCONNECTED` sensores sem leitura há mais que a janela e publica `SensorStatusEvent`. |

#### Aplicação, API e segurança

| Classe | O que faz |
|---|---|
| `application/SensorService` / `SensorServiceImpl` | CRUD com zona obrigatória; `replaceSensor` exige versão (If-Match) e `patchSensor` a verifica se enviada; bloqueia delete de sensor operacional; manutenção; `sendCommand` publica em `home/<zona>/<sensor>/command`. Conflitos de versão → 409. |
| `application/auth/SensorAuthService` / `Impl` | Valida a chave de acesso (comparando hash) e atualiza `lastCommunication`. |
| `security/SensorAccessKeyHasher` | SHA-256 + Base64 da chave; comparação em tempo constante. |
| `api/controller/SensorController` | Endpoints de §5; gera/lê `ETag` a partir de `version`. |
| `api/dto/SensorRequest` | Criação: `name`, `sensor_niche`, `data_description`, `zone_uuid`, `installation`, `power_supply`. |
| `api/dto/SensorReplaceRequest` | PUT: `name`, `data_description`, `zone_uuid`, `installation` (nicho não muda). |
| `api/dto/SensorPatchRequest` | PATCH: mesmos campos, todos opcionais; `isEmpty()` → 400. |
| `api/dto/SensorResponse` | Visão completa do sensor, incluindo `status`, `version` e `zone_uuid`. |
| `api/dto/SensorMapper` | Mapeia e aplica replace/patch na entidade. |
| `api/dto/SensorConstraints` | Regex/mensagem do nome (`^[A-Za-z0-9_-]{1,15}$`). |
| `infrastructure/SensorRepository` | Buscas por nome+zona, contagem por zona, paginação com `zone` carregada (`@EntityGraph`) e `findStaleSensors`. |

Exceções do módulo (todas ProblemDetail):

| Exceção | HTTP | Quando |
|---|---|---|
| `SensorNotFoundException` | 404 | Sensor inexistente |
| `SensorUnavailableException` | 404 | Sensor indisponível (não usada) |
| `SensorAuthenticationException` | 401 | Chave inválida |
| `SensorAccessDeniedException` | 403 | Acesso negado (não usada) |
| `SensorConcurrentModificationException` | 409 | Lock otimista |
| `SensorStillActiveException` | 409 | Delete de sensor operacional |
| `SensorWithoutZoneException` | 409 | Comando para sensor sem zona |
| `SensorPreconditionFailedException` | 412 | `If-Match` diferente da versão atual |
| `SensorPreconditionRequiredException` | 428 | PUT sem `If-Match` |
| `SensorPatchEmptyException` | 400 | PATCH vazio |
| `SensorEventParseException` / `UnsupportedSensorEventException` | 400 | Payload/tipo MQTT inválido |
| `SensorRateLimitExceededException` | 429 | Rate limit, com `Retry-After` (não usada) |
| `SensorCommandDeliveryException` | 503 | Falha ao publicar comando no broker |

### 6.5 `cam` — câmeras

Hierarquia JPA `JOINED`: `BaseCam` (tabela `base_cam`) → `CamFixed` (`fixed_cam`) e `CamPtz` (`ptz_cam`).

#### Domínio

| Classe | O que faz |
|---|---|
| `domain/base/BaseCam` | Dados comuns: modelo, firmware, serial e MAC (únicos), IP, `rtsp`, `camStatus`, resolução (`width`/`height` → `imageQuality` calculada por `setResolution`), fps, bitrate, compressão, URL/horário do frame, `powerSupply`, `installation`, `zone`, `camConnection`, `accessKey`, `lastCommunication`, `homologation`. Métodos: `assignAccessKey` (uma única vez), `rotateAccessKey`, `recordCommunication`, `configureCamConnection` (valida parâmetros exigidos por protocolo). |
| `domain/base/enums/CamStatus` | `OFFLINE`, `ONLINE`, `ERROR`. |
| `domain/base/enums/ImageQuality` | `LOW` (≤640×480), `LOW_MEDIUM` (≤720p), `MEDIUM` (≤1080p), `HIGH` (≤8K), `UNKNOWN`. |
| `domain/fixed/CamFixed` | Câmera fixa: `coverageLocation` (para onde aponta), azimute/elevação de montagem, FOV horizontal/vertical, alcance de detecção, máscara de detecção (JSON), matriz de homografia (JSON), frame *baseline* + horário (para IA comparar cena vazia), linha de cruzamento (JSON) e limiar de confiança. |
| `domain/ptz/CamPtz` | Câmera PTZ (pan/tilt/zoom, presets, último comando, `isMoving`). Sem serviço/controller ainda. |
| `domain/connectioncam/CamConnectionProfile` | `@Embeddable` com protocolo, `streamUri` (RTSP/NATIVE), `host`/`port` (ONVIF/SDK), `vendorSdkId`, `credentialRef` (**referência** a cofre, nunca a senha), capacidades e `NetworkLink`. |
| `domain/connectioncam/CamConnectionParams` | Parâmetros de entrada para `configureCamConnection`. |
| `domain/connectioncam/CamConnectionTestProbe` | **Porta de domínio** para testar conexão; implementada por `SocketRtspHandshakeClient`. |
| `domain/connectioncam/enums/CamProtocol` | `NATIVE` (hardware próprio), `RTSP`, `ONVIF`, `PROPRIETARY_SDK`. |
| `domain/connectioncam/enums/CamCapability` | `PTZ_CONTROL`, `TWO_WAY_AUDIO`, `NIGHT_VISION`, `ONBOARD_MOTION_DETECTION`, `ONBOARD_RECORDING`. |
| `domain/homologation/CamHomologationRecord` | Máquina de estados da homologação (embutida na câmera): `applyTestResult` (sucesso → `PENDING_APPROVAL` com codec; falha → `TEST_FAILED`), `approve`/`reject` (só a partir de `PENDING_APPROVAL`), guardando quem decidiu, quando e motivo. |
| `domain/homologation/CamConnectionTestOutcome` | Resultado do teste: sucesso + codec, ou falha + mensagem. |
| `domain/homologation/enums/CamHomologationStatus` | `PENDING_TEST` → `TEST_FAILED` / `PENDING_APPROVAL` → `APPROVED` / `REJECTED`. |

#### Aplicação

| Classe | O que faz |
|---|---|
| `application/fixed/CamFixedService` / `Impl` | CRUD de câmeras fixas; resolve `zone` e `coverageLocation`; registra `baselineCapturedAt` quando o frame *baseline* é definido/alterado; conflito de versão → 409. |
| `application/CamHomologationService` / `Impl` | `runConnectionTest` (exige conexão configurada e protocolo RTSP/NATIVE, chama a probe e grava o resultado), `approve`, `reject`. Transições inválidas → 409. |
| `application/auth/CamAuthService` / `Impl` | Autentica câmera pela `accessKey` (comparação em tempo constante), registra `lastCommunication`; `requireValidAccessKey` lança 401. |
| `application/auth/CamAccessKeyGenerator` | Gera chave de 256 bits (Base64 URL-safe). Ainda não é chamado por nenhum fluxo. |
| `application/auth/enums/CamAuthMessage` | `AUTORIZADO` / `NEGADO`. |

#### Infraestrutura

| Classe | O que faz |
|---|---|
| `infrastructure/CamRepository`, `CamFixedRepository`, `CamPtzRepository` | Repositórios JPA por nível da hierarquia. |
| `infrastructure/protocol/SocketRtspHandshakeClient` | Implementação da probe: valida a URI (`rtsp://`, sem credenciais embutidas, porta padrão 554), abre socket com timeout de 5s, envia `RTSP DESCRIBE`, trata `401` com `WWW-Authenticate` montando **Digest** (RFC 2617, sem `qop`) ou **Basic**, faz retry em nova conexão e extrai o codec do SDP. Limita o corpo a 64 KB e zera buffers de senha após o uso. |
| `infrastructure/protocol/SdpMetadataExtractor` | Lê o SDP e devolve o codec de vídeo (`a=rtpmap`) ou, na falta, o payload estático RFC 3551 (JPEG, MPV, MP2T, H263). |
| `infrastructure/protocol/CamCredentialResolver` | Porta para resolver `credentialRef` → credenciais reais (cofre). |
| `infrastructure/protocol/DevCamCredentialResolver` | Implementação só do perfil `dev`: sempre retorna `null` (câmeras sem senha). |
| `infrastructure/protocol/RtspCredentials` | Credencial `AutoCloseable` com senha em `char[]`, zerada no `close()`; `toString` não expõe a senha. |

#### API

| Classe | O que faz |
|---|---|
| `api/controller/CamFixedController` | CRUD de câmeras fixas (§5). |
| `api/controller/CamHomologationController` | Teste, aprovação e rejeição de homologação. |
| `api/controller/auth/CamAuthController` | Autenticação da câmera: 200 se `AUTORIZADO`, 401 caso contrário. |
| `api/dto/fixed/CamFixedRequest` / `CamFixedResponse` / `CamFixedMapper` | Entrada/saída e mapeamento da câmera fixa. A resposta expõe `has_access_key` (nunca a chave). |
| `api/dto/fixed/CamConnectionSummary`, `CamNetworkLinkSummary`, `CamHomologationSummary` | Projeções de leitura dos embutidos (sem `credentialRef`). |
| `api/dto/auth/CamAuthRequest` / `CamAuthResponse` / `CamHomologationDecisionRequest` | DTOs de auth e decisão. |
| `api/dto/RtspResponse` | Resposta RTSP parseada (status, headers minúsculos, corpo). |
| `api/ptz/CamPtzMapper`, `CamPtzRequest`, `CamPtzResponse` | Esqueletos vazios para a futura API PTZ. |

#### Exceções

| Exceção | HTTP / uso |
|---|---|
| `CamNotFoundException` | 404 |
| `CamAuthenticationException` | 401 |
| `CamConcurrentModificationException` | 409 |
| `CamHomologationNotEligibleException` | 409 |
| `rtsp/RtspException` (+ `RtspNotFoundException` 404, `RtspSessionNotFoundException` 504) | Runtime exceptions de protocolo RTSP (carregam o status RTSP; ainda não lançadas). |

### 6.6 `connection` — abstração de conexão/rede

Módulo em construção que pretende padronizar "como falar com um dispositivo" independente do protocolo.

| Classe | O que faz |
|---|---|
| `domain/Connection` | Classe abstrata: protocolo de aplicação, tipo de enlace, `establishedAt`, `isReachable()`. |
| `domain/WiredConnection` / `WirelessConnection` | Implementações com *handle* do cliente real; a sem fio considera alcançável se sinal > -90 dBm. |
| `domain/ConnectionParams` | Parâmetros validados (porta 1–65535, sinal só em enlace sem fio) com fábricas `rtsp`, `onvif`, `mqtt`, `http`, `webSocket` e `withSignal`. |
| `domain/NetworkLink` | `@Embeddable` (usado por `CamConnectionProfile`): tipo de enlace, IP, dBm; `updateSignal` só para sem fio. |
| `domain/RtspConnectionResolver`, `MqttConnectionResolver` | Resolvers **stub** (`supports` sempre `false`); a implementação real está comentada. |
| `domain/ProtocolDiscovery`, `rtsp/application/RtspStreamManager` | Classes vazias (placeholders). |
| `infrastructure/ConnectionResolver` | Estratégia: `supports(protocol)` + `resolve(params)`. |
| `application/FindConnection` | Escolhe o primeiro resolver que suporta o protocolo; nenhum → `ConnectionNotFoundException(UNSUPPORTED_PROTOCOL)`. |
| `exceptions/ConnectionNotFoundException` + `enums/ConnectionError` | Erro de conexão com código (`TIMEOUT`, `HOST_UNREACHABLE`…). |
| `api/controller/ConnectionExceptionHandler` | Converte `ConnectionNotFoundException` em **503** com `ErrorResponse(code, message)`. |
| `domain/enums/ApplicationProtocol` | Catálogo de protocolos (HTTP, MQTT, CoAP, RTSP, ONVIF, WebRTC, Modbus, SNMP…). |
| `domain/enums/LinkType` | Enlace físico (ETHERNET, WIFI, BLE, ZIGBEE, THREAD, LORA, 4G, 5G, FIBER) com `ConnectionType` e se suporta IP. |
| `domain/enums/ConnectionType`, `NetworkProtocol`, `TransportProtocol` | `WIRED/WIRELESS`; `IPV4/IPV6/IPV8`; `TCP/UDP/QUIC`. |

### 6.7 `device` — dispositivos alvo de notificação

`Device` representa smartwatch, TV, celular etc. que **recebem** alertas (ver README).

| Classe | O que faz |
|---|---|
| `domain/Device` | Entidade `device`: `deviceName`, `deviceType`, `ipAdress`, `status`, `lastCommunication`, `accessKey` (única, em texto puro). |
| `domain/DeviceSession` | Registro de sessão/mensagem do dispositivo (timestamp + `DeviceMessage`). Sem serviço. |
| `domain/enums/DeviceMessage` | `AUTHORIZED`, `UNAUTHORIZED`, `ERROR`, `DENIED`, `FAILURE`, `ALERT`, `STORAGE_FULL`, `UNKNOWN`. |
| `application/DeviceService` / `DeviceServiceImp` | CRUD; na criação gera a chave com `DeviceKeyGenerator` (ignora a enviada) e inicializa `lastCommunication`. |
| `application/auth/DeviceAuthService` / `DeviceAuthServiceImp` | `authenticate` (retorna `AUTHORIZED`/`DENIED` e atualiza `lastCommunication`), `requireValidAccessKey` (lança exceção), `rotateAccessKey` (gera e devolve nova chave). |
| `api/controller/DeviceController` | CRUD em `/api/v1/device` (ADMIN). |
| `api/controller/auth/DeviceAuthController` | `/device/{id}/authenticate` e `/device/{id}/rotate-key`. |
| `api/dto/DeviceRequest`, `DeviceResponse`, `DeviceMapper` | Entrada/saída/mapeamento (a resposta não expõe a chave). |
| `api/dto/DeviceRegistrationRequest`, `DeviceUpdateRequest` | DTOs alternativos não utilizados. |
| `api/dto/auth/DeviceAuthRequest` / `DeviceAuthResponse` | `access_key` → `(result, message)`. |
| `api/dto/auth/DeviceKeyGenerator` | Chave aleatória de 256 bits em Base64. |
| `exceptions/DeviceNotFoundException`, `DeviceAuthenticationException` | `RuntimeException` simples (sem ProblemDetail → viram 500). |
| `exceptions/DeviceTimeOutConnection` | `ConnectException` baseada em `DeviceSession` (não usada). |
| `exceptions/dto/DeviceErrorResponse` | DTO de erro (não usado). |
| `infrastructure/DeviceRepository` | Repositório JPA. |

### 6.8 `zone` — zonas monitoradas

| Classe | O que faz |
|---|---|
| `domain/Zone` | Área circular: `name`, `description`, `zoneType`, `centerLatitude/Longitude`, `radiusMeters`, `active`, `createdZoneAtDay`. O **nome** da zona compõe o tópico MQTT dos sensores. |
| `domain/enums/ZoneType` | `PERIMETER`, `ENTRANCE`, `INTERNAL`, `RESTRICTED`, `EXTERNAL`. |
| `application/ZoneService` / `ZoneServiceImp` | CRUD; `requireZone` é usado por sensor, câmera, location e telemetry; delete bloqueado (409 `ZoneInUseException`) se houver sensores na zona. |
| `api/controller/ZoneController`, `api/dto/ZoneRequest`, `ZoneResponse`, `ZoneMapper` | API e DTOs. |
| `exceptions/ZoneNotFoundException` (404), `ZoneInUseException` (409) | |
| `infrastructure/ZoneRepository` | Repositório JPA. |

### 6.9 `location` — locais

| Classe | O que faz |
|---|---|
| `domain/Location` | Ponto nomeado com latitude/longitude, obrigatoriamente ligado a uma `Zone` (validado em `@PrePersist/@PreUpdate` → 422 via `GlobalExceptionHandler`). Usado por `Installation.location` e `CamFixed.coverageLocation`. |
| `application/LocationService` / `LocationServiceImp` | CRUD + `requireLocation`. |
| `api/controller/LocationController`, `api/dto/LocationRequest`, `LocationResponse`, `LocationMapper` | API e DTOs. |
| `exceptions/LocationNotFoundException`, `LocationIdNotFoundException` | 404. |
| `infrastructure/LocationRepository` | Repositório JPA. |

### 6.10 `telemetry` — medições

| Classe | O que faz |
|---|---|
| `domain/Telemetry` | Medição inteira (`measuredValue`) em `measuredAt`, ligada a um sensor e a uma zona. |
| `application/TelemetryService` / `TelemetryServiceImpl` | CRUD manual (valida zona e sensor; `measuredAt` padrão = agora) e consultas que conferem se a telemetria pertence à zona/sensor informado (senão 404). `findLatestBySensor` devolve o histórico ordenado. |
| `api/controller/TelemetryController` | Endpoints de §5. |
| `api/dto/TelemetryRequest`, `TelemetryResponse`, `TelemetryMapper` | DTOs e mapeamento. |
| `exceptions/TelemetryNotFoundException`, `TelemetryNullException` (404), `TelemetryConcurrentModificationException` (409) | |
| `infrastructure/TelemetryRepository` | Histórico por sensor, por zona e por janela de tempo. |

> A telemetria recebida por MQTT **não** grava nesta tabela: ela atualiza os campos de diagnóstico do próprio `Sensor`.

### 6.11 `detection` — detecções

Somente domínio (sem serviço/API ainda).

| Classe | O que faz |
|---|---|
| `domain/Detection` | Entidade base (`JOINED`): `description`, `timeDetection`, `lastCommunication`. |
| `domain/SensorDetection` | Detecção de sensor: `sensorDetectionType` + `sensor`. |
| `domain/CamDetection` | Detecção de câmera: `camDetectionType` + `camFixed` ou `camPtz`. |
| `domain/enums/SensorDetectionType` | `GAS`, `CO2`, `LPG`, `HIGH_HEAT`, `EXTREME_HEAT`. |
| `domain/enums/CamDetectionType` | `MOVING_ANIMAL`, `OBJECT`, `MOVING_OBJECT`, `ANIMAL`. |
| `domain/enums/DeviceDetectionType` | Vazio (reservado). |

### 6.12 `event` — eventos do sistema

Somente domínio.

| Classe | O que faz |
|---|---|
| `domain/Event` | Evento/alerta ligado a uma `Detection` e a uma `Zone`, com descrição, horário, tipo, status, categoria e severidade. |
| `domain/enums/EventType` | O que aconteceu (`DETECTED`, `THRESHOLD_EXCEEDED`, `CONNECTION_LOST`, `TAMPERING_DETECTED`…). |
| `domain/enums/EventStatus` | Ciclo de vida: `TRIGGERED` → `ACKNOWLEDGED` → `RESOLVED` / `CLEARED`. |
| `domain/enums/EventCategory` | `DETECTION`, `STATE`, `SYSTEM`, `COMMUNICATION`, `RESOURCE`, `SECURITY`, `ERROR`, `UNKNOWN`. |
| `domain/enums/Severity` | `INFO` → `CRITICAL`. |

### 6.13 `batterysupply` — alimentação elétrica

| Classe | O que faz |
|---|---|
| `domain/PowerSupply` | `@Embeddable` (em `Sensor` e `BaseCam`): tipo, status, percentual, recarregável, última leitura. `applyReading` saneia o percentual (0–100), ignora leitura fora de ordem, resolve o status (`CHARGING`, `CRITICAL`, `LOW`, `NORMAL`; `NOT_APPLICABLE` para fontes sem bateria) e só persiste se mudou ou venceu o heartbeat. `markDisconnected` para fontes com bateria. Retorna `PowerOutcome`. |
| `domain/PowerSupplyStatus` | `NORMAL`, `LOW`, `CRITICAL`, `CHARGING`, `DISCONNECTED`, `NOT_APPLICABLE`. |
| `domain/PowerSupplyType` | `BATTERY`, `WIRED`, `RECHARGEABLE_BATTERY`, `POWER_GRID`, `SOLAR`, `HYBRID`, `NONE`. |
| `domain/PowerSupplyThresholds` | Config `sensor.power.*` (30% / 15% / 15m). |

### 6.14 `installation` — dados de instalação

| Classe | O que faz |
|---|---|
| `domain/Installation` | `@Embeddable`: `location`, `installedAt`, `installedBy`, `installationStatus`, `installationNotes`. |
| `domain/enums/InstallationStatus` | `ACTIVE`, `RELOCATED`, `REMOVED`, `MAINTENANCE`. |

---

## 7. Fluxos principais

### 7.1 Login de usuário

```
POST /api/v1/auth/login {email, password}
  → AuthenticationManager → DaoAuthenticationProvider → CustomUserDetailsService (BCrypt)
  → JwtTokenProvider.generateToken(email, role)
  ← {token, token_type: "Bearer", email, role}

Requisições seguintes: Authorization: Bearer <token>
  → JwtAuthenticationFilter valida, recarrega o usuário, popula o SecurityContext
  → @PreAuthorize nos controllers
```

### 7.2 Ingestão de evento de sensor (MQTT)

```
Sensor publica em home/<zona>/<sensor>/<detection|status|telemetry>
  → MqttSensorClient.messageArrived  → pool "sensor-ingest"
  → SensorEventHandler     (tópico → Sensor por nome+zona)
  → SensorEventFactory     (JSON → SensorEvent)
  → SensorEventProcessor   (até 3 tentativas em conflito otimista)
  → SensorEventApplier     (@Transactional)
        telemetry → Sensor.applyDiagnostics + PowerSupply.applyReading
        status    → Sensor.applyReportedStatus
        detection → SensorNicheReadingHandler do nicho + publishEvent
  → se o status mudou: publica SensorStatusEvent (Spring)

Em paralelo: SensorInactivityScanner marca DISCONNECTED quem ficou em silêncio.
Comando: POST /api/v1/sensors/{id}/commands/{cmd} → MQTT home/<zona>/<sensor>/command
```

### 7.3 Edição de sensor com concorrência otimista

```
GET  /api/v1/sensors/{id}          ← ETag: "7"
PUT  /api/v1/sensors/{id}  If-Match: "7"
     sem If-Match   → 428
     versão ≠ 7     → 412
     corrida no DB  → 409
     ok             ← 200, ETag: "8"
```

### 7.4 Homologação de câmera

```
(câmera cadastrada + camConnection configurada, protocolo RTSP/NATIVE)
POST /api/v1/cam/{id}/homologation/test
  → SocketRtspHandshakeClient: DESCRIBE → (401 → Digest/Basic → DESCRIBE) → codec do SDP
  → CamHomologationRecord: PENDING_APPROVAL (ok) | TEST_FAILED (falha, com motivo)
POST .../approve  (ADMIN) → APPROVED
POST .../reject   (ADMIN) → REJECTED (com motivo)
```

> Hoje nenhum endpoint chama `BaseCam.configureCamConnection`, então a conexão precisa ser gravada por outro meio antes do teste.

---

## 8. Testes

| Classe | Situação |
|---|---|
| `DetectionSystemApplicationTests` | `contextLoads` — sobe o contexto completo (precisa de MySQL, broker MQTT e `APP_JWT_SECRET`). |
| `device/application/DeviceTestService` | Esqueleto sem testes; `@InjectMocks` sobre a **interface** `DeviceService` (não instanciável). |
| `sensor/application/SensorTestService` | Idem, sobre `SensorService`. |

Não há cobertura de testes efetiva das regras de domínio.

---

## 9. Pontos de atenção que estão sendo corrigidos


**Segurança / rotas**

1. `SecurityConfig` libera `/api/v1/cam/auth/**` e `/api/v1/sensor/auth/**`, mas o endpoint real é `/api/v1/cam/{camId}/auth` → a autenticação da câmera exige JWT. Da mesma forma, `/api/v1/cam/homologation/**` não casa com `/api/v1/cam/{camId}/homologation/...`, então a restrição a `TECHNICIAN/ADMIN` do teste não se aplica (qualquer usuário autenticado pode testar).
2. `SensorAccessKeyAuthenticationFilter` usa o prefixo `/api/v1/sensor/`, mas o controller está em `/api/v1/sensors` → o filtro nunca age. Também não existe controller de autenticação de sensor, e `createSensor` não gera `accessKey`.
3. `LocationController` usa `hasRole('Admin')`; a authority é `ROLE_ADMIN`, logo create/update/delete de location sempre retornam 403.
4. `DeviceAuthController` está em `/device` (fora de `/api/v1` e do CORS), e `authenticate` exige JWT. A chave do `Device` é guardada em texto puro e comparada com `equals` (sensor usa hash + tempo constante).
5. Telemetria (inclusive POST/PUT/DELETE) aceita qualquer usuário autenticado, sem `@PreAuthorize`.

**Bugs prováveis**

6. `PowerSupply.applyReading` retorna `new PowerOutcome(previus, PowerSupplyStatus.DISCONNECTED, true)` em vez do status resolvido.
7. `SensorEventApplier.applyPowerReading` chama `power.getStatus()` quando `power == null` → `NullPointerException` para sensores sem `powerSupply`.
8. Não há `@EnableScheduling` → o `SensorInactivityScanner` nunca executa.
9. Não há `@EnableJpaAuditing` nem `AuditorAware` → `createdAt`, `updatedAt`, `createdBy`, `lastModifiedBy` ficam nulos.
10. `DevCamCredentialResolver` só existe no perfil `dev`; em qualquer outro perfil não há bean `CamCredentialResolver` e a aplicação não sobe.
11. `TelemetryController` mapeia `/all` como `"/api/v1/telemetry/all"` dentro de um controller já prefixado → rota final `/api/v1/telemetry/api/v1/telemetry/all`.
12. `SensorPatchRequest.name` tem `@NotBlank`, o que torna o nome obrigatório num PATCH que deveria ser parcial.
13. `ZoneServiceImp.updateZone` força `active = true`, ignorando o valor enviado.
14. `CamDetectionType.fromDescription` percorre e retorna `SensorDetectionType`. `CamDetection` usa `@Enumerated` ordinal (frágil a reordenação).
15. `DeviceNotFoundException`/`DeviceAuthenticationException` são `RuntimeException` sem handler → respondem 500. `DeleteMapping` de device/location devolve 204 com corpo.
16. `DeviceRequest.accessKey` é `@NotBlank`, mas o serviço descarta o valor e gera outra chave.

**Configuração / build**

17. `application.properties`: `spring.datasouce.password` (typo — a senha não é aplicada) e `ddl-auto` declarado duas vezes.
18. `pom.xml` define `jjwt.version=0.12.6`, mas as dependências usam `0.11.5` fixo.
19. Payload de telemetria usa chaves com erro de digitação (`memoru_used`, `power_percentege`) — é o contrato atual com o firmware; mudar exige coordenação.

**Esqueletos / código não usado**

`ProtocolDiscovery`, `RtspStreamManager`, `CamPtzMapper/Request/Response`, `DeviceDetectionType`, resolvers de conexão stub, `CamAccessKeyGenerator`, `SensorPowerSupplyEvent`, `DeviceSession`, `Detection`/`Event` (sem serviço), várias exceções não lançadas e os métodos estáticos `toEntity`/`toResponse` duplicados nos DTOs.
