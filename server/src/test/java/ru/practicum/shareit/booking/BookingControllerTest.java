package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.user.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class BookingControllerTest {
    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingController controller;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private MockMvc mvc;

    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        User user = User.builder()
                .id(1L)
                .name("testUser")
                .email("test@email.ru")
                .build();

        bookingDto = BookingDto.builder()
                .id(1L)
                .booker(user)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now())
                .status(BookingStatus.APPROVED)
                .build();
    }

    @Test
    void create() throws Exception {
        when(bookingService.createNewBooking(any(), any()))
                .thenReturn(bookingDto);

        BookingDtoRequest bookingDtoRequest = BookingDtoRequest.builder()
                .start(LocalDateTime.now())
                .end(LocalDateTime.now())
                .itemId(1L)
                .build();
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDtoRequest))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(bookingDto.getBooker().getId()), Long.class));
    }

    @Test
    void approveBooking() throws Exception {
        when(bookingService.approveBookingRequest(any(), any(), any(Boolean.class)))
                .thenReturn(bookingDto);

        mvc.perform(patch("/bookings/1")
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(bookingDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.status", is(String.valueOf(BookingStatus.APPROVED)), String.class));
    }

    @Test
    void getBookingById() throws Exception {
        when(bookingService.findBookingById(any(), any()))
                .thenReturn(bookingDto);

        mvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(bookingDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.status", is(String.valueOf(BookingStatus.APPROVED)), String.class));

    }

    @Test
    void getBookingByState() throws Exception {
        when(bookingService.getBookingsByState(any(), any()))
                .thenReturn(List.of(bookingDto));

        MvcResult result = mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        Assertions.assertEquals(mapper.writeValueAsString(List.of(bookingDto)),
                result.getResponse().getContentAsString());
    }

    @Test
    void getBookingItemsByState() throws Exception {
        when(bookingService.getBookingItemsByState(any(), any()))
                .thenReturn(List.of(bookingDto));

        MvcResult result = mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        Assertions.assertEquals(mapper.writeValueAsString(List.of(bookingDto)),
                result.getResponse().getContentAsString());
    }
}
