package ru.practicum.shareit.item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.*;
import ru.practicum.shareit.exceptions.CommentConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemExtendedDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    User user1 = User.builder()
            .id(1L)
            .email("test@user.ru")
            .name("User 1")
            .build();

    User user2 = User.builder()
            .id(2L)
            .email("test2@user.ru")
            .name("User 2")
            .build();

    ItemRequest itemRequest = ItemRequest.builder()
            .id(1L)
            .requestor(user2)
            .description("request description")
            .created(LocalDateTime.now())
            .build();

    Item item1 = Item.builder()
            .id(1L)
            .name("Item name")
            .description("Item Description")
            .available(true)
            .owner(user1)
            .request(itemRequest)
            .build();

    Item item2 = Item.builder()
            .id(1L)
            .name("Item name 2")
            .description("Item Description 2")
            .available(false)
            .owner(user1)
            .request(itemRequest)
            .build();

    @Test
    void shouldAddNewItem() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user1));
        Mockito.when(itemRequestRepository.findById(1L)).thenReturn(Optional.ofNullable(itemRequest));
        Mockito.when(itemRepository.save(any(Item.class))).thenReturn(item1);
        ItemDto savedItem = itemService.addNewItem(1L, ItemMapper.toItemDto(item1));
        Assertions.assertEquals(ItemMapper.toItemDto(item1), savedItem);
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void shouldNotAddNewItemUserNotFound() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.empty());
        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> itemService.addNewItem(1L, ItemMapper.toItemDto(item1)));
        Assertions.assertEquals("User not found", exception.getMessage());
    }

    @Test
    void shouldNotAddNewItemRequestNotFound() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user1));
        Mockito.when(itemRequestRepository.findById(1L)).thenReturn(Optional.empty());
        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> itemService.addNewItem(1L, ItemMapper.toItemDto(item1)));
        Assertions.assertEquals("Request not found", exception.getMessage());
    }

    @Test
    void shouldUpdateItem() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user1));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.ofNullable(item1));
        Mockito.when(itemRepository.save(any(Item.class))).thenReturn(item1);
        ItemDto savedItem = itemService.updateItem(1L, 1L, ItemMapper.toItemDto(item1));
        Assertions.assertEquals(ItemMapper.toItemDto(item1), savedItem);
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void shouldNotUpdateItemUserNotFound() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.empty());
        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> itemService.updateItem(1L, 1L, ItemMapper.toItemDto(item1)));
        Assertions.assertEquals("User not found", exception.getMessage());
    }

    @Test
    void shouldNotUpdateItemNotFound() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user1));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.empty());
        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> itemService.updateItem(1L, 1L, ItemMapper.toItemDto(item1)));
        Assertions.assertEquals("Item not found", exception.getMessage());
    }

    @Test
    void shouldGetItemById() {
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.ofNullable(item1));
        Mockito.when(bookingRepository.findFirstByItemIdAndEndIsBeforeAndStatus(any(Long.class),
                        any(LocalDateTime.class),
                        any(BookingStatus.class),
                        any(Sort.class)))
                .thenReturn(null);
        Mockito.when(bookingRepository.findFirstByItemIdAndStartIsAfterAndStatus(any(Long.class),
                        any(LocalDateTime.class),
                        any(BookingStatus.class),
                        any(Sort.class)))
                .thenReturn(null);
        Mockito.when(commentRepository.findAllByItemId(1L)).thenReturn(null);
        ItemExtendedDto item = itemService.getItemById(1L, 1L);
        Assertions.assertEquals(ItemMapper.toItemExtendedDto(item1), item);
    }

    @Test
    void shouldNotGetItemById() {
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.empty());
        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> itemService.getItemById(1L, 1L));
        Assertions.assertEquals("Item not found", exception.getMessage());
    }

    @Test
    void shouldGetUserItems() {
        List<Item> itemList = new ArrayList<>();
        itemList.add(item1);
        itemList.add(item2);
        Mockito.when(itemRepository.findAllByOwnerId(1L)).thenReturn(itemList);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user1));
        Mockito.when(bookingRepository.findAllByItemIdInAndEndIsBeforeAndStatus(any(),
                        any(LocalDateTime.class),
                        any(BookingStatus.class),
                        any(Sort.class)))
                .thenReturn(null);
        Mockito.when(bookingRepository.findAllByItemIdInAndStartIsAfterAndStatus(any(),
                        any(LocalDateTime.class),
                        any(BookingStatus.class),
                        any(Sort.class)))
                .thenReturn(null);
        Mockito.when(commentRepository.findAllByItemIdIn(any())).thenReturn(null);
        List<ItemExtendedDto> items = itemService.getUserItems(1L);
        Assertions.assertEquals(itemList.stream().map(ItemMapper::toItemExtendedDto).toList(), items);
    }

    @Test
    void shouldNotGetUserItemsUserNotFound() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.empty());
        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> itemService.getUserItems(1L));
        Assertions.assertEquals("User not found", exception.getMessage());
    }

    @Test
    void shouldSearchItem() {
        List<Item> itemList = new ArrayList<>();
        itemList.add(item1);
        itemList.add(item2);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user1));
        Mockito.when(itemRepository.findByQuery(any(String.class))).thenReturn(itemList);
        List<ItemDto> findedItems = itemService.search(1L, "text");
        Assertions.assertEquals(itemList.stream().map(ItemMapper::toItemDto).toList(), findedItems);
    }

    @Test
    void shouldNotSearchItemsUserNotFound() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.empty());
        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> itemService.search(1L, "text"));
        Assertions.assertEquals("User not found", exception.getMessage());
    }


    @Test
    void shouldAddComment() {
        Booking booking = Booking.builder()
                .item(item1)
                .booker(user1)
                .status(BookingStatus.APPROVED)
                .start(LocalDateTime.now().minusSeconds(10))
                .end(LocalDateTime.now().minusSeconds(5))
                .build();
        Comment comment = Comment.builder()
                .text("testComment")
                .item(item1)
                .author(user1)
                .created(LocalDate.now())
                .build();
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user1));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.ofNullable(item1));
        Mockito.when(bookingRepository.findByBookerIdAndItemIdAndEndIsBeforeAndStatus(any(), any(),
                        any(LocalDateTime.class),
                        any(BookingStatus.class)))
                .thenReturn(booking);
        Mockito.when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        CommentResponseDto commentResponseDto = itemService.addNewComment(1L, 1L,
                CommentDto.builder().text("testComment").build());
        Assertions.assertEquals(CommentMapper.toCommentResponseDto(comment), commentResponseDto);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void shouldNotAddCommentBookingNotFound() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user1));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.ofNullable(item1));
        Mockito.when(bookingRepository.findByBookerIdAndItemIdAndEndIsBeforeAndStatus(any(), any(),
                        any(LocalDateTime.class),
                        any(BookingStatus.class)))
                .thenReturn(null);

        final CommentConflictException exception = Assertions.assertThrows(CommentConflictException.class,
                () -> itemService.addNewComment(1L, 1L,
                        CommentDto.builder().text("testComment").build()));
        Assertions.assertEquals("User can`t comment this item", exception.getMessage());
    }

    @Test
    void shouldNotAddCommentUserNotFound() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.empty());
        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> itemService.addNewComment(1L, 1L,
                        CommentDto.builder().text("testComment").build()));
        Assertions.assertEquals("author not found", exception.getMessage());
    }

    @Test
    void shouldNotAddCommentItemNotFound() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user1));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.empty());
        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> itemService.addNewComment(1L, 1L,
                        CommentDto.builder().text("testComment").build()));
        Assertions.assertEquals("Item not found", exception.getMessage());
    }

}
