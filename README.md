# ToDoBot: A Telegram-based Task Management Application

ToDoBot is a Spring Boot application that provides a Telegram bot interface for managing tasks. It allows users to create, view, update, and delete tasks through a Telegram chat interface, making task management convenient and accessible.

The application combines a RESTful API backend with a Telegram bot frontend, offering a seamless experience for users to manage their tasks directly from their Telegram app. It utilizes Spring Boot for the backend services, JPA for database operations, and the Telegram Bots API for bot functionality.

Key features of ToDoBot include:
- Adding new tasks with title, description, and deadline
- Viewing all tasks
- Marking tasks as complete
- Deleting tasks
- Automatic reminders for upcoming task deadlines

The application is designed with a clean architecture, separating concerns between the bot interface, service layer, and data access layer. It uses MapStruct for object mapping, Lombok for reducing boilerplate code, and includes comprehensive exception handling for a robust user experience.

## Repository Structure

```
.
├── src
│   ├── main
│   │   ├── java
│   │   │   └── utii
│   │   │       └── todobot
│   │   │           ├── bot
│   │   │           ├── controller
│   │   │           ├── dto
│   │   │           ├── exception
│   │   │           ├── mapper
│   │   │           ├── model
│   │   │           ├── repository
│   │   │           ├── service
│   │   │           └── ToDoBotApplication.java
│   │   └── resources
│   │       └── application.properties
│   └── test
│       └── java
│           └── utii
│               └── todobot
│                   └── ToDoBotApplicationTests.java
├── mvnw
├── mvnw.cmd
└── pom.xml
```

### Key Files:

- `ToDoBotApplication.java`: The main entry point for the Spring Boot application.
- `TelegramBot.java`: Handles the Telegram bot functionality and user interactions.
- `TaskController.java`: REST API endpoints for task management.
- `TaskService.java`: Business logic for task operations.
- `TaskRepository.java`: Data access layer for task persistence.
- `pom.xml`: Maven project configuration file.

## Usage Instructions

### Prerequisites:
- Java 17
- Maven 3.6+
- PostgreSQL database
- Telegram Bot Token (obtain from BotFather on Telegram)

### Installation:

1. Clone the repository:
   ```
   git clone <repository-url>
   cd ToDoBot
   ```

2. Set up environment variables:
   ```
   export BOT_USERNAME=<your_bot_username>
   export BOT_TOKEN=<your_bot_token>
   ```

3. Configure the database connection in `src/main/resources/application.properties`:
   ```
   spring.datasource.url=jdbc:postgresql://localhost:5432/todobot
   spring.datasource.username=<your_username>
   spring.datasource.password=<your_password>
   ```

4. Build the project:
   ```
   mvn clean install
   ```

5. Run the application:
   ```
   java -jar target/ToDoBot-0.0.1-SNAPSHOT.jar
   ```

### Getting Started:

1. Start a chat with your bot on Telegram.
2. Use the following commands:
   - `/start`: Initialize the bot and display the main menu.
   - `/addTask`: Start the process of adding a new task.
   - `/tasks`: View all tasks.

### Configuration Options:

- Adjust logging levels in `application.properties`:
  ```
  logging.level.root=INFO
  logging.level.utii.todobot=DEBUG
  ```

### Common Use Cases:

1. Adding a task:
   ```
   User: /addTask
   Bot: Введите название задачи:
   User: Buy groceries
   Bot: Введите описание задачи:
   User: Get milk, eggs, and bread
   Bot: Введите дедлайн задачи (гггг мм дд чч:мм):
   User: 2023 05 20 18:00
   Bot: Задача успешно добавлена!
   ```

2. Viewing tasks:
   ```
   User: /tasks
   Bot: Список задач:

   📌 Задача 1: Buy groceries
   📝 Описание: Get milk, eggs, and bread
   ⏰ Дедлайн: 2023-05-20T18:00
   ⏳ Статус: Не выполнена
   ```

### Testing & Quality:

Run tests using:
```
mvn test
```

### Troubleshooting:

1. Bot not responding:
   - Ensure the `BOT_TOKEN` and `BOT_USERNAME` environment variables are set correctly.
   - Check the application logs for any errors.

2. Database connection issues:
   - Verify the database credentials in `application.properties`.
   - Ensure the PostgreSQL service is running.

3. Debugging:
   - Enable debug logging in `application.properties`:
     ```
     logging.level.utii.todobot=DEBUG
     ```
   - Check the console output or log files for detailed information.

## Data Flow

1. User sends a command to the Telegram bot.
2. `TelegramBot` class receives the update and processes the command.
3. If the command requires data manipulation, `TaskTelegramService` is called.
4. `TaskTelegramService` communicates with the backend API using `WebClient`.
5. The API request is handled by `TaskController`.
6. `TaskController` delegates business logic to `TaskService`.
7. `TaskService` performs the required operations, often involving `TaskRepository` for data persistence.
8. The response flows back through the same path to the user.

```
User <-> TelegramBot <-> TaskTelegramService <-> WebClient <-> TaskController <-> TaskService <-> TaskRepository <-> Database
```

Notes:
- The `TaskReminderService` periodically checks for upcoming task deadlines and sends reminders through the bot.
- Exception handling is managed globally by `GlobalExceptionHandler` to provide consistent error responses.