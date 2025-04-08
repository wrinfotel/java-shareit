package ru.practicum.shareit.item.service;

import ru.practicum.shareit.comment.CommentDto;
import ru.practicum.shareit.comment.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemExtendedDto;

import java.util.List;

public interface ItemService {

    ItemDto addNewItem(Long userId, ItemDto item);

    ItemDto updateItem(Long userId, Long itemId, ItemDto item);

    ItemExtendedDto getItemById(Long itemId);

    List<ItemExtendedDto> getUserItems(Long userId);

    List<ItemDto> search(Long userId, String text);

    CommentResponseDto addNewComment(Long itemId, Long userId, CommentDto comment);
}
