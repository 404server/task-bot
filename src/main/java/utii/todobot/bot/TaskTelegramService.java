package utii.todobot.bot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import utii.todobot.dto.request.TaskRequestDTO;
import utii.todobot.dto.response.TaskResponseDTO;
import utii.todobot.model.Task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TaskTelegramService {
    private static final Logger log = LoggerFactory.getLogger(TaskTelegramService.class);
    private final WebClient webClient;

    public TaskTelegramService() {
        this.webClient = WebClient.create("http://localhost:8080/api/tasks");
    }

    public String formatTasks(String response) {
        StringBuilder formattedResponse = new StringBuilder();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            List<Task> tasks = objectMapper.readValue(response, new TypeReference<List<Task>>() {
            });

            if (tasks.isEmpty()) {
                formattedResponse.append("Нет задач.");
            } else {
                formattedResponse.append("Список задач:\n\n");
                for (Task task : tasks) {
                    formattedResponse.append(String.format("📌 Задача %d: %s\n", task.getId(), task.getTitle()));
                    formattedResponse.append(String.format("📝 Описание: %s\n", task.getDescription()));
                    formattedResponse.append(String.format("⏰ Дедлайн: %s\n", task.getDeadline()));
                    formattedResponse.append(String.format("⏳ Статус: %s\n\n", task.isCompleted() ? "Выполнена" : "Не выполнена"));
                }
            }
        } catch (Exception e) {
            log.error("Ошибка при обработке задач: ", e);
            formattedResponse.append("Ошибка при обработке задач.");
        }
        return formattedResponse.toString();
    }

    public String addTask(String title, String description, String deadline) {
        try {
            TaskRequestDTO taskRequestDTO = new TaskRequestDTO();
            taskRequestDTO.setTitle(title);
            taskRequestDTO.setDescription(description);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MM dd H:mm");
            taskRequestDTO.setDeadline(LocalDateTime.parse(deadline, formatter));

            TaskResponseDTO response = webClient.post()
                    .uri("/add")
                    .bodyValue(taskRequestDTO)
                    .retrieve()
                    .bodyToMono(TaskResponseDTO.class)
                    .block();


            if (response != null) {
                return "Задача успешно добавлена!";
            } else {
                return "Ошибка при добавлении задачи.";
            }
        } catch (Exception e) {
            log.error("Ошибка при добавлении задачи: ", e);
            return "Не удалось добавить задачу. Попробуйте позже.";
        }
    }

    public String getTasks() {
        try {
            String response = webClient.get()
                    .uri("")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return formatTasks(response);
        } catch (Exception e) {
            log.error("Ошибка при получении задач: ", e);
            return "Ошибка при получении задач.";
        }
    }

    public String markTaskAsDone(Long taskId) {
        try {
            webClient.patch()
                    .uri("/" + taskId + "/complete")
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return "Задача успешно помечена как выполненная!";
        } catch (Exception e) {
            log.error("Ошибка при пометке задачи как выполненной: ", e);
            return "Не удалось пометить задачу как выполненную. Попробуйте позже.";
        }
    }

    public String deleteTask(Long taskId) {
        try {
            webClient.delete()
                    .uri("/" + taskId)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return "Задача успешно удалена!";
        } catch (Exception e) {
            log.error("Ошибка при удалении задачи: ", e);
            return "Не удалось удалить задачу. Попробуйте позже.";
        }
    }
}