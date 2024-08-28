# Проект на Spring

## Профили Spring

- `standalone` - `catalogue-service`, `feedback-service`, `customer-app` и `manager-app` 

## Инфраструктура

### Keycloak

В проекте используется как OAuth 2.0/OIDC-сервер для авторизации сервисов и аутентификации пользователей.

Запуск в Docker:

```shell
docker run --name selmag-keycloak -p 8082:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin -v ./config/keycloak/import:/opt/keycloak/data/import quay.io/keycloak/keycloak:23.0.7 start-dev --import-realm
```

### PostgreSQL

В проекте используется в качестве базы данных для модуля каталога(catalogue-service).

Запуск в Docker:

```shell
docker run --name selmag-catalogue -p 5432:5432 -e POSTGRES_USER=catalogue -e POSTGRES_PASSWORD=admin -e POSTGRES_DB=admin postgres:16
```

### MongoDB

В проекте используется в качестве базы данных для модуля обратной связи(feedback-service).

Запуск в Docker:

```shell
docker run --name selmag-feedback-db -p 27017:27017 mongo:7
```