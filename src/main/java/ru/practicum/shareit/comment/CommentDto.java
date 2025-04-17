package ru.practicum.shareit.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class CommentDto {
    private Long id;

    @NotNull
    @NotBlank
    private String text;

    private LocalDate created;
}
