# GRPC_Service

## О проекте

Учебная микросервисная система из двух Spring Boot-приложений. **auth-service** — сервис авторизации, работает по gRPC (регистрация, вход, смена и сброс пароля, проверка JWT). **store** — REST API магазина (клиенты, поставщики, товары, изображения); каждый запрос он проверяет, вызывая `AuthService/ValidateToken` по gRPC. У каждого сервиса своя база PostgreSQL и свои Flyway-миграции.

## Стек

| Компонент | Технологии |
|---|---|
| Общее | Java 21, Spring Boot 4.1.1, PostgreSQL, Flyway (`ddl-auto=validate`), Lombok |
| auth-service | Maven, spring-grpc, grpc-java 1.84.0, protobuf 4.36.2, protovalidate 1.3.0, Spring Security, JWT |
| store | Gradle (wrapper 9.7.1), Spring Web, Spring Data JPA, MapStruct, springdoc-openapi (Swagger UI), gRPC-клиент к auth-service |
| Инфраструктура | Docker, Docker Compose, PostgreSQL 18 (две отдельные БД) |

Контракт между сервисами: `src/contracts/proto/auth.proto` (используется обоими сервисами при сборке).

## Быстрый старт

Нужны Docker и Docker Compose v2. Файл `gradle-wrapper.jar` хранится в Git LFS, поэтому перед клонированием выполните `git lfs install`.

```bash
git clone https://github.com/Mon600/GRPC_Service.git
cd GRPC_Service
cp .env.example .env
docker compose up --build
```

Порядок старта обеспечивают healthcheck'и: `store-db`, `auth-db` → `auth` (порт 9090) → `store` (порт 8080).

| Что | Адрес |
|---|---|
| Swagger UI store | http://localhost:8080/swagger/index.html |
| OpenAPI JSON | http://localhost:8080/api-docs |
| gRPC auth-service | `localhost:9090` (только с localhost) |
| PostgreSQL store / auth | `127.0.0.1:5432` / `127.0.0.1:5433` |

Остановка: `docker compose down` (с удалением данных: `docker compose down -v`).

## Локальная сборка и отладка

Подготовьте две БД (можно поднять только их):

```bash
docker compose up -d store-db auth-db
```

**auth-service** (Maven, из `src/auth-service/service`):

```bash
cd src/auth-service/service
./mvnw -DskipTests package
DB_URL=jdbc:postgresql://localhost:5433/auth_db DB_USER=auth DB_PASSWORD=auth_dev_password \
SECRET=local-development-only-jwt-secret-change-before-server-deployment EXPIRATION_TIME=900000 \
SPRING_GRPC_SERVER_PORT=9090 ./mvnw spring-boot:run
```

**store** (Gradle, из `src/store`):

```bash
cd src/store
./gradlew test bootJar
DB_URL=jdbc:postgresql://localhost:5432/store_db DB_USERNAME=store DB_PASSWORD=store_dev_password \
AUTH_GRPC_ADDRESS=localhost:9090 ./gradlew bootRun
```

Удалённая отладка в контейнерах: добавьте в `.env` в `JAVA_OPTS` значение
`-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005` и пробросьте порт 5005 в `docker-compose.yml`.
Тесты используют Testcontainers, для них нужен запущенный Docker.

## Проверка функционала

### 1. gRPC (auth-service)

Рефлексия на сервере не включена, поэтому `grpcurl` вызывается с `.proto`-файлами:

```bash
# Вход под администратором из миграции V2
grpcurl -plaintext -import-path src/contracts/proto -proto auth.proto \
  -d '{"email":"admin@example.com","password":"adminpass"}' \
  localhost:9090 AuthService/Login

# Регистрация (пароль: минимум 8 символов, буква и цифра; номер в формате E.164)
grpcurl -plaintext -import-path src/contracts/proto -proto auth.proto \
  -d '{"email":"user@example.com","name":"Ivan","surname":"Ivanov","number":"+79123456789","password":"password1"}' \
  localhost:9090 AuthService/Register

# Сброс пароля
grpcurl -plaintext -import-path src/contracts/proto -proto auth.proto \
  -d '{"email":"user@example.com"}' localhost:9090 AuthService/ResetPassword
```

Публичные методы — `Register`, `Login`, `ResetPassword`; остальные (`ChangePassword`, `ValidateToken`) требуют токен в metadata:

```bash
grpcurl -plaintext -import-path src/contracts/proto -proto auth.proto \
  -H "authorization: Bearer <TOKEN>" localhost:9090 AuthService/ValidateToken
```

### 2. REST (store)

Все маршруты `/api/v1/**`, кроме `/api/v1/auth/**`, требуют заголовок `Authorization: Bearer <TOKEN>`.

```bash
# Получить токен через REST (store обращается к auth-service по gRPC)
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@example.com","password":"adminpass"}' | sed -E 's/.*"token":"([^"]+)".*/\1/')

# Список товаров, клиентов, поставщиков
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/products
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/v1/clients?limit=10&offset=0"
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/suppliers

# Списать товар (id возьмите из списка товаров)
curl -s -X PATCH -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"decreaseBy":1}' http://localhost:8080/api/v1/products/<PRODUCT_ID>/stock
```

| Группа | Маршруты |
|---|---|
| `/api/v1/auth` | `POST /register`, `/login`, `/change-password`, `/reset`, `/validate` |
| `/api/v1/clients` | `GET`, `GET /{id}`, `GET /search`, `POST`, `DELETE /{id}`, `PATCH /{id}/address` |
| `/api/v1/suppliers` | `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` |
| `/api/v1/products` | `GET`, `GET /{id}`, `POST`, `DELETE /{id}`, `PATCH /{id}/stock` |
| `/api/v1/images` | `GET /product/{productId}`, `GET /{id}`, `POST` (Base64), `PUT /{id}`, `DELETE /{id}` |

Полные схемы запросов и ответов — в Swagger UI. Тестовые клиенты, поставщики и товары создаются миграцией store `V2`.

## Структура проекта

```
.
├── docker-compose.yml          # store-db, auth-db, auth, store
├── .env.example                # все переменные окружения
└── src
    ├── contracts/proto         # auth.proto, validate.proto — общий gRPC-контракт
    ├── auth-service/service    # Maven: gRPC-сервер, JWT, Flyway (auth_db)
    └── store                   # Gradle: REST API, gRPC-клиент, Flyway (store_db)
        └── .../web             # controller, dto, security (@Authorize), mapper
```

Слои в обоих сервисах: web/gRPC → domain (бизнес-логика) → datalayer (JPA-репозитории).

## Особенности и ограничения

- Безопасность: `AUTH_JWT_SECRET` и пароли БД в `.env.example` — только для разработки; в реальном окружении замените.
- gRPC без TLS и без reflection; порт 9090 проброшен только на `127.0.0.1`.
- Контейнеры собираются из исходников (`docker compose up --build`), первый запуск скачивает зависимости Maven/Gradle.
- `gradle-wrapper.jar` в Git LFS: без `git lfs pull` сборка store не стартует.
- Сброс пароля: `ResetPassword` (публичный метод) генерирует новый пароль, сохраняет его хэш и **выводит пароль в stdout** сервиса auth (`docker compose logs auth`); отправки писем нет — это учебная заглушка, для реального окружения её нужно заменить.
- Healthcheck'и приложений проверяют открытость TCP-порта (в образе нет curl и grpc_health_probe), а не бизнес-готовность.
