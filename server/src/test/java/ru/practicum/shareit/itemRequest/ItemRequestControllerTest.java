package ru.practicum.shareit.itemRequest;

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
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestExtendedDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ItemRequestControllerTest {
    @Mock
    private ItemRequestService itemRequestService;

    @InjectMocks
    private ItemRequestController controller;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private MockMvc mvc;

    private ItemRequestDto itemRequestDto;

    private ItemRequestExtendedDto itemRequestExtendedDto;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .requestorId(1L)
                .description("Test description")
                .created(LocalDateTime.now())
                .build();

        itemRequestExtendedDto = ItemRequestExtendedDto.builder()
                .id(1L)
                .requestorId(1L)
                .description("Test description")
                .created(LocalDateTime.now())
                .items(Collections.emptyList())
                .build();
    }

    @Test
    void create() throws Exception {
        when(itemRequestService.createNewItemRequest(any(), any()))
                .thenReturn(itemRequestDto);

        ItemRequestCreateDto itemRequestCreateDto = new ItemRequestCreateDto();
        itemRequestCreateDto.setDescription("Test description");
        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(itemRequestCreateDto))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.requestorId", is(1L), Long.class));
    }

    @Test
    void getAllRequestsList() throws Exception {
        when(itemRequestService.getAllRequests(any()))
                .thenReturn(List.of(itemRequestDto));

        MvcResult result = mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        Assertions.assertEquals(mapper.writeValueAsString(List.of(itemRequestDto)),
                result.getResponse().getContentAsString());
    }

    @Test
    void getUserRequestsList() throws Exception {
        when(itemRequestService.getUserRequests(any()))
                .thenReturn(List.of(itemRequestExtendedDto));

        MvcResult result = mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        Assertions.assertEquals(mapper.writeValueAsString(List.of(itemRequestExtendedDto)),
                result.getResponse().getContentAsString());
    }

    @Test
    void getRequest() throws Exception {
        when(itemRequestService.getRequest(any()))
                .thenReturn(itemRequestExtendedDto);

        MvcResult result = mvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        Assertions.assertEquals(mapper.writeValueAsString(itemRequestExtendedDto),
                result.getResponse().getContentAsString());
    }
}
