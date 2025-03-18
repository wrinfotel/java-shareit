package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Item save(Item item);

    Item patchItem(Item item, Long itemId);

    Optional<Item> findById(Long itemId);

    List<Item> findAllByUser(User user);

    List<Item> findByQuery(String text);
}
