package dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDateTime;

public class EndpointHitDto {

    public record NewEndpointHitDto(
            @NotBlank(message = "Название приложения не может быть пустым")
            String app,

            @NotBlank(message = "URI обязателен")
            String uri,

            @NotBlank(message = "IP-адрес не может быть пустым")
            String ip,

            @NotNull(message = "Время события обязательно")
            @PastOrPresent(message = "Время не может быть в будущем")
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")

            LocalDateTime timestamp
    ) {}

    public record FullEndpointHitDto(Long id, String app, String uri, String ip, LocalDateTime timestamp) {}
}
