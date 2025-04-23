package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exceptions.ItemNotAvailableException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

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
            .id(2L)
            .name("Item name 2")
            .description("Item Description 2")
            .available(false)
            .owner(user1)
            .request(itemRequest)
            .build();

    Booking booking = Booking.builder()
            .id(1L)
            .start(LocalDateTime.now().plusMinutes(10))
            .end(LocalDateTime.now().plusMinutes(15))
            .item(item1)
            .status(BookingStatus.WAITING)
            .booker(user2)
            .build();


    @Test
    void shouldCreateNewBooking() {
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.ofNullable(item1));
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.ofNullable(user2));
        Mockito.when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        BookingDtoRequest bookingDtoRequest = BookingDtoRequest.builder()
                .start(booking.getStart())
                .end(booking.getEnd())
                .itemId(item1.getId())
                .build();

        BookingDto result = bookingService.createNewBooking(bookingDtoRequest, 2L);
        assertEquals(BookingMapper.toBookingDto(booking), result);
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void shouldNotCreateNewBookingItemNotAvailable() {
        Mockito.when(itemRepository.findById(2L)).thenReturn(Optional.ofNullable(item2));
        BookingDtoRequest bookingDtoRequest = BookingDtoRequest.builder()
                .start(booking.getStart())
                .end(booking.getEnd())
                .itemId(item2.getId())
                .build();

        final ItemNotAvailableException exception = Assertions.assertThrows(ItemNotAvailableException.class,
                () -> bookingService.createNewBooking(bookingDtoRequest, 2L));

        assertEquals("Item not available", exception.getMessage());
    }

    @Test
    void shouldNotCreateNewBookingUserNotFound() {
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.ofNullable(item1));
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.empty());
        BookingDtoRequest bookingDtoRequest = BookingDtoRequest.builder()
                .start(booking.getStart())
                .end(booking.getEnd())
                .itemId(item1.getId())
                .build();

        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> bookingService.createNewBooking(bookingDtoRequest, 2L));

        Assertions.assertEquals("User not found", exception.getMessage());
    }

    @Test
    void shouldApproveBookingRequest() {
        Mockito.when(bookingRepository.findById(1L)).thenReturn(Optional.ofNullable(booking));
        Booking afterSave = booking;
        afterSave.setStatus(BookingStatus.APPROVED);
        Mockito.when(bookingRepository.save(any(Booking.class))).thenReturn(afterSave);

        BookingDto bookingDto = bookingService.approveBookingRequest(1L, 1L, true);
        Assertions.assertEquals(BookingMapper.toBookingDto(afterSave), bookingDto);
    }

    @Test
    void shouldNotApproveBookingRequestInvalidUser() {
        Mockito.when(bookingRepository.findById(1L)).thenReturn(Optional.ofNullable(booking));
        final ValidationException exception = Assertions.assertThrows(ValidationException.class,
                () -> bookingService.approveBookingRequest(2L, 1L, true));
        Assertions.assertEquals("This user can`t approve this booking", exception.getMessage());
    }

    @Test
    void shouldNotApproveBookingNotFound() {
        Mockito.when(bookingRepository.findById(1L)).thenReturn(Optional.empty());
        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> bookingService.approveBookingRequest(2L, 1L, true));
        Assertions.assertEquals("Booking not found", exception.getMessage());
    }

    @Test
    void shouldFindBookingById() {
        Mockito.when(bookingRepository.findById(1L)).thenReturn(Optional.ofNullable(booking));
        BookingDto bookingDto = bookingService.findBookingById(1L, 1L);
        Assertions.assertEquals(BookingMapper.toBookingDto(booking), bookingDto);
    }

    @Test
    void shouldNotFindBookingById() {
        Mockito.when(bookingRepository.findById(1L)).thenReturn(Optional.ofNullable(booking));
        BookingDto bookingDto = bookingService.findBookingById(4L, 1L);
        Assertions.assertNull(bookingDto);
    }

    @Test
    void shouldGetBookingsByStateAll() {
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);
        Sort sort = Sort.by("start").descending();
        Mockito.when(bookingRepository.findAllByBookerId(1L, sort)).thenReturn(bookings);
        List<BookingDto> result = bookingService.getBookingsByState(1L, BookingState.ALL);
        Assertions.assertEquals(bookings.stream().map(BookingMapper::toBookingDto).toList(), result);
    }

    @Test
    void shouldGetBookingsByStateCurrent() {
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);
        Mockito.when(bookingRepository.findAllByBookerIdAndStartIsBeforeAndEndIsAfter(any(),
                any(LocalDate.class),
                any(LocalDate.class),
                any(Sort.class))).thenReturn(bookings);
        List<BookingDto> result = bookingService.getBookingsByState(1L, BookingState.CURRENT);
        Assertions.assertEquals(bookings.stream().map(BookingMapper::toBookingDto).toList(), result);
    }

    @Test
    void shouldGetBookingsByStatePast() {
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);
        Mockito.when(bookingRepository.findAllByBookerIdAndEndIsBefore(any(),
                any(LocalDate.class),
                any(Sort.class))).thenReturn(bookings);
        List<BookingDto> result = bookingService.getBookingsByState(1L, BookingState.PAST);
        Assertions.assertEquals(bookings.stream().map(BookingMapper::toBookingDto).toList(), result);
    }

    @Test
    void shouldGetBookingsByStateFuture() {
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);
        Mockito.when(bookingRepository.findAllByBookerIdAndStartIsAfter(any(),
                any(LocalDate.class),
                any(Sort.class))).thenReturn(bookings);
        List<BookingDto> result = bookingService.getBookingsByState(1L, BookingState.FUTURE);
        Assertions.assertEquals(bookings.stream().map(BookingMapper::toBookingDto).toList(), result);
    }

    @Test
    void shouldGetBookingsByStateWaiting() {
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);
        Mockito.when(bookingRepository.findAllByBookerIdAndStatus(any(),
                any(BookingState.class),
                any(Sort.class))).thenReturn(bookings);
        List<BookingDto> result = bookingService.getBookingsByState(1L, BookingState.WAITING);
        Assertions.assertEquals(bookings.stream().map(BookingMapper::toBookingDto).toList(), result);
    }

    @Test
    void shouldGetBookingItemsByStateAll() {
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);
        List<Item> itemList = new ArrayList<>();
        itemList.add(item1);
        Sort sort = Sort.by("start").descending();
        Mockito.when(itemRepository.findAllByOwnerId(1L)).thenReturn(itemList);
        Mockito.when(bookingRepository.findAllByItemOwnerId(1L, sort)).thenReturn(bookings);
        List<BookingDto> result = bookingService.getBookingItemsByState(1L, BookingState.ALL);
        Assertions.assertEquals(bookings.stream().map(BookingMapper::toBookingDto).toList(), result);
    }

    @Test
    void shouldGetBookingItemsByStateCurrent() {
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);
        List<Item> itemList = new ArrayList<>();
        itemList.add(item1);
        Mockito.when(itemRepository.findAllByOwnerId(1L)).thenReturn(itemList);
        Mockito.when(bookingRepository.findAllByItemOwnerIdAndStartIsBeforeAndEndIsAfter(any(),
                any(LocalDate.class),
                any(LocalDate.class),
                any(Sort.class))).thenReturn(bookings);
        List<BookingDto> result = bookingService.getBookingItemsByState(1L, BookingState.CURRENT);
        Assertions.assertEquals(bookings.stream().map(BookingMapper::toBookingDto).toList(), result);
    }

    @Test
    void shouldGetBookingItemsByStatePast() {
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);
        List<Item> itemList = new ArrayList<>();
        itemList.add(item1);
        Mockito.when(itemRepository.findAllByOwnerId(1L)).thenReturn(itemList);
        Mockito.when(bookingRepository.findAllByItemOwnerIdAndEndIsBefore(any(),
                any(LocalDate.class),
                any(Sort.class))).thenReturn(bookings);
        List<BookingDto> result = bookingService.getBookingItemsByState(1L, BookingState.PAST);
        Assertions.assertEquals(bookings.stream().map(BookingMapper::toBookingDto).toList(), result);
    }

    @Test
    void shouldGetBookingItemsByStateFuture() {
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);
        List<Item> itemList = new ArrayList<>();
        itemList.add(item1);
        Mockito.when(itemRepository.findAllByOwnerId(1L)).thenReturn(itemList);
        Mockito.when(bookingRepository.findAllByItemOwnerIdAndStartIsAfter(any(),
                any(LocalDate.class),
                any(Sort.class))).thenReturn(bookings);
        List<BookingDto> result = bookingService.getBookingItemsByState(1L, BookingState.FUTURE);
        Assertions.assertEquals(bookings.stream().map(BookingMapper::toBookingDto).toList(), result);
    }

    @Test
    void shouldGetBookingItemsByStateWaiting() {
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);
        List<Item> itemList = new ArrayList<>();
        itemList.add(item1);
        Mockito.when(itemRepository.findAllByOwnerId(1L)).thenReturn(itemList);
        Mockito.when(bookingRepository.findAllByItemOwnerIdAndStatus(any(),
                any(BookingState.class),
                any(Sort.class))).thenReturn(bookings);
        List<BookingDto> result = bookingService.getBookingItemsByState(1L, BookingState.WAITING);
        Assertions.assertEquals(bookings.stream().map(BookingMapper::toBookingDto).toList(), result);
    }

    @Test
    void shouldNotGetBookingItemsUserWthoutItems() {
        Mockito.when(itemRepository.findAllByOwnerId(1L)).thenReturn(Collections.emptyList());
        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> bookingService.getBookingItemsByState(1L, BookingState.WAITING));
        Assertions.assertEquals("User don`t have items", exception.getMessage());
    }
}
