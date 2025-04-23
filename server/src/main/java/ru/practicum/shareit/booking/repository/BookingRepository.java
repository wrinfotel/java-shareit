package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByBookerId(Long userId, Sort sort);

    List<Booking> findAllByBookerIdAndStartIsBeforeAndEndIsAfter(Long userId,
                                                                 LocalDate now, LocalDate now1, Sort sort);

    List<Booking> findAllByBookerIdAndEndIsBefore(Long userId, LocalDate now, Sort sort);

    List<Booking> findAllByBookerIdAndStartIsAfter(Long userId, LocalDate now, Sort sort);

    List<Booking> findAllByBookerIdAndStatus(Long userId, BookingState state, Sort sort);

    List<Booking> findAllByItemOwnerId(Long userId, Sort sort);

    List<Booking> findAllByItemOwnerIdAndStartIsBeforeAndEndIsAfter(Long userId,
                                                                    LocalDate now, LocalDate now1, Sort sort);

    List<Booking> findAllByItemOwnerIdAndEndIsBefore(Long userId, LocalDate now, Sort sort);

    List<Booking> findAllByItemOwnerIdAndStartIsAfter(Long userId, LocalDate now, Sort sort);

    List<Booking> findAllByItemOwnerIdAndStatus(Long userId, BookingState state, Sort sort);

    Booking findFirstByItemIdAndEndIsBeforeAndStatus(Long itemId, LocalDateTime now, BookingStatus bookingStatus, Sort prevSort);

    Booking findFirstByItemIdAndStartIsAfterAndStatus(Long itemId, LocalDateTime now, BookingStatus bookingStatus, Sort nextSort);

    Booking findByBookerIdAndItemIdAndEndIsBeforeAndStatus(Long id, Long itemId, LocalDateTime now, BookingStatus bookingStatus);

    List<Booking> findAllByItemIdInAndEndIsBeforeAndStatus(List<Long> itemId, LocalDateTime now, BookingStatus bookingStatus, Sort prevSort);

    List<Booking> findAllByItemIdInAndStartIsAfterAndStatus(List<Long> itemId, LocalDateTime now, BookingStatus bookingStatus, Sort nextSort);

}
