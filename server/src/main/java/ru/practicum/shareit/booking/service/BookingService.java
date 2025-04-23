package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;

import java.util.List;

public interface BookingService {

    BookingDto createNewBooking(BookingDtoRequest booking, Long userId);

    BookingDto approveBookingRequest(Long userId, Long bookingId, boolean approved);

    BookingDto findBookingById(Long userId, Long bookingId);

    List<BookingDto> getBookingsByState(Long userId, BookingState state);

    List<BookingDto> getBookingItemsByState(Long userId, BookingState state);
}
