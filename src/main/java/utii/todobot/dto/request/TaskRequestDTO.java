package utii.todobot.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskRequestDTO {
    @NotBlank(message = "cannot be blank")
    private String title;
    @NotNull(message = "cannot be null")
    @Size(min = 1, max = 255)
    private String description;
    @Future
    @NotNull(message = "cannot be null")
    private LocalDateTime deadline;

    public TaskRequestDTO() {
    }
}
