# 🔐 OTP Service

Backend-сервис на Java для генерации и проверки временных кодов (OTP) с отправкой через Email, SMS (SMPP эмулятор), Telegram и сохранением в файл. Сервис предназначен для защиты действий пользователей с помощью одноразовых паролей (OTP).

---

## 📋 Основные функции

- **Регистрация и аутентификация пользователей** с ролями: `ADMIN` и `USER`
- **Генерация и отправка OTP-кодов и их отправка по:**
    - Email (JavaMail)
    - SMS (SMPP-эмулятор)
    - Telegram Bot API
    - Сохранение OTP в файл в корне проекта
- **Проверка OTP-кодов** с учетом статусов: `ACTIVE`, `USED`, `EXPIRED`
- **Администрирование** (настройка TTL и длины OTP, управление пользователями)
- **Токенная авторизация** с проверкой ролей
- **Логирование** всех ключевых операций через SLF4J/Logback

---

## ⚙️ Технологии

- **Java 17**
- **PostgreSQL 17 + JDBC**
- **Maven** (система сборки)
- **JavaMail** для отправки Email
- **SMPP** (OpenSMPP-core, эмулятор SMPPsim)
- **Telegram Bot API** (Apache HttpClient)
- **HttpServer** (встроенный com.sun.net.httpserver)
- **SLF4J/Logback** для логирования

---

## 🛠 Установка и запуск

### 1. Подготовка

- Убедитесь, что установлены:
    - Java 21
    - PostgreSQL 17
    - Maven

Создайте базу данных `otp_service`:

```sql
CREATE DATABASE otp_service;
```

### 2. Настройка

Клонируйте репозиторий:

```bash
git clone https://github.com/vanhellthing93/sf.mephi.java_si_homework
```

Заполните конфигурационные файлы в `src/main/resources/application.properties`:

Пример `application.properties`:

```properties
#Период действия JWT токена
jwt.expiration.time=3600000

# JWT код
jwt.secret=YOUR_JWT_SECRET_PASS

# Данные для подключения к БД
db.url=jdbc:postgresql://address:port/db
db.user=postgres
db.password=postgres

# Данные для подключения е-mail 
email.username=username@example.com
email.password=password
email.from=username@example.com
mail.smtp.host=smtp.example.com
mail.smtp.port=465
mail.smtp.auth=true
mail.smtp.ssl.enable=true

# Данные для подключения сервера-заглушки SMPP
smpp.host=localhost
smpp.port=2775
smpp.system_id=smppclient1
smpp.password=password
smpp.system_type=OTP
smpp.source_addr=OTPService

#Данные для подключения Telegram
telegram.bot.token=YOUR_TELEGRAM_BOT_TOKEN
telegram.chat.id=YOUR_TELEGRAM_CHAT_ID

#Время проверки ОТП (в секундах). При текущей настройке раз в 60 секунд будет проверяться истекли ли активнвые отп коды
otp.expiration.time=60
```

### 3. Сборка и запуск

Соберите проект и запустите приложение:

```bash
mvn clean package
java -jar ./target/otp-1.0-SNAPSHOT.jar
```

---

## 📂 Структура проекта

```
otp-protection-service/
├── src/                      # Исходный код и ресурсы
│   └── main/
│       ├── java/             # Java-код
│       │   └── sf.mephi.study.otp/
│       │       ├── api/      # HTTP-контроллеры и фильтры (API-слой)
│       │       ├── config/   # Конфигурация приложения (загрузка конфигураций)
│       │       ├── dao/      # Доступ к базе данных (JDBC-реализация)
│       │       ├── model/    # Модели данных (DTO и сущности)
│       │       ├── service/  # Бизнес-логика и сервисы
│       │       └── util/     # Вспомогательные классы и утилиты
│       └── resources/        # Конфигурационные файлы и ресурсы
│           └── application.properties     # Общие настройки приложения
├── pom.xml                   # Конфигурация Maven
└── README.md                 # Описание проекта
```

---

## 🔑 Роли и авторизация

- **ADMIN**: полные права управления
    - настройка конфигурации OTP
    - просмотр и удаление пользователей (при удалении пользователей удаляются их OTP коды)
- **USER**: ограниченные права
    - генерация и валидация OTP

### Токены

- Генерируются при логине, имеют ограниченный TTL
- Передаются в заголовке:

```http
Authorization: Bearer <token>
```

---

## 📖 Примеры API-запросов

### Регистрация пользователя

```bash
curl -X POST "http://localhost:8080/register?login=user1&password=password123&role=USER"
```

### Авторизация (получение токена)

```bash
curl -X POST "http://localhost:8080/login?login=user1&password=password123"
```

### Генерация OTP  (требует авторизации)

```bash
curl -X POST "http://localhost:8080/sendOTP?operationId=test123&phone=+79991234567" \
     -H "Authorization: Bearer ваш_jwt_токен"
```

### Проверка OTP  (требует авторизации)

```bash
curl -X POST "http://localhost:8080/validateOTP?operationId=test123&code=123456" \
     -H "Authorization: Bearer ваш_jwt_токен"
```

### Действия администратора (требуют авторизации)

```bash
# Получение информации о пользователе
curl -X GET "http://localhost:8080/getUser?login=user1" \
     -H "Authorization: Bearer ваш_jwt_токен"

# Удаление пользователя (удаляет всего его otp коды)
curl -X DELETE "http://localhost:8080/deleteUser?login=user1" \
     -H "Authorization: Bearer ваш_jwt_токен"

#  Получение списка всех пользователей
curl -X GET "http://localhost:8080/getAllUsers" \
     -H "Authorization: Bearer ваш_jwt_токен"
     
#  Получение текущей конфигурации OTP
curl -X GET "http://localhost:8080/getOTPConfig" \
     -H "Authorization: Bearer ваш_jwt_токен"

#  Обновление конфигурации OTP
curl -X PUT "http://localhost:8080/updateOTPConfig?codeLength=6&expirationTime=300" \
     -H "Authorization: Bearer ваш_jwt_токен"
```

---

## 🧪 Тестирование

Используйте **Postman** или **curl** для проверки API. Убедитесь, что работают:

- регистрация и аутентификация
- генерация и отправка OTP
- проверка OTP-кодов
- админ-функции

---

## 🖋 Автор

**Косовский Иван**\
Проект реализован в рамках учебного задания МИФИ\
GitHub: [github.com/vanhellthing93](https://github.com/vanhellthing93)