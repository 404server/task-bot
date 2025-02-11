package utii.todobot.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utii.todobot.dto.request.TaskRequestDTO;
import utii.todobot.dto.response.TaskResponseDTO;
import utii.todobot.exception.DuplicateTaskException;
import utii.todobot.exception.ResourceNotFoundException;
import utii.todobot.exception.TaskAlreadyCompletedException;
import utii.todobot.mapper.TaskMapper;
import utii.todobot.model.Task;
import utii.todobot.repository.TaskRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    @Autowired
    public TaskService(TaskRepository taskRepository, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
    }

    public List<TaskResponseDTO> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(taskMapper::toTaskResponseDTO)
                .toList();

    }

    public TaskResponseDTO addTask(TaskRequestDTO taskRequestDTO) {
        if (taskRepository.existsByTitle(taskRequestDTO.getTitle())) {
            throw new DuplicateTaskException(taskRequestDTO.getTitle());
        }
        Task task = taskMapper.toModel(taskRequestDTO);
        Task savedTask = taskRepository.save(task);
        return taskMapper.toTaskResponseDTO(savedTask);
    }

    public TaskResponseDTO getTaskById(Long id) {
        return taskRepository.findById(id)
                .map(taskMapper::toTaskResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    @Transactional
    public TaskResponseDTO updateTask(Long id, TaskRequestDTO taskRequestDTO) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        task.setTitle(taskRequestDTO.getTitle());
        task.setDescription(taskRequestDTO.getDescription());

        task.setDeadline(taskRequestDTO.getDeadline());
        return taskMapper.toTaskResponseDTO(task);
    }

    @Transactional
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        taskRepository.delete(task);
    }

    @Transactional
    public void markAsComplete(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        if (task.isCompleted()) {
            throw new TaskAlreadyCompletedException("Task is already completed");
        }
        task.setCompleted(true);
    }

    public List<Task> getTasksWithUpcomingDeadlines(LocalDateTime localDateTime) {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(1);
        return taskRepository.findByDeadlineBetween(start, end);
    }
}
