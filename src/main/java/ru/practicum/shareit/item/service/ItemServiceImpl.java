package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.*;
import ru.practicum.shareit.exceptions.CommentConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemExtendedDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    private final UserRepository userRepository;

    private final BookingRepository bookingRepository;

    private final CommentRepository commentRepository;

    @Override
    public ItemDto addNewItem(Long userId, ItemDto item) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Item newItem = ItemMapper.toItem(item);
        newItem.setOwner(user);
        return ItemMapper.toItemDto(itemRepository.save(newItem));
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Item oldItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));
        Item item = ItemMapper.toItem(itemDto);
        if (!oldItem.getOwner().equals(user)) {
            throw new NotFoundException("Item not found");
        }
        return ItemMapper.toItemDto(itemRepository.save(updateItem(oldItem, item)));
    }

    @Override
    public ItemExtendedDto getItemById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));
        ItemExtendedDto itemExtendedDto = ItemMapper.toItemExtendedDto(item);
        if (Objects.equals(userId, item.getOwner().getId())) {
            Sort prevSort = Sort.by("end").descending();
            Booking prevBooking = bookingRepository.findFirstByItemIdAndEndIsBeforeAndStatus(itemExtendedDto.getId(),
                    LocalDateTime.now(), BookingStatus.APPROVED, prevSort);

            if (prevBooking != null) {
                itemExtendedDto.setLastBooking(prevBooking.getEnd());
            }
            Sort nextSort = Sort.by("start").descending();
            Booking nextBooking = bookingRepository.findFirstByItemIdAndStartIsAfterAndStatus(itemId,
                    LocalDateTime.now(), BookingStatus.APPROVED, nextSort);
            if (nextBooking != null) {
                itemExtendedDto.setNextBooking(nextBooking.getStart());
            }
        }

        List<Comment> comments = commentRepository.findAllByItemId(item.getId());
        if (comments != null && !comments.isEmpty()) {
            itemExtendedDto.setComments(comments);
        }

        return itemExtendedDto;
    }

    @Override
    public List<ItemExtendedDto> getUserItems(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        List<ItemExtendedDto> items = itemRepository.findAllByOwnerId(userId).stream()
                .map(ItemMapper::toItemExtendedDto)
                .toList();

        List<Long> ItemIds = items.stream().map(ItemExtendedDto::getId).toList();
        Sort prevSort = Sort.by("end").descending();
        List<Booking> prevBookingList = bookingRepository.findAllByItemIdInAndEndIsBeforeAndStatus(ItemIds,
                LocalDateTime.now(), BookingStatus.APPROVED, prevSort);

        Sort nextSort = Sort.by("start").ascending();
        List<Booking> nextBookingList = bookingRepository.findAllByItemIdInAndStartIsAfterAndStatus(ItemIds,
                LocalDateTime.now(), BookingStatus.APPROVED, nextSort);
        List<Comment> commentsList = commentRepository.findAllByItemIdIn(ItemIds);
        for (ItemExtendedDto itemExtendedDto : items) {

            Optional<Booking> prevBooking = prevBookingList.stream()
                    .filter(booking -> itemExtendedDto.getId().equals(booking.getItem().getId())).findFirst();
            prevBooking.ifPresent(booking -> itemExtendedDto.setLastBooking(booking.getEnd()));
            Optional<Booking> nextBooking = nextBookingList.stream()
                    .filter(booking -> itemExtendedDto.getId().equals(booking.getItem().getId())).findFirst();
            nextBooking.ifPresent(booking -> itemExtendedDto.setNextBooking(booking.getStart()));

            List<Comment> comments = commentsList.stream().filter(comment -> itemExtendedDto.getId().equals(comment.getItem().getId())).toList();
            if (!comments.isEmpty()) {
                itemExtendedDto.setComments(comments);
            }
        }
        return items;
    }

    @Override
    public List<ItemDto> search(Long userId, String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return itemRepository.findByQuery(text).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    private Item updateItem(Item oldItem, Item item) {
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
    public CommentResponseDto addNewComment(Long itemId, Long userId, CommentDto comment) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("author not found"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));
        Booking checkBooking = bookingRepository.findByBookerIdAndItemIdAndEndIsBeforeAndStatus(author.getId(),
                item.getId(), LocalDateTime.now(), BookingStatus.APPROVED);
        if (checkBooking != null) {
            Comment newComment = Comment.builder()
                    .author(author)
                    .item(item)
                    .text(comment.getText())
                    .created(LocalDate.now())
                    .build();
            return CommentMapper.toCommentResponseDto(commentRepository.save(newComment));
        }
        throw new CommentConflictException("User can`t comment this item");
    }
}
