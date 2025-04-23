package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByOwnerId(Long userId);

    @Query("select it from Item as it where it.available = true and " +
            "(lower(it.name) like lower(concat('%', ?1,'%')) or " +
            "lower(it.description) like lower(concat('%', ?1,'%')))")
    List<Item> findByQuery(String text);

    List<Item> findAllByRequestId(Long requestId);

    List<Item> findAllByRequestIdNotNull();
}
