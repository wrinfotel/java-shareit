package ru.practicum.shareit.comment;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class CommentResponseDto {
    private Long id;

    private String text;

    private String authorName;

    private LocalDate created;
}
