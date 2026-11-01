## Сервис авторизации

Сервис работает по протоколу GRPC

## Модель данных пользователя

| Поле     | Тип    | Описание                               | Пример данных                                                                       |
|----------|--------|----------------------------------------|-------------------------------------------------------------------------------------|
| id       | UUID   | Идентификатор пользователя             | 123e4567-e89b-12d3-a456-426614174000                                                |
| email    | string | Уникаольный email пользователя         | test@test.ru                                                                        |
| name     | string | Имя пользователя                       | Steve                                                                               |
| surname  | string | Фамилия пользователя                   | Jobs                                                                                |
| password | string | Пароль пользователя(В БД хранится хэш) | 123456 (Пример хэша:`$2y$12$e0M2/S2n19q6u1Y9f5yX5OQW7Y17MvHwR9E0q8H1K6bZ9Yx7S2u1i`) |

## Структура проекта

```
src
└── main
    ├── java
    │   └── app
    │       ├── Application.java ---> Главный класс приложения
    │       ├── config ---> Конфигурационные классы
    │       │   └── SecurityConfig.java
    │       ├── datalayer ---> Слой данных
    │       │   ├── model ---> Модели данных Hibernate
    │       │   │   └── UserEntity.java
    │       │   └── repository ---> Репозитории
    │       │       └── UserRepository.java
    │       ├── domain ---> Слой бизнес-логики
    │       │   ├── model ---> DTO
    │       │   │   ├── ChangePasswordDto.java
    │       │   │   ├── CredentialsDto.java
    │       │   │   ├── ResetPasswordDto.java
    │       │   │   ├── ResetPasswordResponseDto.java
    │       │   │   ├── TokenDto.java
    │       │   │   └── UserDto.java
    │       │   ├── PasswordService.java
    │       │   ├── TokenService.java
    │       │   └── UserService.java
    │       ├── exceptions ---> Исключения
    │       │   ├── handler ---> Обработчики исключений
    │       │   │   └── GlobalExceptionHandler.java
    │       │   ├── InvalidTokenException.java
    │       │   ├── InvalidUuidException.java
    │       │   ├── UserAlreadyExistsException.java
    │       │   └── UserNotFoundException.java
    │       ├── interceptor ---> Интерсепторы
    │       │   ├── AuthorizationInterceptor.java
    │       │   └── ValidateInterceptor.java
    │       ├── mapper ---> Мапперы
    │       │   └── GrpcMapper.java
    │       ├── service ---> GRPC Сервисы
    │       │   └── AuthServiceImpl.java
    │       └── util ---> Вспомогательные классы
    │           └── ContextHelper.java
    ├── proto --- > Protobuf файлы
    │   ├── auth.proto
    │   └── validate.proto
    └── resources ---> Конфигурационные файлы
        ├── application.properties ---> Конфигурация приложения
        └── db.migration ---> Миграции БД
            └── V1__create_user_table.sql
```

## Запуск

```./mvnw spring-boot:run```

## Методы

Базовый URL приложения:`grpc://127.0.0.1:9090`

В проекте реализованы 4 метода:

### 1. Регистрация ('AuthService/Register')

Пример принимаемых данных:

```json
{
  "email": "example@gmail.com",
  "name": "Alfredia",
  "surname": "Papaya",
  "number": "89123456789",
  "password": "Password123-"
}
```

Пример возвращаемых данных:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhOWRhMmIyYy0wOGNlLTQ2NzItYmEwOC1mM2NlNjI3NGRmNTQiLCJlbWFpbCI6Im1pa2kucnVsbGwyMDEyQGdtYWlsLmNvbSIsImlhdCI6MTc5MTAyMzI4MCwiZXhwIjoxNzkxMDI0MTgwfQ.MoSZ2D8qLURfVL05lqZhKARtCycdXEuVkmCDJoBwD7c",
  "token_type": "Bearer"
}
```

### 2. Авторизация ('AuthService/Login')

Пример принимаемых данных:

```json
{
  "email": "example@gmail.com",
  "password": "Password123-"
}
```

Пример возвращаемых данных:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhOWRhMmIyYy0wOGNlLTQ2NzItYmEwOC1mM2NlNjI3NGRmNTQiLCJlbWFpbCI6Im1pa2kucnVsbGwyMDEyQGdtYWlsLmNvbSIsImlhdCI6MTc5MTAyMzI4MCwiZXhwIjoxNzkxMDI0MTgwfQ.MoSZ2D8qLURfVL05lqZhKARtCycdXEuVkmCDJoBwD7c",
  "token_type": "Bearer"
}
```

### 3. Сброс пароля ('AuthService/ResetPassword')

Пример принимаемых данных:

```json
{
  "email": "example@gmail.com"
}
```

Пример возвращаемых данных:

```json
{
  "message": "If the email example@gmail.com is exists, a password reset link will be sent to you."
}
```

### 4. Изменение пароля ('AuthService/ChangePassword') *Требуется авторизация*

Пример принимаемых данных:

```json
{
  "old_password": "Password67-",
  "new_password": "Password228+"
}
```

Пример возвращаемых данных:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJiODk0ZmYwNy1jNmY4LTQ3MjAtOGE3ZC1mNzAzOGM4YjY2OTUiLCJlbWFpbCI6Im1pa2kucnVsbGwyMDEyQGdtYWlsLmNvbSIsImlhdCI6MTc5MTAzNDI0MCwiZXhwIjoxNzkxMDM1MTQwfQ.hBr7qoRnV0s-xjjXqMrZ3Tq2frGr7ZhMz2iALEOqAcE",
  "token_type": "Bearer"
}
```
