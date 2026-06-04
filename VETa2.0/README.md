# VETa Messenger — инструкция по сборке

## Настройка PostgreSQL

```sql
-- Создать пользователя и базу (выполнить от postgres)
CREATE USER veta_user WITH PASSWORD 'veta_pass';
CREATE DATABASE veta_db OWNER veta_user;
GRANT ALL PRIVILEGES ON DATABASE veta_db TO veta_user;
```

Таблицы создаются автоматически при первом запуске сервера.

Если база уже была создана без колонки `is_group`, DatabaseManager добавит её сам через:
```sql
ALTER TABLE chats ADD COLUMN IF NOT EXISTS is_group BOOLEAN NOT NULL DEFAULT FALSE;
```

## Запуск

1. Запустить PostgreSQL
2. Запустить `ServerLauncher.main()` — сервер на порту 8887
3. Запустить `Main.main()` — клиент

