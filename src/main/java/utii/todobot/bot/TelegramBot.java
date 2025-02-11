package utii.todobot.bot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import utii.todobot.model.Task;

import java.util.ArrayList;
import java.util.List;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    private static final Logger log = LoggerFactory.getLogger(TelegramBot.class);
    private final String BOT_USERNAME = System.getenv("BOT_USERNAME");
    private final UserStateManager userStateManager = new UserStateManager();
    private final TaskTelegramService telegramTaskService;

    @Autowired
    public TelegramBot(TaskTelegramService telegramTaskService) {
        super(System.getenv("BOT_TOKEN"));
        this.telegramTaskService = telegramTaskService;
    }

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            handleCommand(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update);
        }
    }

    private void handleCommand(Message message) {
        String chatId = message.getChatId().toString();
        String userText = message.getText();

        switch (userText) {
            case "/start":
                sendResponse(chatId, "Добро пожаловать!", getMainKeyboard());
                break;
            case "/addTask":
                userStateManager.setState(chatId, "AWAITING_TASK_TITLE");
                sendResponse(chatId, "Введите название задачи:");
                break;
            case "/tasks":
                String tasksResponse = telegramTaskService.getTasks();
                sendResponse(chatId, tasksResponse, getTasksKeyboard(), true);
                break;
            default:
                sendResponse(chatId, "Неизвестная команда. Введите /help для списка команд.", getMainKeyboard());
        }
    }

    private void handleCallbackQuery(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();

        if (callbackData.startsWith("mark_done_")) {
            Long taskId = Long.parseLong(callbackData.replace("mark_done_", ""));
            String response = telegramTaskService.markTaskAsDone(taskId);
            sendResponse(chatId, response);
        } else if (callbackData.startsWith("delete_task_")) {
            Long taskId = Long.parseLong(callbackData.replace("delete_task_", ""));
            String response = telegramTaskService.deleteTask(taskId);
            InlineKeyboardMarkup updatedKeyboard = getTasksKeyboard();
            editMessageWithKeyboard(chatId, messageId, "Задача удалена.", updatedKeyboard);
        }
    }

    private void editMessageWithKeyboard(String chatId, Integer messageId, String newText, InlineKeyboardMarkup keyboardMarkup) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(newText);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка при обновлении сообщения: ", e);
        }
    }

    private void handleUserState(String chatId, String userText) {
        String state = userStateManager.getState(chatId);

        switch (state) {
            case "AWAITING_TASK_TITLE":
                userStateManager.setState(chatId, "AWAITING_TASK_DESC");
                userStateManager.setTaskTitle(chatId, userText);
                sendResponse(chatId, "Введите описание задачи:");
                break;

            case "AWAITING_TASK_DESC":
                userStateManager.setState(chatId, "AWAITING_TASK_DEADLINE");
                userStateManager.setTaskDescription(chatId, userText);
                sendResponse(chatId, "Введите дедлайн задачи (гггг мм дд чч:мм):");
                break;

            case "AWAITING_TASK_DEADLINE":
                String title = userStateManager.getTaskTitle(chatId);
                String description = userStateManager.getTaskDescription(chatId);
                String response = telegramTaskService.addTask(title, description, userText);
                sendResponse(chatId, response);
                userStateManager.clearState(chatId);
                break;

            default:
                sendResponse(chatId, "Произошла ошибка. Введите /help для помощи.");
                break;
        }
    }

    void sendResponse(String chatId, String text) {
        sendResponse(chatId, text, null);
    }

    private void sendResponse(String chatId, String text, ReplyKeyboardMarkup keyboard) {
        SendMessage message = new SendMessage(chatId, text);
        if (keyboard != null) {
            message.setReplyMarkup(keyboard);
        }
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения: ", e);
        }
    }

    private void sendResponse(String chatId, String text, InlineKeyboardMarkup keyboardMarkup, boolean active) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения: ", e);
        }
    }

    private ReplyKeyboardMarkup getMainKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("/tasks"));
        row1.add(new KeyboardButton("/addTask"));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("/help"));

        keyboardRows.add(row1);
        keyboardRows.add(row2);

        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }

    private InlineKeyboardMarkup getTasksKeyboard() {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        WebClient webClient = WebClient.create("http://localhost:8080/api/tasks");
        String response = webClient.get()
                .uri("")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            List<Task> tasks = objectMapper.readValue(response, new TypeReference<List<Task>>() {
            });

            for (Task task : tasks) {
                List<InlineKeyboardButton> row = getInlineKeyboardButtons(task);
                keyboardRows.add(row);
            }
        } catch (Exception e) {
            log.error("Ошибка при создании inline-клавиатуры задач: ", e);
        }
        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }

    private static List<InlineKeyboardButton> getInlineKeyboardButtons(Task task) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton markDoneButton = new InlineKeyboardButton();
        markDoneButton.setText("✅ Выполнить задачу " + task.getId());
        markDoneButton.setCallbackData("mark_done_" + task.getId());

        InlineKeyboardButton deleteTaskButton = new InlineKeyboardButton();
        deleteTaskButton.setText("❌ Удалить задачу " + task.getId());
        deleteTaskButton.setCallbackData("delete_task_" + task.getId());

        row.add(markDoneButton);
        row.add(deleteTaskButton);
        return row;
    }

}