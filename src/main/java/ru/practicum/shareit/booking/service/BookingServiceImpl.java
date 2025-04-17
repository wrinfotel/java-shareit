package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.ItemNotAvailableException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    private final ItemRepository itemRepository;

    private final UserRepository userRepository;

    @Override
    public BookingDto createNewBooking(BookingDtoRequest booking, Long userId) {
        if (booking.getStart().isAfter(booking.getEnd()) || booking.getStart().isEqual(booking.getEnd())) {
            throw new ItemNotAvailableException("Start date or End date error");
        }
        Item item = itemRepository.findById(booking.getItemId())
                .orElseThrow(() -> new NotFoundException("Item not found"));

        if (!item.getAvailable()) {
            throw new ItemNotAvailableException("Item not available");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Booking newBooking = BookingMapper.toBooking(booking, user, item);
        newBooking.setStatus(BookingStatus.WAITING);
        return BookingMapper.toBookingDto(bookingRepository.save(newBooking));
    }

    @Override
    public BookingDto approveBookingRequest(Long userId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));
        if (Objects.equals(userId, booking.getItem().getOwner().getId())) {
            if (approved) {
                booking.setStatus(BookingStatus.APPROVED);
            } else {
                booking.setStatus(BookingStatus.REJECTED);
            }
            return BookingMapper.toBookingDto(bookingRepository.save(booking));
        }
        throw new ValidationException("This user can`t approve this booking");
    }

    @Override
    public BookingDto findBookingById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));
        if (Objects.equals(booking.getBooker().getId(), userId) ||
                Objects.equals(booking.getItem().getOwner().getId(), userId)) {
            return BookingMapper.toBookingDto(booking);
        }
        return null;
    }

    @Override
    public List<BookingDto> getBookingsByState(Long userId, BookingState state) {
        Sort sort = Sort.by("start").descending();
        if (state.equals(BookingState.ALL)) {
            return bookingRepository.findAllByBookerId(userId, sort).stream()
                    .map(BookingMapper::toBookingDto)
                    .toList();
        }
        if (state.equals(BookingState.CURRENT)) {
            return bookingRepository.findAllByBookerIdAndStartIsBeforeAndEndIsAfter(userId,
                            LocalDate.now(), LocalDate.now(), sort)
                    .stream()
                    .map(BookingMapper::toBookingDto)
                    .toList();
        }
        if (state.equals(BookingState.PAST)) {
            return bookingRepository.findAllByBookerIdAndEndIsBefore(userId,
                            LocalDate.now(), sort)
                    .stream()
                    .map(BookingMapper::toBookingDto)
                    .toList();
        }
        if (state.equals(BookingState.FUTURE)) {
            return bookingRepository.findAllByBookerIdAndStartIsAfter(userId,
                            LocalDate.now(), sort)
                    .stream()
                    .map(BookingMapper::toBookingDto)
                    .toList();
        }
        return bookingRepository.findAllByBookerIdAndStatus(userId, state, sort)
                .stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }

    @Override
    public List<BookingDto> getBookingItemsByState(Long userId, BookingState state) {
        List<Item> items = itemRepository.findAllByOwnerId(userId);
        if (items.isEmpty()) {
            throw new NotFoundException("User don`t have items");
        }
        Sort sort = Sort.by("start").descending();
        if (state.equals(BookingState.ALL)) {
            return bookingRepository.findAllByItemOwnerId(userId, sort).stream()
                    .map(BookingMapper::toBookingDto)
                    .toList();
        }
        if (state.equals(BookingState.CURRENT)) {
            return bookingRepository.findAllByItemOwnerIdAndStartIsBeforeAndEndIsAfter(userId,
                            LocalDate.now(), LocalDate.now(), sort)
                    .stream()
                    .map(BookingMapper::toBookingDto)
                    .toList();
        }
        if (state.equals(BookingState.PAST)) {
            return bookingRepository.findAllByItemOwnerIdAndEndIsBefore(userId,
                            LocalDate.now(), sort)
                    .stream()
                    .map(BookingMapper::toBookingDto)
                    .toList();
        }
        if (state.equals(BookingState.FUTURE)) {
            return bookingRepository.findAllByItemOwnerIdAndStartIsAfter(userId,
                            LocalDate.now(), sort)
                    .stream()
                    .map(BookingMapper::toBookingDto)
                    .toList();
        }
        return bookingRepository.findAllByItemOwnerIdAndStatus(userId, state, sort)
                .stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }
}
