package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {

    ItemDto addNewItem(Long userId, Item item);

    ItemDto updateItem(Long userId, Long itemId, ItemDto item);

    ItemDto getItemById(Long itemId);

    List<ItemDto> getUserItems(Long userId);

    List<ItemDto> search(Long userId, String text);
}
