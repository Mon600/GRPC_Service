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

## Проверка функционала

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
