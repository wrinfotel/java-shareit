package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Repository
public class InMemoryItemRepository implements ItemRepository {

    private final HashMap<Long, Item> items = new HashMap<>();

    @Override
    public Item save(Item item) {
        item.setId(getNextId());
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item updateItem(Item item, Long itemId) {
        Item oldItem = items.get(itemId);
        if (item.getName() != null) {
            oldItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            oldItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            oldItem.setAvailable(item.getAvailable());
        }
        return oldItem;
    }

    @Override
    public Optional<Item> findById(Long itemId) {
        Item item = items.get(itemId);
        if (item == null) {
            return Optional.empty();
        }
        return Optional.of(item);
    }

    @Override
    public List<Item> findAllByUser(User user) {
        return items.values().stream()
                .filter(item -> item.getOwner().equals(user))
                .toList();
    }

    @Override
    public List<Item> findByQuery(String text) {
        if (text != null && !text.isBlank()) {
            String searchQuery = text.toLowerCase();
            return items.values().stream()
                    .filter(item -> ((item.getName() != null && item.getName().toLowerCase().contains(searchQuery)) ||
                            (item.getDescription() != null &&
                                    item.getDescription().toLowerCase().contains(searchQuery))) && item.getAvailable())
                    .toList();
        }
        return new ArrayList<>();
    }

    private long getNextId() {
        long currentMaxId = items.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
