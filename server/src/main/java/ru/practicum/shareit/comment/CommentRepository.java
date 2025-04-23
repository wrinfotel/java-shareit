package ru.practicum.shareit.comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByItemId(Long id);

    List<Comment> findAllByItemIdIn(List<Long> ids);
}
