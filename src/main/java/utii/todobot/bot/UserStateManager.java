package utii.todobot.bot;

import java.util.HashMap;
import java.util.Map;

public class UserStateManager {
    private final Map<String, String> userStates = new HashMap<>();
    private final Map<String, String> userTaskTitles = new HashMap<>();
    private final Map<String, String> userTaskDescriptions = new HashMap<>();
    private final Map<String, Long> userTaskIds = new HashMap<>();

    public void setState(String chatId, String state) {
        userStates.put(chatId, state);
    }

    public String getState(String chatId) {
        return userStates.get(chatId);
    }

    public void setTaskTitle(String chatId, String title) {
        userTaskTitles.put(chatId, title);
    }

    public String getTaskTitle(String chatId) {
        return userTaskTitles.get(chatId);
    }

    public void setTaskDescription(String chatId, String description) {
        userTaskDescriptions.put(chatId, description);
    }

    public String getTaskDescription(String chatId) {
        return userTaskDescriptions.get(chatId);
    }

    public void setTaskId(String chatId, Long taskId) {
        userTaskIds.put(chatId, taskId);
    }

    public Long getTaskId(String chatId) {
        return userTaskIds.get(chatId);
    }

    public void clearState(String chatId) {
        userStates.remove(chatId);
        userTaskTitles.remove(chatId);
        userTaskDescriptions.remove(chatId);
        userTaskIds.remove(chatId);
    }
}