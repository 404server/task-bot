package utii.todobot.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ErrorResponse {
    private String status;
    private String message;
    private List<String> details;

    public ErrorResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public ErrorResponse(String status, List<String> details) {
        this.status = status;
        this.details = details;
    }
}
