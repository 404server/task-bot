package utii.todobot.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import utii.todobot.model.Task;
import utii.todobot.service.TaskService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskReminderService {
    private final TelegramBot telegramBot;
    private final TaskService taskService;

    @Autowired
    public TaskReminderService(TelegramBot telegramBot, TaskService taskService) {
        this.telegramBot = telegramBot;
        this.taskService = taskService;
    }

    @Scheduled(fixedRate = 600000)
    public void checkDeadlines() {
        List<Task> tasks = taskService.getTasksWithUpcomingDeadlines(LocalDateTime.now().plusHours(1));
        for (Task task : tasks) {
            Duration duration = Duration.between(LocalDateTime.now(), task.getDeadline());
            telegramBot.sendResponse(
                    "985214483",
                    "Напоминание! Задача \""
                            + task.getTitle() + "\" скоро истекает. Осталось времени " +
                            duration.toMinutes() + " минут");
        }
    }
}
