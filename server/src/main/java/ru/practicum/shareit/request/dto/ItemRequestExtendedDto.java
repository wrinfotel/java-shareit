package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
public class ItemRequestExtendedDto {

    private Long id;
    private String description;
    private Long requestorId;
    private List<Item> items;
    private LocalDateTime created;
}
