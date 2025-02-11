package utii.todobot.mapper;

import org.mapstruct.Mapper;
import utii.todobot.dto.request.TaskRequestDTO;
import utii.todobot.dto.response.TaskResponseDTO;
import utii.todobot.model.Task;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    Task toModel(TaskRequestDTO taskRequestDTO);

    TaskResponseDTO toTaskResponseDTO(Task task);
}
