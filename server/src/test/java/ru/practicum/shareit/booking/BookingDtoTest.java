package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingDtoTest {
    private final JacksonTester<BookingDto> json;

    @Test
    void testBookingDto() throws Exception {
        BookingDto bookingDto = BookingDto.builder()
                .id(1L)
                .start(LocalDateTime.of(2025, 5, 1, 10, 34, 5))
                .end(LocalDateTime.of(2025, 5, 5, 10, 34, 5))
                .status(BookingStatus.APPROVED)
                .build();
        JsonContent<BookingDto> result = json.write(bookingDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start")
                .isEqualTo(bookingDto.getStart().toString());
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo(bookingDto.getEnd().toString());
        assertThat(result).extractingJsonPathStringValue("$.status")
                .isEqualTo(String.valueOf(BookingStatus.APPROVED));
    }
}

