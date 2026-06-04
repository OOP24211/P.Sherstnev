# GoWork — Планировщик задач

Веб-приложение для управления задачами с тегами, дедлайнами и статусами. Написано на Spring Boot + Thymeleaf + PostgreSQL.

## Запуск

### Создай базу данных

```bash
sudo -u postgres psql
```

```sql
CREATE DATABASE gowork;
CREATE USER pavr_go_work WITH PASSWORD '4283';
GRANT ALL PRIVILEGES ON DATABASE gowork TO pavr_go_work;
\q
```

### Настрой подключение к БД

Открой файл `src/main/resources/application.properties` и проверь:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/gowork
spring.datasource.username=pavr_go_work
spring.datasource.password=4283
```
Замени на свои данные если нужно.

### Открой в браузере

```
http://localhost:8080
```



